package com.miguelgaeta.android_media_picker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.miguelgaeta.media_picker.MediaPicker;
import com.miguelgaeta.media_picker.MimeType;
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

        }, MimeType.IMAGES));

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
            public void onSuccess(final Uri uri, final String mimeType, RequestType request) {
                Log.e("MediaPicker", "Got file result: '" + uri + "', with mime type: '" + mimeType + "', for code: '" + request + "'.");

                if (request != RequestType.CROP && MimeType.isImage(mimeType)) {

//                    final int paramColor = ContextCompat.getColor(AppActivity.this, android.R.color.black);
                    final int paramWidth = 512;
                    final int paramHeight = 512;

                    MediaPicker.startForImageCrop(AppActivity.this, uri, paramWidth, paramHeight, e -> {

                        Log.e("MediaPicker", "Open cropper error.", e);
                    });

                } else {
                    ImageView imageView = (ImageView) findViewById(R.id.image_result);
                    imageView.setImageURI(uri);
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
