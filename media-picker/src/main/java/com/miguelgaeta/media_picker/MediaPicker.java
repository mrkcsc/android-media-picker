package com.miguelgaeta.media_picker;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import com.android.camera.CropImageIntentBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by Miguel Gaeta on 2/10/16.
 */
@SuppressWarnings({"UnusedDeclaration", "DefaultFileTemplate", "JavadocReference", "WeakerAccess"})
public class MediaPicker {

    /**
     * Create a chooser intent that matches all types of activities
     * for taking photos or selecting media.
     *
     * @param provider Source {@link Provider}.
     * @param title Chooser title.
     * @param onError {@link OnError}
     * @param mimeType Mime type filter.
     */
    public static void openMediaChooser(final Provider provider, final String title, final OnError onError, final String mimeType) {
        try {
            final Uri captureFileURI = createTempImageFileAndPersistUri(provider);

            final Intent intent = MediaPickerChooser.getMediaChooserIntent(provider.getContext().getPackageManager(), title, captureFileURI, mimeType);

            final Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            // Grant camera write access just in case user chooses it.
            grantWriteAccessToURI(provider.getContext(), cameraIntent, captureFileURI);

            startFor(provider, intent, RequestType.CHOOSER.getCode());

        } catch (final IOException e) {

            onError.onError(e);
        }
    }

    /**
     * @see #openMediaChooser(Provider, String, OnError, String)
     */
    public static void openMediaChooser(final Provider provider, final String title, final OnError onError) {
        openMediaChooser(provider, title, onError, MimeType.ALL);
    }

    /**
     * Start the camera application.
     *
     * @param provider {@link Provider}
     * @param onError {@link OnError}
     */
    public static void startForCamera(final Provider provider, final OnError onError) {
        try {
            final Uri captureFileURI = createTempImageFileAndPersistUri(provider);

            final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                .putExtra(MediaStore.EXTRA_OUTPUT, captureFileURI)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            grantWriteAccessToURI(provider.getContext(), intent, captureFileURI);

            startFor(provider, intent, RequestType.CAMERA.getCode());

        } catch (final IOException e) {

            onError.onError(e);
        }
    }

    /**
     * Start the gallery application directly.
     *
     * @param provider {@link Provider}
     * @param onError {@link OnError}
     * @param mimeType Mime type filter.
     */
    public static void startForGallery(final Provider provider, final OnError onError, final String mimeType) {
        try {

            final Intent intent = getIntent(Intent.ACTION_PICK, mimeType);

            startFor(provider, intent, RequestType.GALLERY.getCode());

        } catch (final IOException e) {

            onError.onError(e);
        }
    }

    /**
     * @see #startForGallery(Provider, OnError, String)
     */
    public static void startForGallery(final Provider provider, final OnError onError) {
        startForGallery(provider, onError, MimeType.ALL);
    }

    /**
     * Start the documents chooser directly.
     *
     * @param provider Source {@link Provider}.
     * @param onError {@link OnError}
     * @param mimeType Mime type filter.
     */
    public static void startForDocuments(final Provider provider, final OnError onError, final String mimeType) {

        try {

            final Intent intent = getIntent(Intent.ACTION_GET_CONTENT, mimeType);

            startFor(provider, intent, RequestType.DOCUMENTS.getCode());

        } catch (final IOException e) {

            onError.onError(e);
        }
    }

    /**
     * @see #startForDocuments(Provider, OnError, String)
     */
    public static void startForDocuments(final Provider provider, final OnError onError) {
        startForDocuments(provider, onError, MimeType.ALL);
    }

    /**
     * @see #startForImageCrop(Provider, File, int, int, int, OnError)
     */
    public static void startForImageCrop(final Provider provider, final File file, int outputWidth, int outputHeight, int colorInt, final OnError onError) {

        startForImageCrop(provider, Uri.fromFile(file), outputWidth, outputHeight, colorInt, onError);
    }

    /**
     * Start activity for cropping.
     *
     * @param provider Source {@link Provider}.
     * @param uri Source file URI.
     * @param outputWidth Cropped file output width.
     * @param outputHeight Cropped file output height.
     * @param colorInt Cropping UI circle color.
     * @param onError Result callbacks.
     */
    private static void startForImageCrop(final Provider provider, final Uri uri, int outputWidth, int outputHeight, int colorInt, final OnError onError) {
        try {
            final Uri captureFileURI = createTempImageFileAndPersistUri(provider);

            final CropImageIntentBuilder intentBuilder = new CropImageIntentBuilder(outputWidth, outputHeight, captureFileURI);

            intentBuilder.setSourceImage(uri);
            intentBuilder.setDoFaceDetection(false);
            intentBuilder.setOutlineCircleColor(colorInt);
            intentBuilder.setOutlineColor(colorInt);
            intentBuilder.setScaleUpIfNeeded(true);

            final Intent intent = intentBuilder.getIntent(provider.getContext());

            grantWriteAccessToURI(provider.getContext(), intent, captureFileURI);

            startFor(provider, intent, RequestType.CROP.getCode());

        } catch (final IOException e) {

            onError.onError(e);
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

        } catch (final ActivityNotFoundException e) {

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
     * @param onError Result callbacks.
     */
    public static void handleActivityResult(final Context context, final int requestCode, final int resultCode, final Intent data, final OnResult onError) {

        final RequestType request = RequestType.create(requestCode);

        if (request == null) {

            return;
        }

        try {

            switch (resultCode) {

                case Activity.RESULT_OK:

                    final Uri uri = handleActivityUriResult(context, request, data);
                    final File file = MediaPickerUri.resolveToFile(context, uri);

                    refreshSystemMediaScanDataBase(context, file);

                    onError.onSuccess(file, MimeType.getMimeType(context, uri), request);

                    break;

                case Activity.RESULT_CANCELED:

                    onError.onCancelled();

                    break;

                default:

                    throw new IOException("Bad activity result code: " + resultCode + ", for request code: " + requestCode);
            }

        } catch (final IOException e) {

            onError.onError(e);
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
    private static Uri handleActivityUriResult(final Context context, final RequestType request, final Intent data) throws IOException {

        switch (request) {

            case CAMERA:
            case CROP:

                return getCaptureFileUriAndClear(context);

            case CHOOSER:

                if (data != null && data.getData() != null) {

                    return data.getData();
                }

                return getCaptureFileUriAndClear(context);

            case DOCUMENTS:
            case GALLERY:

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
    private static Uri createTempImageFileAndPersistUri(final Provider provider) throws IOException {
        final File file = provider.getImageFile();
        final Context context = provider.getContext();

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
     * This is a hack to allow the file provider API to still
     * work on older API versions.
     *
     * @see http://bit.ly/2iC4bUJ
     */
    private static void grantWriteAccessToURI(final @NonNull Context context,
                                              final @NonNull Intent intent,
                                              final @NonNull Uri uri) {
        final List<ResolveInfo> resInfoList = context
            .getPackageManager()
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resInfoList) {
            final String packageName = resolveInfo.activityInfo.packageName;
            final int mode = Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION;

            context.grantUriPermission(packageName, uri, mode);
        }
    }

    /**
     * Refresh so file appears in associated
     * gallery and media explorer applications.
     */
    private static void refreshSystemMediaScanDataBase(final Context context, final File file) {
        final Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);

        mediaScanIntent.setData(Uri.fromFile(file));

        context.sendBroadcast(mediaScanIntent);
    }

    /**
     * Fetch intent for target action using the
     * provided mime type.
     */
    static Intent getIntent(final @NonNull String action, final @NonNull String mimeType) {
        final Intent intent = new Intent(action);

        intent.setType(mimeType);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType);
        }

        return intent;
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

        void onSuccess(final File mediaFile, final String mimeType, final RequestType request);

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

        File getImageFile();

        void startActivityForResult(final Intent intent, final int requestCode);
    }
}
