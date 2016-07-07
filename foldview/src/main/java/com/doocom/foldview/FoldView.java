package com.doocom.foldview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by Mars on 2016/6/18.
 */
public class FoldView extends FrameLayout {
  Paint testPaint = new Paint();

  public static final int ACTION_NEXT = 0;
  public static final int ACTION_PRE = 1;
  public static final int ACTION_CUR = 3;

  private int action = ACTION_CUR;
  private float lastDownY;
  private ContentCallback contentCallback;
  private Bitmap preBitmap;
  private Bitmap curBitmap;
  private Bitmap nextBitmap;
  private int scaledTouchSlop;
  private float lastMoveY;
  private float distance;
  private int maxDistance;
  private float progress;
  private int meshWidth = 20;
  private int meshHeight = 20;
  private float[] origin;
  private float[] preVer;
  private float[] curVer;
  private float[] nextVer;
  boolean dragging = false;
  private boolean runningAnimation = false;
  private View foldContentV;
  private View refreshV;
  private View loadV;
  private Paint coverPaint = new Paint();

  private Rect srcTop = new Rect();
  private Rect srcBottom = new Rect();
  private static final TimeInterpolator interpolator = new DecelerateInterpolator();
  private ObjectAnimator foldAnimator;

  public FoldView(Context context) {
    super(context);
    init(context);
  }

  public FoldView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public FoldView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    maxDistance = Utils.dip2px(context, 100);
    testPaint.setColor(Color.GREEN);
    coverPaint.setColor(Color.BLACK);

    LayoutInflater inflater = LayoutInflater.from(context);
    refreshV = inflater.inflate(R.layout.use_fold_refresh,this,false);
    loadV = inflater.inflate(R.layout.use_fold_load,this,false);
    addView(refreshV);
    addView(loadV);
  }

  public void setRefreshView(View view){
    refreshV = view;
    if (refreshV != null){
      removeView(refreshV);
    }
    addView(refreshV);
  }

  public void setLoadView(View view){
    if (loadV != null){
      removeView(loadV);
    }
    loadV = view;
    addView(loadV);
  }

  private boolean hasPrePage(){
    return preVer != null;
  }

  private boolean hasNextPage(){
    return nextVer != null;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (runningAnimation){
      return true;
    }
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        break;
      case MotionEvent.ACTION_MOVE:
        onDrag(ev);
        break;
      case MotionEvent.ACTION_UP:
        onActionEnd();
        break;
    }
    if (dragging) {
      return true;
    }
    return super.onTouchEvent(ev);
  }

  private void onDrag(MotionEvent e) {
    lastMoveY = e.getY();
    //正在显示前一页
    if (action == ACTION_PRE) {
      //变成显示后一页的操作了
      if (lastMoveY < lastDownY) {
        lastDownY = lastMoveY;
      }
    }
    if (action == ACTION_NEXT) {
      if (lastMoveY > lastDownY) {
        lastDownY = lastMoveY;
      }
    }
    distance = Math.abs(lastMoveY - lastDownY);
    if (action == ACTION_PRE && !hasPrePage() ||
      action == ACTION_NEXT && !hasNextPage()){
      distance *= 0.1f;
    }else {
      distance *= 0.5;
    }
    progress = distance / maxDistance;

    if (progress > 1) {
      if (action == ACTION_NEXT) {
        //补偿
        lastDownY -= (distance - maxDistance);
      }
      if (action == ACTION_PRE) {
        lastDownY += (distance - maxDistance);
      }
      progress = 1;
    }

    setProgress(progress);
  }

  public void setProgress(float progress){
    Log.d("progress", "progress=======" + progress);
    this.progress = progress;
    if (action == ACTION_NEXT) {
      dragToShowNext(progress);
    }
    if (action == ACTION_PRE) {
      dragToShowPre(progress);
    }
    invalidate();
  }

  /*一旦开始拖拽就要显示当前的了，要在重绘制前准备好*/
  private void buildCurBitmap(){
    srcTop.set(0,0,getWidth(),getHeight() / 2);
    srcBottom.set(0,srcTop.bottom,getWidth(),getHeight());
    curBitmap = Utils.view2Bitmap(this,this.getWidth(),this.getHeight());
  }

  private void buildOtherBitmap() {
    int count = (meshWidth + 1) * (meshHeight + 1);
    origin = new float[count * 2];
    curVer = new float[count * 2];
    float[] dst = new float[count * 2];
    float bitmapWidth = curBitmap.getWidth();
    float bitmapHeight = curBitmap.getHeight();
    int index = 0;
    for (int y = 0; y <= meshHeight; y++) {
      float fy = bitmapHeight * y / meshHeight;
      for (int x = 0; x <= meshWidth; x++) {
        float fx = bitmapWidth * x / meshWidth;
        int index1 = index * 2 + 0;
        origin[index1] = curVer[index1] = dst[index1] = fx;
        int index2 = index * 2 + 1;
        origin[index2] = curVer[index2] = dst[index2] = fy;
        index += 1;
      }
    }
    if (action == ACTION_PRE) {
      View preView = contentCallback.getPreView();
      if (null != preView){
        preBitmap = Utils.view2Bitmap(preView,this.getWidth(),this.getHeight());
        preVer = dst;
      }else {
        refreshV.setVisibility(VISIBLE);
        loadV.setVisibility(INVISIBLE);
        preVer = null;
      }
    }
    if (action == ACTION_NEXT) {
      View nextView = contentCallback.getNextView();
      if (null != nextView){
        nextBitmap = Utils.view2Bitmap(nextView,this.getWidth(),this.getHeight());
        nextVer = dst;
      }else {
        refreshV.setVisibility(INVISIBLE);
        loadV.setVisibility(VISIBLE);
        nextVer = null;
      }
    }
    invalidate();
  }

  private void dragToShowNext(float progress) {
    for (int i = 0; i < origin.length; i++){
      float originVal = origin[i];
      curVer[i] = originVal;
      if (nextVer != null){
        nextVer[i] = originVal;
      }
    }
    float bitmapWidth = curBitmap.getWidth();
    float bitmapHeight = curBitmap.getHeight();
    int index = 0;
    float maxOffset = bitmapWidth / 6;
    float mdx = bitmapWidth / 2;
    float mdy = bitmapHeight / 2;
    for (int y = 0; y <= meshHeight; y++) {
      float fy = bitmapHeight * y / meshHeight;
      float curMeshY = 0;
      float curOffsetX = 0;
      float nextOffsetX = 0;
      float nextMeshY = 0;

      if (progress < 0.5){
        //当前页
        if (fy < mdy){
          //上半部分
          curMeshY = fy;
          curOffsetX = 0;
        }else {
          //下半部分
          //向y负方向偏移，偏移最大值mdy
          //progress越大偏移越大
          //fy越小偏移越小
          float curOffsetY = mdy * (progress * 2) * ((fy - mdy) / mdy);
          curMeshY = fy - curOffsetY;
          //progress越大，偏移越大
          //fy越大偏移越大
          curOffsetX = maxOffset * progress * 2 * ((fy - mdy) / mdy);
        }
      }else {
        //下一页
        if (fy < mdy){
          //上半部分
          //开始显示next了，要处理
          //向y正方向偏移，偏移最大值mdy
          //progress越大，偏移小
          //fy越小偏移越大
          float nextOffsetY = mdy * (1 - (progress - 0.5f) * 2) * (1 - fy / mdy);
          nextMeshY = fy + nextOffsetY;
          //fy越接近0，偏移越大，progress越大，偏移越小
          nextOffsetX = maxOffset * (1 - fy / mdy) * (1 - (progress - 0.5f) * 2);
        }else {
          //下半部分
          nextMeshY = fy;
          nextOffsetX = 0;
        }
      }
      for (int x = 0; x <= meshWidth; x++) {
        float fx = bitmapWidth * x / meshWidth;
        float curMeshX = fx;
        float offsetRate = Math.abs(fx - mdx) / mdx;
        float curPointOffsetX = curOffsetX * offsetRate;
        if (curMeshX < mdx){
          curMeshX -= curPointOffsetX;
        }
        if (curMeshX > mdx){
          curMeshX += curPointOffsetX;
        }
        curVer[index * 2 + 0] = curMeshX;
        curVer[index * 2 + 1] = curMeshY;

        if (nextVer != null){
          float nextMeshX = fx;
          float nextPointOffsetX = nextOffsetX * offsetRate;
          if (nextMeshX < mdx){
            //越往中间偏移越小
            nextMeshX -= nextPointOffsetX;
          }
          if (nextMeshX > mdx){
            nextMeshX += nextPointOffsetX;
          }
          nextVer[index * 2 + 0] = nextMeshX;
          nextVer[index * 2 + 1] = nextMeshY;
        }
        index += 1;
      }
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    foldContentV = findViewById(R.id.fold_content);
  }

  private void dragToShowPre(float progress) {
    for (int i = 0; i < origin.length; i++){
      float originVal = origin[i];
      curVer[i] = originVal;
      if (null != preVer){
        preVer[i] = originVal;
      }
    }
    float bitmapWidth = curBitmap.getWidth();
    float bitmapHeight = curBitmap.getHeight();
    int index = 0;
    float maxOffset = bitmapWidth / 6;
    float mdx = bitmapWidth / 2;
    float mdy = bitmapHeight / 2;
    for (int y = 0; y <= meshHeight; y++) {
      float fy = bitmapHeight * y / meshHeight;
      float curMeshY = 0;
      float curOffsetX = 0;
      float preOffsetX = 0;
      float preMeshY = 0;
      if (progress < 0.5){
        //显示当前页
        if (fy < mdy){
          //上半部分
          //向y正方向偏移，最大值mdy
          //progress越大，偏移越大
          //fy越大，偏移越小
          float curOffsetY = mdy * (2 * progress) * (1 - fy / mdy);
          curMeshY = fy + curOffsetY;
          //progress越大，偏移越大
          //fy越大，偏移越小
          curOffsetX = maxOffset * (2 * progress) * (1 - fy / mdy);
        }else {
          //下半部分
          curMeshY = fy;
          curOffsetX = 0;
        }
      }else {
        //显示前一页
        if (fy < mdy){
          //上半部分
          preMeshY = fy;
          preOffsetX = 0;
        }else {
          //下半部分
          //向y负方向偏移，偏移最大值mdy
          //progress越大，偏移越小
          //fy越大，偏移越大
          float preOffsetY = mdy * (1 - (progress - 0.5f) * 2) * ((fy - mdy) / mdy);
          preMeshY = fy - preOffsetY;
          //progress越大，偏移越小
          //fy越大，偏移越大
          preOffsetX = maxOffset * (1 - (progress - 0.5f) * 2) * ((fy - mdy) / mdy);
        }
      }
      for (int x = 0; x <= meshWidth; x++) {
        float fx = bitmapWidth * x / meshWidth;
        float curMeshX = fx;
        float offsetRate = Math.abs(fx - mdx) / mdx;
        float curPointOffsetX = curOffsetX * offsetRate;
        if (curMeshX < mdx){
          curMeshX -= curPointOffsetX;
        }
        if (curMeshX > mdx){
          curMeshX += curPointOffsetX;
        }
        curVer[index * 2 + 0] = curMeshX;
        curVer[index * 2 + 1] = curMeshY;

        if (null != preVer){
          float preMeshX = fx;
          float nextPointOffsetX = preOffsetX * offsetRate;
          if (preMeshX < mdx){
            //越往中间偏移越小
            preMeshX -= nextPointOffsetX;
          }
          if (preMeshX > mdx){
            preMeshX += nextPointOffsetX;
          }
          preVer[index * 2 + 0] = preMeshX;
          preVer[index * 2 + 1] = preMeshY;
        }
        index += 1;
      }
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    endAnimators();
    super.onDetachedFromWindow();
  }

  @Override
  protected void onAttachedToWindow() {
    endAnimators();
    super.onAttachedToWindow();
  }

  private void endAnimators(){
    if (foldAnimator != null && foldAnimator.isRunning()){
      foldAnimator.end();
    }
  }

  private void onActionEnd() {
    if (0 < progress && progress < 1){
      if ((action == ACTION_PRE && hasPrePage()) ||
        (action == ACTION_NEXT && hasNextPage())){
        foldAnimator = ObjectAnimator.ofFloat(this,"progress",progress,1);
      }else {
        foldAnimator = ObjectAnimator.ofFloat(this,"progress",progress,0);
      }
      foldAnimator.setInterpolator(interpolator);
      foldAnimator.addListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          resetViewState();
          runningAnimation = false;
        }
      });
      runningAnimation = true;
      foldAnimator.start();
    }else {
      resetViewState();
    }
  }

  private void resetViewState(){
    dragging = false;
    action = ACTION_CUR;
    progress = 0;
    foldContentV.setVisibility(VISIBLE);
    if (preBitmap != null) {
      preBitmap.recycle();
      preBitmap = null;
    }
    if (nextBitmap != null) {
      nextBitmap.recycle();
      nextBitmap = null;
    }
    if (null != curBitmap) {
      curBitmap.recycle();
      curBitmap = null;
    }
    invalidate();
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (!dragging){
      return;
    }
    coverPaint.setAlpha((int) (1 - 255 * progress * 2));
    canvas.drawRect(0,0,getWidth(),getHeight(),coverPaint);
    if (action == ACTION_NEXT){
      if (progress > 0){
        canvas.drawBitmap(curBitmap,srcTop,srcTop,null);
        if (null != nextVer){
          canvas.drawBitmap(nextBitmap,srcBottom,srcBottom,null);
        }
      }
      if (progress > 0.5){
        if (null != nextVer){
          canvas.drawBitmapMesh(nextBitmap,meshWidth,meshHeight,nextVer,0,null,0,null);
        }
      }
    }
    if (action == ACTION_PRE){
      if (progress > 0){
        canvas.drawBitmap(curBitmap,srcBottom,srcBottom,null);
        if (null != preVer){
          canvas.drawBitmap(preBitmap,srcTop,srcTop,null);
        }
      }
      if (progress > 0.5){
        if (null != preVer){
          canvas.drawBitmapMesh(preBitmap,meshWidth,meshHeight,preVer,0,null,0,null);
        }
      }
    }
    if (progress < 0.5f){
      canvas.drawBitmapMesh(curBitmap, meshWidth, meshHeight, curVer, 0, null, 0, null);
    }
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {

    if (runningAnimation || dragging) {
      return true;
    }
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        lastDownY = ev.getY();
        break;
      case MotionEvent.ACTION_MOVE:
        lastMoveY = ev.getY();
        distance = Math.abs(lastMoveY - lastDownY);
        if (distance > scaledTouchSlop) {
          if (action == ACTION_CUR){
            if (lastMoveY < lastDownY) {
              //drag up
              //can only show nextBitmap
              action = ACTION_NEXT;
            }

            if (lastMoveY > lastDownY) {
              //drag down
              //can only show preBitmap
              action = ACTION_PRE;
            }
          }
          buildCurBitmap();
          dragging = true;
          buildOtherBitmap();
          foldContentV.setVisibility(INVISIBLE);
          lastDownY = lastMoveY;
          return true;
        }
        break;
      case MotionEvent.ACTION_UP:
        break;
    }
    return super.onInterceptTouchEvent(ev);
  }

  public void setContentCallback(ContentCallback contentCallback) {
    this.contentCallback = contentCallback;
  }
}
