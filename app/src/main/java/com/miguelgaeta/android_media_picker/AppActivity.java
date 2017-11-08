package com.miguelgaeta.android_media_picker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.miguelgaeta.media_picker.MediaPicker;
import com.miguelgaeta.media_picker.RequestType;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings({"ConstantConditions", "CodeBlock2Expr"})
public class AppActivity extends AppCompatActivity implements MediaPicker.Provider {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_activity);

        RxPermissions
            .getInstance(this)
            .requestEach(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .subscribe(permission -> {

                Log.e("MediaPicker", "Permission result: " + permission);
            });

        findViewById(R.id.activity_open_camera_provider).setOnClickListener(v -> MediaPicker.startForCamera(this, e -> {

            Log.e("MediaPicker", "Start for camera error.", e);
        }));

        findViewById(R.id.activity_open_gallery).setOnClickListener(v -> MediaPicker.startForGallery(this, e -> {

            Log.e("MediaPicker", "Start for gallery error.", e);
        }));

        findViewById(R.id.activity_open_gallery_with_image).setOnClickListener(v -> MediaPicker.startForGallery(this, e -> {

            Log.e("MediaPicker", "Start for gallery with images error.", e);

        }, "image/*"));

        findViewById(R.id.activity_open_documents).setOnClickListener(v -> MediaPicker.startForDocuments(this, e -> {

            Log.e("MediaPicker", "Start for documents error.", e);
        }));

        findViewById(R.id.activity_open_chooser).setOnClickListener(v -> MediaPicker.openMediaChooser(this, "Choose now", e -> {

            Log.e("MediaPicker", "Open chooser error.", e);
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        MediaPicker.handleActivityResult(this, requestCode, resultCode, data, new MediaPicker.OnResult() {

            @Override
            public void onError(IOException e) {
                Log.e("MediaPicker", "Got file error.", e);
                Toast.makeText(getContext(), "Got file error:" + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(final Uri uri, final RequestType request) {
                final String mimeType = getMimeType(getContext(), uri);

                Log.e("MediaPicker", "Got file result: '" + uri + "', with mime type: '" + mimeType + "', for code: '" + request + "'.");

                if (request != RequestType.CROP && isImage(mimeType)) {

                    final int paramWidth = 512;
                    final int paramHeight = 512;

                    MediaPicker.startForImageCrop(AppActivity.this, uri, paramWidth, paramHeight, e -> {

                        Log.e("MediaPicker", "Open cropper error.", e);
                    });

                } else {
                    ImageView imageView = findViewById(R.id.image_result);
                    imageView.setImageURI(uri);
                }
            }

            @Override
            public void onCancelled() {
                Log.e("MediaPicker", "Got cancelled event.");
            }
        });
    }

    /**
     * Convenience method to determine if a given
     * mime type is an image.
     *
     * @param mimeType Mime type string.
     *
     * @return True if image, false otherwise.
     */
    public static boolean isImage(final String mimeType) {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Get a best guess of the mime type associated with the target {@link Uri}.
     *
     * @param context Optional {@link Context} that checks for mime type from {@link ContentResolver}.
     * @param uri Target {@link Uri} to obtain mime type for.
     *
     * @return Discovered mime type or null.
     */
    public static String getMimeType(final Context context, final Uri uri) {
        if (uri == null) {
            return null;
        }

        if (context != null) {
            final String mimeType = getMimeTypeFromContentResolver(context.getContentResolver(), uri);

            if (mimeType != null) {
                return mimeType;
            }
        }

        return getMimeTypeFromExtension(uri.toString());
    }

    /**
     * Uses {@link ContentResolver#getType(Uri)} to extract a mime type.
     *
     * @param contentResolver {@link ContentResolver}.
     * @param uri {@link Uri}.
     *
     * @return Discovered mime type or null.
     */
    private static String getMimeTypeFromContentResolver(final ContentResolver contentResolver,
                                                         final Uri uri) {
        if (uri == null || uri.getScheme() == null || contentResolver == null) {
            return null;
        }

        if (!uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            return null;
        }

        return contentResolver.getType(uri);
    }

    /**
     * Uses {@link MimeTypeMap#getMimeTypeFromExtension(String)} to extract a mime type.
     *
     * @param url {@link Uri}.
     *
     * @return Discovered mime type or null.
     */
    private static String getMimeTypeFromExtension(final String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {
            extension = extension.toLowerCase();

            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return null;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public File getImageFile() {
        final File imagesDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Pictures");

        if (!imagesDirectory.mkdirs() && !imagesDirectory.isDirectory()) {
            Log.e("MediaPicker", "Directory not created");
        }

        @SuppressLint("SimpleDateFormat")
        final String timeStamp = (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date());
        final String imageFileName = timeStamp + ".jpg";

        return new File(imagesDirectory, "imageFile-" + imageFileName);
    }
}
