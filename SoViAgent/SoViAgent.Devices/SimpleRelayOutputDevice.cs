using SoViAgent.AllJoyn.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.Gpio;

namespace SoViAgent.Devices
{
    public class SimpleRelayOutputDevice : IOutputDevice
    {
        private GpioPinValue pinValue;
        private GpioPin pin;
        private int pinNo;

        private void InitGPIO()
        {
            var gpio = GpioController.GetDefault();

            // Show an error if there is no GPIO controller
            if (gpio == null)
            {
                throw new Exception("There is no GPIO controller on this device.");
            }

            pin = gpio.OpenPin(pinNo);
            pinValue = GpioPinValue.High;
            pin.Write(pinValue);
            pin.SetDriveMode(GpioPinDriveMode.Output);
        }


        /// <summary>
        /// Initializes simple on/off device hooked to one of GPIO pins
        /// </summary>
        /// <param name="pin"></param>
        public SimpleRelayOutputDevice(int pin)
        {
            this.pinNo = pin;
            this.InitGPIO();
        }

        public void SetOutput(int value)
        {
            if(value > 0)
            {
                pin.Write(GpioPinValue.Low);
                pinValue = GpioPinValue.Low;
            }
            else
            {
                pin.Write(GpioPinValue.High);
                pinValue = GpioPinValue.High;
            }
        }

        public int GetOutput()
        {
            return pinValue == GpioPinValue.Low ? 100 : 0;
        }
    }
}
