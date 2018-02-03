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

package com.hippo.android.gallery.demo;

/*
 * Created by Hippo on 2018/2/3.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class DrawableView extends View implements Drawable.Callback {

  private Drawable drawable;
  private int drawableWidth = -1;
  private int drawableHeight = -1;

  public DrawableView(Context context) {
    super(context);
  }

  public DrawableView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setDrawable(Drawable drawable) {
    if (this.drawable == drawable) return;

    if (this.drawable != null) {
      this.drawable.setCallback(null);
      unscheduleDrawable(this.drawable);
    }

    this.drawable = drawable;

    int oldDrawableWidth = drawableWidth;
    int oldDrawableHeight = drawableHeight;

    if (drawable != null) {
      drawable.setCallback(this);
      drawable.setBounds(0, 0, getWidth(), getHeight());
      drawableWidth = drawable.getIntrinsicWidth();
      drawableHeight = drawable.getIntrinsicHeight();
    } else {
      drawableWidth = -1;
      drawableHeight = -1;
    }

    if (drawableWidth != oldDrawableWidth || drawableHeight != oldDrawableHeight) {
      requestLayout();
    } else {
      invalidate();
    }
  }

  public Drawable getDrawable() {
    return drawable;
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (drawable != null) {
      drawable.setBounds(0, 0, w, h);
    }
  }

  @Override
  public void invalidateDrawable(@NonNull Drawable who) {
    if (who == drawable) {
      int oldDrawableWidth = drawableWidth;
      int oldDrawableHeight = drawableHeight;
      drawableWidth = who.getIntrinsicWidth();
      drawableHeight = who.getIntrinsicHeight();

      if (drawableWidth != oldDrawableWidth || drawableHeight != oldDrawableHeight) {
        requestLayout();
      } else {
        invalidate();
      }
    } else {
      super.invalidateDrawable(who);
    }
  }

  private int measureNoAspect(int specMode, int specSize, int drawableSize) {
    switch (specMode) {
      case MeasureSpec.EXACTLY:
        return specSize;
      case MeasureSpec.AT_MOST:
        return drawableSize <= 0 ? specSize : Math.min(specSize, drawableSize);
      default:
      case MeasureSpec.UNSPECIFIED:
        return drawableSize <= 0 ? 0 : drawableSize;
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

    int width, height;
    if (drawable == null) {
      width = widthSpecMode == MeasureSpec.EXACTLY ? widthSpecSize : 0;
      height = heightSpecMode == MeasureSpec.EXACTLY ? heightSpecSize : 0;
    } else if (drawableWidth <= 0 || drawableHeight <= 0) {
      // No aspect
      width = measureNoAspect(widthSpecMode, widthSpecSize, drawableWidth);
      height = measureNoAspect(heightSpecMode, heightSpecSize, drawableHeight);
    } else {
      width = widthSpecMode == MeasureSpec.UNSPECIFIED ? drawableWidth : widthSpecSize;
      switch (heightSpecMode) {
        case MeasureSpec.EXACTLY:
          height = heightSpecSize;
          break;
        default:
        case MeasureSpec.AT_MOST:
        case MeasureSpec.UNSPECIFIED:
          float aspect = (float) drawableWidth / (float) drawableHeight;
          height = (int) (width / aspect);
          if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
            height = heightSpecSize;
            if (widthSpecMode != MeasureSpec.EXACTLY) {
              width = (int) (height * aspect);
            }
          }
          break;
      }
    }
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (drawable != null) {
      drawable.draw(canvas);
    }
  }
}
