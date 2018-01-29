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
    if (drawable == null) {
      cut.setEmpty();
      return;
    }

    switch (cutMode) {
      case CUT_NONE:
        cut.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        break;
      case CUT_RECT:
        if (cutRect.isEmpty()) {
          cut.setEmpty();
        } else {
          cut.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
          if (!cut.intersect(cutRect)) {
            cut.setEmpty();
          }
        }
        break;
      case CUT_PERCENT:
        if (cutPercent.isEmpty()) {
          cut.setEmpty();
        } else {
          cut.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
          if (!cut.intersect(
              (int) (cutPercent.left * drawable.getIntrinsicWidth()),
              (int) (cutPercent.top * drawable.getIntrinsicHeight()),
              (int) (cutPercent.right * drawable.getIntrinsicWidth()),
              (int) (cutPercent.bottom * drawable.getIntrinsicHeight()))) {
            cut.setEmpty();
          }
        }
        break;
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
      // Map rect, rect -> getBounds(), (0, 0, dWidth, dHeight) -> dBounds
      Rect bounds = getBounds();
      float scaleX = (float) bounds.width() / (float) cut.width();
      float scaleY = (float) bounds.height() / (float) cut.height();
      drawable.setBounds(
          bounds.left + (int) (-cut.left * scaleX),
          bounds.top + (int) (-cut.top * scaleY),
          bounds.left + (int) ((drawable.getIntrinsicWidth() - cut.left) * scaleX),
          bounds.top + (int) ((drawable.getIntrinsicHeight() - cut.top) * scaleY)
      );
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
    updateCut();
    updateBounds();
    super.invalidateDrawable(who);
  }
}
