package com.example.permanentlove.tongzxing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;
import java.util.Map;

import static com.example.permanentlove.tongzxing.DensityUtils.dp2px;

/**
 * Created by fengxitong on 2016/10/27.
 * Email address is m18824124885@163.com
 * 生成二维码工具类
 */

public class CodeUtils {
    //宽度值，影响中间图片大小
    private static final int IMAGE_WIDTH_VALUE=80;
    //压缩的背景大小
    private static final int BG_WIDTH_VALUE=300;
    //生成中间带图片的二维码
    public static Bitmap createCode(Context context,String content,Bitmap logo) throws WriterException {


        Matrix m=new Matrix();
        float sx=(float) 2*IMAGE_WIDTH_VALUE/logo.getWidth();
        float sy=(float) 2*IMAGE_WIDTH_VALUE/logo.getHeight();

        //设置缩放信息
        m.setScale(sx,sy);

        //将logo图片按martix设置的信息缩放
        logo=Bitmap.createBitmap(logo,0,0,logo.getWidth(),logo.getHeight(),m,false);

        Map<EncodeHintType,Object> hst=new Hashtable<>();
        //设置字符编码
        hst.put(EncodeHintType.CHARACTER_SET,"UTF-8");
        //设置二维码容错率
        hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        //生成二维码矩阵信息

        BitMatrix matrix=new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE, dp2px(context,600), dp2px(context,600),hst);

        //矩阵高度
        int width=matrix.getWidth();
        //矩阵宽度
        int height=matrix.getHeight();
        int halfW=width/2;
        int halfH=height/2;
        //定义数组长度为矩阵高度*矩阵宽度，用于记录矩阵中像素信息
        int[] pixels=new int[width*height];
        //从行开始迭代矩阵
        for(int y=0;y<height;y++){
            //迭代列
            for(int x=0;x<width;x++){
            if (x>halfW-IMAGE_WIDTH_VALUE&&x<halfW+IMAGE_WIDTH_VALUE&&y>halfH-IMAGE_WIDTH_VALUE&&y<halfH-IMAGE_WIDTH_VALUE){
                //记录图片每个像素信息
                pixels[y*width+x]=logo.getPixel(x-halfW+IMAGE_WIDTH_VALUE,y-halfH+IMAGE_WIDTH_VALUE);

            }else {
                //如果有黑块点，记录信息
                if(matrix.get(x,y)){
                    pixels[y*width+x]=0xff000000;
                }else {
                    pixels[y*width+x]=0xffffffff;
                }
            }
        }}
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels,0,width,0,0,width,height);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawBitmap(logo, (bitmap.getWidth() -logo.getWidth()) / 2, (bitmap.getHeight()-logo.getHeight()) / 2, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();


        return bitmap;
    }
    //生成普通二维码
    public static Bitmap createCode(Context context,String content) throws WriterException {
        //生成二维码矩阵，编码时指定大小，不要生成图片以后再进行缩放，这样会模糊导致识别失败
        BitMatrix matrix=new MultiFormatWriter().encode(content,BarcodeFormat.QR_CODE, dp2px(context,600), dp2px(context,600));
       int width=matrix.getWidth();
        int height=matrix.getHeight();
        //二维矩阵转为一维像素数组，也就是一直横着排
        int[] pixels=new int[width*height];
        for(int y=0;y<height;y++){
            for(int x=0;x<width;x++){
                if(matrix.get(x,y)){
                    pixels[y*width+x]=0xff000000;
                }else {
                    pixels[y*width+x]=0xffffffff;
                }
            }
        }
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        //通过像素数组生成bitmap
        bitmap.setPixels(pixels,0,width,0,0,width,height);
        return bitmap;
    }
    //生成有背景的二维码
    public static Bitmap createBackgroundCode(Context context,String content,Bitmap bg) throws WriterException {




        Map<EncodeHintType, Object> hst = new Hashtable<>();
        //设置字符编码
        hst.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        //设置二维码容错率
        hst.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        //生成二维码矩阵，编码时指定大小，不要生成图片以后再进行缩放，这样会模糊导致识别失败
        BitMatrix matrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, dp2px(context, 300), dp2px(context, 300), hst);
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        //二维矩阵转为一维像素数组，也就是一直横着排
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (matrix.get(x, y)) {
                    pixels[y * width + x] = 0xff000000;
                } else {
                    pixels[y * width + x] = 0x00ffffff;
                }
            }
        }
        Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        //通过像素数组生成bitmap
        bitmap.setPixels(pixels,0,width,0,0,width,height);

        //压缩图片
        Matrix m=new Matrix();
        float sx=(float) bitmap.getWidth()/bg.getWidth();
        float sy=(float) bitmap.getHeight()/bg.getHeight();

        //设置缩放信息
        m.setScale(sx,sy);

        //将logo图片按martix设置的信息缩放
        bg=Bitmap.createBitmap(bg,0,0,bg.getWidth(),bg.getHeight(),m,false);

        Bitmap nBitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(nBitmap);
        canvas.drawBitmap(bg,0,0, null);
        canvas.drawBitmap(bitmap,0,0, null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return nBitmap;
    }

}
