/*
 * Copyright 2017 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.android.gallery;

/*
 * Created by Hippo on 2017/8/24.
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Parcelable;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatPropertyCompat;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class GalleryView extends ViewGroup {

  private static final String LOG_TAG = "GalleryView";

  private static final int INVALID_INDEX = -1;
  private static final int INVALID_TYPE = -1;

  public static final FloatPropertyCompat<GalleryView> SCROLL_BY = new FloatPropertyCompat<GalleryView>("scrollBy") {
    @Override
    public void setValue(GalleryView view, float value) {
      float d = value - view.lastFling;
      view.lastFling = value;
      view.scrollBy((int) (d * view.flingScaleX), (int) (d * view.flingScaleY));
    }

    @Override
    public float getValue(GalleryView view) {
      return view.lastFling;
    }
  };

  private Nest nest = new Nest(this);
  private LayoutManager layoutManager;
  private GestureRecognizer gestureRecognizer;

  private float flingScaleX;
  private float flingScaleY;
  private float lastFling;
  private FlingAnimation flingAnimation;

  public GalleryView(Context context) {
    super(context);
    init(context);
  }

  public GalleryView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    gestureRecognizer = new GestureRecognizer(context, listener);
    flingAnimation = new FlingAnimation(this, SCROLL_BY);
  }

  public void setLayoutManager(LayoutManager layoutManager) {
    // TODO
    this.layoutManager = layoutManager;
  }

  public void setAdapter(Adapter adapter) {
    // TODO
    nest.setAdapter(adapter);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    // TODO
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    // TODO
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
      throw new IllegalStateException("Width mode and height mode must be MeasureSpec.EXACTLY");
    }

    setMeasuredDimension(widthSize, heightSize);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    if (layoutManager == null) {
      Log.e(LOG_TAG, "No layout manager attached; skipping layout");
      return;
    }
    nest.layout(layoutManager, r - l, b - t);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gestureRecognizer.onTouchEvent(event);
  }

  @Override
  public void scrollBy(int dx, int dy) {
    if (layoutManager == null) {
      Log.e(LOG_TAG, "Cannot scroll without a LayoutManager set. "
          + "Call setLayoutManager with a non-null argument.");
      return;
    }
    layoutManager.scrollBy(nest, dx, dy);
  }

  @Override
  public void scrollTo(int x, int y) {
    Log.w(LOG_TAG, "GalleryView does not support scrolling to an absolute position. "
        + "Use scrollToPosition instead");
  }

  /**
   * Override to prevent freezing of any views created by the adapter.
   */
  @Override
  protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
    dispatchFreezeSelfOnly(container);
  }

  /**
   * Override to prevent thawing of any views created by the adapter.
   */
  @Override
  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
    dispatchThawSelfOnly(container);
  }

  // Stop fling animation and scale animation
  private void cancelAnimations() {
    flingAnimation.cancel();
  }

  private GestureRecognizer.Listener listener = new GestureRecognizer.Listener() {
    @Override
    public void onSingleTapUp(float x, float y) {

    }

    @Override
    public void onSingleTapConfirmed(float x, float y) {

    }

    @Override
    public void onDoubleTap(float x, float y) {

    }

    @Override
    public void onDoubleTapConfirmed(float x, float y) {

    }

    @Override
    public void onLongPress(float x, float y) {

    }

    @Override
    public void onScroll(float dx, float dy, float totalX, float totalY, float x, float y) {
      layoutManager.scrollBy(nest, (int) dx, (int) dy);
    }

    @Override
    public void onFling(float velocityX, float velocityY) {
      float velocity;
      lastFling = 0.0f;
      if (Math.abs(velocityX) > Math.abs(velocityY)) {
        velocity = velocityX;
        flingScaleX = 1.0f;
        flingScaleY = velocityY / velocityX;
      } else {
        velocity = velocityY;
        flingScaleY = 1.0f;
        flingScaleX = velocityX / velocityY;
      }

      flingAnimation.cancel();
      flingAnimation.setStartVelocity(-velocity)
          .setMinValue(-Float.MAX_VALUE)
          .setMaxValue(Float.MAX_VALUE)
          .start();
    }

    @Override
    public void onScaleBegin(float focusX, float focusY) {

    }

    @Override
    public void onScale(float focusX, float focusY, float scale) {

    }

    @Override
    public void onScaleEnd() {

    }

    @Override
    public void onDown(float x, float y) {
      cancelAnimations();
    }
  };

  public static class Nest {

    private static final int MAX_PAGE = 5;

    // Current pages
    private List<Page> pages = new LinkedList<>();
    // Page cache
    private SparseArray<Stack<Page>> cache = new SparseArray<>();

    private final GalleryView view;
    private Adapter adapter;

    private boolean inLayout;

    Nest(GalleryView view) {
      this.view = view;
    }

    void setAdapter(Adapter adapter) {
      this.adapter = adapter;
    }

    /**
     * Layout the GalleryView.
     */
    public void layout(LayoutManager layoutManager, int width, int height) {
      if (inLayout) {
        Log.e(LOG_TAG, "Cannot layout GalleryView recursively");
        return;
      }

      startLayout();
      layoutManager.layout(this, width, height);
      endLayout();
    }

    void startLayout() {
      // Make all pages unpinned
      for (Page page : pages) {
        page.pinned = false;
      }

      inLayout = true;
    }

    void endLayout() {
      inLayout = false;

      // Remove all unpinned pages
      for (Iterator<Page> iterator = pages.iterator(); iterator.hasNext();) {
        Page page = iterator.next();
        if (!page.pinned) {
          iterator.remove();

          view.removeView(page.view);
          adapter.unbindPage(page);

          Stack<Page> stack = cache.get(page.getType());
          if (stack == null) {
            stack = new Stack<>();
            cache.put(page.getType(), stack);
          }

          if (stack.size() < MAX_PAGE) {
            stack.push(page);
          } else {
            adapter.destroyPage(page);
          }
        }
      }
    }

    /**
     * Pins the page with the the index to gallery.
     * If the page is already pinned, just return it.
     */
    @NonNull
    public Page pinPage(int index) {
      if (!inLayout) {
        throw new IllegalStateException("Cannot only call pinPage() in layout");
      }

      if (adapter == null) {
        throw new IllegalStateException("Don't unset adapter in layout");
      }

      // Get from unpinned attached page
      for (Page page : pages) {
        if (page.getIndex() == index) {
          page.pinned = true;
          return page;
        }
      }

      // The page isn't attached.
      // Get the page, bind it and attach it.

      // Get from cache
      Page page = null;
      int type = adapter.getPageViewType(index);
      Stack<Page> stack = cache.get(type);
      if (stack != null && !stack.empty()) {
        page = stack.pop();
      }

      // Create page
      if (page == null) {
        page = adapter.createPage(view, type);
      }

      // Bind and attach
      pages.add(page);
      page.pinned = true;

      view.addView(page.view);
      adapter.bindPage(page, index);

      return page;
    }

    public void unpinPage(Page page) {
      // Just mark it unpinned
      page.pinned = false;
    }

    public int getWidth() {
      return view.getWidth();
    }

    public int getHeight() {
      return view.getHeight();
    }

    public int getPageCount() {
      return adapter.getPageCount();
    }
  }

  public static abstract class LayoutManager {

    public abstract void layout(Nest nest, int width, int height);

    public abstract void scrollBy(Nest nest, int dx, int dy);
  }

  public static abstract class Adapter {

    private Page createPage(GalleryView parent, int type) {
      Page page = onCreatePage(parent, type);
      page.setType(type);
      return page;
    }

    public abstract Page onCreatePage(GalleryView parent, int type);

    private void destroyPage(Page page) {
      onDestroyPage(page);
      page.setType(INVALID_TYPE);
    }

    public abstract void onDestroyPage(Page page);

    private void bindPage(Page page, int index) {
      page.setIndex(index);
      onBindPage(page);
    }

    public abstract void onBindPage(Page page);

    private void unbindPage(Page page) {
      onUnbindPage(page);
      page.setIndex(INVALID_INDEX);
    }

    public abstract void onUnbindPage(Page page);

    public abstract int getPageCount();

    public int getPageViewType(int index) {
      return 0;
    }

    public final void notifyPageSetChanged() {
    }

    public final void notifyPageChanged(int position) {
    }

    public final void notifyPageRangeChanged(int positionStart, int pageCount) {
    }

    public final void notifyPageInserted(int position) {
    }

    public final void notifyPageRangeInserted(int positionStart, int pageCount) {
    }

    public final void notifyPageRemoved(int position) {
    }

    public final void notifyPageRangeRemoved(int positionStart, int pageCount) {
    }

    public final void notifyPageMoved(int fromPosition, int toPosition) {
    }
  }

  public static class Page {

    public final View view;

    private int index = INVALID_INDEX;
    private int type = INVALID_TYPE;

    boolean pinned = false;

    public Page(View view) {
      this.view = view;
    }

    void setIndex(int index) {
      this.index = index;
    }

    void setType(int type) {
      this.type = type;
    }

    public int getIndex() {
      return index;
    }

    public int getType() {
      return type;
    }

    @Override
    public String toString() {
      return "Page{" + Integer.toHexString(hashCode()) + " index=" + index + ", type=" + type + "}";
    }
  }
}
