window.FollowMe = {};
window.FollowMeAndroid = window.FollowMeAndroid || FollowMe.CallbackStubs;

document.addEventListener("DOMContentLoaded", function() {
  FollowMe.subscribe();
});

(function() {
  var peer, connection;
  var connections = [];

  FollowMe.subscribe = function() {
    peer = new Peer({key: 'lwjd5qra8257b9'});
    peer.on('open', function(id) {
      connection = peer.connect(id, { serialization: 'json'});
      FollowMeAndroid.didConnect(id);
    });

    peer.on('close', function() { FollowMeAndroid.didDisconnect(); });
    peer.on('error', function(err) { FollowMeAndroid.didError(err.type); });
    peer.on('connection', function(conn) {
      connections.push(conn);
      FollowMeAndroid.clientDidConnect();
    });
  };

  FollowMe.sendLocation = function(lat, lng) {
    var data = { event: 'location-update', lat: lat, lng: lng };

    for (var i = 0; i < connections.length; i++) {
      connections[i].send(data);
    }

    FollowMeAndroid.didSendLocation();
  };

  FollowMe.unsubscribe = function() {
    if (peer) {
      peer.destroy();
    }
  };

  FollowMe.CallbackStubs = {};
  FollowMe.CallbackStubs.didError = function() { console.log("Got Error!", arguments); };
  FollowMe.CallbackStubs.didConnect = function() { console.log("Connected!", arguments); };
  FollowMe.CallbackStubs.didDisconnect = function() { console.log("Disconnected!", arguments); };
  FollowMe.CallbackStubs.didSendLocation = function() { console.log("Sent location!", arguments); };
  FollowMe.CallbackStubs.clientDidConnect = function() { console.log("Client connected!", arguments); };
})();