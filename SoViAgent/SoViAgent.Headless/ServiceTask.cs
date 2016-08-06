using org.stranger80.SoViAgent;
using SoViAgent.AllJoyn;
using SoViAgent.AllJoyn.Controllers;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.ApplicationModel.Background;

namespace SoViAgent.Headless
{
    public sealed class ServiceTask : IBackgroundTask
    {
        private SoViAgentProducer allJoynProducer;
        BackgroundTaskDeferral deferral;

        public void Run(IBackgroundTaskInstance taskInstance)
        {
            deferral = taskInstance.GetDeferral();

            try
            {
                var factory = new SoViAgentProducerFactory();
                this.allJoynProducer = factory.CreateAllJoynProducer(new FakeController());
                this.allJoynProducer.Start();

                // how can we confirm all went well???
            }
            catch (Exception exc)
            {
                // what can we do to log some info???
            }
        }
    }
}
