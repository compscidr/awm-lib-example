# awm-lib-example
An example implementation of an app using the [awm-lib](https://github.com/compscidr/awm-lib) measurement library. The app is available on the [Google Play Store](https://play.google.com/store/apps/details?id=io.rightmesh.awm_lib_example).

![Alt text](/logo/featured.jpg?raw=true)

The app currently supports collecting Wi-Fi and Bluetooth scan data,
stores it locally on the device using Android Room persistent storage,
and then uploads the data using JSON to a server running the [awm-lib-server](https://github.com/compscidr/awm-lib-server) project.

While the device is running, locally, a heatmap is generated showing then position of other Bluetooth and Wi-Fi devices, with the intensity of
the heatmap reprsenting the number of devices in range at that data point.

<img src="/screenshots/1.png?raw=true" width="300"/>  <img src="/screenshots/2.png?raw=true" width="300"/>

