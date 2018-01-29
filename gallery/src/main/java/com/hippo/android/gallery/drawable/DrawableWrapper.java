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
 * Created by Hippo on 2018/1/25.
 */

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

/**
 * Drawable container with only one child element.
 */
public class DrawableWrapper extends Drawable implements Drawable.Callback {

  @Nullable
  private Drawable drawable;

  public final void setDrawable(@Nullable Drawable drawable) {
    if (this.drawable == drawable) {
      return;
    }

    if (this.drawable != null) {
      this.drawable.setCallback(null);
    }
    if (drawable != null) {
      drawable.setCallback(this);
    }

    this.drawable = drawable;
    onSetWrappedDrawable(this.drawable, drawable);

    invalidateSelf();
  }

  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    if (newDrawable != null) {
      newDrawable.setBounds(getBounds());
    }
  }

  @Nullable
  public Drawable getDrawable() {
    return drawable;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (drawable != null) {
      drawable.draw(canvas);
    }
  }

  @Override
  public boolean getPadding(@NonNull Rect padding) {
    return drawable != null && drawable.getPadding(padding);
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void setHotspot(float x, float y) {
    if (drawable != null) {
      drawable.setHotspot(x, y);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void setHotspotBounds(int left, int top, int right, int bottom) {
    if (drawable != null) {
      drawable.setHotspotBounds(left, top, right, bottom);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public void getHotspotBounds(@NonNull Rect outRect) {
    if (drawable != null) {
      drawable.getHotspotBounds(outRect);
    } else {
      outRect.set(getBounds());
    }
  }

  @Override
  public boolean setVisible(boolean visible, boolean restart) {
    final boolean superChanged = super.setVisible(visible, restart);
    final boolean changed = drawable != null && drawable.setVisible(visible, restart);
    return superChanged | changed;
  }

  @Override
  public void setAlpha(int alpha) {
    if (drawable != null) {
      drawable.setAlpha(alpha);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.KITKAT)
  @Override
  public int getAlpha() {
    return drawable != null ? drawable.getAlpha() : 255;
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    if (drawable != null) {
      drawable.setColorFilter(colorFilter);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void setTintList(@Nullable ColorStateList tint) {
    if (drawable != null) {
      drawable.setTintList(tint);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
    if (drawable != null) {
      drawable.setTintMode(tintMode);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.M)
  @Override
  public boolean onLayoutDirectionChanged(int layoutDirection) {
    return drawable != null && drawable.setLayoutDirection(layoutDirection);
  }

  @Override
  public int getOpacity() {
    return drawable != null ? drawable.getOpacity() : PixelFormat.TRANSPARENT;
  }

  @Override
  public boolean isStateful() {
    return drawable != null && drawable.isStateful();
  }

  @Override
  protected boolean onStateChange(int[] state) {
    if (drawable != null && drawable.isStateful()) {
      final boolean changed = drawable.setState(state);
      if (changed) {
        onBoundsChange(getBounds());
      }
      return changed;
    }
    return false;
  }

  @Override
  protected boolean onLevelChange(int level) {
    return drawable != null && drawable.setLevel(level);
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    if (drawable != null) {
      drawable.setBounds(bounds);
    }
  }

  @Override
  public int getIntrinsicWidth() {
    return drawable != null ? drawable.getIntrinsicWidth() : -1;
  }

  @Override
  public int getIntrinsicHeight() {
    return drawable != null ? drawable.getIntrinsicHeight() : -1;
  }

  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
  @Override
  public void getOutline(@NonNull Outline outline) {
    if (drawable != null) {
      drawable.getOutline(outline);
    } else {
      super.getOutline(outline);
    }
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
