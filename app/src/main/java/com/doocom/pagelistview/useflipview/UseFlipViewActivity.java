package com.doocom.pagelistview.useflipview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.doocom.foldview.FlipLayout;
import com.doocom.pagelistview.R;
import com.doocom.pagelistview.usefoldview.ListAdapter;
import com.doocom.pagelistview.usefoldview.ListItem;

import java.util.ArrayList;
import java.util.List;

public class UseFlipViewActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  int preScrollY = 0;
  int curScrollY = 0;
  private ListAdapter adapter;
  private FlipLayout flipLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_use_flip_view);
    flipLayout = (FlipLayout) findViewById(R.id.flipL);
    recyclerView = (RecyclerView) findViewById(R.id.fold_content);
    List<ListItem> data = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      data.add(new ListItem(R.drawable.f, "flip content" + i));
    }
    data.get(1).img = -1;
    data.get(4).img = -1;

    adapter = new ListAdapter(data);
    recyclerView.setAdapter(adapter);
    LinearLayoutManager layout = new LinearLayoutManager(
      getApplicationContext(), LinearLayoutManager.VERTICAL, false);
    recyclerView.setLayoutManager(layout);
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        preScrollY = curScrollY;
        curScrollY += dy;
      }
    });
    flipLayout.setCallback(callback);

  }

  FlipLayout.Callback callback = new FlipLayout.Callback() {
    public boolean hasPre() {
      return curScrollY > 0;
    }

    public boolean hasNext() {
      return curScrollY < (recyclerView.getAdapter().getItemCount() - 1) * recyclerView.getHeight();
    }

    @Override
    public void onProgressChange(float progress) {
    }

    @Override
    public void onAction(int action) {
      switch (action){
        case FlipLayout.ACTION_CUR:
          if (curScrollY != (recyclerView.getAdapter().getItemCount() - 1) * recyclerView.getHeight()
            && curScrollY != 0){
            recyclerView.scrollBy(0,preScrollY - curScrollY);
          }
          break;
        case FlipLayout.ACTION_NEXT:
          recyclerView.scrollBy(0,recyclerView.getHeight());
          break;
        case FlipLayout.ACTION_PRE:
          recyclerView.scrollBy(0,-recyclerView.getHeight());
          break;
      }
    }

    @Override
    public void onStartFlip() {
      flipLayout.setHasPre(hasPre());
      flipLayout.setHasNext(hasNext());
    }
  };
}
