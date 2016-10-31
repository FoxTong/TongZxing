package com.example.permanentlove.tongzxing;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.permanentlove.tongzxing.zxing.android.CaptureActivity;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.WriterException;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import static com.example.permanentlove.tongzxing.CodeUtils.createBackgroundCode;
import static com.example.permanentlove.tongzxing.CodeUtils.createCode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_SCAN = 0x0000;

    private static final int LOGO_RECORD=0x0001;
    private static final int BG_RECORD=0x0002;
    private static final int JUMP_RECORD=0x0003;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";

    private Button btn_scan, btn_generate, btn_save, btn_jump, btn_bg, btn_logo;
    private EditText et_link;
    private ImageView iv_logo;
    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initListener();
    }

    private void initListener() {
        btn_scan.setOnClickListener(this);
        btn_generate.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        btn_jump.setOnClickListener(this);
        btn_bg.setOnClickListener(this);
        btn_jump.setOnClickListener(this);
        btn_logo.setOnClickListener(this);

    }

    private void init() {
        btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_generate = (Button) findViewById(R.id.btn_generate);
        btn_save = (Button) findViewById(R.id.btn_save);
        et_link = (EditText) findViewById(R.id.et_link);
        iv_logo = (ImageView) findViewById(R.id.iv_logo);
        btn_jump = (Button) findViewById(R.id.btn_jump);
        btn_bg = (Button) findViewById(R.id.btn_bgk);
        btn_logo = (Button) findViewById(R.id.btn_logo);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                Scanning();
                break;
            case R.id.btn_generate:
                //判断editText是否为空
                if (TextUtils.isEmpty(et_link.getText())) {
                    Toast.makeText(this, "输入二维码要存储的信息", Toast.LENGTH_SHORT).show();
                } else {
                    try {

                        bitmap = createCode(this, et_link.getText().toString().trim());
                        iv_logo.setImageBitmap(bitmap);
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case R.id.btn_logo:

                getOpenimage(LOGO_RECORD);
                break;
            case R.id.btn_save:
                if (bitmap != null) {
                    saveBitmap(bitmap);
                } else {
                    Toast.makeText(this, "请输入要存储在二维码的信息", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btn_bgk:

                getOpenimage(BG_RECORD);
                break;
            case R.id.btn_jump:

                getOpenimage(JUMP_RECORD);

                break;

        }
    }

    //bitmap文件保存成jpg格式
    public void saveBitmap(Bitmap bmp) {

        File appDir = new File(Environment.getExternalStorageDirectory(), "TZxing");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "异常" + e, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "异常" + e, Toast.LENGTH_SHORT).show();
        }

        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(this.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File("/sdcard/TZxing/" + fileName))));
        //this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));


    }

    //扫描
    public void Scanning() {
        Intent intent = new Intent(MainActivity.this,
                CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK && requestCode != REQUEST_CODE_SCAN) {
            Uri uri = data.getData();
            ContentResolver cr = this.getContentResolver();

            Bitmap bitmap2 = null;
            try {
                bitmap2 = BitmapFactory.decodeStream(cr.openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (requestCode == JUMP_RECORD) {
                scanningImage(bitmap2);
                iv_logo.setImageBitmap(bitmap2);
            }
            if (requestCode == LOGO_RECORD) {
                generateLogo(bitmap2);
            }
            if (requestCode == BG_RECORD) {
                generateBackground(bitmap2);
            }
        }
       else if (resultCode==RESULT_OK&&requestCode == REQUEST_CODE_SCAN) {
            //获取扫描结果
            if (data != null) {
                Toast.makeText(this, "测试", Toast.LENGTH_SHORT).show();
                //解码
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);

                et_link.setText("解码结果： \n" + content);
                iv_logo.setImageBitmap(bitmap);

            } else {
                Toast.makeText(this, "扫描失败", Toast.LENGTH_SHORT).show();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 扫描二维码图片的方法
     */
    public void scanningImage(Bitmap b) {


        Hashtable<DecodeHintType, String> hints = new Hashtable<>();

        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); //设置二维码内容的编码

        int[] pixels = new int[b.getWidth() * b.getHeight()];
        b.getPixels(pixels, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(b.getWidth(), b.getHeight(), pixels);

        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();


        try {
            et_link.setText(reader.decode(bitmap1, hints).toString());

        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }

    }

    /**
     * 打开系统图库
     */
    public void getOpenimage(int code) {
        Intent innerIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        innerIntent.setType("image/*");
        PackageManager manager = getPackageManager();
        List<ResolveInfo> apps = manager.queryIntentActivities(innerIntent, 0);
        if (apps.size() > 0) {
            startActivityForResult(innerIntent,code);
        }

    }

    /**
     * 生成带logo二维码
     */
    public void generateLogo(Bitmap b){


        if (TextUtils.isEmpty(et_link.getText())) {
            Toast.makeText(this, "输入二维码要存储的信息", Toast.LENGTH_SHORT).show();
        } else {
            try {
                bitmap = createCode(this, et_link.getText().toString().trim(),b);
                iv_logo.setImageBitmap(bitmap);

            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成带背景的二维码
     */
    public void generateBackground(Bitmap b){

        if (TextUtils.isEmpty(et_link.getText())) {
            Toast.makeText(this, "输入二维码要存储的信息", Toast.LENGTH_SHORT).show();
        } else {
            try {
                bitmap = createBackgroundCode(this, et_link.getText().toString().trim(),b);
                iv_logo.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }
}
