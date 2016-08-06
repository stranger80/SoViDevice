using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SoViAgent.AllJoyn.Controllers
{
    public interface IOutputDevice
    {
        /// <summary>
        /// Send the output value to the device.
        /// </summary>
        /// <param name="value"></param>
        void SetOutput(int value);

        /// <summary>
        /// Get the current output value from the device.
        /// </summary>
        /// <returns></returns>
        int GetOutput();

    }
}
