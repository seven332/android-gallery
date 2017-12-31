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

/**
 * LayoutManager handles page layout and touch events.
 */
public abstract class GalleryLayoutManager {

  @Nullable
  private GalleryNest nest;

  void attach(GalleryNest nest) {
    if (this.nest != null) {
      throw new IllegalStateException("This LayoutManager is already attached to a GalleryView.");
    }
    this.nest = nest;
  }

  void detach() {
    this.nest = null;
  }

  /**
   * Returns the Nest of attached GalleryView.
   * Returns {@code null} if the LayoutManager isn't attached to a GalleryView.
   */
  @Nullable
  public GalleryNest getNest() {
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
   * Requests layout for the GalleryView attached to this LayoutManager.
   */
  protected void requestLayout() {
    if (nest != null) {
      nest.view.requestLayout();
    }
  }

  /**
   * Returns {@code true} if this LayoutManager is attached to a GalleryView,
   * and the GalleryView is in layout.
   */
  protected boolean isInLayout() {
    return nest != null && nest.view.inLayout;
  }

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
   * Cancel all animations of the LayoutManager.
   */
  protected abstract void cancelAnimations();
}
