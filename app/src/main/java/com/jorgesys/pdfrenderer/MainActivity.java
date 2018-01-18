package com.jorgesys.pdfrenderer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView)findViewById(R.id.imageView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Check for permissions, Android 6.0+
            checkExternalStoragePermission();
        }

        CopyRawToSDCard(R.raw.my_pdf_file, Environment.getExternalStorageDirectory() + "/pdf_file.pdf" );
        //* Rendering page number 2 (index 1)
        renderPDF(1);

    }

    private void CopyRawToSDCard(int id, String path) {
        InputStream in = getResources().openRawResource(id);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            byte[] buff = new byte[1024];
            int read = 0;
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            in.close();
            out.close();
            Log.i(TAG, "copyFile, success!");
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyFile FileNotFoundException " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "copyFile IOException " + e.getMessage());
        }
    }

    private void renderPDF(int currentPage) {
        try {
            int CONTAINER_WIDTH = getScreenWidth();
            int CONTAINER_HEIGHT = getScreenHeight();

            Bitmap bitmap = Bitmap.createBitmap(CONTAINER_WIDTH, CONTAINER_HEIGHT, Bitmap.Config.ARGB_4444);

            File file = new File(Environment.getExternalStorageDirectory() + "/pdf_file.pdf");
            String msj = file.exists()?"File exists! ":"File doesn´t exists! ";
            Log.i(TAG, msj +  file.getAbsolutePath());

            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY));

            if (currentPage < 0) {
                currentPage = 0;
            } else if (currentPage > renderer.getPageCount()) {
                currentPage = renderer.getPageCount() - 1;
            }

            Matrix m = imageView.getImageMatrix();
            Rect rect = new Rect(0, 0, CONTAINER_WIDTH, CONTAINER_HEIGHT);
            renderer.openPage(currentPage).render(bitmap, rect, m, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            imageView.setImageMatrix(m);
            imageView.setImageBitmap(bitmap);
            imageView.invalidate();

        } catch (Exception e) {
            Log.i(TAG, "renderPDF() "  + e.getMessage());
        }
    }


    private void checkExternalStoragePermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission not granted.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 225);
        } else {
            Log.i(TAG, "Permission granted!");
        }
    }

     private int getScreenWidth(){
        return (int) (getResources().getDisplayMetrics().widthPixels / getResources().getDisplayMetrics().density);
    }

    private int getScreenHeight(){
        return (int) (getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().density);
    }


}
