package com.doocom.pagelistview.usefoldview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.doocom.foldview.ContentCallback;
import com.doocom.foldview.FoldView;
import com.doocom.pagelistview.R;

import java.util.ArrayList;
import java.util.List;

public class UseFoldViewActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private ListAdapter adapter;
  private FoldView foldView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_use_fold_view);
    recyclerView = (RecyclerView) findViewById(R.id.fold_content);
    List<ListItem> data = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      data.add(new ListItem(R.drawable.f, "this is content" + i));
    }
    data.get(1).img = -1;
    data.get(4).img = -1;

    adapter = new ListAdapter(data);
    recyclerView.setAdapter(adapter);
    LinearLayoutManager layout = new LinearLayoutManager(
      getApplicationContext(), LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layout);
    foldView = (FoldView) findViewById(R.id.foldV);
    foldView.setContentCallback(mulCallback);
    recyclerView.addOnScrollListener(mulCallback);
  }

  class MulCallback extends RecyclerView.OnScrollListener implements ContentCallback {
    int preScrollY = 0;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      preScrollY += dy;
    }

    @Override
    public View getPreView() {
      if (preScrollY == 0) {
        return null;
      } else {
        recyclerView.scrollBy(0, -recyclerView.getHeight());
        return recyclerView;
      }
    }

    @Override
    public View getNextView() {
      int max = (recyclerView.getAdapter().getItemCount() - 1) * recyclerView.getHeight();
      if (preScrollY == max) {
        return null;
      } else {
        recyclerView.scrollBy(0, recyclerView.getHeight());
        return recyclerView;
      }
    }

    @Override
    public void onFoldEnd(int action) {
      if (action == FoldView.ACTION_CUR){
        recyclerView.scrollTo(0,preScrollY);
      }
    }
  }

  MulCallback mulCallback = new MulCallback();

}
