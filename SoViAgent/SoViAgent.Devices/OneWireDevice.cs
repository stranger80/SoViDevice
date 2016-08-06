using Rinsen.IoT.OneWire;
using SoViAgent.AllJoyn.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SoViAgent.Devices
{
    public class OneWireDevice : ITemperatureDevice
    {
        public double GetTemperature()
        {
            using (var oneWireDeviceHandler = new OneWireDeviceHandler(false, false))
            {
                foreach (var device in oneWireDeviceHandler.GetDevices<DS18B20>())
                {
                    var result = device.GetTemperature();

                    return result;
                }
            }

            return 0;
        }
    }
}
