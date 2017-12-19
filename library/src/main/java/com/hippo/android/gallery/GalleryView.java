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

  /**
   * Set a LayoutManager for this GalleryView.
   * One LayoutManager can only used for one GalleryView.
   *
   * @throws IllegalStateException if it called during laying
   */
  public void setLayoutManager(@Nullable LayoutManager layoutManager) {
    if (inLayout) throw new IllegalStateException("Can't set LayoutManager during laying");
    if (this.layoutManager == layoutManager) return;

    LayoutManager oldLayoutManager = this.layoutManager;
    if (oldLayoutManager != null) {
      oldLayoutManager.cancelAnimations();
      oldLayoutManager.detach();
      nest.reset();
    }

    this.layoutManager = layoutManager;
    if (layoutManager != null) {
      layoutManager.attach(nest);
      requestLayout();
    }
  }

  /**
   * Returns the LayoutManager set in {@link #setLayoutManager(LayoutManager)}.
   */
  @Nullable
  public LayoutManager getLayoutManager() {
    return layoutManager;
  }

  /**
   * Set a Adapter for this GalleryView.
   * One Adapter can only used for one GalleryView.
   *
   * @throws IllegalStateException if it called during laying
   */
  public void setAdapter(@Nullable Adapter adapter) {
    if (inLayout) throw new IllegalStateException("Can't set Adapter during laying");
    if (nest.adapter == adapter) return;

    Adapter oldAdapter = nest.adapter;
    if (oldAdapter != null) {
      if (layoutManager != null) {
        layoutManager.cancelAnimations();
      }
      oldAdapter.detach();
      nest.reset();
    }

    nest.adapter = adapter;
    if (adapter != null) {
      adapter.attach(nest);
      requestLayout();
    }
  }

  /**
   * Returns the Adapter set in {@link #setAdapter(Adapter)}.
   */
  @Nullable
  public Adapter getAdapter() {
    return nest.adapter;
  }

  /**
   * Returns the page with the specified index.
   * Returns {@code null} if the GalleryView is in laying.
   */
  @Nullable
  public Page getPageAt(int index) {
    return nest.getPageAt(index);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    /*
     * The GalleryView is detached from the window.
     * It's the good to reset the nest to clear all page.
     */
    nest.reset();
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
      Log.e(LOG_TAG, "Cannot layout without a LayoutManager set");
      return;
    }
    nest.layout(layoutManager);
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

  private GestureRecognizer.Listener listener = new GestureRecognizer.Listener() {
    @Override
    public void onSingleTapUp(float x, float y) {}

    @Override
    public void onSingleTapConfirmed(float x, float y) {}

    @Override
    public void onDoubleTap(float x, float y) {}

    @Override
    public void onDoubleTapConfirmed(float x, float y) {}

    @Override
    public void onLongPress(float x, float y) {}

    @Override
    public void onScroll(float dx, float dy, float totalX, float totalY, float x, float y) {
      if (layoutManager != null) {
        layoutManager.scroll(dx, dy);
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
        layoutManager.scale(focusX, focusY, scale);
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
    public void onCancel() {}

    @Override
    public void onPointerDown(float x, float y) {}

    @Override
    public void onPointerUp(float x, float y) {}
  };

  public static class Nest {

    private static final int MAX_PAGE = 5;

    // Pages which are attached to GalleryView
    @SuppressLint("UseSparseArrays")
    private Map<Integer, Page> pages = new HashMap<>();
    // Page cache, key is page type
    @SuppressLint("UseSparseArrays")
    private Map<Integer, Stack<Page>> cache = new HashMap<>();

    private final GalleryView view;

    @Nullable
    private Adapter adapter;

    Nest(GalleryView view) {
      this.view = view;
    }

    // Remove all page, clear cache
    private void reset() {
      if (view.inLayout) throw new IllegalStateException("Can't reset Nest during laying");

      // Remove all views attached the GalleryView
      view.removeAllViews();

      if (adapter != null) {
        // Unbind and destroy all attached page
        for (Page page : pages.values()) {
          adapter.unbindPage(page);
          adapter.destroyPage(page);
        }
        pages.clear();

        // Destroy all cached page
        for (Stack<Page> stack : cache.values()) {
          for (Page page : stack) {
            adapter.destroyPage(page);
          }
        }
        cache.clear();
      }
    }

    /**
     * Layout the GalleryView attached to this Nest.
     * The GalleryView should be measured.
     */
    public void layout(@NonNull LayoutManager layoutManager) {
      if (view.inLayout) {
        Log.e(LOG_TAG, "Cannot layout GalleryView recursively");
        return;
      }

      if (adapter == null) {
        Log.e(LOG_TAG, "Cannot layout without a Adapter set");
        return;
      }

      int width = view.getWidth();
      int height = view.getHeight();

      startLayout();
      if (width > 0 && height > 0 && adapter.getPageCount() > 0) {
        layoutManager.layout(width, height);
      }
      endLayout();
    }

    private void startLayout() {
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

    private void endLayout() {
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

      /*
       * The page isn't attached.
       * Get the page, bind it and attach it.
       */

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
     * Returns the count of pages.
     */
    public int getPageCount() {
      if (adapter == null) {
        throw new IllegalStateException("Can only call getPageCount() in layout");
      }
      return adapter.getPageCount();
    }

    /**
     * Returns the page with the specified index.
     * Returns {@code null} if the GalleryView is in laying.
     */
    @Nullable
    public Page getPageAt(int index) {
      if (view.inLayout) return null;

      Page page = pages.get(index);
      return page != null && page.pinned ? page : null;
    }

    private void notifyPageChanged(int index) {
      if (view.inLayout) return;
      Page page = pages.get(index);
      if (page == null) return;

      page.pinned = false;
      view.requestLayout();
    }

    private void notifyPageSetChanged() {
      if (view.inLayout) return;
      if (pages.isEmpty()) return;

      for (Page page : pages.values()) {
        page.pinned = false;
      }
      view.requestLayout();
    }
  }

  /**
   * LayoutManager handles page layout and touch events.
   */
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
    }

    /**
     * Returns the Nest of attached GalleryView.
     * Returns {@code null} if the LayoutManager isn't attached to a GalleryView.
     */
    @Nullable
    public Nest getNest() {
      return nest;
    }

    /**
     * Lays the GalleryView attached to LayoutManager.
     * It's sure that width > 0, height > 0 and page count > 0.
     *
     * @param width the width of the GalleryView
     * @param height the height of the GalleryView
     */
    protected abstract void layout(int width, int height);

    /**
     * Scrolls the GalleryView attached to LayoutManager.
     *
     * @param dx left to right is positive
     * @param dy top to bottom is positive
     */
    protected abstract void scroll(float dx, float dy);

    /**
     * Scales the GalleryView attached to LayoutManager.
     *
     * @param x the x of the center point
     * @param y the y of the center point
     * @param factor the factor of the scaling
     */
    protected abstract void scale(float x, float y, float factor);

    /**
     * Flings the GalleryView attached to LayoutManager.
     *
     * @param velocityX the velocity in horizontal direction
     * @param velocityY the velocity in vertical direction
     */
    protected abstract void fling(float velocityX, float velocityY);

    /**
     * The first point of a touch event flow is down.
     *
     * @param x the x of the point
     * @param y the y of the point
     */
    protected abstract void down(float x, float y);

    /**
     * The last point of a touch event flow is up.
     *
     * @param x the x of the point
     * @param y the y of the point
     */
    protected abstract void up(float x, float y);

    /**
     * Cancel all animations of the LayoutManager.
     */
    protected abstract void cancelAnimations();
  }

  /**
   * Adapter handles Page creation, destroying, binding and unbinding.
   */
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
      page.type = type;
      return page;
    }

    /**
     * Creates a Page of the specified type.
     */
    public abstract Page onCreatePage(GalleryView parent, int type);

    private void destroyPage(Page page) {
      onDestroyPage(page);
      page.type = INVALID_TYPE;
    }

    /**
     * Destroys the Page.
     *
     * The index of the Page is invalid.
     * The type of the Page is valid.
     */
    public abstract void onDestroyPage(Page page);

    private void bindPage(Page page, int index) {
      page.index = index;
      onBindPage(page);
    }

    /**
     * Binds the Page.
     *
     * The view of the page is already attached to the GalleryView.
     *
     * The index of the Page is valid.
     * The type of the Page is valid.
     */
    public abstract void onBindPage(Page page);

    private void unbindPage(Page page) {
      onUnbindPage(page);
      page.index = INVALID_INDEX;
    }

    /**
     * Unbinds the Page.
     *
     * The view of the page is already detached from the GalleryView.
     *
     * The index of the Page is valid.
     * The type of the Page is valid.
     */
    public abstract void onUnbindPage(Page page);

    /**
     * Returns the total count of all pages.
     */
    public abstract int getPageCount();

    /**
     * Returns the type of the page with the specified index.
     */
    public int getPageType(int index) {
      return 0;
    }

    /**
     * Notifies the page with the specified index is changed.
     */
    public final void notifyPageChanged(int index) {
      if (nest != null) {
        nest.notifyPageChanged(index);
      }
    }

    /**
     * Notifies all pages might be changed.
     */
    public final void notifyPageSetChanged() {
      if (nest != null) {
        nest.notifyPageSetChanged();
      }
    }
  }

  /**
   * Page of a GalleryView, the base element to lay.
   */
  public static class Page {

    /**
     * The view of this Page.
     */
    public final View view;

    private int index = INVALID_INDEX;
    private int type = INVALID_TYPE;

    /*
     * The pinned of All valid attached page is true.
     * If the attached page is invalid, namely,
     * notifyPageChanged() or notifyPageSetChanged() is called,
     * the pinned is false.
     * In layout, the pinned of all attached page are set to false at the beginning,
     * pining a page to set the pinned to true.
     * Remove the pages whose pinned is false.
     */
    boolean pinned = false;

    public Page(View view) {
      this.view = view;
    }

    /**
     * Returns the index of this Page.
     * It's valid from {@link Adapter#onBindPage(Page)} to {@link Adapter#onUnbindPage(Page)}.
     */
    public int getIndex() {
      return index;
    }

    /**
     * Returns the type of this Page.
     * It's valid until the page destroyed.
     */
    public int getType() {
      return type;
    }

    @Override
    public String toString() {
      return "Page{" + Integer.toHexString(hashCode()) + " index=" + index + ", type=" + type + "}";
    }
  }
}
