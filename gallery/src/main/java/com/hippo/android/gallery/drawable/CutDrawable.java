/*
 * Copyright 2018 Hippo Seven
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

package com.hippo.android.gallery.drawable;

/*
 * Created by Hippo on 2018/1/26.
 */

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * CutDrawable cuts a drawable.
 */
public class CutDrawable extends DrawableWrapper {

  private static final int CUT_NONE = 0;
  private static final int CUT_RECT = 1;
  private static final int CUT_PERCENT = 2;

  private int cutMode = CUT_NONE;
  private Rect cutRect = new Rect();
  private RectF cutPercent = new RectF();
  private Rect cut = new Rect();

  private int drawableWidth = -1;
  private int drawableHeight = -1;

  /**
   * Cuts drawable to a specified region.
   * If the region is out of drawable size, clamp the region.
   */
  public void cutRect(Rect cut) {
    cutRect(cut.left, cut.top, cut.right, cut.bottom);
  }

  /**
   * Cuts drawable to a specified region.
   * If the region is out of drawable size, clamp the region.
   */
  public void cutRect(int left, int top, int right, int bottom) {
    if (cutMode != CUT_RECT || cutRect.left != left || cutRect.top != top ||
        cutRect.right != right || cutRect.bottom != bottom) {
      cutMode = CUT_RECT;
      cutRect.set(left, top, right, bottom);
      updateCut();
      updateBounds();
      invalidateSelf();
    }
  }

  /**
   * Cuts drawable to a specified region.
   * The region is described in percent, {@code [0.0f, 1.0f]}.
   * If the region is out of drawable size, clamp the region.
   */
  public void cutPercent(RectF cut) {
    cutPercent(cut.left, cut.top, cut.right, cut.bottom);
  }

  /**
   * Cuts drawable to a specified region.
   * The region is described in percent, {@code [0.0f, 1.0f]}.
   * If the region is out of drawable size, clamp the region.
   */
  public void cutPercent(float left, float top, float right, float bottom) {
    if (cutMode != CUT_PERCENT || cutPercent.left != left || cutPercent.top != top ||
        cutPercent.right != right || cutPercent.bottom != bottom) {
      cutMode = CUT_PERCENT;
      cutPercent.set(left, top, right, bottom);
      updateCut();
      updateBounds();
      invalidateSelf();
    }
  }

  /**
   * Clear the cut region.
   */
  public void clearCut() {
    if (cutMode != CUT_NONE) {
      cutMode = CUT_NONE;
      updateCut();
      updateBounds();
      invalidateSelf();
    }
  }

  private void updateCut() {
    Drawable drawable = getDrawable();
    Rect rect = getBounds();
    if (drawable == null || rect.isEmpty()) {
      cut.setEmpty();
      return;
    }

    int width = drawableWidth > 0 ? drawableWidth : rect.width();
    int height = drawableHeight > 0 ? drawableHeight : rect.height();
    if (width <= 0 || height <= 0) {
      cut.setEmpty();
      return;
    }

    switch (cutMode) {
      case CUT_NONE:
        cut.set(0, 0, width, height);
        break;
      case CUT_RECT:
        if (cutRect.isEmpty()) {
          cut.setEmpty();
        } else {
          cut.set(0, 0, width, height);
          if (!cut.intersect(cutRect)) {
            cut.setEmpty();
          }
        }
        break;
      case CUT_PERCENT:
        if (cutPercent.isEmpty()) {
          cut.setEmpty();
        } else {
          cut.set(0, 0, width, height);
          if (!cut.intersect(
              (int) (cutPercent.left * width),
              (int) (cutPercent.top * height),
              (int) (cutPercent.right * width),
              (int) (cutPercent.bottom * height))) {
            cut.setEmpty();
          }
        }
        break;
    }

    // If no intrinsic width or height, no cut
    if (drawableWidth <= 0) {
      cut.left = 0;
      cut.right = width;
    }
    if (drawableHeight <= 0) {
      cut.top = 0;
      cut.bottom = height;
    }
  }

  private void updateBounds() {
    Drawable drawable = getDrawable();
    if (drawable == null) {
      return;
    }

    if (cut.isEmpty()) {
      drawable.setBounds(cut);
    } else {
      int left, top, right, bottom;
      Rect bounds = getBounds();
      if (drawableWidth > 0) {
        float scaleX = (float) bounds.width() / (float) cut.width();
        left = bounds.left + (int) (-cut.left * scaleX);
        right = bounds.left + (int) ((drawableWidth - cut.left) * scaleX);
      } else {
        left = bounds.left;
        right = bounds.right;
      }
      if (drawableHeight > 0) {
        float scaleY = (float) bounds.height() / (float) cut.height();
        top = bounds.top + (int) (-cut.top * scaleY);
        bottom = bounds.top + (int) ((drawableWidth - cut.top) * scaleY);
      } else {
        top = bounds.top;
        bottom = bounds.bottom;
      }
      drawable.setBounds(left, top, right, bottom);
    }
  }

  /**
   * Returns actual cut region.
   */
  protected Rect getCut() {
    return cut;
  }

  @Override
  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    if (newDrawable != null) {
      drawableWidth = newDrawable.getIntrinsicWidth();
      drawableHeight = newDrawable.getIntrinsicHeight();
    } else {
      drawableWidth = -1;
      drawableHeight = -1;
    }

    updateCut();
    updateBounds();
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    updateCut();
    updateBounds();
  }

  @Override
  public int getIntrinsicWidth() {
    return cut.width();
  }

  @Override
  public int getIntrinsicHeight() {
    return cut.height();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    int saved = canvas.save();
    // only the cut region is visible
    canvas.clipRect(getBounds());
    super.draw(canvas);
    canvas.restoreToCount(saved);
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    if (who == getDrawable()) {
      int oldDrawableWidth = drawableWidth;
      int oldDrawableHeight = drawableHeight;
      drawableWidth = who.getIntrinsicWidth();
      drawableHeight = who.getIntrinsicHeight();

      if (drawableWidth != oldDrawableWidth || drawableHeight != oldDrawableHeight) {
        updateCut();
        updateBounds();
      }
    }
    super.invalidateDrawable(who);
  }
}
