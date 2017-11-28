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
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class GalleryView extends ViewGroup {

  private static final String LOG_TAG = "GalleryView";

  private static final int INVALID_INDEX = -1;
  private static final int INVALID_TYPE = -1;

  private Nest nest = new Nest(this);
  private GestureRecognizer gestureRecognizer;

  @Nullable
  private LayoutManager layoutManager;

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

  public void setLayoutManager(@Nullable LayoutManager layoutManager) {
    LayoutManager oldLayoutManager = this.layoutManager;
    if (oldLayoutManager != null) {
      oldLayoutManager.detach();
    }

    this.layoutManager = layoutManager;
    if (layoutManager != null) {
      layoutManager.attach(nest);
    }
  }

  public void setAdapter(@Nullable Adapter adapter) {
    Adapter oldAdapter = nest.adapter;
    if (oldAdapter != null) {
      oldAdapter.detach();
    }

    nest.adapter = adapter;
    if (adapter != null) {
      adapter.attach(nest);
    }
  }

  /**
   * Gets the page with the specified index.
   *
   * @throws IllegalStateException if called during layout.
   */
  public Page getPageAt(int index) {
    return nest.getPageAt(index);
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

  @Override
  public void requestLayout() {
    // Block requestLayout() if it isn't in layout
    if (!inLayout) {
      super.requestLayout();
    }
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
      if (layoutManager != null) {
        layoutManager.scrollBy(dx, dy);
      }
    }

    @Override
    public void onFling(float velocityX, float velocityY) {
      if (layoutManager != null) {
        layoutManager.fling(velocityX, velocityY);
      }
    }

    @Override
    public void onScaleBegin(float focusX, float focusY) {}

    @Override
    public void onScale(float focusX, float focusY, float scale) {
      if (layoutManager != null) {
        layoutManager.scaleBy(focusX, focusY, scale);
      }
    }

    @Override
    public void onScaleEnd() {}

    @Override
    public void onDown(float x, float y) {
      if (layoutManager != null) {
        layoutManager.down(x, y);
      }
    }

    @Override
    public void onUp(float x, float y) {
      if (layoutManager != null) {
        layoutManager.up(x, y);
      }
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onPointerDown(float x, float y) {

    }

    @Override
    public void onPointerUp(float x, float y) {

    }
  };

  public static class Nest {

    private static final int MAX_PAGE = 5;

    // Pages which are attached to GalleryView
    @SuppressLint("UseSparseArrays")
    private Map<Integer, Page> pages = new HashMap<>();
    // Page cache, key is page type
    private SparseArray<Stack<Page>> cache = new SparseArray<>();

    private final GalleryView view;

    @Nullable
    private Adapter adapter;

    Nest(GalleryView view) {
      this.view = view;
    }

    /**
     * Layout the GalleryView.
     */
    public void layout(LayoutManager layoutManager, int width, int height) {
      if (view.inLayout) {
        Log.e(LOG_TAG, "Cannot layout GalleryView recursively");
        return;
      }

      startLayout();
      layoutManager.layout(this, width, height);
      endLayout();
    }

    void startLayout() {
      view.inLayout = true;

      Iterator<Page> iterator = pages.values().iterator();
      while (iterator.hasNext()) {
        Page page = iterator.next();
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

    void endLayout() {
      Iterator<Page> iterator = pages.values().iterator();
      while (iterator.hasNext()) {
        Page page = iterator.next();
        // Remove all unpinned pages
        if (!page.pinned) {
          unpinPageInternal(page);
          iterator.remove();
        }
      }

      view.inLayout = false;
    }

    // pages still keeps the page
    private void unpinPageInternal(Page page) {
      page.pinned = false;
      view.removeView(page.view);
      //noinspection ConstantConditions
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

    /**
     * Pins the page with the the index to gallery.
     * If the page is already pinned, just return it.
     */
    @NonNull
    public Page pinPage(int index) {
      if (!view.inLayout) {
        throw new IllegalStateException("Cannot only call pinPage() in layout");
      }

      if (adapter == null) {
        throw new IllegalStateException("Don't unset adapter in layout");
      }

      // Get from unpinned attached page
      Page page = pages.get(index);
      if (page != null && adapter.getPageType(index) == page.getType()) {
        page.pinned = true;
        return page;
      }

      // The page isn't attached.
      // Get the page, bind it and attach it.

      // Get from cache
      page = null;
      int type = adapter.getPageType(index);
      Stack<Page> stack = cache.get(type);
      if (stack != null && !stack.empty()) {
        page = stack.pop();
      }

      // Create page
      if (page == null) {
        page = adapter.createPage(view, type);
      }

      // Bind and attach
      page.pinned = true;

      view.addView(page.view);
      adapter.bindPage(page, index);
      pages.put(page.getIndex(), page);

      return page;
    }

    /**
     * Unpin the page.
     */
    public void unpinPage(Page page) {
      if (!view.inLayout) {
        throw new IllegalStateException("Cannot only call unpinPage() in layout");
      }

      if (adapter == null) {
        throw new IllegalStateException("Don't unset adapter in layout");
      }

      pages.remove(page.getIndex());
      unpinPageInternal(page);
    }

    /**
     * Returns the width of the {@link GalleryView}.
     */
    public int getWidth() {
      return view.getWidth();
    }

    /**
     * Returns the height of the {@link GalleryView}.
     */
    public int getHeight() {
      return view.getHeight();
    }

    /**
     * Returns the count of pages.
     */
    public int getPageCount() {
      if (adapter == null) {
        throw new IllegalStateException("Can only call getPageCount() in layout");
      }
      return adapter.getPageCount();
    }

    /**
     * Gets the page with the specified index.
     *
     * @throws IllegalStateException if called during layout.
     */
    public Page getPageAt(int index) {
      if (view.inLayout) {
        throw new IllegalStateException("Cannot call getPageAt() in layout");
      }

      Page page = pages.get(index);
      if (page != null && page.pinned) {
        return page;
      } else {
        return null;
      }
    }

    private void notifyPageChanged(int index) {
      Page page = pages.get(index);
      if (page != null) {
        page.pinned = false;
        view.requestLayout();
      }
    }

    private void notifyPageSetChanged() {
      if (!pages.isEmpty()) {
        for (Page page : pages.values()) {
          page.pinned = false;
        }
        view.requestLayout();
      }
    }
  }

  public static abstract class LayoutManager {

    @Nullable
    private Nest nest;

    private void attach(Nest nest) {
      if (this.nest != null) {
        throw new IllegalStateException("This LayoutManager is already attached to a GalleryView.");
      }
      this.nest = nest;
    }

    private void detach() {
      this.nest = null;
      cancelAnimations();
    }

    public void layout(int width, int height) {
      if (nest != null) {
        layout(nest, width, height);
      }
    }

    public void scrollBy(float dx, float dy) {
      if (nest != null) {
        scrollBy(nest, dx, dy);
      }
    }

    public void scaleBy(float x, float y, float factor) {
      if (nest != null) {
        scaleBy(nest, x, y, factor);
      }
    }

    public void fling(float velocityX, float velocityY) {
      if (nest != null) {
        fling(nest, velocityX, velocityY);
      }
    }

    public void down(float x, float y) {
      if (nest != null) {
        down(nest, x, y);
      }
    }

    public void up(float x, float y) {
      if (nest != null) {
        up(nest, x, y);
      }
    }

    protected abstract void layout(Nest nest, int width, int height);

    protected abstract void scrollBy(Nest nest, float dx, float dy);

    protected abstract void scaleBy(Nest nest, float x, float y, float factor);

    protected abstract void fling(Nest nest, float velocityX, float velocityY);

    protected abstract void down(Nest nest, float x, float y);

    protected abstract void up(Nest nest, float x, float y);

    protected abstract void cancelAnimations();
  }

  public static abstract class Adapter {

    @Nullable
    private Nest nest;

    private void attach(Nest nest) {
      if (this.nest != null) {
        throw new IllegalStateException("This Adapter is already attached to a GalleryView.");
      }
      this.nest = nest;
    }

    private void detach() {
      this.nest = null;
    }

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

    public int getPageType(int index) {
      return 0;
    }

    public final void notifyPageChanged(int index) {
      if (nest != null) {
        nest.notifyPageChanged(index);
      }
    }

    public final void notifyPageSetChanged() {
      if (nest != null) {
        nest.notifyPageSetChanged();
      }
    }
  }

  public static class Page {

    public final View view;

    private int index = INVALID_INDEX;
    private int type = INVALID_TYPE;

    // The pinned of All valid attached page is true.
    // If the attached page is invalid, namely,
    // notifyPageChanged() or notifyPageSetChanged() is called,
    // the pinned is false.
    // In layout, the pinned of all attached page are set to false at the beginning,
    // pining a page to set the pinned to true.
    // Remove the pages whose pinned is false.
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
