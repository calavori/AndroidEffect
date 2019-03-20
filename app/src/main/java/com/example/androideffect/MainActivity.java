package com.example.androideffect;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.net.Uri;


import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.ByteArrayOutputStream;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private Button gallerybutton;
    private Button camerabutton;
    private ImageView imageView;

    private static final int REQUEST_ID_IMAGE_CAPTURE = 1;
    private final static int GALLERY_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(MainActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };
        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }


    @Override
    protected void onStart() {
        super.onStart();
        this.gallerybutton = (Button) this.findViewById(R.id.gallerybutton);
        this.camerabutton = (Button) this.findViewById(R.id.camerabutton);
        this.imageView = (ImageView) this.findViewById(R.id.imageView3);

        this.camerabutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        this.gallerybutton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();
            }
        });
    }

    private void captureImage() {
        // Tạo một Intent không tường minh,
        // để yêu cầu hệ thống mở Camera chuẩn bị chụp hình.
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Start Activity chụp hình, và chờ đợi kết quả trả về.
        this.startActivityForResult(intent, REQUEST_ID_IMAGE_CAPTURE);
    }

    private void openImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ID_IMAGE_CAPTURE) {     //ket qua chup anh
            if (resultCode == RESULT_OK) {
                Bitmap bp = (Bitmap) data.getExtras().get("data");
                this.imageView.setImageBitmap(bp);


                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();


                Intent edit = new Intent(MainActivity.this, ImageEdit.class);
                edit.putExtra("byteArray", byteArray);
                MainActivity.this.startActivity(edit);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Action canceled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show();
            }
        } else if(requestCode == GALLERY_REQUEST){
            if (resultCode == RESULT_OK) {
                Uri selectedImage = data.getData();
                try {
                    Bitmap bp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    this.imageView.setImageBitmap(bp);
                } catch (Exception e) {
                    System.out.print(e);
                }
                Intent edit = new Intent(MainActivity.this, ImageEdit.class);
                edit.putExtra("string", selectedImage.toString());
                MainActivity.this.startActivity(edit);
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Action canceled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Action Failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
