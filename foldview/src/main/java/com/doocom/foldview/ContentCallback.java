package com.doocom.foldview;

import android.view.View;

/**
 * Created by Mars on 2016/6/18.
 */
public interface ContentCallback {
  View getPreView();
  View getNextView();
  void onFoldEnd(int action);
}
