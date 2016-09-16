# Cordova Camera Roll

The Cordova Camera Roll plugin makes it easy to read from the camera roll.

Cordova comes with some camera features such as `navigator.camera.getPicture`. The problem is you have no
control over the experience beyond that. On iOS, for example, a modal slides in that lets you choose a picture.

With this plugin you can get a list of all photos and do whatever you want with them after that.

## Installation

```sh
cordova plugin add https://github.com/bamlab/cordova-camera-roll.git
```

## Usage

```javascript

CameraRoll.getPhotos(function(photo) {
  // `photo` is an object with `path`, `date`,
  // `thumbnailPath` (android only) and `orientation` (in degrees) (android only).
  //
  // On iOS, paths are prefixed asset-library://
  // So if you are using Angular and ng-src, make sure to whitelist this URL scheme.
  //
  // You can use the paths directly in an img src or as a background-image in CSS.
  //
  // This callback will be called for each photo in the roll. It's async, yo!
  //
  // When the last photo is reached, the callback is called one last time with an empty object.
}, function(err) {
  // Something bad happened :O
}, {
  limit: 12, // Optional, will only return the last N photos.
});

// (iOS only) Quickly save a base-64 encoded data uri to the camera roll.
CameraRoll.saveToCameraRoll(base64String, function() {
  // File saved
}, function(err) {
  // Something bad happened :O
});

```


## About this module

Originally created by Max Lynch [@maxlynch](http://twitter.com/maxlynch), with Android support by Jeremy Bouillanne [johnnyoin](https://github.com/johnnyoin).

Tested on Cordova android 5.1.1 and Cordova ios 4.1.1.

### TODO

* Change the interface to have a single success callback called once, with the full list of photos.
