using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using GalaSoft.MvvmLight;
using Windows.UI;

namespace ConconiTestApp.ViewModels
{
    public class HomeScreenViewModel : ViewModelBase
    {
        // Injected by Fody
        public double ActualSpeed { get; set; }
        public double AimSpeed { get; set; }
        public DateTime passedTime { get; set; }
        public int Pulse { get; set; }
        public int Distance { get; set; }
        public Color ActualSpeedColor { get; set; }
        public Color ButtonColor { get; set; }
        public String ButtonContent { get; set; }



        //TODO: Get Speed,Pulse and Distance




    }
}
