package jp.techacademy.takashi.sasaki.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    protected Timer timer;
    protected Handler handler = new Handler();

    protected Button rewindButton;
    protected Button playButton;
    protected Button forwardButton;
    protected ImageView image;

    protected Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("debug", ":: onCreate ::::::::::::::::::::::::::::::::::::::::::::::::::");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = findViewById(R.id.image);

        rewindButton = findViewById(R.id.rewindButton);
        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.moveToPrevious()) {
                    setImage();
                } else {
                    cursor.moveToLast();
                    setImage();
                }
            }
        });

        playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timer != null) {
                    stopSlide();
                    playButton.setText("再生");
                    rewindButton.setEnabled(true);
                    forwardButton.setEnabled(true);
                } else {
                    startSlide();
                    playButton.setText("停止");
                    rewindButton.setEnabled(false);
                    forwardButton.setEnabled(false);
                }
            }
        });

        forwardButton = findViewById(R.id.forwardButton);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cursor.moveToNext()) {
                    setImage();
                } else {
                    cursor.moveToFirst();
                    setImage();
                }
            }
        });

        // 外部ストレージのパーミッション検証
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0以降
            Log.d("debug", "Android 6.0以降");
            checkPermission();
        } else {
            // Android 5系以下
            Log.d("debug", "Android 5系以下");
            prepareImage();
        }
    }

    private void startSlide() {
        Log.d("debug", ":: startSlide ::::::::::::::::::::::::::::::::::::::::::::::::");
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (cursor.moveToNext()) {
                                setImage();
                            } else {
                                cursor.moveToFirst();
                                setImage();
                            }
                        }
                    });
                }
            }, 2000, 2000);
        }
    }

    private void stopSlide() {
        Log.d("debug", ":: stopSlide :::::::::::::::::::::::::::::::::::::::::::::::::");
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void checkPermission() {
        Log.d("debug", ":: checkPermission :::::::::::::::::::::::::::::::::::::::::::");
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "パーミッション許可済み");
            prepareImage();
        } else {
            Log.d("debug", "パーミッション許可選択ダイアログ表示");
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("debug", ":: onRequestPermissionsResult ::::::::::::::::::::::::::::::::");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("debug", "パーミッション許可済み");
            prepareImage();
        } else {
            Log.d("debug", "パーミッション不許可");
            rewindButton.setEnabled(false);
            playButton.setEnabled(false);
            forwardButton.setEnabled(false);
        }
    }

    private void prepareImage() {
        Log.d("debug", ":: prepareImage ::::::::::::::::::::::::::::::::::::::::::::::");
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            setImage();
        } else {
            rewindButton.setEnabled(false);
            playButton.setEnabled(false);
            forwardButton.setEnabled(false);
        }
    }

    private void setImage() {
        Log.d("debug", ":: setImage ::::::::::::::::::::::::::::::::::::::::::::::::::");
        if (cursor != null) {
            Long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            image.setImageURI(imageUri);
        }
    }

    @Override
    protected void onDestroy() {
        Log.d("debug", ":: onDestroy :::::::::::::::::::::::::::::::::::::::::::::::::");
        super.onDestroy();
        if (cursor != null) {
            cursor.close();
        }
        if (timer != null) {
            timer = null;
        }
    }
}
