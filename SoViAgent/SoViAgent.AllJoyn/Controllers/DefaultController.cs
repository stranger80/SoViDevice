using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using org.stranger80.SoViAgent;
using Windows.System.Threading;

namespace SoViAgent.AllJoyn.Controllers
{
    public class DefaultController : ISoViController
    {
        public class ErrorCodes
        {
            public static int TemperatureDeviceError = 1;
            public static int ControllerError = 2;
            public static int ExecuteHeatPatternError = 3;
            public static int ShutdownError = 4;
        }

        public ITemperatureDevice TemperatureSource { get; set; }
        public IOutputDevice CirculationDevice { get; set; }
        public IOutputDevice HeaterDevice { get; set; }

        private int circulation;
        private int heater;
        private double temperature;
        protected ThreadPoolTimer timer;

        private List<SoViAgentHeatPointArrayItem> currentHeatPattern;
        private int? currentHeatPatternIndex;
        private DateTime? lastControllerTickTimestamp;
        // set to time when temperature set by current heat point has been reached - start elapsing since that moment
        private DateTime? regulationStartedTimestamp; 
        
        // PID controller factors
        private double P;
        private double I;
        private double D;

        private double currentIntegralValue = 0;
        private double lastTemperature = 0;

        public DefaultController(ITemperatureDevice temperatureSource, 
                                 IOutputDevice circulationDevice, 
                                 IOutputDevice heaterDevice,
                                 double P,
                                 double I,
                                 double D)
        {
            this.TemperatureSource = temperatureSource;
            this.CirculationDevice = circulationDevice;
            this.HeaterDevice = heaterDevice;

            this.P = P;
            this.I = I;
            this.D = D;

            // launch the ticking clock to periodically action the controller
            lastControllerTickTimestamp = DateTime.Now;
            timer = ThreadPoolTimer.CreatePeriodicTimer(ControllerTick, TimeSpan.FromSeconds(3));
        }

        /// <summary>
        /// Main logic of the controller.
        /// Measures - acts
        /// </summary>
        /// <param name="timer"></param>
        protected void ControllerTick(ThreadPoolTimer timer)
        {
            try
            {
                if (this.TemperatureSource != null)
                {
                    try
                    {
                        var temp = this.TemperatureSource.GetTemperature();

                        this.Temperature = temp;
                    }
                    catch (Exception exc)
                    {
                        if (OnError != null)
                        {
                            OnError(ErrorCodes.TemperatureDeviceError, $"Error measuring temperature: {exc.Message}");
                        }

                        // in case of error - make no action (or should we shutdown???)

                        return;
                    }
                }

                if (this.currentHeatPattern != null && this.currentHeatPatternIndex != null)
                {
                    // 1. Check elapsed time
                    if (this.regulationStartedTimestamp == null)
                    {
                        if (Math.Abs(currentHeatPattern[currentHeatPatternIndex.Value].Value1
                                    - this.Temperature) < 2.0)
                        {
                            this.regulationStartedTimestamp = DateTime.Now;
                        }
                    }
                    else
                    {   // check if elapsed time of heating expired
                        if ((DateTime.Now - this.regulationStartedTimestamp).Value.TotalSeconds >
                            currentHeatPattern[currentHeatPatternIndex.Value].Value2)
                        {
                            bool isHeatPatternComplete = false;

                            // move to next heatPattern index (if exists)
                            if(currentHeatPatternIndex.Value < currentHeatPattern.Count - 1)
                            {
                                currentHeatPatternIndex++;
                                this.currentIntegralValue = 0;
                                isHeatPatternComplete = false;
                            }
                            else
                            {
                                // end of heat pattern - shutdown
                                this.Shutdown();
                                isHeatPatternComplete = true;
                            }

                            // raise interim HeatPointCompleted event
                            if (this.OnHeatPointCompleted != null)
                            {
                                this.OnHeatPointCompleted(isHeatPatternComplete);
                            }

                            this.regulationStartedTimestamp = null;
                        }
                    }
                }

                if (this.currentHeatPattern != null && this.currentHeatPatternIndex != null)
                {
                    // Act upon the temperature and required presets

                    // add value to integral

                    var diffTime = (DateTime.Now - this.lastControllerTickTimestamp).Value.TotalSeconds;
                    var diffTemp = this.Temperature - this.lastTemperature;
                    var diff = diffTemp / diffTime;

                    // only accumulate the integral if we are in 'regulation' mode
                    if (regulationStartedTimestamp != null)
                    {
                        this.currentIntegralValue += diffTime * diffTemp;
                    }

                    // calculate the input
                    double input = currentHeatPattern[currentHeatPatternIndex.Value].Value1
                                    - this.Temperature; // delta of temperatures

                    // calculate the output
                    double output = this.P * input + this.D * diff + this.I * this.currentIntegralValue;

                    // set the heater power
                    this.HeaterDevice.SetOutput((int)output);
                    this.Heater = this.HeaterDevice.GetOutput();
                }
            }
            catch(Exception exc)
            {
                // raise error event on bus
                if (this.OnError != null)
                {
                    this.OnError(ErrorCodes.ControllerError, $"Error in controller: {exc.Message}");
                }
            }

            this.lastControllerTickTimestamp = DateTime.Now;
            this.lastTemperature = this.Temperature;
        }

        public bool ExecuteHeatPattern(IEnumerable<SoViAgentHeatPointArrayItem> heatPoints)
        {
            try
            {
                // reset programmed pattern to one received in parameter
                this.currentHeatPattern = new List<SoViAgentHeatPointArrayItem>(heatPoints);
                this.currentHeatPatternIndex = 0;

                this.CirculationDevice.SetOutput(100);
                this.Circulation = this.CirculationDevice.GetOutput();
                return true;
            }
            catch (Exception exc)
            {
                // raise error event on bus
                if (this.OnError != null)
                {
                    this.OnError(ErrorCodes.ExecuteHeatPatternError, $"Error on execute heat pattern: {exc.Message}");
                }
                return false;
            }


        }

        public bool Shutdown()
        {
            try
            {
                this.CirculationDevice.SetOutput(0);
                this.HeaterDevice.SetOutput(0);

                // reset programmed pattern to null
                this.currentHeatPattern = null;
                this.currentHeatPatternIndex = null;

                this.Circulation = 0;
                this.Heater = 0;
                return true;
            }
            catch (Exception exc)
            {
                // raise error event on bus
                if(this.OnError != null)
                {
                    this.OnError(ErrorCodes.ShutdownError, $"Error on shutdown: {exc.Message}");
                }
                return false;
            }
        }

        public int Circulation
        {
            get
            {
                return circulation;
            }

            protected set
            {
                if (circulation != value)
                {
                    circulation = value;
                    if (this.OnCirculationChanged != null)
                    {
                        this.OnCirculationChanged(circulation);
                    }
                }
            }
        }

        public int Heater
        {
            get
            {
                return heater;
            }

            protected set
            {
                if (heater != value)
                {
                    heater = value;
                    if (this.OnHeaterChanged != null)
                    {
                        this.OnHeaterChanged(heater);
                    }
                }
            }
        }

        public double Temperature
        {
            get
            {
                return temperature;
            }

            protected set
            {
                if(temperature != value)
                {
                    temperature = value;
                    if(this.OnTemperatureChanged != null)
                    {
                        this.OnTemperatureChanged(temperature);
                    }
                }
            }
        }

        public event OnTemperatureChangedDelegate OnTemperatureChanged;
        public event OnHeaterChangedDelegate OnHeaterChanged;
        public event OnCirculationChangedDelegate OnCirculationChanged;
        public event OnHeatPointCompletedDelegate OnHeatPointCompleted;
        public event OnErrorDelegate OnError;
    }
}
