using org.stranger80.SoViAgent;
using SoViAgent.AllJoyn.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.AllJoyn;

namespace SoViAgent.AllJoyn
{
    public class SoViAgentProducerFactory
    {
        public SoViAgentProducer CreateAllJoynProducer(ISoViController controller)
        {
            AllJoynBusAttachment busAttachment = new AllJoynBusAttachment();
            SoViAgentProducer producer = new SoViAgentProducer(busAttachment);
            producer.Service = new SoViAgentService(producer, controller);
            

            return producer;
        }
    }
}
