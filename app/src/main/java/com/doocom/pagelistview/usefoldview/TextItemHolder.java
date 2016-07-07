package com.doocom.pagelistview.usefoldview;

import android.view.View;
import android.widget.TextView;

import com.doocom.pagelistview.R;

/**
 * Created by Mars on 2016/6/18.
 */
public class TextItemHolder extends ItemHolder {
  public TextItemHolder(View itemView) {
    super(itemView);
  }

  public void onBind(ListItem item){
    TextView contentTv = (TextView) itemView.findViewById(R.id.contentTv);
    contentTv.setText(item.content);
  }
}
