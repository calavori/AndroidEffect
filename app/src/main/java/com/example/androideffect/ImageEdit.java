package com.example.androideffect;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.androideffect.filter.AndroidUtils;
import com.example.androideffect.filter.RippleFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Random;

import static android.content.ContentValues.TAG;


public class ImageEdit extends Activity {

    private Button snowbutton;
    private Button mirrorbutton;
    private Button glitchbutton;
    private Button waterbutton;
    private Button resetbutton;
    private Button savebutton;
    private ImageView imageView;
    private Bitmap src_bmp;
    private Bitmap final_bmp;
    private GlitchInfo glitchInfo;
    private int snowLevel;

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
        this.imageView = this.findViewById(R.id.imageview);
        imageView.setImageBitmap(src_bmp);
        final_bmp = src_bmp;


        this.snowbutton = (Button) this.findViewById(R.id.snowbutton);
        this.mirrorbutton = (Button) this.findViewById(R.id.mirrorbutton);
        this.glitchbutton = (Button) this.findViewById(R.id.glitchbutton);
        this.waterbutton = (Button) this.findViewById(R.id.waterbutton);
        this.resetbutton = (Button) this.findViewById(R.id.resetbutton);
        this.savebutton = (Button) this.findViewById(R.id.savebutton);

        //snow
        this.snowbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(supported(final_bmp)==true) {
                    setSnowInfo();

                }
                else {
                    Toast.makeText(ImageEdit.this, "Photo format is not supported, please try again with 16:9 or 4:3 ratio", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //mirror
        this.mirrorbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(supported(final_bmp)==true) {
                    final_bmp = mirror(final_bmp);
                    imageView.setImageBitmap(final_bmp);
                }
                else {
                    Toast.makeText(ImageEdit.this, "Photo format is not supported, please try again with 16:9 or 4:3 ratio", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //glitch
        this.glitchbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(supported(final_bmp)==true) {
                    setGitchInfo();
                }
                else {
                    Toast.makeText(ImageEdit.this, "Photo format is not supported, please try again with 16:9 or 4:3 ratio", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //water reflect
        this.waterbutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                final_bmp = waterReflection(final_bmp);
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

    private void setSnowInfo(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.set_snow_dialog);
        dialog.setCancelable(true);
        TextView level = (TextView) dialog.findViewById(R.id.level);
        SeekBar level_bar = (SeekBar) dialog.findViewById(R.id.level_bar);
        Button ok_button = (Button) dialog.findViewById(R.id.ok_button);

        level_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                Toast.makeText(ImageEdit.this, "Level: " + String.valueOf(progress), Toast.LENGTH_SHORT).show();
                snowLevel = progress;
            }
        });
        //ok button
        ok_button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(snowLevel != 0) {
                    final_bmp = snow(final_bmp, snowLevel);
                    imageView.setImageBitmap(final_bmp);
                    snowLevel = 0;
                }
            }
        });
        dialog.show();
    }

    private Bitmap snow(Bitmap src, int lv){
        Bitmap effect_bmp = null;
        if(src.getHeight()/src.getWidth() == 16/9) {
            effect_bmp = getBitmapFromAssets("snow16-" + lv +".png");
        }
        else if (src.getHeight()/src.getWidth() == 4/3) {
            effect_bmp = getBitmapFromAssets("snow4-" + lv +".png");
        }
        //xu li anh
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        Bitmap dst =  Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, new Matrix(), null);
        canvas.drawBitmap(effect_bmp, null, new RectF(0, 0, src.getWidth(), src.getHeight()), paint);
        //
        return dst;

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
        Bitmap dst =  Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(dst);
        canvas.drawBitmap(src, new Matrix(), null);
        canvas.drawBitmap(effect_bmp, null, new RectF(0, 0, src.getWidth(), src.getHeight()), paint);
        //
        return dst;
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
                final_bmp = glitch(final_bmp, glitchInfo.getSeed(), glitchInfo.getRbg_shift(), glitchInfo.getThickness());
                imageView.setImageBitmap(final_bmp);
                glitchInfo = new GlitchInfo();
            }
        });
        dialog.show();
    }

    private Bitmap glitch (Bitmap src, int seed, int shift, int thickness){
        Bitmap dst = null;
        try {
            dst = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
            Canvas canvas = new Canvas(dst);
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
            p.setAlpha(60);
            canvas.drawBitmap(btemp, 0, 0, p);

            //ghep anh scanline
            Bitmap effect_bmp = null;
            if(src.getHeight()/src.getWidth() == 16/9) {
                effect_bmp = getBitmapFromAssets("scanline16.png");
            }
            else if (src.getHeight()/src.getWidth() == 4/3) {
                effect_bmp = getBitmapFromAssets("scanline4.png");
            }
            p.setAlpha(15);
            canvas.drawBitmap(effect_bmp, null, new RectF(0, 0, src.getWidth(), src.getHeight()), p);

        } catch (Exception e){}
        return dst;
    }


    //mat nuoc
    public Bitmap waterReflection(Bitmap src) {
        src = resize(src, src.getWidth()/2, src.getHeight()/2);
        int width = src.getWidth();
        int height = src.getHeight();

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);

        //tao anh phan chieu
        Bitmap reflectionImage = Bitmap.createBitmap(src, 0, 0, width, height , matrix, false);
        reflectionImage=Bitmap.createScaledBitmap(reflectionImage, width, (3*height/4), true);

        //lam mo anh phan chieu
        reflectionImage = blur(reflectionImage);

        //ghep song vao anh phan chieu
        int[] src_temp = AndroidUtils.bitmapToIntArray(reflectionImage);
        RippleFilter filter = new RippleFilter();
        filter.setWaveType(0);
        src_temp = filter.filter(src_temp, reflectionImage.getWidth(), reflectionImage.getHeight());
        reflectionImage = Bitmap.createBitmap(src_temp, reflectionImage.getWidth(), reflectionImage.getHeight(), Bitmap.Config.ARGB_8888);


        //ghepp anh phan chieu
        Bitmap reflectedBitmap = Bitmap.createBitmap(width, (height + (3 * height / 4)), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(reflectedBitmap);
        canvas.drawBitmap(src, 0, 0, null);
        Paint defaultPaint = new Paint();
        canvas.drawRect(0, height, width, height, defaultPaint);

        canvas.drawBitmap(reflectionImage, 0, height, null);
        Paint paint = new Paint();

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, height, width, reflectedBitmap.getHeight()
                    , paint);
        return reflectedBitmap;

    }

    //lam mo anh
    public Bitmap blur(Bitmap image) {
        if (null == image) return null;
        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);
        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(15f);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > ratioBitmap) {
                finalWidth = (int) ((float) maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float) maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    private boolean supported(Bitmap src){
        int ratio = src.getHeight()/src.getWidth();
        if (ratio == 16/9 || ratio == 4/3){
            return true;
        }
        else return false;
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
