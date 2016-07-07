package com.doocom.pagelistview.useflipview;

import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.doocom.foldview.FlipLayout;
import com.doocom.foldview.ReactFlipView;
import com.doocom.pagelistview.R;
import com.doocom.pagelistview.usefoldview.ListAdapter;
import com.doocom.pagelistview.usefoldview.ListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class UseReactFlipViewActivity extends AppCompatActivity {

  private RecyclerView recyclerView;
  private ListAdapter adapter;
  private ReactFlipView flipLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_use_react_flip_view);
    flipLayout = (ReactFlipView) findViewById(R.id.flipL);
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
    flipLayout.setCallback(callback);

  }

  ReactFlipView.Callback callback = new ReactFlipView.Callback() {
    int curPage = 0;
    int prePage = 0;
    boolean hasPre;
    boolean hasNext;

    @Override
    public void onProgressChange(float progress) {
    }

    @Override
    public void onTouchUp(float touchUpProgress, int action) {

    }

    @Override
    public void onAction(int action) {
      switch (action) {
        case FlipLayout.ACTION_CUR:
          recyclerView.scrollBy(0, recyclerView.getHeight() * (prePage - curPage));
          curPage = prePage;
          break;
        case FlipLayout.ACTION_NEXT:
          if (hasNext) {
            prePage = curPage;
            curPage += 1;
            recyclerView.scrollBy(0, recyclerView.getHeight());
          } else {
            prePage = curPage;
          }
//          flipLayout.updateOtherBitmap(hasPre,hasNext);
          handler.sendEmptyMessageDelayed(0, 500);
          break;
        case FlipLayout.ACTION_PRE:
          if (hasPre) {
            prePage = curPage;
            curPage -= 1;
            recyclerView.scrollBy(0, -recyclerView.getHeight());
          } else {
            prePage = curPage;
          }
          handler.sendEmptyMessageDelayed(0, 500);
//          flipLayout.updateOtherBitmap(hasPre,hasNext);
          break;
      }
    }

    android.os.Handler handler = new android.os.Handler() {
      @Override
      public void handleMessage(Message msg) {
        flipLayout.updateOtherBitmap(hasPre, hasNext);
      }
    };

    boolean hasPre() {
      return curPage > 0;
    }

    boolean hasNext() {
      return curPage < adapter.getItemCount() - 1;
    }

    @Override
    public void onFlipStart() {
      hasPre = hasPre();
      hasNext = hasNext();
    }

    @Override
    public void onFlipEnd() {

    }
  };

}
