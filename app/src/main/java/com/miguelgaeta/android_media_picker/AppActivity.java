package com.miguelgaeta.android_media_picker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.miguelgaeta.media_picker.MediaPicker;
import com.miguelgaeta.media_picker.MediaPickerEncoder;
import com.miguelgaeta.media_picker.MediaPickerRequest;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        findViewById(R.id.activity_open_camera_provider).setOnClickListener(v -> MediaPicker.startForCamera(
                AppActivity.this,
                new MyContentFileProvider(),
                new MediaPicker.BaseOnError() {
                    @Override
                    public void onError(final IOException e) {
                        Log.e("MediaPicker", "Start for camera error.", e);
                    }

                    @Override
                    public void onFileCreationError(final Exception e) {
                        Log.e("MediaPicker", "Creating output file error.", e);
                    }
                }));

        findViewById(R.id.activity_open_gallery).setOnClickListener(v -> MediaPicker.startForGallery(this, e -> {

            Log.e("MediaPicker", "Start for gallery error.", e);
        }));

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
            }

            @Override
            public void onSuccess(File mediaFile, MediaPickerRequest request) {

                Log.e("MediaPicker", "Got file result: " + mediaFile + " for code: " + request);

                if (request != MediaPickerRequest.REQUEST_CROP) {

                    final int paramColor = ContextCompat.getColor(AppActivity.this, android.R.color.black);
                    final int paramWidth = 128;
                    final int paramHeight = 128;

                    MediaPicker.startForImageCrop(AppActivity.this, mediaFile, paramWidth, paramHeight, paramColor, e -> {

                        Log.e("MediaPicker", "Open cropper error.", e);
                    });

                } else {

                    try {

                        @SuppressWarnings("unused")
                        final String dataUrl = MediaPickerEncoder.toDataUrl(mediaFile);

                    } catch (IOException e) {

                        Log.e("MediaPicker", "Unable to get data url.", e);
                    }
                }
            }

            @Override
            public void onCancelled() {

                Log.e("MediaPicker", "Got cancelled event.");
            }
        });
    }

    @Override
    public Context getContext() {
        return this;
    }

    private class MyContentFileProvider implements MediaPicker.ContentFileProvider {
        @Override
        public File createFile() throws IOException {
            File imagePath = getExternalFilesDir("images");

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "img_" + timeStamp;
            return File.createTempFile(imageFileName, ".jpg", imagePath);

        }

        @Override
        public Uri toUri(final File file) {
            return FileProvider.getUriForFile(AppActivity.this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    file);
        }
    }
}
