package com.miguelgaeta.media_picker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import com.android.camera.CropImageIntentBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
@SuppressWarnings({"UnusedDeclaration", "DefaultFileTemplate", "JavadocReference"})
public class MediaPicker {

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
            final Uri captureFileURI = createTempImageFileAndPersistUri(provider.getContext());

            final Intent intent = MediaPickerChooser.getMediaChooserIntent(provider.getContext().getPackageManager(), title, captureFileURI);

            startFor(provider, intent, MediaPickerRequest.REQUEST_CHOOSER.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * Start the camera application.
     *
     * @param provider Source {@link Provider}.
     * @param result The camera open action can fail so capture the result.
     */
    public static void startForCamera(final Provider provider, final OnError result) {
        try {
            final Uri captureFileURI = createTempImageFileAndPersistUri(provider.getContext());

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startFor(provider, intent, MediaPickerRequest.REQUEST_CAPTURE.getCode());

        } catch (final Exception e) {
            result.onError(new IOException("Unable to create temporary file.", e));
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

                    final Uri uri = handleActivityUriResult(context, request, data);
                    final File file = MediaPickerUri.resolveToFile(context, uri);

                    refreshSystemMediaScanDataBase(context, file);

                    result.onSuccess(file, MimeType.getMimeType(context, uri), request);

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
     * @throws IOException Error thrown when no result can be extracted.
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
     * @throws IOException Throws if cannot be created or persisted.
     */
    private static Uri createTempImageFileAndPersistUri(final Context context) throws IOException {
        final File file = MediaPickerFile.create(context.getFilesDir(), "files", ".jpg");

        final String authority = context.getPackageName() + ".file-provider";

        final Uri captureFileURI = FileProvider.getUriForFile(context, authority, file);

        persistUri(context, file.toURI().toString());

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

        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();

        editor.putString("picker_uri", uri);
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

        final String uriString = getSharedPreferences(context).getString("picker_uri", null);

        if (uriString != null) {

            persistUri(context, null);

            return Uri.parse(uriString);
        }

        return null;
    }

    /***
     * Fetch private instance of shared preferences used for catching.
     *
     * @param context {@link Context}.
     *
     * @return Instance of {@link SharedPreferences}.
     */
    private static SharedPreferences getSharedPreferences(final Context context) {
        return context.getSharedPreferences("picker", Context.MODE_PRIVATE);
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
    @SuppressWarnings("WeakerAccess")
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

    /**
     * Complete result callback, can also throw IO errors or
     * a cancellation result from the user.
     */
    public interface OnResult extends OnError {

        void onSuccess(final File mediaFile, final String mimeType, final MediaPickerRequest request);

        void onCancelled();
    }

    /**
     * Provider interface used to drive the operation of the picker.
     *
     * Recommended to be implemented on top of a {@link Activity} or {@link android.app.Fragment}
     * lifecycle object as they provide the most direct implementation of the context.
     *
     * For API 24+ you must use a {@link android.support.v4.content.FileProvider} content URI.
     */
    public interface Provider {

        Context getContext();

        void startActivityForResult(final Intent intent, final int requestCode);
    }
}
