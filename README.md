# followme-android

A simple application to live-share your location with others. Experimental only, and realistically only intended for my own use at this point (though usage and reporting of issues is welcome).

Motivation
---

I'm often asked to pick somebody up, meet somebody, or am just travelling and would like for somebody else to know where I am. I developed this application to broadcast my current location while the application is running to a live-updating map.

Techology Stack
---

* Google Play Services
* Google Maps API for Android v2
* WebRTC (via Peer.js)
* Built in Android Studio with Gradle

Privacy
---

This application uses WebRTC for communicating between connected clients. This means that this application is serverless - while it uses a cloud service to broker connections, once a connection is established, the data is sent directly peer-to-peer. Locations are never stored anywhere.

Working
---

1. Location communication on a stable network
2. Location provider fallbacks, etc (via Play Services)
3. Sharing 
4. Toast messages to let the user know what's going on.

Slightly Iffy
---

1. Moving between networks can sometimes cause the connection to be re-established with a new ID - so anyone viewing the live map loses updates from that point.
2. No mechanism to control location streaming - it's on while the app is open, and will keep running until the app is destroyed.
3. Location collection should happen in a service to move complexity out of the activity.

User Interface Changes
---

I don't feel like a map is necessarily the best default display, as it makes the interface change a lot (e.g. on bearing changes, etc), and ends up consuming more resources than is necessary. I'm hoping to make a change soon where the map is a secondary display to more of a dashboard-type display showing the last location, how many people viewing the map, etc.

Building your own
---

Not too hard - just make sure you have a string resource somewhere in your resources named 'google_maps_key' whose value is a valid Google Maps API Key. If you don't have one, you can generate one in the [Developer Console](https://console.developers.google.com).

Peer.js also has a key and will eventually require a similar step - at the moment a development key is being used though that is already widely published. 

Questions, Issues, Changes
---

Please make use of Github's [Issue Tracker](https://github.com/joshmcarthur/followme-android/issues) and pull request functionality to send through any questions, issues or changes you have.

License
---

See LICENSE.



