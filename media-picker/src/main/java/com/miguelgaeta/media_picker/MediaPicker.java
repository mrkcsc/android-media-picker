package com.miguelgaeta.media_picker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;

import com.android.camera.CropImageIntentBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
@SuppressWarnings({"UnusedDeclaration", "DefaultFileTemplate"})
public class MediaPicker {

    static final String NAME = "media_picker";

    /**
     * @see #openMediaChooser(Provider, String, OnError)
     */
    public static void openMediaChooser(final Provider provider, final int title, final OnError result) {

        openMediaChooser(provider, provider.getContext().getString(title), result);
    }

    /**
     * Create a chooser intent that matches all types of activities
     * for taking photos or selecting media.
     *
     * @param provider Source {@link Provider}.
     *
     * @param title Chooser title string resource.
     *
     * @param result Can fail to create the file needed for the camera intents.
     */
    public static void openMediaChooser(final Provider provider, final String title, final OnError result) {

        try {

            final Context context = provider.getContext();

            final Uri captureFileURI = createTempImageFileAndPersistUri(context);

            final Intent intent = MediaPickerChooser.getMediaChooserIntent(context.getPackageManager(), title, captureFileURI);

            startFor(provider, intent, MediaPickerRequest.REQUEST_CHOOSER.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * Start the camera application correctly.
     *
     * @param provider Source {@link Provider}.
     *
     * @param result The camera open action can fail so capture the result.
     */
    public static void startForCamera(final Provider provider, final OnError result) {

        try {
            final Uri captureFileURI = createTempImageFileAndPersistUri(provider.getContext());
            startForCamera(provider, captureFileURI, result);
        } catch (IOException e) {
            result.onError(e);
        }
    }

    public static void startForCamera(final Provider provider, final ContentFileProvider fileProvider, final BaseOnError result) {
        Uri captureFileURI = null;
        try{
            File photoFile =  fileProvider.createFile();
            captureFileURI = fileProvider.toUri(photoFile);
            persistUri(provider.getContext(), photoFile.toURI().toString());
        } catch (Exception e) {
            result.onFileCreationError(e);
            return;
        }

        startForCamera(provider, captureFileURI, result);
    }

    public interface ContentFileProvider {
        File createFile() throws IOException;
        Uri toUri(File file);
    }

    public static void startForCamera(final Provider provider, final Uri captureFileURI, final OnError result) {

        try {
            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI)
                    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startFor(provider, intent, MediaPickerRequest.REQUEST_CAPTURE.getCode());

        } catch (IOException e) {
            result.onError(e);
        }
    }

    /**
     * Start the gallery application directly.
     *
     * @param provider Source {@link Provider}.
     *
     * @param result Failure to open gallery captured in result callback..
     */
    public static void startForGallery(final Provider provider, final OnError result) {

        try {

            final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startFor(provider, intent, MediaPickerRequest.REQUEST_GALLERY.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * Start the documents chooser directly.
     *
     * @param provider Source {@link Provider}.
     *
     * @param result The document open action can fail so capture the result.
     */
    public static void startForDocuments(final Provider provider, final OnError result) {

        try {

            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

            intent.setType("image/*");

            startFor(provider, intent, MediaPickerRequest.REQUEST_DOCUMENTS.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * @see #startForImageCrop(Provider, File, int, int, int, OnError)
     */
    public static void startForImageCrop(final Provider provider, final File file, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        startForImageCrop(provider, Uri.fromFile(file), outputWidth, outputHeight, colorInt, result);
    }

    /**
     * Start activity for cropping.
     *
     * @param provider Source {@link Provider}.
     * @param uri Source file URI.
     * @param outputWidth Cropped file output width.
     * @param outputHeight Cropped file output height.
     * @param colorInt Cropping UI circle color.
     * @param result Result callbacks.
     */
    private static void startForImageCrop(final Provider provider, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        try {

            final Uri captureFileURI = createTempImageFileAndPersistUri(provider.getContext());

            final CropImageIntentBuilder intentBuilder = new CropImageIntentBuilder(outputWidth, outputHeight, captureFileURI);

            intentBuilder.setSourceImage(uri);
            intentBuilder.setDoFaceDetection(false);
            intentBuilder.setOutlineCircleColor(colorInt);
            intentBuilder.setOutlineColor(colorInt);
            intentBuilder.setScaleUpIfNeeded(true);

            startFor(provider, intentBuilder.getIntent(provider.getContext()), MediaPickerRequest.REQUEST_CROP.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * Start activity for result helper that accepts both activities
     * and or fragments.
     *
     * @param provider Source {@link Provider}.
     * @param intent Source {@link Intent}
     * @param requestCode Request code for capturing result.
     */
    private static void startFor(final Provider provider, final Intent intent, final int requestCode) throws IOException {

        try {

            if (provider != null) {
                provider.startActivityForResult(intent, requestCode);
            }

        } catch (ActivityNotFoundException e) {

            throw new IOException("No application available for media picker.");
        }
    }

    /**
     * Handle result of one of the defined start actions.
     *
     * @param context Used to resolve resulting file.
     * @param requestCode Request code, should be defined.
     * @param resultCode Result code.
     * @param data Data containing the result.
     * @param result Result callbacks.
     */
    public static void handleActivityResult(final Context context, final int requestCode, final int resultCode, final Intent data, final OnResult result) {

        final MediaPickerRequest request = MediaPickerRequest.create(requestCode);

        if (request == null) {

            return;
        }

        try {

            switch (resultCode) {

                case Activity.RESULT_OK:

                    final File file = MediaPickerUri.resolveToFile(context, handleActivityUriResult(context, request, data));

                    refreshSystemMediaScanDataBase(context, file);

                    result.onSuccess(file, request);

                    break;

                case Activity.RESULT_CANCELED:

                    deleteCaptureFileUri(context);

                    result.onCancelled();

                    break;

                default:

                    throw new IOException("Bad activity result code: " + resultCode + ", for request code: " + requestCode);
            }

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * Given a request code and a data result intent from an activity, attempt to
     * extract the returned file URI.
     *
     * @param context Context.
     * @param request Source request, should be library defined.
     * @param data Data result intent.
     *
     * @return File URI.
     *
     * @throws IOException
     */
    private static Uri handleActivityUriResult(final Context context, final MediaPickerRequest request, final Intent data) throws IOException {

        switch (request) {

            case REQUEST_CAPTURE:
            case REQUEST_CROP:

                return getCaptureFileUriAndClear(context);

            case REQUEST_CHOOSER:

                if (data != null && data.getData() != null) {

                    deleteCaptureFileUri(context);

                    return data.getData();
                }

                return getCaptureFileUriAndClear(context);

            case REQUEST_DOCUMENTS:
            case REQUEST_GALLERY:

                deleteCaptureFileUri(context);

                if (data == null || data.getData() == null) {

                    throw new IOException("Picker returned no data result.");
                }

                return data.getData();
        }

        throw new IOException("Picker returned unknown request.");
    }

    /**
     * Create a temporary image file and persist it for later retrieval.  We use shared
     * preferences here in the case that we lose our current
     * instance by the time the activity returns a result.
     *
     * @param context Source {@link Context}.
     *
     * @return Uri of the created file.
     *
     * @throws IOException
     */
    private static Uri createTempImageFileAndPersistUri(final Context context) throws IOException {

        final Uri captureFileURI = Uri.fromFile(MediaPickerFile.createWithSuffix(".jpg"));

        persistUri(context, captureFileURI.toString());

        return captureFileURI;
    }

    /**
     * Persist a string Uri to shared preferences.  Can be null to remove
     * any existing value.
     *
     * @param context Source {@link Context}.
     * @param uri Target Uri string.
     */
    private static void persistUri(final Context context, final String uri) {

        final SharedPreferences sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(NAME, uri);
        editor.apply();
    }

    /**
     * When taking a picture from a camera or cropping, we need to store the
     * Uri to a temporary file to be filled.  Fetch it here and once
     * recovered remove it from memory.
     *
     * @param context Source {@link Context}.
     *
     * @return Associated file Uri if found.
     */
    private static Uri getCaptureFileUriAndClear(final Context context) {

        final String uriString = context.getSharedPreferences(NAME, Context.MODE_PRIVATE).getString(NAME, null);

        if (uriString != null) {

            persistUri(context, null);

            return Uri.parse(uriString);
        }

        return null;
    }

    /**
     * If present, delete capture file URI from disk.
     *
     * @return Returns true if cleaned successfully.
     */
    private static boolean deleteCaptureFileUri(final Context context) throws IOException {

        final Uri uri = getCaptureFileUriAndClear(context);

        if (uri != null) {

            final File file = MediaPickerUri.resolveToFile(context, uri);
            final boolean result = file.delete();

            refreshSystemMediaScanDataBase(context, file);

            return result;
        }

        return true;
    }

    /**
     * Refresh so file appears in associated
     * gallery and media explorer applications.
     */
    public static void refreshSystemMediaScanDataBase(final Context context, final File file) {
        final Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        mediaScanIntent.setData(Uri.fromFile(file));

        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * Invoked when any sort of input or output error occurs.  In the case of
     * the media picker this is most likely to be a result of a bad
     * file path or invalid file itself.
     */
    public interface OnError {

        void onError(final IOException e);
    }

    public static abstract class BaseOnError implements OnError {

        public void onFileCreationError(final Exception e) {
            return;
        }
    }

    /**
     * Complete result callback, can also throw IO errors or
     * a cancellation result from the user.
     */
    public interface OnResult extends OnError {

        void onSuccess(final File mediaFile, MediaPickerRequest request);

        void onCancelled();

        /**
         * Most consumers only care about the success callback.
         */
        abstract class Default implements OnResult {

            @Override
            public void onCancelled() {

            }

            @Override
            public void onError(IOException e) {

            }
        }
    }

    /**
     * Provider for context and start activity for
     * result.  Typically should be a target
     * activity fragment, or app compat fragment instance.
     */
    public interface Provider {

        Context getContext();

        void startActivityForResult(final Intent intent, final int requestCode);
    }
}
