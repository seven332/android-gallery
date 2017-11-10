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

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class GalleryView extends ViewGroup {

  private static final int INVALID_INDEX = -1;
  private static final int INVALID_TYPE = -1;

  private GestureDetector gestureDetector;

  private Nest nest = new Nest(this);

  private LayoutManager layoutManager;

  public GalleryView(Context context) {
    super(context);
    init(context);
  }

  public GalleryView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    gestureDetector = new GestureDetector(context, new GestureDetector.OnGestureListener() {
      @Override
      public boolean onDown(MotionEvent e) {
        return false;
      }

      @Override
      public void onShowPress(MotionEvent e) {

      }

      @Override
      public boolean onSingleTapUp(MotionEvent e) {
        return false;
      }

      @Override
      public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        layoutManager.scroll(nest, (int) distanceX, (int) distanceY);
        return true;
      }

      @Override
      public void onLongPress(MotionEvent e) {

      }

      @Override
      public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
      }
    });
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
    if (layoutManager != null) {
      nest.startLayout();
      layoutManager.layout(nest, r - l, b - t);
      nest.endLayout();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    return true;
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
        Log.w("GalleryNest", "The view is already in layout");
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
        throw new IllegalStateException("Can't only call obtainPage() in layout");
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

    public abstract void scroll(Nest nest, int distanceX, int distanceY);
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
