# Description: #
Eyevinn Andriod app to test HLS and MPEG-dash stream.

## How to setup: ##
- Download java version 1.8+.
- Install Android studio IDE.
- Set up the code in the Android studio.


## How to run the app: ##
- Set up the code in the Android studio.
- Create a virtual device in AVD manager.
- Run the application in virtual device.
- Click on HLS or Mpeg-Dash button. 
- Ensure that you have sample stream in the input box.
- Click the Load button.
- Selected stream will be played in the player.


## How to run the tests: ##
- Right click on the test and execute it in Android studio.


## Features: ## 
- HLS Button to load sample HLS stream.
- MPEG-DASH Button to load sample MPEG-DASH stream.
- Load button to start the selected video stream.
- User can add valid HLS/Mpeg-Dash video.

# Load list of sources from URL
A json document containing a list of video urls can be loaded by entering
the url to the json document in the textinput and pressing the `load` button.
For each video in the json document, a button to play the video will appear
in the ui.

Example json document
```
{
    "sourceList": [
        {
            "name": "HLS_VOD",
            "url": "https://f53accc45b7aded64ed8085068f31881.egress.mediapackage-vod.eu-north-1.amazonaws.com/out/v1/1c63bf88e2664639a6c293b4d055e6bb/ade303f83e8444d69b7658f988abb054/2a647c0cf9b7409598770b9f11799178/manifest.m3u8"
        },
        {
            "name": "Some video,
            "url": "http://some-url/bbb.mp4"
        }
    ]
}
```


![alt text](https://github.com/Eyevinn/android-player/blob/exoplayer-integration/Screenshot.png?raw=true)
