using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SoViAgent.AllJoyn.Controllers
{
    public interface ITemperatureDevice
    {
        double GetTemperature();
    }
}
