package com.miguelgaeta.android_media_picker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.miguelgaeta.media_picker.MediaPicker;

import java.io.File;
import java.io.IOException;

public class AppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.app_activity);

        findViewById(R.id.activity_open_camera).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.e("MediaPicker", "On open camera clicked.");

                MediaPicker.startForCamera(AppActivity.this, new MediaPicker.OnError() {

                    @Override
                    public void onError(IOException e) {

                        Log.e("MediaPicker", "Start for camera error.", e);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("MediaPicker", "On activity result.");

        MediaPicker.handleActivityResult(this, requestCode, resultCode, data, new MediaPicker.OnResult() {

            @Override
            public void onError(IOException e) {

                Log.e("MediaPicker", "Got file error.", e);
            }

            @Override
            public void onSuccess(File mediaFile) {

                Log.e("MediaPicker", "Got file result: " + mediaFile);
            }

            @Override
            public void onCancelled() {

                Log.e("MediaPicker", "Got cancelled event.");
            }
        });
    }
}
