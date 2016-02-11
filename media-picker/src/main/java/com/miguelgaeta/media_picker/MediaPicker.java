package com.miguelgaeta.media_picker;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;

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

    /*
    public static void startForImageCrop(@NonNull Fragment fragment, @NonNull Uri uri, @ColorRes int colorResId, Action1<Void> onError) {

        captureFileURI = Uri.fromFile(MediaPickerFile.create());

        startForIntent(null, fragment, file -> {

            CropImageIntentBuilder intentBuilder = new CropImageIntentBuilder(128, 128, Uri.fromFile(file));

            final int color = MGColor.fromResource(fragment.getContext(), colorResId);

            intentBuilder.setSourceImage(uri);
            intentBuilder.setDoFaceDetection(true);
            intentBuilder.setOutlineCircleColor(color);
            intentBuilder.setOutlineColor(color);
            intentBuilder.setScaleUpIfNeeded(true);

            return intentBuilder.getIntent(fragment.getActivity());

        }, REQUEST_CROP, onError);
    }*/


    public static void handleActivityResult(final Context context, final int requestCode, final int resultCode, final Intent data, final HandleResult result) {

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

        void onError(IOException error);
    }

    public interface HandleResult {

        void onError(IOException error);

        void onSuccess(File mediaFile);
    }
}
