package com.doocom.pagelistview.usefoldview;

import android.view.View;
import android.widget.TextView;

import com.doocom.pagelistview.R;

public class ImgItemHolder extends ItemHolder {
  public ImgItemHolder(View itemView) {
    super(itemView);
  }

  public void onBind(ListItem item){
    TextView contentTv = (TextView) itemView.findViewById(R.id.contentTv);
    contentTv.setText(item.content);
  }
}