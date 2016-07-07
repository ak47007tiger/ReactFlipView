package com.doocom.pagelistview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TestMeshImageActivity extends AppCompatActivity {
  MeshImageView meshImageView;
  private View.OnClickListener l = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      meshImageView.startAnimation();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test_mesh_image);
    meshImageView = (MeshImageView) findViewById(R.id.meshIv);
    findViewById(R.id.startBtn1).setOnClickListener(l);
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(getResources(),R.drawable.f,options);
    options.inJustDecodeBounds = false;
    options.inSampleSize = 2;
    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.f,options);
    meshImageView.setBitmap(bitmap);
  }

}
