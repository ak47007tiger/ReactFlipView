package com.doocom.foldview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * Created by Mars on 2016/6/21.
 */
public class ReactFlipView extends FrameLayout {
  Paint testPaint = new Paint();

  public static final int ACTION_NEXT = 0;
  public static final int ACTION_PRE = 1;
  public static final int ACTION_CUR = 3;

  private float upProgress;
  private int action = ACTION_CUR;
  private int animationAction;
  private float lastDownY;
  private float firstDownY;
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
  private Paint coverPaint = new Paint();

  private Rect srcTop = new Rect();
  private Rect srcBottom = new Rect();
  private static final TimeInterpolator interpolator = new DecelerateInterpolator();
  private ObjectAnimator foldAnimator;
  private boolean hasPre = false;
  private boolean hasNext = false;
  private float[] dst;
  private boolean buildAllBitmap = false;
  private boolean pressed = false;
  private boolean flipping = false;
  private boolean showVirtualContent;
  private Callback callback;
  private boolean hasFutureFlipTask = false;
  Animator.AnimatorListener flipAnimatorListener = new AnimatorListenerAdapter() {
    @Override
    public void onAnimationEnd(Animator animation) {
      runningAnimation = false;
      onFlipEnd();
    }
  };

  public ReactFlipView(Context context) {
    super(context);
    init(context);
  }

  public ReactFlipView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public ReactFlipView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  private void init(Context context) {
    scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    maxDistance = Utils.dip2px(context, 100);
    testPaint.setColor(Color.GREEN);
    coverPaint.setColor(Color.BLACK);
  }

  public void setProgress(float progress){
    callback.onProgressChange(progress);
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
    curBitmap = Utils.view2Bitmap(getChildAt(getChildCount() - 1),this.getWidth(),this.getHeight());
  }

  private static boolean needCreate(float[] meshArray, int count){
    return !(meshArray != null && meshArray.length == count);
  }

  private void initMesh(){
    int count = (meshWidth + 1) * (meshHeight + 1);
    if (needCreate(origin,count)){
      origin = new float[count * 2];
    }
    if (needCreate(curVer,count)){
      curVer = new float[count * 2];
    }
    if (needCreate(dst,count)){
      dst = new float[count * 2];
    }
    float bitmapWidth = getWidth();
    float bitmapHeight = getHeight();
    int index = 0;
    for (int y = 0; y <= meshHeight; y++) {
      float fy = bitmapHeight * y / meshHeight;
      for (int x = 0; x <= meshWidth; x++) {
        float fx = bitmapWidth * x / meshWidth;
        int index1 = index * 2 + 0;
        origin[index1] = fx;
        int index2 = index * 2 + 1;
        origin[index2] = fy;
        index += 1;
      }
    }
  }

  private void buildOtherBitmap() {
    if (action == ACTION_PRE) {
      if (hasPre){
        preBitmap = Utils.view2Bitmap(getChildAt(getChildCount() - 1),this.getWidth(),this.getHeight());
        preVer = dst;
      }else {
        preVer = null;
      }
    }
    if (action == ACTION_NEXT) {
      if (hasNext){
        nextBitmap = Utils.view2Bitmap(getChildAt(getChildCount() - 1),this.getWidth(),this.getHeight());
        nextVer = dst;
      }else {
        nextVer = null;
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback{
    void onAction(int action);
    void onFlipStart();
    void onFlipEnd();
    void onProgressChange(float progress);
    void onTouchUp(float touchUpProgress,int action);
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
        float offsetRate = Math.abs(fx - mdx) / mdx;
        if (progress < 0.5){
          float curMeshX = fx;
          float curPointOffsetX = curOffsetX * offsetRate;
          if (curMeshX < mdx){
            curMeshX -= curPointOffsetX;
          }
          if (curMeshX > mdx){
            curMeshX += curPointOffsetX;
          }
          curVer[index * 2 + 0] = curMeshX;
          curVer[index * 2 + 1] = curMeshY;
        }

        if (nextVer != null && progress > 0.5){
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
        float offsetRate = Math.abs(fx - mdx) / mdx;
        if (progress < 0.5){
          float curMeshX = fx;
          float curPointOffsetX = curOffsetX * offsetRate;
          if (curMeshX < mdx){
            curMeshX -= curPointOffsetX;
          }
          if (curMeshX > mdx){
            curMeshX += curPointOffsetX;
          }
          curVer[index * 2 + 0] = curMeshX;
          curVer[index * 2 + 1] = curMeshY;
        }

        if (null != preVer && progress > 0.5){
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
    cancelAnimators();
    super.onDetachedFromWindow();
  }

  @Override
  protected void onAttachedToWindow() {
    flipping = false;
    hasFutureFlipTask = false;
    action = ACTION_CUR;
    cancelAnimators();
    super.onAttachedToWindow();
  }

  private void cancelAnimators(){
    if (foldAnimator != null && foldAnimator.isRunning()){
      foldAnimator.cancel();
    }
  }

  private void onFastFlip(){
    ObjectAnimator autoProgress = ObjectAnimator.ofFloat(this,"progress",0,upProgress);
    autoProgress.setInterpolator(interpolator);

    foldAnimator = ObjectAnimator.ofFloat(this,"progress",upProgress,0);
    foldAnimator.setInterpolator(interpolator);
    foldAnimator.addListener(flipAnimatorListener);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playSequentially(autoProgress,foldAnimator);
    animatorSet.start();
  }

  private void runFlipAnimation() {
    runningAnimation = true;
    if (action == ACTION_PRE && !hasPre || action == ACTION_NEXT && !hasNext){
      callback.onAction(ACTION_CUR);
      if (dragging){
        foldAnimator = ObjectAnimator.ofFloat(this,"progress",progress,0);
      }else {
        onFastFlip();
        return;
      }
    }else {
      if (action != animationAction){
        foldAnimator = ObjectAnimator.ofFloat(this,"progress",progress,0);
        callback.onAction(ACTION_CUR);
      }else {
        foldAnimator = ObjectAnimator.ofFloat(this,"progress",progress,1);
      }
    }
    foldAnimator.setInterpolator(interpolator);
    foldAnimator.addListener(flipAnimatorListener);
    foldAnimator.start();
  }

  private void onFlipEnd(){
    showContent();
    callback.onTouchUp(upProgress,action);
    flipping = false;
    hasFutureFlipTask = false;
    callback.onFlipEnd();
    action = ACTION_CUR;
    invalidate();
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
    if (showVirtualContent){
      canvas.drawBitmap(curBitmap,0,0,null);
    }
    if (!flipping || !buildAllBitmap){
      return;
    }
    if (action == ACTION_NEXT){
      canvas.drawBitmap(curBitmap,srcTop,srcTop,null);
      if (hasNext){
        canvas.drawBitmap(nextBitmap,srcBottom,srcBottom,null);
      }
      if (progress < 0.5){
        canvas.drawBitmapMesh(curBitmap, meshWidth, meshHeight, curVer, 0, null, 0, null);
      }else {
        canvas.drawBitmapMesh(nextBitmap,meshWidth,meshHeight,nextVer,0,null,0,null);
      }
    }
    if (action == ACTION_PRE){
      canvas.drawBitmap(curBitmap,srcBottom,srcBottom,null);
      if (hasPre){
        canvas.drawBitmap(preBitmap,srcTop,srcTop,null);
      }
      if (progress < 0.5){
        canvas.drawBitmapMesh(curBitmap, meshWidth, meshHeight, curVer, 0, null, 0, null);
      }else {
        canvas.drawBitmapMesh(preBitmap,meshWidth,meshHeight,preVer,0,null,0,null);
      }
    }
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    //在flip期间不做其他任何响应
    if (hasFutureFlipTask || runningAnimation){
      return true;
    }
    switch (ev.getAction()) {
      case MotionEvent.ACTION_DOWN:
        onPress(ev);
        break;
      case MotionEvent.ACTION_MOVE:
        onMove(ev);
        break;
      case MotionEvent.ACTION_UP:
        onUp(ev);
        break;
    }
    return true;
  }

  private void onPress(MotionEvent ev){
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
    lastDownY = ev.getY();
    firstDownY = lastDownY;
    pressed = true;
    dragging = false;
  }

  private void onMove(MotionEvent ev){
    float curMoveY = ev.getY();
    if (!dragging){
      distance = Math.abs(curMoveY - lastDownY);
      if (distance > scaledTouchSlop) {
        onStartDrag(ev);
        lastDownY = curMoveY;
      }
    }else {
      if (buildAllBitmap){
        onDrag(ev);
      }else {
        lastDownY = lastMoveY;
      }
    }
    lastMoveY = ev.getY();
  }

  private void onStartDrag(MotionEvent ev){
    dragging = true;
    float curMoveY = ev.getY();
    if (curMoveY < lastDownY) {
      //drag up
      //can only show nextBitmap
      action = ACTION_NEXT;
    }
    if (curMoveY > lastDownY) {
      //drag down
      //can only show preBitmap
      action = ACTION_PRE;
    }
    animationAction = action;
    progress = 0;
    buildAllBitmap = false;
    flipping = true;

    showVirtualContent = true;
    initMesh();
    buildCurBitmap();
    invalidate();
    callback.onFlipStart();
    callback.onAction(action);
  }

  private void onDrag(MotionEvent e) {
    float moveY = e.getY();
    if (moveY < lastMoveY){
      animationAction = ACTION_NEXT;
    }
    if (moveY > lastMoveY){
      animationAction = ACTION_PRE;
    }

    //正在显示前一页
    if (action == ACTION_PRE) {
      //变成显示后一页的操作了
      if (moveY < lastDownY) {
        lastDownY = moveY;
      }
    }
    if (action == ACTION_NEXT) {
      if (moveY > lastDownY) {
        lastDownY = moveY;
      }
    }
    distance = Math.abs(moveY - lastDownY);
    if (action == ACTION_PRE && !hasPre ||
      action == ACTION_NEXT && !hasNext){
      distance *= 0.1f;
    }else {
      distance *= 0.5;
    }
    float progress = distance / maxDistance;
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

  private void onUp(MotionEvent ev){
    if (this.progress > 0){
      this.upProgress = this.progress;
    }else {
      float distance = Math.abs(ev.getY() - firstDownY);
      if (action == ACTION_PRE && !hasPre ||
        action == ACTION_NEXT && !hasNext){
        distance *= 0.1f;
      }else {
        distance *= 0.5;
      }
      float progress = distance / maxDistance;
      if (progress > 1){
        progress = 1;
      }
      this.upProgress = progress;
    }
    if (flipping){
      if (buildAllBitmap && !hasFutureFlipTask){
        runFlipAnimation();
      }else {
        hasFutureFlipTask = true;
      }
      pressed = false;
      dragging = false;
      invalidate();
    }
  }

  public void updateOtherBitmap(boolean hasPre, boolean hasNext){
    this.hasPre = hasPre;
    this.hasNext = hasNext;
    buildOtherBitmap();
    buildAllBitmap = true;
    notShowContent();
    showVirtualContent = false;
//    invalidate();
    if (!pressed){
      runFlipAnimation();
    }
  }

  private void showContent(){
    getChildAt(getChildCount() - 1).setTranslationX(0);
  }

  private void notShowContent(){
    getChildAt(getChildCount() - 1).setTranslationX(getWidth());
  }
}