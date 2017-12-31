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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ViewGroup;

/**
 * GalleryView displays pages like a gallery.
 */
public class GalleryView extends ViewGroup {

  public static final int INVALID_INDEX = -1;
  public static final int INVALID_TYPE = -1;

  private static final String LOG_TAG = "GalleryView";

  private GalleryNest nest = new GalleryNest(this);
  private GestureRecognizer gestureRecognizer;

  @Nullable
  private GalleryLayoutManager layoutManager;
  @Nullable
  private GalleryGestureHandler gestureHandler;

  // Whether the GalleryView is in layout
  boolean inLayout;

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
  public void setLayoutManager(@Nullable GalleryLayoutManager layoutManager) {
    if (inLayout) throw new IllegalStateException("Can't set LayoutManager during laying");
    if (this.layoutManager == layoutManager) return;

    GalleryLayoutManager oldLayoutManager = this.layoutManager;
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
   * Returns the LayoutManager set in {@link #setLayoutManager(GalleryLayoutManager)}.
   */
  @Nullable
  public GalleryLayoutManager getLayoutManager() {
    return layoutManager;
  }

  /**
   * Set a GalleryGestureHandler to handle gestures.
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
   * Set a Adapter for this GalleryView.
   * One Adapter can only used for one GalleryView.
   *
   * @throws IllegalStateException if it called during laying
   */
  public void setAdapter(@Nullable GalleryAdapter adapter) {
    if (inLayout) throw new IllegalStateException("Can't set Adapter during laying");
    if (nest.adapter == adapter) return;

    GalleryAdapter oldAdapter = nest.adapter;
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
   * Returns the Adapter set in {@link #setAdapter(GalleryAdapter)}.
   */
  @Nullable
  public GalleryAdapter getAdapter() {
    return nest.adapter;
  }

  /**
   * Returns the page with the specified index.
   * Returns {@code null} if the GalleryView is in laying.
   */
  @Nullable
  public GalleryPage getPageAt(int index) {
    return nest.getPageAt(index);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    // Stop animations to avoid laying anymore
    if (layoutManager != null) {
      layoutManager.cancelAnimations();
    }
    // Reset nest to avoid memory leak
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
