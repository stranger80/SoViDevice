using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using org.stranger80.SoViAgent;

namespace SoViAgent.AllJoyn.Controllers
{
    public class FakeController : ISoViController
    {
        public int Circulation
        {
            get
            {
                return 100;
            }
        }

        public int Heater
        {
            get
            {
                return 1;
            }
        }

        public double Temperature
        {
            get
            {
                return 25.5;
            }
        }

        public event OnCirculationChangedDelegate OnCirculationChanged;
        public event OnErrorDelegate OnError;
        public event OnHeaterChangedDelegate OnHeaterChanged;
        public event OnHeatPointCompletedDelegate OnHeatPointCompleted;
        public event OnTemperatureChangedDelegate OnTemperatureChanged;

        public bool ExecuteHeatPattern(IEnumerable<SoViAgentHeatPointArrayItem> heatPoints)
        {
            throw new NotImplementedException();
        }

        public bool Shutdown()
        {
            throw new NotImplementedException();
        }
    }
}
