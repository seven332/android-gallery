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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ViewGroup;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

/**
 * GalleryView displays pages like a gallery.
 */
public class GalleryView extends ViewGroup {

  private static final String LOG_TAG = "GalleryView";

  public static final int INVALID_INDEX = -1;
  public static final int INVALID_TYPE = -1;

  private static final int MAX_PAGES_EACH_TYPR = 5;

  private GestureRecognizer gestureRecognizer;

  @Nullable
  private GalleryLayoutManager layoutManager;
  @Nullable
  private GalleryGestureHandler gestureHandler;
  @Nullable
  private GalleryAdapter adapter;

  // Pages which are attached to GalleryView
  @SuppressLint("UseSparseArrays")
  private Map<Integer, GalleryPage> pages = new HashMap<>();
  // Page cache, key is page type
  @SuppressLint("UseSparseArrays")
  private Map<Integer, Stack<GalleryPage>> cache = new HashMap<>();

  // Whether the GalleryView is in layout
  private boolean inLayout;

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
  }

  /*
   * Remove all pages, clear cache.
   */
  private void reset() {
    // Remove all views attached the GalleryView
    removeAllViews();

    if (adapter != null) {
      // Unbind and destroy all attached page
      for (GalleryPage page : pages.values()) {
        adapter.unbindPage(page);
        adapter.destroyPage(page);
      }
      pages.clear();

      // Destroy all cached page
      for (Stack<GalleryPage> stack : cache.values()) {
        for (GalleryPage page : stack) {
          adapter.destroyPage(page);
        }
      }
      cache.clear();
    }
  }

  /*
   * Throw IllegalStateException if the GalleryView is currently undergoing a layout pass.
   */
  private void checkInLayout(String message) {
    if (!inLayout) {
      throw new IllegalStateException(message);
    }
  }

  /*
   * Throw IllegalStateException if the GalleryView is currently undergoing a layout pass.
   */
  private void checkNotInLayout(String message) {
    if (inLayout) {
      throw new IllegalStateException(message);
    }
  }

  /**
   * <b>Note</b>: It's the android original one.
   * <p>
   * {@inheritDoc}
   */
  @Override
  public boolean isInLayout() {
    return super.isInLayout();
  }

  /**
   * The GalleryView version isInLayout().
   */
  public boolean isInLayout2() {
    return inLayout;
  }

  /**
   * Sets a GalleryLayoutManager for this GalleryView.
   * One GalleryLayoutManager can only used for one GalleryView.
   *
   * @throws IllegalStateException if it called in layout
   */
  public void setLayoutManager(@Nullable GalleryLayoutManager layoutManager) {
    checkNotInLayout("Can't set GalleryLayoutManager in layout");

    if (this.layoutManager == layoutManager) {
      // Skip the same GalleryLayoutManager
      return;
    }

    GalleryLayoutManager oldLayoutManager = this.layoutManager;
    if (oldLayoutManager != null) {
      oldLayoutManager.cancelAnimations();
      reset();
      oldLayoutManager.detach();
    }

    this.layoutManager = layoutManager;
    if (layoutManager != null) {
      layoutManager.attach(this);
      requestLayout();
    }
  }

  /**
   * Returns the GalleryLayoutManager set in {@link #setLayoutManager(GalleryLayoutManager)}.
   */
  @Nullable
  public GalleryLayoutManager getLayoutManager() {
    return layoutManager;
  }

  /**
   * Sets a GalleryGestureHandler to handle gestures.
   */
  public void setGestureHandler(@Nullable GalleryGestureHandler gestureHandler) {
    this.gestureHandler = gestureHandler;
  }

  /**
   * Returns the GalleryGestureHandler set in {@link #setGestureHandler(GalleryGestureHandler)}.
   */
  @Nullable
  public GalleryGestureHandler getGestureHandler() {
    return gestureHandler;
  }

  /**
   * Sets a GalleryAdapter for this GalleryView.
   * One GalleryAdapter can only used for one GalleryView.
   *
   * @throws IllegalStateException if it called in layout
   */
  public void setAdapter(@Nullable GalleryAdapter adapter) {
    checkNotInLayout("Can't set GalleryAdapter in layout");

    if (this.adapter == adapter) {
      // Skip the same GalleryAdapter
      return;
    }

    GalleryAdapter oldAdapter = this.adapter;
    if (oldAdapter != null) {
      if (layoutManager != null) {
        layoutManager.cancelAnimations();
      }
      reset();
      oldAdapter.detach();
    }

    this.adapter = adapter;
    if (adapter != null) {
      adapter.attach(this);
      requestLayout();
    }
  }

  /**
   * Returns the GalleryAdapter set in {@link #setAdapter(GalleryAdapter)}.
   */
  @Nullable
  public GalleryAdapter getAdapter() {
    return adapter;
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    // Stop animations to avoid laying anymore
    if (layoutManager != null) {
      layoutManager.cancelAnimations();
    }
    // Reset view to avoid memory leak
    reset();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    // Only accept width and height both are MeasureSpec.EXACTLY
    if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
      throw new IllegalStateException("Width mode and height mode must be MeasureSpec.EXACTLY");
    }

    setMeasuredDimension(widthSize, heightSize);
  }

  /**
   * Measures and layout all children.
   * The actual task is assigned to GalleryLayoutManager.
   *
   * @throws IllegalStateException if it's called recursively
   */
  public void layout() {
    checkNotInLayout("Can't call layout recursively");

    if (layoutManager == null) {
      Log.e(LOG_TAG, "Can't layout without a LayoutManager set");
      return;
    }

    if (adapter == null) {
      Log.e(LOG_TAG, "Can't layout without a Adapter set");
      return;
    }

    int width = getWidth();
    int height = getHeight();

    startLayout();
    if (width > 0 && height > 0 && adapter.getPageCount() > 0) {
      layoutManager.layout(width, height);
    }
    endLayout();
  }

  private void startLayout() {
    inLayout = true;

    Iterator<GalleryPage> iterator = pages.values().iterator();
    while (iterator.hasNext()) {
      GalleryPage page = iterator.next();
      if (page.pinned) {
        // Mark the page unpinned if it's pinned
        page.pinned = false;
      } else {
        // Unpin the page if it's not pinned
        unpinPageInternal(page);
        iterator.remove();
      }
    }
  }

  private void endLayout() {
    Iterator<GalleryPage> iterator = pages.values().iterator();
    while (iterator.hasNext()) {
      GalleryPage page = iterator.next();
      // Remove all unpinned pages
      if (!page.pinned) {
        unpinPageInternal(page);
        iterator.remove();
      }
    }

    inLayout = false;
  }

  /*
   * Unpin the page.
   * Note: pages still keeps the page.
   */
  private void unpinPageInternal(GalleryPage page) {
    page.pinned = false;
    removeView(page.view);
    //noinspection ConstantConditions
    adapter.unbindPage(page);

    Stack<GalleryPage> stack = cache.get(page.getType());
    if (stack == null) {
      stack = new Stack<>();
      cache.put(page.getType(), stack);
    }

    if (stack.size() < MAX_PAGES_EACH_TYPR) {
      stack.push(page);
    } else {
      adapter.destroyPage(page);
    }
  }

  /**
   * Pins the page with the the index to gallery.
   * If the page is already pinned, just return it.
   *
   * @throws IllegalStateException if it called out of layout
   */
  @NonNull
  public GalleryPage pinPage(int index) {
    checkInLayout("Can only pin page in layout");

    if (adapter == null) {
      throw new IllegalStateException("Don't unset adapter in layout");
    }

    // Get from unpinned attached page
    GalleryPage page = pages.get(index);
    if (page != null && adapter.getPageType(index) == page.getType()) {
      page.pinned = true;
      return page;
    }

    /*
     * The page isn't attached.
     * Get the page, bind it and attach it.
     */

    // Get from cache
    page = null;
    int type = adapter.getPageType(index);
    Stack<GalleryPage> stack = cache.get(type);
    if (stack != null && !stack.empty()) {
      page = stack.pop();
    }

    // Create page
    if (page == null) {
      page = adapter.createPage(this, type);
    }

    // Bind and attach
    page.pinned = true;

    addView(page.view);
    adapter.bindPage(page, index);
    pages.put(page.getIndex(), page);

    return page;
  }

  /**
   * Unpin the page.
   *
   * @throws IllegalStateException if it called out of layout
   */
  public void unpinPage(GalleryPage page) {
    checkInLayout("Can only unpin page in layout");

    if (adapter == null) {
      throw new IllegalStateException("Don't unset adapter in layout");
    }

    pages.remove(page.getIndex());
    unpinPageInternal(page);
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    layout();
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    return gestureRecognizer.onTouchEvent(event);
  }

  @Override
  public void scrollBy(int dx, int dy) {
    if (layoutManager == null) {
      Log.e(LOG_TAG, "Cannot scroll without a LayoutManager set");
      return;
    }
    layoutManager.scroll(dx, dy);
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

  @Override
  public void requestLayout() {
    // Block requestLayout() if it isn't in layout
    if (!inLayout) {
      super.requestLayout();
    }
  }

  /**
   * Returns the count of pages.
   * Returns {@code -1} if no adapter.
   */
  public int getPageCount() {
    return adapter != null ? adapter.getPageCount() : -1;
  }

  /**
   * Returns the attached page with the specified index.
   */
  @Nullable
  public GalleryPage getPageAt(int index) {
    return pages.get(index);
  }

  /**
   * Returns an unmodifiable collection of all attached page.
   */
  public Collection<GalleryPage> getPages() {
    return Collections.unmodifiableCollection(pages.values());
  }

  /**
   * Returns {@code true} if the view of the page with the specific index is attached.
   */
  public boolean isViewAttached(int index) {
    if (adapter != null) {
      GalleryPage page = pages.get(index);
      return page != null && adapter.getPageType(index) == page.getType();
    }
    return false;
  }

  void notifyPageChanged(int index) {
    if (inLayout) return;
    GalleryPage page = pages.get(index);
    if (page == null) return;

    page.pinned = false;
    requestLayout();
  }

  void notifyPageSetChanged() {
    if (inLayout) return;
    if (pages.isEmpty()) return;

    for (GalleryPage page : pages.values()) {
      page.pinned = false;
    }
    requestLayout();
  }

  private GestureRecognizer.Listener listener = new GestureRecognizer.Listener() {
    @Override
    public void onSingleTapUp(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onSingleTapUp(GalleryView.this, x, y);
      }
    }

    @Override
    public void onSingleTapConfirmed(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onSingleTapConfirmed(GalleryView.this, x, y);
      }
    }

    @Override
    public void onDoubleTap(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onDoubleTap(GalleryView.this, x, y);
      }
    }

    @Override
    public void onDoubleTapConfirmed(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onDoubleTapConfirmed(GalleryView.this, x, y);
      }
    }

    @Override
    public void onLongPress(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onLongPress(GalleryView.this, x, y);
      }
    }

    @Override
    public void onScroll(float dx, float dy, float totalX, float totalY, float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onScroll(GalleryView.this, dx, dy, totalX, totalY, x, y);
      }
    }

    @Override
    public void onFling(float velocityX, float velocityY) {
      if (gestureHandler != null) {
        gestureHandler.onFling(GalleryView.this, velocityX, velocityY);
      }
    }

    @Override
    public void onScaleBegin(float focusX, float focusY) {
      if (gestureHandler != null) {
        gestureHandler.onScaleBegin(GalleryView.this, focusX, focusY);
      }
    }

    @Override
    public void onScale(float focusX, float focusY, float scale) {
      if (gestureHandler != null) {
        gestureHandler.onScale(GalleryView.this, focusX, focusY, scale);
      }
    }

    @Override
    public void onScaleEnd() {
      if (gestureHandler != null) {
        gestureHandler.onScaleEnd(GalleryView.this);
      }
    }

    @Override
    public void onDown(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onDown(GalleryView.this, x, y);
      }
    }

    @Override
    public void onUp(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onUp(GalleryView.this, x, y);
      }
    }

    @Override
    public void onCancel() {
      if (gestureHandler != null) {
        gestureHandler.onCancel(GalleryView.this);
      }
    }

    @Override
    public void onPointerDown(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onPointerDown(GalleryView.this, x, y);
      }
    }

    @Override
    public void onPointerUp(float x, float y) {
      if (gestureHandler != null) {
        gestureHandler.onPointerUp(GalleryView.this, x, y);
      }
    }
  };
}
