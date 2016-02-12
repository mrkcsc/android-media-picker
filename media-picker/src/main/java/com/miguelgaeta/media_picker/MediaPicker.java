package com.miguelgaeta.media_picker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.android.camera.CropImageIntentBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
@SuppressWarnings("UnusedDeclaration")
public class MediaPicker {

    @Target({METHOD, PARAMETER, FIELD}) @Retention(SOURCE) @Documented
    public @interface Request {

    }

    public static final @Request int REQUEST_CAPTURE   = 777;
    public static final @Request int REQUEST_GALLERY   = 779;
    public static final @Request int REQUEST_DOCUMENTS = 800;
    public static final @Request int REQUEST_CROP      = 801;

    @SuppressWarnings("FieldCanBeLocal")
    private static Uri captureFileURI;

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

            captureFileURI = Uri.fromFile(MediaPickerFile.createWithSuffix(".jpg"));

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI);

            startFor(activity, fragment, intent, REQUEST_CAPTURE);

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * @see #startForGallery(Activity, Fragment)
     */
    public static void startForGallery(final Activity activity) {

        startForGallery(activity, null);
    }

    /**
     * @see #startForGallery(Activity, Fragment)
     */
    public static void startForGallery(final Fragment fragment) {

        startForGallery(null, fragment);
    }

    /**
     * Start the gallery application directly.
     *
     * @param activity Source {@link Activity}.
     * @param fragment Source {@link Fragment}.
     */
    private static void startForGallery(final Activity activity, final Fragment fragment) {

        final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startFor(activity, fragment, intent, REQUEST_GALLERY);
    }

    /**
     * @see #startForDocuments(Activity, Fragment)
     */
    private static void startForDocuments(final Fragment fragment) {

        startForDocuments(null, fragment);
    }

    /**
     * @see #startForDocuments(Activity, Fragment)
     */
    private static void startForDocuments(final Activity activity) {

        startForDocuments(activity, null);
    }

    /**
     * Start the documents chooser directly.
     *
     * @param activity Source activity.
     * @param fragment Source fragment.
     */
    private static void startForDocuments(final Activity activity, final Fragment fragment) {

        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");

        startFor(activity, fragment, intent, REQUEST_DOCUMENTS);
    }

    /**
     * @see #startForImageCrop(Activity, Fragment, Uri, int, int, int, OnError)
     */
    private static void startForImageCrop(final Fragment fragment, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        startForImageCrop(null, fragment, uri, outputWidth, outputHeight, colorInt, result);
    }

    /**
     * @see #startForImageCrop(Activity, Fragment, Uri, int, int, int, OnError)
     */
    private static void startForImageCrop(final Activity activity, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        startForImageCrop(activity, null, uri, outputWidth, outputHeight, colorInt, result);
    }

    private static void startForImageCrop(final Activity activity, final Fragment fragment, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError result) {

        try {

            captureFileURI = Uri.fromFile(MediaPickerFile.createWithSuffix(".jpg"));

            final CropImageIntentBuilder intentBuilder = new CropImageIntentBuilder(outputWidth, outputHeight, captureFileURI);

            intentBuilder.setSourceImage(uri);
            intentBuilder.setDoFaceDetection(false);
            intentBuilder.setOutlineCircleColor(colorInt);
            intentBuilder.setOutlineColor(colorInt);
            intentBuilder.setScaleUpIfNeeded(true);

            final Context context = activity != null ? activity : fragment.getContext();

            startFor(activity, fragment, intentBuilder.getIntent(context), REQUEST_CROP);

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
    private static void startFor(final Activity activity, final Fragment fragment, final Intent intent, final int requestCode) {

        if (activity != null) {
            activity.startActivityForResult(intent, requestCode);
        }

        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
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

        try {

            switch (resultCode) {

                case Activity.RESULT_OK:

                    result.onSuccess(MediaPickerUri.resolveToFile(context, handleActivityUriResult(requestCode, data)), requestCode);

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
     * @param requestCode Source request code, should be library defined.
     * @param data Data result intent.
     *
     * @return File URI.
     *
     * @throws IOException
     */
    private static Uri handleActivityUriResult(final int requestCode, final Intent data) throws IOException {

        switch (requestCode) {

            case REQUEST_CAPTURE:
            case REQUEST_CROP:

                if (captureFileURI == null) {

                    throw new IOException("Capture result file data not found.");
                }

                return captureFileURI;
        }

        if (data == null || data.getData() == null) {

            throw new IOException("Picker returned no data result.");
        }

        return data.getData();
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

        void onSuccess(final File mediaFile, @Request int requestCode);

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
