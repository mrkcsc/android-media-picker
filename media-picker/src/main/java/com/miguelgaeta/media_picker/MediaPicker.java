package com.miguelgaeta.media_picker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.android.camera.CropImageIntentBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
@SuppressWarnings("UnusedDeclaration")
public class MediaPicker {

    static final String NAME = "media_picker";

    /**
     * @see #openMediaChooser(Activity, Fragment, String, OnError)
     */
    public static void openMediaChooser(final Activity activity, final String title, final OnError result) {

        openMediaChooser(activity, null, title, result);
    }

    /**
     * @see #openMediaChooser(Activity, String, OnError)
     */
    public static void openMediaChooser(final Activity activity, @StringRes final int title, final OnError result) {

        openMediaChooser(activity, activity.getString(title), result);
    }

    /**
     * @see #openMediaChooser(Activity, Fragment, String, OnError)
     */
    public static void openMediaChooser(final Fragment fragment, final String title, final OnError result) {

        openMediaChooser(null, fragment, title, result);
    }

    /**
     * @see #openMediaChooser(Fragment, String, OnError)
     */
    public static void openMediaChooser(final Fragment fragment, @StringRes final int title, final OnError result) {

        openMediaChooser(fragment, fragment.getString(title), result);
    }

    /**
     * Create a chooser intent that matches all types of activities
     * for taking photos or selecting media.
     *
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     *
     * @param title Chooser title.
     *
     * @param result Can fail to create the file needed for the camera intents.
     */
    private static void openMediaChooser(final Activity activity, final Fragment fragment, final String title, final OnError result) {

        try {

            final Context context = activity != null ? activity : fragment.getContext();

            final Uri captureFileURI = createTempImageFileAndPersistUri(activity, fragment);

            final Intent intent = MediaPickerChooser.getMediaChooserIntent(context.getPackageManager(), title, captureFileURI);

            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_CHOOSER.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * @see #startForCamera(Activity, Fragment, OnError)
     */
    public static void startForCamera(final Activity activity, final OnError result) {

        startForCamera(activity, null, result);
    }

    /**
     * @see #startForCamera(Activity, Fragment, OnError)
     */
    public static void startForCamera(final Fragment fragment, final OnError result) {

        startForCamera(null, fragment, result);
    }

    /**
     * Start the camera application correctly.
     *
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     *
     * @param result The camera open action can fail so capture the result.
     */
    private static void startForCamera(final Activity activity, final Fragment fragment, final OnError result) {

        try {

            final Uri captureFileURI = createTempImageFileAndPersistUri(activity, fragment);

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI);

            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_CAPTURE.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * @see #startForGallery(Activity, Fragment, OnError)
     */
    public static void startForGallery(final Activity activity, final OnError result) {

        startForGallery(activity, null, result);
    }

    /**
     * @see #startForGallery(Activity, Fragment, OnError)
     */
    public static void startForGallery(final Fragment fragment, final OnError result) {

        startForGallery(null, fragment, result);
    }

    /**
     * Start the gallery application directly.
     *
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     *
     * @param result Failure to open gallery captured in result callback..
     */
    private static void startForGallery(final Activity activity, final Fragment fragment, final OnError result) {

        try {

            final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_GALLERY.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * @see #startForDocuments(Activity, Fragment, OnError)
     */
    public static void startForDocuments(final Fragment fragment, final OnError result) {

        startForDocuments(null, fragment, result);
    }

    /**
     * @see #startForDocuments(Activity, Fragment, OnError)
     */
    public static void startForDocuments(final Activity activity, final OnError result) {

        startForDocuments(activity, null, result);
    }

    /**
     * Start the documents chooser directly.
     *
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     *
     * @param result The document open action can fail so capture the result.
     */
    private static void startForDocuments(final Activity activity, final Fragment fragment, final OnError result) {

        try {

            final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

            intent.setType("image/*");

            startFor(activity, fragment, intent, MediaPickerRequest.REQUEST_DOCUMENTS.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * @see #startForImageCrop(Activity, Fragment, Uri, int, int, int, OnError)
     */
    public static void startForImageCrop(final Fragment fragment, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        startForImageCrop(null, fragment, uri, outputWidth, outputHeight, colorInt, result);
    }

    /**
     * @see #startForImageCrop(Fragment, File, int, int, int, OnError)
     */
    public static void startForImageCrop(final Fragment fragment, final File file, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        startForImageCrop(fragment, Uri.fromFile(file), outputWidth, outputHeight, colorInt, result);
    }

    /**
     * @see #startForImageCrop(Activity, Fragment, Uri, int, int, int, OnError)
     */
    public static void startForImageCrop(final Activity activity, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        startForImageCrop(activity, null, uri, outputWidth, outputHeight, colorInt, result);
    }

    /**
     * @see #startForImageCrop(Activity, File, int, int, int, OnError)
     */
    public static void startForImageCrop(final Activity activity, final File file, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        startForImageCrop(activity, null, Uri.fromFile(file), outputWidth, outputHeight, colorInt, result);
    }

    /**
     * Start activity for cropping.
     *
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     * @param uri Source file URI.
     * @param outputWidth Cropped file output width.
     * @param outputHeight Cropped file output height.
     * @param colorInt Cropping UI circle color.
     * @param result Result callbacks.
     */
    private static void startForImageCrop(final Activity activity, final Fragment fragment, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        try {

            final Uri captureFileURI = createTempImageFileAndPersistUri(activity, fragment);

            final CropImageIntentBuilder intentBuilder = new CropImageIntentBuilder(outputWidth, outputHeight, captureFileURI);

            intentBuilder.setSourceImage(uri);
            intentBuilder.setDoFaceDetection(false);
            intentBuilder.setOutlineCircleColor(colorInt);
            intentBuilder.setOutlineColor(colorInt);
            intentBuilder.setScaleUpIfNeeded(true);

            final Context context = activity != null ? activity : fragment.getContext();

            startFor(activity, fragment, intentBuilder.getIntent(context), MediaPickerRequest.REQUEST_CROP.getCode());

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * Start activity for result helper that accepts both activities
     * and or fragments.
     *
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     * @param intent Source {@link Intent}
     * @param requestCode Request code for capturing result.
     */
    private static void startFor(final Activity activity, final Fragment fragment, final Intent intent, final int requestCode) throws IOException {

        try {

            if (activity != null) {
                activity.startActivityForResult(intent, requestCode);
            }

            if (fragment != null) {
                fragment.startActivityForResult(intent, requestCode);
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

                    result.onSuccess(MediaPickerUri.resolveToFile(context, handleActivityUriResult(context, request, data)), request);

                    break;

                case Activity.RESULT_CANCELED:

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

                    return data.getData();
                }

                return getCaptureFileUriAndClear(context);

            case REQUEST_DOCUMENTS:
            case REQUEST_GALLERY:

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
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     *
     * @return Uri of the created file.
     *
     * @throws IOException
     */
    private static Uri createTempImageFileAndPersistUri(final Activity activity, final Fragment fragment) throws IOException {

        final Context context = activity != null ? activity : fragment.getContext();

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
}
