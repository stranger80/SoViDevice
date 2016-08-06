using org.stranger80.SoViAgent;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SoViAgent.AllJoyn.Controllers
{
    public delegate void OnTemperatureChangedDelegate(double temperature);
    public delegate void OnHeaterChangedDelegate(int powerPcnt);
    public delegate void OnCirculationChangedDelegate(int powerPcnt);
    public delegate void OnErrorDelegate(int errorCode, string errorDescription);
    public delegate void OnHeatPointCompletedDelegate(bool isHeatPatternComplete);
    public interface ISoViController
    {
        double Temperature { get; }
        int Heater { get; }
        int Circulation { get; }

        event OnTemperatureChangedDelegate OnTemperatureChanged;
        event OnHeaterChangedDelegate OnHeaterChanged;
        event OnCirculationChangedDelegate OnCirculationChanged;
        event OnHeatPointCompletedDelegate OnHeatPointCompleted;
        event OnErrorDelegate OnError;

        bool ExecuteHeatPattern(IEnumerable<SoViAgentHeatPointArrayItem> heatPoints);
        bool Shutdown();

    }
}
