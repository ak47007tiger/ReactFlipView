package com.doocom.foldview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

/**
 * Created by Mars on 2016/6/18.
 */
public class Utils {
  public static Bitmap view2Bitmap(View view,int width, int height){
    Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    view.draw(canvas);
    return bitmap;
  }
  public static int dip2px(Context context, float dipValue) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dipValue * scale + 0.5f);
  }
}
