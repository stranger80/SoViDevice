using org.stranger80.SoViAgent;
using SoViAgent.AllJoyn;
using SoViAgent.AllJoyn.Controllers;
using SoViAgent.Devices;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace SoViAgent.UI
{
    public enum NotifyType
    {
        StatusMessage,
        ErrorMessage
    };

    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        private CoreDispatcher dispatcher;
        private SoViAgentProducer allJoynProducer;

        public MainPage()
        {
            this.InitializeComponent();

            this.dispatcher = CoreWindow.GetForCurrentThread().Dispatcher;

            this.InitializeAllJoynService();

        }

        private void InitializeAllJoynService()
        {
            try
            {
                var factory = new SoViAgentProducerFactory();

                var temperatureSource = new OneWireDevice();
                var heaterOutput = new SimpleRelayOutputDevice(16);
                var circulationOutput = new SimpleRelayOutputDevice(20);
                var controller = new DefaultController(temperatureSource, circulationOutput, heaterOutput, 100, 0, 0);

                controller.OnTemperatureChanged += Controller_OnTemperatureChanged;

                this.allJoynProducer = factory.CreateAllJoynProducer(controller);
                this.allJoynProducer.Start();

                this.NotifyUser(
                    $"AllJoyn Service Initialized",
                    NotifyType.StatusMessage);
            }
            catch (Exception exc)
            {
                this.NotifyUser(
                    $"Error initializing AllJoyn Service: {exc.Message}",
                    NotifyType.ErrorMessage);
            }
        }

        private void Controller_OnTemperatureChanged(double temperature)
        {
            NotifyUserAsync($"[Event] Temperature changed: {temperature}", NotifyType.StatusMessage);
        }

        public async void NotifyUserAsync(string status, NotifyType statusType)
        {
            await dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                this.NotifyUser(status, statusType);
            });

        }

        /// <summary>
        /// Used to display messages to the user
        /// </summary>
        /// <param name="strMessage"></param>
        /// <param name="type"></param>
        public void NotifyUser(string strMessage, NotifyType type)
        {
            switch (type)
            {
                case NotifyType.StatusMessage:
                    brdStatusBorder.Background = new SolidColorBrush(Windows.UI.Colors.Green);
                    break;
                case NotifyType.ErrorMessage:
                    brdStatusBorder.Background = new SolidColorBrush(Windows.UI.Colors.Red);
                    break;
            }
            txtStatusLabel.Text = strMessage;

        }

    }
}
