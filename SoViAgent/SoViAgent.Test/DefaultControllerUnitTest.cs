using System;
using Microsoft.VisualStudio.TestPlatform.UnitTestFramework;
using SoViAgent.AllJoyn.Controllers;
/* using FakeItEasy; no FakeItEasy in UWP until Castle Windsor ported... what a bummer... */
using org.stranger80.SoViAgent;
using System.Collections.Generic;
using System.Threading.Tasks;

namespace SoViAgent.Test
{
    class FakeTemperatureDevice : ITemperatureDevice
    {
        private Func<double> getTemperatureDelegate;

        public FakeTemperatureDevice()
        {
            this.getTemperatureDelegate = () => { return 25.0; };
        }

        public FakeTemperatureDevice(Func<double> getTemperatureDelegate)
        {
            this.getTemperatureDelegate = getTemperatureDelegate;
        }
        public double GetTemperature()
        {
            return getTemperatureDelegate();
        }
    }

    class FakeOutputDevice : IOutputDevice
    {
        int output;

        public int GetOutput()
        {
            return output;
        }

        public void SetOutput(int value)
        {
            output = value;
        }
    }

    [TestClass]
    public class DefaultControllerUnitTest
    {

        [TestMethod]
        public void Should_EnableHeaterAndCirculation_When_ExecuteHeatPatternCalled()
        {
            var temperatureSource = new FakeTemperatureDevice();
            var circulationDevice = new FakeOutputDevice();
            var heaterDevice = new FakeOutputDevice();

            var controller = new DefaultController(temperatureSource, 
                            circulationDevice, 
                            heaterDevice, 
                            20, 0, 0);

            var heatPattern = new List<SoViAgentHeatPointArrayItem>(
                    new SoViAgentHeatPointArrayItem[]
                    {
                        new SoViAgentHeatPointArrayItem() { Value1 = 30, Value2 = 600 }
                    }
                );

            controller.ExecuteHeatPattern(heatPattern);

            Task.Delay(6000).Wait();

            Assert.AreEqual(100, circulationDevice.GetOutput(), "Circulator should be on!");
            Assert.AreEqual(100, heaterDevice.GetOutput(), "Heater should be on!");

        }

        [TestMethod]
        public void Should_DisableHeaterAndCirculation_When_ShutdownCalled()
        {
            var temperatureSource = new FakeTemperatureDevice();
            var circulationDevice = new FakeOutputDevice();
            var heaterDevice = new FakeOutputDevice();

            var controller = new DefaultController(temperatureSource,
                            circulationDevice,
                            heaterDevice,
                            20, 0, 0);

            var heatPattern = new List<SoViAgentHeatPointArrayItem>(
                    new SoViAgentHeatPointArrayItem[]
                    {
                        new SoViAgentHeatPointArrayItem() { Value1 = 30, Value2 = 600 }
                    }
                );

            controller.ExecuteHeatPattern(heatPattern);

            Task.Delay(4000).Wait();

            controller.Shutdown();

            Task.Delay(3000).Wait();

            Assert.AreEqual(0, circulationDevice.GetOutput(), "Circulator should be off!");
            Assert.AreEqual(0, heaterDevice.GetOutput(), "Heater should be off!");

        }

        [TestMethod]
        public void Should_RaiseError_When_TemperatureDeviceThrowsException()
        {
            var temperatureSource = new FakeTemperatureDevice(() => { throw new Exception(); });
            var circulationDevice = new FakeOutputDevice();
            var heaterDevice = new FakeOutputDevice();

            var controller = new DefaultController(temperatureSource,
                            circulationDevice,
                            heaterDevice,
                            20, 0, 0);

            var heatPattern = new List<SoViAgentHeatPointArrayItem>(
                    new SoViAgentHeatPointArrayItem[]
                    {
                        new SoViAgentHeatPointArrayItem() { Value1 = 26, Value2 = 5 }
                    }
                );

            bool errorRaised = false;

            controller.OnError += new OnErrorDelegate((code, description) =>
                { errorRaised = true; }
            );

            controller.ExecuteHeatPattern(heatPattern);

            Task.Delay(4000).Wait();

            Assert.AreEqual(true, errorRaised, "Error event should have been raised!");
        }

        private void Controller_OnError(int errorCode, string errorDescription)
        {
            throw new NotImplementedException();
        }

        [TestMethod]
        public void Should_RaiseEvent_When_HeatPointFinished()
        {
            var temperatureSource = new FakeTemperatureDevice();
            var circulationDevice = new FakeOutputDevice();
            var heaterDevice = new FakeOutputDevice();

            var controller = new DefaultController(temperatureSource,
                            circulationDevice,
                            heaterDevice,
                            20, 0, 0);

            var heatPattern = new List<SoViAgentHeatPointArrayItem>(
                    new SoViAgentHeatPointArrayItem[]
                    {
                        new SoViAgentHeatPointArrayItem() { Value1 = 26, Value2 = 5 }
                    }
                );

            bool heatPointCompletedRaised = false;
            bool isHeatPatternComplete = false;

            controller.OnHeatPointCompleted += (heatPointCompleted) =>
            {
                heatPointCompletedRaised = true;
                isHeatPatternComplete = heatPointCompleted;
            };

            controller.ExecuteHeatPattern(heatPattern);

            Task.Delay(4000).Wait();
            Assert.AreNotEqual(0, circulationDevice.GetOutput(), "Circulator should be on!");

            Task.Delay(5000).Wait();

            Assert.AreEqual(0, circulationDevice.GetOutput(), "Circulator should be off!");
            Assert.AreEqual(0, heaterDevice.GetOutput(), "Heater should be off!");
            Assert.IsTrue(heatPointCompletedRaised, "HeatPointCompleted event should have been raised!");
            Assert.IsTrue(isHeatPatternComplete, "HeatPointCompleted event should raise a heatPointCompleted=true!");

        }

        [TestMethod]
        public void Should_RaiseInterimEvents_When_CalledForMultipleHeatPoints()
        {
            var temperatureSource = new FakeTemperatureDevice();
            var circulationDevice = new FakeOutputDevice();
            var heaterDevice = new FakeOutputDevice();

            var controller = new DefaultController(temperatureSource,
                            circulationDevice,
                            heaterDevice,
                            20, 0, 0);

            var heatPattern = new List<SoViAgentHeatPointArrayItem>(
                    new SoViAgentHeatPointArrayItem[]
                    {
                        new SoViAgentHeatPointArrayItem() { Value1 = 26, Value2 = 5 },
                        new SoViAgentHeatPointArrayItem() { Value1 = 26, Value2 = 5 }
                    }
                );

            bool heatPointCompletedRaised = false;
            bool isHeatPatternComplete = false;

            controller.OnHeatPointCompleted += (heatPointCompleted) =>
            {
                heatPointCompletedRaised = true;
                isHeatPatternComplete = heatPointCompleted;
            };

            controller.ExecuteHeatPattern(heatPattern);

            Task.Delay(9000).Wait();

            Assert.AreNotEqual(0, circulationDevice.GetOutput(), "Circulator should be on!");
            Assert.IsTrue(heatPointCompletedRaised, "HeatPointCompleted event should have been raised!");
            Assert.IsFalse(isHeatPatternComplete, "HeatPointCompleted event should raise a heatPointCompleted=false!");

            // reset the marker
            heatPointCompletedRaised = false;

            Task.Delay(9000).Wait();

            Assert.AreEqual(0, circulationDevice.GetOutput(), "Circulator should be off!");
            Assert.IsTrue(heatPointCompletedRaised, "HeatPointCompleted event should have been raised!");
            Assert.IsTrue(isHeatPatternComplete, "HeatPointCompleted event should raise a heatPointCompleted=true!");

        }

    }
}
