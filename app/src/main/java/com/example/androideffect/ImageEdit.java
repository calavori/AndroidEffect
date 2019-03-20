package com.example.androideffect;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import static org.bytedeco.javacpp.opencv_core.*;


public class ImageEdit extends Activity {

    private Button snowbutton;
    private Button mirrorbutton;
    private Button glitchbutton;
    private Button eyebutton;
    private Button resetbutton;
    private Button savebutton;
    private ImageView imageView;
    private Bitmap src_bmp;
    private Bitmap final_bmp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
    }

    @Override
    protected void onStart() {
        super.onStart();
        String value = new String();
        Intent edit = getIntent();
        if (edit.hasExtra("byteArray")) {
            Bundle extras = edit.getExtras();
            byte[] byteArray = extras.getByteArray("byteArray");

            src_bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } else if (edit.hasExtra("string")) {
            try {
                value = edit.getStringExtra("string");
                src_bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(value));
            } catch (Exception e) {
                System.out.print(e);
            }
        }

        this.imageView = this.findViewById(R.id.imageView);
        imageView.setImageBitmap(src_bmp);
        final_bmp = src_bmp;


        this.snowbutton = (Button) this.findViewById(R.id.snowbutton);
        this.mirrorbutton = (Button) this.findViewById(R.id.mirrorbutton);
        this.glitchbutton = (Button) this.findViewById(R.id.glitchbutton);
        this.eyebutton = (Button) this.findViewById(R.id.eyebutton);
        this.resetbutton = (Button) this.findViewById(R.id.resetbutton);
        this.savebutton = (Button) this.findViewById(R.id.savebutton);

        //snow
        this.snowbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                final_bmp = snow(final_bmp);
                imageView.setImageBitmap(final_bmp);
            }
        });

        //mirror
        this.mirrorbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                final_bmp = mirror(final_bmp);
                imageView.setImageBitmap(final_bmp);
            }
        });

        //glitch
        this.glitchbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                final_bmp = glitch(final_bmp);
                imageView.setImageBitmap(final_bmp);
            }
        });

        //reset
        this.resetbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                final_bmp = src_bmp;
                imageView.setImageBitmap(final_bmp);
            }
        });

        //save
        this.savebutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(final_bmp);
            }
        }
        );

    }


    private Bitmap snow(Bitmap src){
        Bitmap effect_bmp = null;
        if(src.getHeight()/src.getWidth() == 16/9) {
            effect_bmp = getBitmapFromAssets("snow16.png");
        }
        else if (src.getHeight()/src.getWidth() == 4/3) {
            effect_bmp = getBitmapFromAssets("snow4.png");
        }
        //xu li anh
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        final_bmp =  Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(final_bmp);
        canvas.drawBitmap(src, new Matrix(), null);
        canvas.drawBitmap(effect_bmp, null, new RectF(0, 0, src.getWidth(), src.getHeight()), paint);
        //
        return final_bmp;
      }

    private Bitmap mirror(Bitmap src){
        Bitmap effect_bmp = null;
        if(src.getHeight()/src.getWidth() == 16/9) {
            effect_bmp = getBitmapFromAssets("mirror16.png");
        }
        else if (src.getHeight()/src.getWidth() == 4/3) {
            effect_bmp = getBitmapFromAssets("mirror4.png");
        }
        //xu li anh
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        final_bmp =  Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(final_bmp);
        canvas.drawBitmap(src, new Matrix(), null);
        canvas.drawBitmap(effect_bmp, null, new RectF(0, 0, src.getWidth(), src.getHeight()), paint);
        //
        return final_bmp;
    }

    private Bitmap glitch(Bitmap src){
        try {
            final_bmp = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            Canvas canvas = new Canvas(final_bmp);
            canvas.drawBitmap(src, new Matrix(), null);

            //blend mau
            Bitmap btemp = null;
            Paint p = new Paint();
            ColorFilter blue_filter = new LightingColorFilter(Color.BLUE,0);
            ColorFilter green_filter = new LightingColorFilter(Color.GREEN,0);
            btemp = Bitmap.createBitmap(final_bmp, 80, 0, final_bmp.getWidth()-80, final_bmp.getHeight());
            Canvas filter = new Canvas(btemp);
            p.setColorFilter(blue_filter);
            filter.drawBitmap(final_bmp, new Matrix(), p);
            p.setAlpha(40);
            canvas.drawBitmap(btemp, 0, 0, p);

            btemp = Bitmap.createBitmap(final_bmp, 0, 0, final_bmp.getWidth()-80, final_bmp.getHeight());
            filter.setBitmap(btemp);
            p.setColorFilter(green_filter);
            filter.drawBitmap(final_bmp, new Matrix(), p);
            p.setAlpha(40);
            canvas.drawBitmap(btemp, 80, 0, p);

            //glitch
            Random r = new Random();
            for(int i=0; i<= 20; i++) {
                int x = r.nextInt(final_bmp.getWidth() - 0) + 0;
                int y = r.nextInt(final_bmp.getHeight() - 0) + 0;
                int width = r.nextInt(final_bmp.getWidth()/3 - final_bmp.getWidth()/4) + final_bmp.getWidth()/4;
                int height = r.nextInt(final_bmp.getHeight()/20 - final_bmp.getHeight()/25) + final_bmp.getHeight()/15;
                btemp = Bitmap.createBitmap(final_bmp, x, y, width, height);
                canvas.drawBitmap(btemp, x + 100, y, null);
                if((x+100+width)> final_bmp.getWidth() || (y+height) > final_bmp.getHeight()){
                    i--;
                }
            }

        } catch (Exception e){}
        return final_bmp;
    }


    private Bitmap getBitmapFromAssets(String name){
        Bitmap bmp = null;
        try {
            AssetManager assetManager = getAssets();
            InputStream istr = assetManager.open(name);
            bmp = BitmapFactory.decodeStream(istr);
            istr.close();
        } catch (IOException e) { e.printStackTrace(); }
        return bmp;
    }
    private void saveImage(Bitmap finalBitmap) {
        Date d = new Date();
        CharSequence s  = DateFormat.format("MM-dd-yy hh-mm-ss", d.getTime());
        String fname = "Image-" + s.toString()+ ".jpg";
        MediaStore.Images.Media.insertImage(this.getContentResolver(), finalBitmap,fname, null);
        Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
    }
}
