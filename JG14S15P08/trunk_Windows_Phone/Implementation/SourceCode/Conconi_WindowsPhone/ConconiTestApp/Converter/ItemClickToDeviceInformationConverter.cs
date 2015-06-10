using System;
using Windows.Devices.Enumeration;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Data;

namespace ConconiTestApp.Converter
{
    public class ItemClickToDeviceInformationConverter : IValueConverter
    {
        public object Convert(object value, Type targetType, object parameter, string language)
        {
            var args = value as ItemClickEventArgs;
            if (args == null) return null;

            return args.ClickedItem as DeviceInformation;
        }

        public object ConvertBack(object value, Type targetType, object parameter, string language)
        {
            throw new NotImplementedException();
        }
    }
}