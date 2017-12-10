# GPSmeasurements
ready-to-use example for Android 8.x Orea+ GPS measurement - backwards compatible

This example implementation uses a background (i.e. non-UI) fragment to get positional information via GPS.
The fragment can easily be integrated into other applications.
For Android 25 (Marshmallow) and above, the fragment uses an intent-based function that supervises the device-global GPS backround service and delivers new positions as rapidly as available. This is done via the SharePreferences concept, while the user is informed about the process via notifications. In this version, more GPS data are delivered as the use is specifically intended for 3D navigation.
For devices with an API level lover than 25, a previously-common background service is used to deliver the requested information.
The example also integrates a common permission request for positional data.

The license for using the code is Creative Commons Share-alike. For details, please consult the LICENSE.md.

For acaedmic users, I encourage to reference the following, related article:

Kroehnert, M., Kehl, C., Litschke, H. and Buckley, S.J., "Image-to-Geometry Registration on Mobile Devices - Concepts, Challenges and Applications", Proc. 20th 3D-NordOst, ISBN 978-3-942709-17-0, p.99-108, 2017.
