package com.doocom.pagelistview.usefoldview;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Mars on 2016/6/18.
 */
public abstract class ItemHolder extends RecyclerView.ViewHolder{
  public ItemHolder(View itemView) {
    super(itemView);
  }

  public abstract void onBind(ListItem item);
}
