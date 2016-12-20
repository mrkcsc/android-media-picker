## 1.3.3 - 2016-19-22

- Add support for all file types via intent filter.

## 1.3.2 - 2016-06-22

- Deprecate old API in favor of using FileProvers.

## 1.3.1 - 2016-06-22

- Add support for using FileProvider URIs.

## 1.3.0 - 2016-06-22

- Cancelled or unused temporary files are now removed.  Saved images should show immediately.

## 1.2.8 - 2016-04-22

- Added additional guard when reading from cursor.

## 1.2.7 - 2016-03-21
## 1.2.6 - 2016-03-19

- Added #getImageDimensions utility method.
- Added more robust error handling in MediaPickerUri #getPath
- Added updated App Compat to `23.2.1`

- Fixed Camera `uses-feature` tags are now explicitly marked as not required.

## 1.2.5 - 2016-02-16
## 1.2.4 - 2016-02-16

- Fixed a bug where camera permission was not granted on some Marshmallow devices (thanks Cole!).

## 1.2.3 - 2016-02-16
## 1.2.2 - 2016-02-16

- Bug fix for camera and crop results not being captured when called with chooser request type.

## 1.2.1 - 2016-02-12

- Bug fix for encoding image file to data url.

## 1.2.0 - 2016-02-12

- Expose cropping API.

## 1.1.3 - 2016-02-12

- Add overloads to `MediaPickerUri`

## 1.1.2 - 2016-02-12

- Allow string resource id for the chooser title.

## 1.1.1 - 2016-02-12

- Made base document chooser method private.

## 1.1.0 - 2016-02-11

- Added an error callback to open documents and gallery.

## 1.0.1 - 2016-02-10

- Initial implementation.

## 1.0.0 - 2016-02-10

- Initial commit.
