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

package com.hippo.android.gallery.demo;

/*
 * Created by Hippo on 2017/11/27.
 */

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.hippo.android.gallery.Photo;
import java.util.Arrays;

public class TransformedDrawable extends Drawable implements Drawable.Callback {

  @Nullable
  private Drawable drawable;

  @Photo.ScaleType
  private int scaleType = Photo.SCALE_TYPE_FIT;
  @Photo.StartPosition
  private int startPosition = Photo.START_POSITION_TOP_LEFT;

  // Actual intrinsic width of drawable
  private int aDWidth;
  // Actual intrinsic height of drawable
  private int aDHeight;
  // Virtual intrinsic width of drawable
  private int vDWidth;
  // Virtual intrinsic height of drawable
  private int vDHeight;

  private float offsetX;
  private float offsetY;
  private float scale;
  private float minScale;
  private float maxScale;
  private float[] scaleLevels;
  // True if drawable can be drawn
  private boolean valid;

  public TransformedDrawable(Drawable drawable) {
    setWrappedDrawable(drawable);
  }

  public Drawable getWrappedDrawable() {
    return drawable;
  }

  public void setWrappedDrawable(Drawable drawable) {
    if (this.drawable != null) {
      this.drawable.setCallback(null);
    }

    this.drawable = drawable;

    if (drawable != null) {
      aDWidth = drawable.getIntrinsicWidth();
      aDHeight = drawable.getIntrinsicHeight();
      valid = true;
      drawable.setCallback(this);
      onBoundsChange(getBounds());
    } else {
      aDWidth = 0;
      aDHeight = 0;
      valid = false;
    }

    invalidateSelf();
  }

  public void setScaleType(int scaleType) {
    this.scaleType = scaleType;

    Rect bounds = getBounds();
    if (!bounds.isEmpty() && drawable != null) {
      resetLayout(bounds);
      invalidateSelf();
    }
  }

  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;

    Rect bounds = getBounds();
    if (!bounds.isEmpty() && drawable != null) {
      resetLayout(bounds);
      invalidateSelf();
    }
  }

  public void offset(float dx, float dy, float[] remain) {
    if (!valid) {
      return;
    }

    float oldOffsetX = offsetX;
    float oldOffsetY = offsetY;
    offsetX += dx;
    offsetY += dy;
    fixOffset();

    remain[0] = dx - (offsetX - oldOffsetX);
    remain[1] = dy - (offsetY - oldOffsetY);

    invalidateSelf();
  }

  public void scale(float x, float y, float factor) {
    if (!valid) {
      return;
    }

    float oldScale = scale;
    scale = Utils.clamp(scale * factor, minScale, maxScale);
    if (scale == oldScale) {
      return;
    }

    offsetX = x - ((x - offsetX) * factor);
    offsetY = y - ((y - offsetY) * factor);
    fixOffset();

    invalidateSelf();
  }

  @Override
  public int getIntrinsicWidth() {
    return aDWidth;
  }

  @Override
  public int getIntrinsicHeight() {
    return aDHeight;
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    if (drawable == null) {
      return;
    }

    if (bounds.isEmpty()) {
      valid = false;
    } else {
      valid = true;
      vDWidth = aDWidth > 0 ? aDWidth : bounds.width();
      vDHeight = aDHeight > 0 ? aDHeight : bounds.height();

      drawable.setBounds(0, 0, vDWidth, vDHeight);
      resetLayout(bounds);
    }
  }

  // Reset offset and scale to fit
  private void resetLayout(Rect bounds) {
    if (!valid) {
      return;
    }

    int vWidth = bounds.width();
    int vHeight = bounds.height();
    int dWidth = this.vDWidth;
    int dHeight = this.vDHeight;

    float wScale = (float) vWidth / (float) dWidth;
    float hScale = (float) vHeight / (float) dHeight;
    if (Math.max(wScale, hScale) < 3.0f) {
      scaleLevels = new float[] { 1.0f, wScale, hScale, 3.0f};
    } else {
      scaleLevels = new float[] { 1.0f, wScale, hScale};
    }
    Arrays.sort(scaleLevels);
    minScale = scaleLevels[0];
    maxScale = scaleLevels[scaleLevels.length - 1];

    float tWidth;
    float tHeight;

    switch (scaleType) {
      case Photo.SCALE_TYPE_ORIGIN:
        scale = 1.0f;
        tWidth = dWidth;
        tHeight = dHeight;
        break;
      case Photo.SCALE_TYPE_FIT_WIDTH:
        scale = wScale;
        tWidth = vWidth;
        tHeight = dHeight * wScale;
        break;
      case Photo.SCALE_TYPE_FIT_HEIGHT:
        scale = hScale;
        tWidth = dWidth * hScale;
        tHeight = vHeight;
        break;
      default:
      case Photo.SCALE_TYPE_FIT:
        scale = Math.min(wScale, hScale);
        tWidth = dWidth * scale;
        tHeight = dHeight * scale;
        break;
//      TODO case Photo.SCALE_TYPE_FIXED:
//        break;
    }

    switch (startPosition) {
      default:
      case Photo.START_POSITION_TOP_LEFT:
        offsetX = 0;
        offsetY = 0;
        break;
      case Photo.START_POSITION_TOP_RIGHT:
        offsetX = vWidth - tWidth;
        offsetY = 0;
        break;
      case Photo.START_POSITION_BOTTOM_LEFT:
        offsetX = 0;
        offsetY = vHeight - tHeight;
        break;
      case Photo.START_POSITION_BOTTOM_RIGHT:
        offsetX = vWidth - tWidth;
        offsetY = vHeight - tHeight;
        break;
      case Photo.START_POSITION_CENTER:
        offsetX = (vWidth - tWidth) / 2;
        offsetY = (vHeight - tHeight) / 2;
        break;
    }

    fixOffset();
  }

  private void fixOffset() {
    int vWidth = getBounds().width();
    int vHeight = getBounds().height();
    float tWidth = vDWidth * scale;
    float tHeight = vDHeight * scale;

    if (tWidth > vWidth) {
      offsetX = Utils.clamp(offsetX, vWidth - tWidth, 0);
    } else {
      offsetX = (vWidth - tWidth) / 2;
    }

    if (tHeight > vHeight) {
      offsetY = Utils.clamp(offsetY, vHeight - tHeight, 0);
    } else {
      offsetY = (vHeight - tHeight) / 2;
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (valid && drawable != null) {
      int saved = canvas.save();
      canvas.translate(offsetX, offsetY);
      canvas.scale(scale, scale);
      drawable.draw(canvas);
      canvas.restoreToCount(saved);
    }
  }

  @Override
  public void setAlpha(int alpha) {
    if (drawable != null) {
      drawable.setAlpha(alpha);
    }
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    if (drawable != null) {
      drawable.setColorFilter(colorFilter);
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    invalidateSelf();
  }

  @Override
  public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
    scheduleSelf(what, when);
  }

  @Override
  public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
    unscheduleSelf(what);
  }
}
