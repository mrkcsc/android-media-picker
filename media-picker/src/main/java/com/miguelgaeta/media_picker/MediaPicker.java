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

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
public class MediaPicker {

    public static final int REQUEST_CAPTURE   = 777;
    public static final int REQUEST_GALLERY   = 779;
    public static final int REQUEST_DOCUMENTS = 800;
    public static final int REQUEST_CROP      = 801;

    @SuppressWarnings("FieldCanBeLocal")
    private static Uri captureFileURI;

    /**
     * @see #startForCamera(Activity, Fragment, StartResult)
     */
    @SuppressWarnings("unused")
    public static void startForCamera(final Activity activity, final StartResult result) {

        startForCamera(activity, null, result);
    }

    /**
     * @see #startForCamera(Activity, Fragment, StartResult)
     */
    @SuppressWarnings("unused")
    public static void startForCamera(final Fragment fragment, final StartResult result) {

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
    private static void startForCamera(final Activity activity, final Fragment fragment, final StartResult result) {

        try {

            captureFileURI = Uri.fromFile(MediaPickerFile.create());

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI);

            startFor(activity, fragment, intent, REQUEST_CAPTURE);

        } catch (IOException e) {

            result.onError(e);
        }
    }

    /**
     * @see #startForGallery(Activity, Fragment)
     */
    @SuppressWarnings("unused")
    public static void startForGallery(final Activity activity) {

        startForGallery(activity, null);
    }

    /**
     * @see #startForGallery(Activity, Fragment)
     */
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    private static void startForDocuments(final Fragment fragment) {

        startForDocuments(null, fragment);
    }

    /**
     * @see #startForDocuments(Activity, Fragment)
     */
    @SuppressWarnings("unused")
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
     * @see #startForImageCrop(Activity, Fragment, Uri, int, int, int, StartResult)
     */
    @SuppressWarnings("unused")
    private static void startForImageCrop(final Fragment fragment, final Uri uri, int outputWidth, int outputHeight, int colorInt, final StartResult result) {

        startForImageCrop(null, fragment, uri, outputWidth, outputHeight, colorInt, result);
    }

    /**
     * @see #startForImageCrop(Activity, Fragment, Uri, int, int, int, StartResult)
     */
    @SuppressWarnings("unused")
    private static void startForImageCrop(final Activity activity, final Uri uri, int outputWidth, int outputHeight, int colorInt, final StartResult result) {

        startForImageCrop(activity, null, uri, outputWidth, outputHeight, colorInt, result);
    }

    private static void startForImageCrop(final Activity activity, final Fragment fragment, final Uri uri, int outputWidth, int outputHeight, int colorInt, final StartResult result) {

        try {

            captureFileURI = Uri.fromFile(MediaPickerFile.create());

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

    public static void handleActivityResult(final Context context, final int requestCode, final int resultCode, final Intent data, final Result result) {

        result.onError(null);
        result.onSuccess(null);
    }

    /*
    public Observable<FileResult> handleResult(@NonNull Context context, int requestCode, int resultCode, Intent data) {

        return Observable.create(subscriber -> {

            Uri uri = null;

            if (resultCode == Activity.RESULT_OK) {

                switch (requestCode) {

                    case REQUEST_CAPTURE:
                    case REQUEST_CROP:

                        uri = fileUri;

                        break;

                    case REQUEST_GALLERY:

                        if (data != null) {

                            uri = data.getData();
                        }

                        break;
                }
            }

            if (uri != null) {

                File file = MediaPickerUriResolver.getFile(context, uri);

                if (file != null && file.exists()) {

                    subscriber.onNext(FileResult.create(new TypedFile(MediaPickerUriResolver.getMimeType(file), file), FileResult.Status.OK));

                } else {

                    subscriber.onNext(FileResult.create(null, FileResult.Status.FILE_NOT_FOUND));
                }

            } else  {

                subscriber.onNext(FileResult.create(null, FileResult.Status.URI_NOT_FOUND));
            }

            subscriber.onCompleted();
        });
    }
    */

    public interface StartResult {

        void onError(final IOException e);
    }

    public interface Result {

        void onError(final IOException e);

        void onSuccess(final File mediaFile);
    }
}
