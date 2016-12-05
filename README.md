### Android Media Picker

A utility library that allows a user to easily take a picture from the gallery, camera or documents.

### Installation

[![Download](https://api.bintray.com/packages/mrkcsc/maven/com.miguelgaeta.media-picker/images/download.svg)](https://bintray.com/mrkcsc/maven/com.miguelgaeta.media-picker/_latestVersion)

```groovy

compile 'com.miguelgaeta.android-media-picker:media-picker:1.3.2'

```

### Usage

First implement the MediaPicker.Provider interface in your Activities or Fragments.  Then open media chooser - presents the user with a chooser showing all matching activities for picking media:

```java

    MediaPicker.openMediaChooser(Provider provider, MediaPicker.OnError result);

```

Choosing media from camera:

```java

    MediaPicker.startForCamera(Provider provider, MediaPicker.OnError result);

```

Choosing media from gallery:

```java

    MediaPicker.startForGallery(Provider provider, MediaPicker.OnError result);

```

Choosing media from documents:

```java

    MediaPicker.startForDocuments(Provider provider, MediaPicker.OnError result);

```

Handling media result:

```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    MediaPicker.handleActivityResult(this, requestCode, resultCode, data, new MediaPicker.OnResult() {

        @Override
        public void onError(IOException e) {

            Log.e("MediaPicker", "Got file error.", e);
        }

        @Override
        public void onSuccess(File mediaFile, MediaPickerRequest request) {

            Log.e("MediaPicker", "Got file result: " + mediaFile + " for code: " + request);
        }

        @Override
        public void onCancelled() {

            Log.e("MediaPicker", "Got cancelled event.");
        }
    });
}
```

### Configuration

This library provides image cropping functionality via a dependency to Lorenzo Villani's [Crop Image](https://github.com/lvillani/android-cropimage).  When including this library add his repository to your `build.gradle` to obtain the dependency.

```groovy

allprojects {

    repositories {
        jcenter()

        maven {
            url 'http://lorenzo.villani.me/android-cropimage/'
        }
    }
}

```

For operations that require it, `WRITE_EXTERNAL_STORAGE` permission is added to the merged [Manifest](http://developer.android.com/guide/topics/manifest/manifest-intro.html).  You do not need to add this permission into your own manifest.

### License

*Copyright 2015 Miguel Gaeta*

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
