package com.example.peacock.bluredsvg;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.FileDescriptor;

public class MainActivity extends AppCompatActivity {

    public static Bitmap bitmap;

    private int SELECT_FILE = 1;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, SELECT_FILE);

                } else {

                    intent = new Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(Intent.createChooser(intent, "Select File"),
                            SELECT_FILE);

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECT_FILE && data != null) {

            Bitmap bmp = null;

            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

                bmp = getImageFromKitkat(data);

            } else {

                bmp = onSelectFromGalleryResult(data);

            }

            if (bmp != null) {

                int numberScale = Math.max(bmp.getHeight(), bmp.getWidth());

                if (numberScale >= 500) {

                    bitmap = bmp.createScaledBitmap(bmp, numberScale, numberScale, true);

                } else {

                    bitmap = bmp.createScaledBitmap(bmp, 500, 500, true);

                }

                startActivity(new Intent(MainActivity.this, BlurSVG.class));

            }
        }
    }

    private Bitmap onSelectFromGalleryResult(Intent data) {

        Uri selectedImageUri = data.getData();
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(selectedImageUri, projection, null, null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();

        String selectedImagePath = cursor.getString(column_index);

        System.out.println("\n\n\n selectedImagePath --> " + selectedImagePath + "\n\n\n");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(selectedImagePath, options);
        final int REQUIRED_SIZE = 1080;
        int scale = 1;
        while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                && options.outHeight / scale / 2 >= REQUIRED_SIZE)
            scale *= 2;
        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);

        return bitmap;

    }

    private Bitmap getImageFromKitkat(Intent data) {

        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor = getContentResolver().openFileDescriptor(data.getData(), "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
            final int REQUIRED_SIZE = 700;
            int scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                    && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;

            Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor,
                    null, options);

            parcelFileDescriptor.close();

            return bmp;

        } catch (Exception e) {

            System.out.print("KitkatGalleryException ...>>>..." + e.getMessage());

            return null;

        }
    }
}
