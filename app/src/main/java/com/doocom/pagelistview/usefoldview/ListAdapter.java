package com.doocom.pagelistview.usefoldview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.doocom.pagelistview.R;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ItemHolder> {
  List<ListItem> data = new ArrayList<>();
  public static int type_img = 0;
  public static int type_text = 1;

  public ListAdapter(List<ListItem> data) {
    this.data = data;
  }

  @Override
  public int getItemViewType(int position) {
    ListItem listItem = data.get(position);
    if (listItem.img < 0) {
      return type_text;
    }
    return type_img;
  }

  @Override
  public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    if (viewType == type_img) {
      return new ImgItemHolder(inflater.inflate(R.layout.use_fold_img_item, parent, false));
    }else {
      return new TextItemHolder(inflater.inflate(R.layout.use_fold_text_item, parent, false));
    }
  }

  @Override
  public void onBindViewHolder(ItemHolder holder, int position) {
    holder.onBind(data.get(position));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }
}