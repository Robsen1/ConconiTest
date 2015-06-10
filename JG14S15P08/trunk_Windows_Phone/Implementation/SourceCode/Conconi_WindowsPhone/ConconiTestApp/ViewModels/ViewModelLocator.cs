using ConconiTestApp.Pages;
using GalaSoft.MvvmLight.Ioc;
using GalaSoft.MvvmLight.Views;
using Microsoft.Practices.ServiceLocation;

namespace ConconiTestApp.ViewModels
{
    public class ViewModelLocator
    {

        static ViewModelLocator()
        {
            ServiceLocator.SetLocatorProvider(() => SimpleIoc.Default);


            NavigationService navigationService = new NavigationService();
            //register pages at the Navigationservice
            navigationService.Configure("MainPage", typeof(MainPage));
            navigationService.Configure("HomeScreen", typeof(HomeScreen));

            SimpleIoc.Default.Register<INavigationService>(() => navigationService);

            SimpleIoc.Default.Register<MainViewModel>();
            SimpleIoc.Default.Register<HomeScreenViewModel>();
        }

        public MainViewModel Main { get { return ServiceLocator.Current.GetInstance<MainViewModel>(); } }
        public HomeScreenViewModel HomeScreen { get { return ServiceLocator.Current.GetInstance<HomeScreenViewModel>(); } }
    }
}