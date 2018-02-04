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
 * Created by Hippo on 2018/1/24.
 */

import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.hippo.android.gallery.BuildConfig;
import com.hippo.android.gallery.Utils;
import com.hippo.android.gallery.intf.Accurate;
import com.hippo.android.gallery.intf.Transformable;
import java.util.Arrays;

/**
 * TransformDrawable can apply scaling or scrolling to any kind of drawable.
 */
public class TransformDrawable extends DrawableWrapper implements Transformable {

  private static final float MIN_SCALE = 1.0f;
  private static final float MAX_SCALE = 3.0f;

  @ScaleType
  private int scaleType = SCALE_TYPE_FIT;
  @StartPosition
  private int startPosition = START_POSITION_TOP_LEFT;

  private RectF srcRect = new RectF();
  private RectF dstRect = new RectF();
  private boolean drawRectFDirty = true;

  private int width = -1;
  private int height = -1;

  private float offsetX;
  private float offsetY;
  private float scale;

  private float minScale;
  private float maxScale;
  private float[] scaleLevels;

  private int drawableWidth = -1;
  private int drawableHeight = -1;

  @Override
  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    if (newDrawable != null) {
      drawableWidth = newDrawable.getIntrinsicWidth();
      drawableHeight = newDrawable.getIntrinsicHeight();
      updateWrapperDrawableBounds();
      if (width > 0 && height > 0 && !getBounds().isEmpty()) {
        updateScaleLevels();
        resetLayout();
      }
    } else {
      drawableWidth = -1;
      drawableHeight = -1;
      width = -1;
      height = -1;
      scale = 0.0f;
      drawRectFDirty = true;
    }
  }

  /**
   * Returns next scale level for automatic scale-level changing.
   *
   * It always returns a bigger scale level if current is not bigger than the max scale level,
   * or the smallest scale level.
   */
  public float getNextScaleLevel() {
    float result = scaleLevels[0];
    for (float value: scaleLevels) {
      if (scale < value - 0.01f) {
        result = value;
        break;
      }
    }
    return result;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    drawRectFDirty = true;
    updateWrapperDrawableBounds();
    if (width > 0 && height > 0 && !getBounds().isEmpty()) {
      updateScaleLevels();
      resetLayout();
      invalidateSelf();
    }
  }

  /*
   * Update wrapper drawable bounds. Returns true if the bounds changes.
   */
  private void updateWrapperDrawableBounds() {
    Rect bounds = getBounds();
    Drawable drawable = getDrawable();

    if (drawable != null) {
      int dWidth = drawable.getIntrinsicWidth();
      int dHeight = drawable.getIntrinsicHeight();
      width = dWidth > 0 ? dWidth : Math.max(bounds.width(), 0);
      height = dHeight > 0 ? dHeight : Math.max(bounds.height(), 0);
      drawable.setBounds(0, 0, width, height);
    } else {
      width = -1;
      height = -1;
    }

    drawRectFDirty = true;
  }

  /*
   * Update max scale, min scale and scale levels.
   */
  private void updateScaleLevels() {
    Rect bounds = getBounds();
    int vWidth = bounds.width();
    int vHeight = bounds.height();
    int dWidth = width;
    int dHeight = height;

    float wScale = (float) vWidth / (float) dWidth;
    float hScale = (float) vHeight / (float) dHeight;
    if (Math.max(wScale, hScale) < MAX_SCALE) {
      scaleLevels = new float[] {MIN_SCALE, wScale, hScale, MAX_SCALE};
    } else {
      scaleLevels = new float[] {MIN_SCALE, wScale, hScale};
    }
    Arrays.sort(scaleLevels);
    minScale = scaleLevels[0];
    maxScale = scaleLevels[scaleLevels.length - 1];
  }

  /*
   * Reset scale and offset to fit scale type and start position.
   */
  private void resetLayout() {
    Rect bounds = getBounds();
    int vWidth = bounds.width();
    int vHeight = bounds.height();
    int dWidth = width;
    int dHeight = height;

    float wScale = (float) vWidth / (float) dWidth;
    float hScale = (float) vHeight / (float) dHeight;

    float tWidth;
    float tHeight;
    switch (scaleType) {
      case SCALE_TYPE_ORIGIN:
        scale = 1.0f;
        tWidth = dWidth;
        tHeight = dHeight;
        break;
      case SCALE_TYPE_FIT_WIDTH:
        scale = wScale;
        tWidth = vWidth;
        tHeight = dHeight * wScale;
        break;
      case SCALE_TYPE_FIT_HEIGHT:
        scale = hScale;
        tWidth = dWidth * hScale;
        tHeight = vHeight;
        break;
      default:
      case SCALE_TYPE_FIT:
        scale = Math.min(wScale, hScale);
        tWidth = dWidth * scale;
        tHeight = dHeight * scale;
        break;
      case SCALE_TYPE_FIXED:
        scale = Utils.clamp(scale, minScale, maxScale);
        tWidth = dWidth * scale;
        tHeight = dHeight * scale;
        break;
    }

    switch (startPosition) {
      default:
      case START_POSITION_TOP_LEFT:
        offsetX = 0;
        offsetY = 0;
        break;
      case START_POSITION_TOP_RIGHT:
        offsetX = vWidth - tWidth;
        offsetY = 0;
        break;
      case START_POSITION_BOTTOM_LEFT:
        offsetX = 0;
        offsetY = vHeight - tHeight;
        break;
      case START_POSITION_BOTTOM_RIGHT:
        offsetX = vWidth - tWidth;
        offsetY = vHeight - tHeight;
        break;
      case START_POSITION_CENTER:
        offsetX = (vWidth - tWidth) / 2;
        offsetY = (vHeight - tHeight) / 2;
        break;
    }

    fixOffset();
    drawRectFDirty = true;
  }

  private void fixScale() {
    scale = Utils.clamp(scale, minScale, maxScale);
  }

  private void fixOffset() {
    Rect bounds = getBounds();

    float actualWidth = width * scale;
    if (actualWidth <= bounds.width()) {
      offsetX = (bounds.width() - actualWidth) / 2;
    } else {
      offsetX = Utils.clamp(offsetX, bounds.width() - actualWidth, 0);
    }

    float actualHeight = height * scale;
    if (actualHeight <= bounds.height()) {
      offsetY = (bounds.height() - actualHeight) / 2;
    } else {
      offsetY = Utils.clamp(offsetY, bounds.height() - actualHeight, 0);
    }
  }

  @Override
  public void setScale(float scale) {
    if (scaleType != SCALE_TYPE_FIXED || this.scale != scale) {
      this.scale = scale;
      scaleType = SCALE_TYPE_FIXED;
      resetLayout();
      invalidateSelf();
    }
  }

  // TODO if the scaleType is the same, resetLayout() should still be called
  @Override
  public void setScaleType(int scaleType) {
    if (this.scaleType != scaleType) {
      this.scaleType = scaleType;
      resetLayout();
      invalidateSelf();
    }
  }

  // TODO if the startPosition is the same, resetLayout() should still be called
  @Override
  public void setStartPosition(int startPosition) {
    if (this.startPosition != startPosition) {
      this.startPosition = startPosition;
      resetLayout();
      invalidateSelf();
    }
  }

  // For debug
  private void checkLayout() {
    // Check scale
    if (scale < minScale) {
      throw new IllegalStateException("scale < minScale");
    }
    if (scale > maxScale) {
      throw new IllegalStateException("scale > maxScale");
    }

    Rect bounds = getBounds();

    // Check offsetX
    float actualWidth = width * scale;
    if (actualWidth <= bounds.width()) {
      if (offsetX != (bounds.width() - actualWidth) / 2) {
        throw new IllegalStateException("offsetX != (bounds.width() - actualWidth) / 2");
      }
    } else {
      if (offsetX > 0) {
        throw new IllegalStateException("offsetX > 0");
      }
      if (offsetX + actualWidth < bounds.width()) {
        throw new IllegalStateException("offsetX + actualWidth < bounds.width()");
      }
    }

    // Check offsetY
    float actualHeight = height * scale;
    if (actualHeight <= bounds.height()) {
      if (offsetY != (bounds.height() - actualHeight) / 2) {
        throw new IllegalStateException("offsetY != (bounds.height() - actualHeight) / 2");
      }
    } else {
      if (offsetY > 0) {
        throw new IllegalStateException("offsetY > 0");
      }
      if (offsetY + actualHeight < bounds.height()) {
        throw new IllegalStateException("offsetY + actualHeight < bounds.height()");
      }
    }
  }

  @Override
  public void scroll(float dx, float dy, @Nullable float[] remain) {
    Rect bounds = getBounds();
    if (width <= 0 || height <= 0 || bounds.isEmpty()) {
      return;
    }

    // Assume offset and scale is in bounds
    if (BuildConfig.DEBUG) {
      checkLayout();
    }

    // Try to avoid float operation. Comparison only.

    float remainX;
    float assumedOffsetX = offsetX + dx;
    float actualWidth = width * scale;
    if (offsetX > 0) {
      // actualWidth < bounds.width(), can't scroll along x axis
      remainX = dx;
    } else if (assumedOffsetX > 0) {
      // dx > 0, dx is too positive large
      offsetX = 0;
      // 0 = offsetX + (dx - (assumedOffsetX))
      remainX = assumedOffsetX;
    } else if (assumedOffsetX + actualWidth < bounds.width()) {
      // dx < 0, dx is too negative large
      offsetX = bounds.width() - actualWidth;
      // bounds.width() - actualWidth = offsetX + (dx - (actualWidth + assumedOffsetX - bounds.width()))
      remainX = actualWidth + assumedOffsetX - bounds.width();
    } else {
      offsetX = assumedOffsetX;
      remainX = 0;
    }

    float remainY;
    float assumedOffsetY = offsetY + dy;
    float actualHeight = height * scale;
    if (offsetY > 0) {
      // actualHeight < bounds.height(), can't scroll along y axis
      remainY = dy;
    } else if (assumedOffsetY > 0) {
      // dy > 0, dy is too positive large
      offsetY = 0;
      // 0 = offsetY + (dy - (assumedOffsetY))
      remainY = assumedOffsetY;
    } else if (assumedOffsetY + actualHeight < bounds.height()) {
      // dy < 0, dy is too negative large
      offsetY = bounds.height() - actualHeight;
      // bounds.height() - actualHeight = offsetY + (dy - (actualHeight + assumedOffsetY - bounds.height()))
      remainY = actualHeight + assumedOffsetY - bounds.height();
    } else {
      offsetY = assumedOffsetY;
      remainY = 0;
    }

    if (remain != null) {
      remain[0] = remainX;
      remain[1] = remainY;
    }

    if (dx != remainX || dy != remainY) {
      drawRectFDirty = true;
      invalidateSelf();
    }
  }

  @Override
  public void scale(float x, float y, float factor, @Nullable float[] remain) {
    Rect bounds = getBounds();
    if (width <= 0 || height <= 0 || bounds.isEmpty()) {
      return;
    }

    // Assume offset and scale is in bounds
    if (BuildConfig.DEBUG) {
      checkLayout();
    }

    // Try to avoid float operation. Comparison only.

    float actualFactor;
    float remainFactor;
    if ((scale != minScale && factor < 1) ||
        (scale != maxScale && factor > 1)) {
      float assumedScale = scale * factor;
      float newScale = Utils.clamp(assumedScale, minScale, maxScale);
      if (assumedScale == newScale) {
        scale = newScale;
        actualFactor = factor;
        remainFactor = 1;
      } else {
        actualFactor = newScale / scale;
        remainFactor = factor / actualFactor;
        scale = newScale;
      }
    } else {
      actualFactor = 1;
      remainFactor = factor;
    }

    if (remain != null) {
      remain[0] = remainFactor;
    }

    if (factor != remainFactor) {
      offsetX = x - ((x - offsetX) * actualFactor);
      offsetY = y - ((y - offsetY) * actualFactor);
      fixOffset();
      drawRectFDirty = true;
      invalidateSelf();
    }
  }

  /*
   * Apply bounds, clipRect, scale, offsetX, offsetY to
   * srcRect and dstRect.
   */
  private void updateDrawRect() {
    if (!drawRectFDirty) {
      return;
    }
    drawRectFDirty = false;

    Rect bounds = getBounds();
    RectF srcRect = this.srcRect;
    RectF dstRect = this.dstRect;

    dstRect.set(bounds);
    srcRect.set(0, 0, width * scale, height * scale);
    srcRect.offset(bounds.left + offsetX, bounds.top + offsetY);

    if (!dstRect.intersect(srcRect)) {
      srcRect.setEmpty();
      dstRect.setEmpty();
      return;
    }

    srcRect.set(dstRect);
    srcRect.offset(-bounds.left - offsetX, -bounds.top - offsetY);
    srcRect.left = srcRect.left / scale;
    srcRect.top = srcRect.top / scale;
    srcRect.right = srcRect.right / scale;
    srcRect.bottom = srcRect.bottom / scale;

    if (!srcRect.intersect(0, 0, width, height)) {
      srcRect.setEmpty();
      dstRect.setEmpty();
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    Rect bounds = getBounds();
    Drawable drawable = getDrawable();
    if (width <= 0 || height <= 0 || bounds.isEmpty() || drawable == null) {
      return;
    }

    if (drawable instanceof Accurate) {
      updateDrawRect();
      if (!srcRect.isEmpty() && !dstRect.isEmpty()) {
        ((Accurate) drawable).draw(canvas, srcRect, dstRect);
      }
    } else {
      int saved = canvas.save();
      canvas.translate(bounds.left + offsetX, bounds.top + offsetY);
      canvas.scale(scale, scale);
      drawable.draw(canvas);
      canvas.restoreToCount(saved);
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    if (who == getDrawable()) {
      int oldDrawableWidth = drawableWidth;
      int oldDrawableHeight = drawableHeight;
      drawableWidth = who.getIntrinsicWidth();
      drawableHeight = who.getIntrinsicHeight();

      if (drawableWidth != oldDrawableWidth || drawableHeight != oldDrawableHeight) {
        drawRectFDirty = true;
        updateWrapperDrawableBounds();
        if (width > 0 && height > 0 && !getBounds().isEmpty()) {
          updateScaleLevels();
          fixScale();
          fixOffset();
        }
      }
    }
    super.invalidateDrawable(who);
  }
}
