using System;
using System.Collections.ObjectModel;
using System.Diagnostics;
using System.Linq;
using System.Windows.Input;
using Windows.ApplicationModel.Core;
using Windows.Devices.Bluetooth;
using Windows.Devices.Bluetooth.GenericAttributeProfile;
using Windows.Devices.Enumeration;
using Windows.Devices.Geolocation;
using Windows.Storage.Streams;
using Windows.UI.Core;
using Windows.UI.Popups;
using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.Command;
using GalaSoft.MvvmLight.Views;

namespace ConconiTestApp.ViewModels
{
    public class MainViewModel : ViewModelBase
    {

        private double speed;
        private double distance;

        public ObservableCollection<DeviceInformation> Devices { get; set; }
        public ICommand ConnectCommand { get; set; }
        public BluetoothLEDevice MyHeartRateDevice { get; set; }
        public ushort HeartRate { get; set; }
        public readonly INavigationService navigationService;

        public Geolocator Geolocator { get; set; }
        public double Latitude { get; set; }
        public double Longitude { get; set; }

        public double Distance
        {
            get { return distance; }
            set
            {
                RaisePropertyChanged();
                distance = value;
            }
        }

        public double Speed
        {
            get
            {
                GetCurrentLocation();
                return speed;
            }
            set
            {
                RaisePropertyChanged();
                speed = value;
            }
        }


        public MainViewModel()
        {
            Devices = new ObservableCollection<DeviceInformation>();
            ConnectCommand = new RelayCommand<DeviceInformation>(ConnectToDevice);

            GetCurrentLocation();


        }

        public MainViewModel(INavigationService navigationService)
        {
            this.navigationService = navigationService;
            Devices = new ObservableCollection<DeviceInformation>();
            ConnectCommand = new RelayCommand<DeviceInformation>(ConnectToDevice);

            NavigateToHomeScreen();
        }





        private async void GetCurrentLocation()
        {
            Geolocator = new Geolocator();

            Geolocator.DesiredAccuracy = PositionAccuracy.High;
            Geolocator.MovementThreshold = 1; // The units are meters.
            Geolocator.PositionChanged += geolocator_PositionChanged;



        }

        void geolocator_PositionChanged(Geolocator sender, PositionChangedEventArgs args)
        {

            var dispatcher = CoreApplication.MainView.CoreWindow.Dispatcher;

            dispatcher.RunAsync(Windows.UI.Core.CoreDispatcherPriority.Normal, () =>
            {
                Latitude = args.Position.Coordinate.Latitude;
                Longitude = args.Position.Coordinate.Longitude;

                Distance += GetDistance(Latitude, Longitude, args.Position.Coordinate.Latitude,
                    args.Position.Coordinate.Longitude, 2);



                if (args.Position.Coordinate.Speed > 0.1)
                {
                    Speed = (double)(args.Position.Coordinate.Speed) * 3600 / 1000;
                }
                else
                {
                    Speed = 0.0;
                }

            });


        }

        public double GetDistance(double Latitude1, double Longitude1, double Latitude2, double Longitude2, int type)
        {

            double R = (type == 1) ? 3960 : 6371;
            double dLat = this.toRadian(Latitude2 - Latitude1);
            double dLon = this.toRadian(Longitude2 - Longitude1);

            double a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) + Math.Cos(this.toRadian(Latitude1)) * Math.Cos(this.toRadian(Latitude2)) * Math.Sin(dLon / 2) * Math.Sin(dLon / 2);

            double c = 2 * Math.Asin(Math.Min(1, Math.Sqrt(a)));
            double d = R * c;

            return d;
        }

        private double toRadian(double val)
        {
            return (Math.PI / 180) * val;
        }



      


        /// <summary>
        /// Trys to connect to the choosen Bluetooth device
        /// </summary>
        /// <param name="device"> The choosen Bluetooth device of the List</param>
        private async void ConnectToDevice(DeviceInformation device)
        {

            MyHeartRateDevice = await BluetoothLEDevice.FromIdAsync(device.Id);

            var deviceService = MyHeartRateDevice.GetGattService(GattServiceUuids.HeartRate);
            var characteristic = deviceService.GetCharacteristics(GattCharacteristicUuids.HeartRateMeasurement).First();
            var status = await characteristic.WriteClientCharacteristicConfigurationDescriptorAsync(GattClientCharacteristicConfigurationDescriptorValue.Notify);
            if (status == GattCommunicationStatus.Unreachable)
            {
                MessageDialog dialog = new MessageDialog("Check if the Device is turned on");
                await dialog.ShowAsync();
            }
            else
            {
                characteristic.ValueChanged += characteristic_ValueChanged;
                NavigateToHomeScreen();
            }
        }
        /// <summary>
        /// Gets called, if the value of the heart ratement device gets changed
        /// </summary>

        void characteristic_ValueChanged(GattCharacteristic sender, GattValueChangedEventArgs args)
        {
            var data = new byte[args.CharacteristicValue.Length];

            DataReader.FromBuffer(args.CharacteristicValue).ReadBytes(data);

            // Process the raw data received from the device.
            var value = ProcessData(data);
            // Debug.WriteLine(value);
            Windows.ApplicationModel.Core.CoreApplication.MainView.CoreWindow.Dispatcher.RunAsync(CoreDispatcherPriority.Normal, () => { HeartRate = value; });
            Debug.WriteLine(HeartRate);
        }
        /// <summary>
        /// Transfers the bytes of the device into a readable heart rate
        /// </summary>
        /// <param name="data"></param>
        /// <returns> The actual Heart Rate</returns>
        private ushort ProcessData(byte[] data)
        {
            const byte HEART_RATE_VALUE_FORMAT = 0x01;
            byte currentOffset = 0;
            byte flags = data[currentOffset];
            bool isHeartRateValueSizeLong = ((flags & HEART_RATE_VALUE_FORMAT) != 0);
            currentOffset++;
            ushort heartRateMeasurementValue = 0;
            if (isHeartRateValueSizeLong)
            {
                heartRateMeasurementValue = (ushort)((data[currentOffset + 1] << 8) + data[currentOffset]);
                currentOffset += 2;
            }
            else
            {
                heartRateMeasurementValue = data[currentOffset];
                currentOffset++;
            }

            return heartRateMeasurementValue;
        }
        /// <summary>
        /// Lists the Bluetooth Devices which are paired with the Device
        /// </summary>
        public async void GetBluetoothDevices()
        {
            Devices.Clear();
            var devices = await DeviceInformation.FindAllAsync(
                GattDeviceService.GetDeviceSelectorFromUuid(GattServiceUuids.HeartRate));


            if (devices.Count <= 0)
            {
                MessageDialog dialog = new MessageDialog("Check if Bluetooth is turned on/ Device is paired");
                await dialog.ShowAsync();
                NavigateToHomeScreen();
            }
            foreach (var device in devices)
            {
                Devices.Add(device);
            }



        }

        /// <summary>
        /// Navigation to the Home Screen
        /// </summary>
        private void NavigateToHomeScreen()
        {
            navigationService.NavigateTo("HomeScreen");
        }



    }
}