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
 * Created by Hippo on 2017/12/31.
 */

import android.support.annotation.Nullable;
import android.view.View;

/**
 * LayoutManager handles page layout and actions about layout, like scrolling and scaling.
 */
public abstract class GalleryLayoutManager {

  @Nullable
  private GalleryView view;

  void attach(GalleryView view) {
    if (this.view != null) {
      throw new IllegalStateException("This LayoutManager is already attached to a GalleryView.");
    }
    this.view = view;
  }

  void detach() {
    this.view = null;
  }

  /**
   * Returns the Nest of attached GalleryView.
   * Returns {@code null} if the LayoutManager isn't attached to a GalleryView.
   */
  @Nullable
  public GalleryView getGalleryView() {
    return view;
  }

  /**
   * Returns a index to represent current state.
   * The index must be the index of a shown page.
   */
  public abstract int getSelectedIndex();

  /**
   * Make the page with the index is displayed in screen.
   */
  public abstract void setSelectedIndex(int index);

  /**
   * Lays the GalleryView attached to LayoutManager.
   * It's sure that width > 0, height > 0 and page count > 0.
   *
   * @param width the width of the GalleryView
   * @param height the height of the GalleryView
   */
  protected abstract void layout(int width, int height);

  /**
   * Requests layout for the GalleryView attached to this LayoutManager.
   */
  public void requestLayout() {
    if (view != null) {
      view.requestLayout();
    }
  }

  /**
   * Returns {@code true} if this LayoutManager is attached to a GalleryView,
   * and the GalleryView is in layout.
   */
  public boolean isInLayout() {
    return view != null && view.isInLayout2();
  }

  /**
   * Returns {@code true} if the child view of GalleryView should be drawn.
   */
  public boolean shouldDrawView(View view) {
    return true;
  }

  /**
   * Scrolls the GalleryView attached to LayoutManager.
   *
   * @param dx left to right is positive
   * @param dy top to bottom is positive
   */
  public abstract void scroll(float dx, float dy, @Nullable float[] remain);

  /**
   * Scales the GalleryView attached to LayoutManager.
   *
   * @param x the x of the center point
   * @param y the y of the center point
   * @param factor the factor of the scaling
   */
  public abstract void scale(float x, float y, float factor, @Nullable float[] remain);

  /**
   * Flings the GalleryView attached to LayoutManager.
   *
   * @param velocityX the velocity in horizontal direction
   * @param velocityY the velocity in vertical direction
   */
  public abstract void fling(float velocityX, float velocityY);

  /**
   * Cancel all animations of the LayoutManager.
   */
  public abstract void cancelAnimations();
}
