package com.doocom.pagelistview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Mars on 2016/6/17.
 */
public class MeshImageView extends View {
  Bitmap bitmap;
  float[] origin;
  float[] dst;
  int width = 20;
  int height = 20;

  public MeshImageView(Context context) {
    this(context, null);
  }

  public MeshImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MeshImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public void setBitmap(Bitmap bitmap) {
    this.bitmap = bitmap;

    int count = (width + 1) * (height + 1);
    origin = new float[count * 2];
    dst = new float[count * 2];
    float bitmapWidth = bitmap.getWidth();
    float bitmapHeight = bitmap.getHeight();
    int index = 0;
    for (int y = 0; y <= height; y++) {
      float fy = bitmapHeight * y / height;
      for (int x = 0; x <= width; x++) {
        float fx = bitmapWidth * x / width;
        origin[index * 2 + 0] = dst[index * 2 + 0] = fx;
        origin[index * 2 + 1] = dst[index * 2 + 1] = fy;
        index += 1;
      }
    }
    invalidate();
  }


  public void setRotateDirection(){

  }

  ObjectAnimator animator;

  public void startAnimation(){
    animator = ObjectAnimator.ofFloat(this,"progress",0,1).setDuration(3000);
    animator.setRepeatMode(ValueAnimator.REVERSE);
    animator.setRepeatCount(3);
    animator.start();
  }

  public void setProgress(float progress){
    for (int i = 0 ; i < origin.length; i++){
      dst[i] = origin[i];
    }
    float bitmapWidth = bitmap.getWidth();
    float bitmapHeight = bitmap.getHeight();
    int index = 0;
    float maxOffset = bitmapWidth / 6;
    float mdx = bitmapWidth / 2;

    for (int y = 0; y <= height; y++) {
      float fy = bitmapHeight * y / height;
      float my = fy * (1 - progress);
      float offset = maxOffset * (fy / bitmapHeight) * progress;
      for (int x = 0; x <= width; x++) {
        float fx = bitmapWidth * x / width;
        float mx = fx;
        if (mx < mdx){
          mx -= offset;
        }
        if (mx > mdx){
          mx += offset;
        }
        dst[index * 2 + 0] = mx;
        dst[index * 2 + 1] = my;
        index += 1;
      }
    }
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (null != bitmap){
      canvas.save();
      int dx = (getWidth() - bitmap.getWidth()) / 2;
      int dy = (getHeight() - bitmap.getHeight()) / 2;
      canvas.translate(dx, dy);
      canvas.drawBitmapMesh(bitmap,width,height,dst,0,null,0,null);
      canvas.restore();
    }
  }
}
