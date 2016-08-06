using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using org.stranger80.SoViAgent;
using Windows.Devices.AllJoyn;
using Windows.Foundation;
using System.Reflection;
using SoViAgent.AllJoyn.Controllers;

namespace SoViAgent.AllJoyn
{
    class SoViAgentService : ISoViAgentService
    {
        public ISoViController DeviceController { get; set; }

        public SoViAgentProducer AllJoynProducer { get; set; }

        /// <summary>
        /// Constructor.
        /// </summary>
        public SoViAgentService(SoViAgentProducer producer, ISoViController controller)
        {
            this.AllJoynProducer = producer;
            this.DeviceController = controller;

            this.DeviceController.OnTemperatureChanged += DeviceController_OnTemperatureChanged;
            this.DeviceController.OnHeaterChanged += DeviceController_OnHeaterChanged;
            this.DeviceController.OnCirculationChanged += DeviceController_OnCirculationChanged;
            this.DeviceController.OnHeatPointCompleted += DeviceController_OnHeatPointCompleted;
            this.DeviceController.OnError += DeviceController_OnError;
        }

        private void DeviceController_OnError(int errorCode, string errorDescription)
        {
            var task = new Task(() =>
            {
                this.AllJoynProducer.Signals.DeviceErrorOccurred(new SoViAgentErrorDescription()
                    { Value1 = errorCode, Value2 = errorDescription }
                );
            }
            );

            task.Start();
        }

        private void DeviceController_OnHeatPointCompleted(bool isHeatPatternComplete)
        {
            var task = new Task(() =>
            {
                this.AllJoynProducer.Signals.HeatPointCompleted(isHeatPatternComplete);
            }
            );

            task.Start();
        }

        private void DeviceController_OnCirculationChanged(int powerPcnt)
        {
            var task = new Task(() =>
            {
                this.AllJoynProducer.EmitCirculationChanged();
            }
            );

            task.Start();
        }

        private void DeviceController_OnHeaterChanged(int powerPcnt)
        {
            var task = new Task(() =>
            {
                this.AllJoynProducer.EmitHeaterChanged();
            }
            );

            task.Start();
        }

        private void DeviceController_OnTemperatureChanged(double temperature)
        {
            var task = new Task(() =>
            {
                this.AllJoynProducer.EmitTemperatureChanged();
            }
            );

            task.Start();
        }

        public IAsyncOperation<SoViAgentExecuteHeatPatternResult> ExecuteHeatPatternAsync(AllJoynMessageInfo info, IReadOnlyList<SoViAgentHeatPointArrayItem> heatPoints)
        {
            var task = new Task<SoViAgentExecuteHeatPatternResult>(() =>
            {
                return SoViAgentExecuteHeatPatternResult.CreateSuccessResult(this.DeviceController.ExecuteHeatPattern(heatPoints));
            }
            );

            task.Start();
            return task.AsAsyncOperation<SoViAgentExecuteHeatPatternResult>();
        }

        public IAsyncOperation<SoViAgentGetCirculationResult> GetCirculationAsync(AllJoynMessageInfo info)
        {
            var task = new Task<SoViAgentGetCirculationResult>(() =>
            {
                return SoViAgentGetCirculationResult.CreateSuccessResult(this.DeviceController.Circulation);
            }
            );

            task.Start();
            return task.AsAsyncOperation<SoViAgentGetCirculationResult>();
        }

        public IAsyncOperation<SoViAgentGetHeaterResult> GetHeaterAsync(AllJoynMessageInfo info)
        {
            var task = new Task<SoViAgentGetHeaterResult>(() =>
            {
                return SoViAgentGetHeaterResult.CreateSuccessResult(this.DeviceController.Heater);
            }
            );

            task.Start();
            return task.AsAsyncOperation<SoViAgentGetHeaterResult>();
        }

        public IAsyncOperation<SoViAgentGetStateHistoryResult> GetStateHistoryAsync(AllJoynMessageInfo info, uint cutoffTime)
        {
            throw new NotImplementedException();
        }

        public IAsyncOperation<SoViAgentGetTemperatureResult> GetTemperatureAsync(AllJoynMessageInfo info)
        {
            var task = new Task<SoViAgentGetTemperatureResult>(() =>
            {
                return SoViAgentGetTemperatureResult.CreateSuccessResult(this.DeviceController.Temperature);
            }
            );

            task.Start();
            return task.AsAsyncOperation<SoViAgentGetTemperatureResult>();
        }

        public IAsyncOperation<SoViAgentGetVersionResult> GetVersionAsync(AllJoynMessageInfo info)
        {
            var task = new Task<SoViAgentGetVersionResult>(() =>
                {
                    return SoViAgentGetVersionResult.CreateSuccessResult(1);
                }
            );

            task.Start();
            return task.AsAsyncOperation<SoViAgentGetVersionResult>();
        }

        public IAsyncOperation<SoViAgentShutdownResult> ShutdownAsync(AllJoynMessageInfo info)
        {
            var task = new Task<SoViAgentShutdownResult>(() =>
            {
                return SoViAgentShutdownResult.CreateSuccessResult(this.DeviceController.Shutdown());
            }
            );

            task.Start();
            return task.AsAsyncOperation<SoViAgentShutdownResult>();
        }
    }
}
