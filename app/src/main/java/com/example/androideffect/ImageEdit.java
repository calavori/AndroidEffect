package com.example.androideffect;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.bytedeco.javacv.FrameFilter;

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
    private GlitchInfo glitchInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_edit);
//        getActionBar().setTitle("Choose your effects");
    }

    @Override
    protected void onStart() {
        super.onStart();
        String value = new String();
        Intent edit = getIntent();
            try {
                value = edit.getStringExtra("string");
                src_bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(value));
            } catch (Exception e) {
                System.out.print(e);
            }
            if (src_bmp.getWidth() > src_bmp.getHeight()) {
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                src_bmp = Bitmap.createBitmap(src_bmp , 0, 0, src_bmp.getWidth(), src_bmp.getHeight(), matrix, true);
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
                setGitchInfo();
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

    //chinh thong so glitch
    private void setGitchInfo(){
        glitchInfo = new GlitchInfo();
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.seekbar_dialog);
        dialog.setCancelable(true);
        TextView seed = (TextView) dialog.findViewById(R.id.seed);
        SeekBar seed_bar = (SeekBar) dialog.findViewById(R.id.seed_bar);
        TextView shift = (TextView) dialog.findViewById(R.id.shift);
        SeekBar shift_bar = (SeekBar) dialog.findViewById(R.id.shift_bar);
        TextView thickness = (TextView) dialog.findViewById(R.id.thickness);
        SeekBar thickness_bar = (SeekBar) dialog.findViewById(R.id.thickness_bar);
        Button ok_button = (Button) dialog.findViewById(R.id.ok_button);

        // seed
        seed_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(ImageEdit.this, "Seed: " + String.valueOf(progress), Toast.LENGTH_SHORT).show();
                glitchInfo.setSeed(progress);
            }
        });

        // rbg_shift
        shift_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(ImageEdit.this, "RBG Shift: " + String.valueOf(progress), Toast.LENGTH_SHORT).show();
                glitchInfo.setRbg_shift(progress);
            }
        });

        // Thickness
        thickness_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(ImageEdit.this, "Thickness: " + String.valueOf(progress), Toast.LENGTH_SHORT).show();
                glitchInfo.setThickness(progress);
            }
        });

        //ok button
        ok_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                glitch(final_bmp, glitchInfo.getSeed(), glitchInfo.getRbg_shift(), glitchInfo.getThickness());
                imageView.setImageBitmap(final_bmp);
            }
        });
        dialog.show();
    }

    private Bitmap glitch (Bitmap src, int seed, int shift, int thickness){
        try {
            final_bmp = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            Canvas canvas = new Canvas(final_bmp);
            canvas.drawBitmap(src, new Matrix(), null);
            Bitmap btemp = null;

            //glitch
            Random r = new Random();
            for(int i=0; i<= seed; i++) {
                int x = r.nextInt(src.getWidth()/4 - 0) + 0;
                int y = r.nextInt((3*src.getHeight()/4) - (src.getHeight()/4)) + (src.getHeight()/4);
                int width = src.getWidth() - (x+100);
                int height = r.nextInt(thickness - (thickness*80/100)) + (thickness*80/100);
                btemp = Bitmap.createBitmap(src, x, y, width, height);
                canvas.drawBitmap(btemp, x + 100, y, null);
            }

            //
            Paint p = new Paint();
            btemp = Bitmap.createBitmap(src, shift, 0, src.getWidth()-shift, src.getHeight());
            Canvas filter = new Canvas(btemp);
            p.setAlpha(70);
            canvas.drawBitmap(btemp, 0, 0, p);

            //ghep anh scanline
            Bitmap effect_bmp = null;
            if(src.getHeight()/src.getWidth() == 16/9) {
                effect_bmp = getBitmapFromAssets("scanline16.png");
            }
            else if (src.getHeight()/src.getWidth() == 4/3) {
                effect_bmp = getBitmapFromAssets("scanline4.png");
            }
            p.setAlpha(20);
            canvas.drawBitmap(effect_bmp, null, new RectF(0, 0, src.getWidth(), src.getHeight()), p);

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
