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
 * Created by Hippo on 2018/1/29.
 */

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.hippo.android.gallery.intf.Accurate;
import com.hippo.android.gallery.intf.Clippable;

/**
 * CutDrawable clips a drawable.
 * The size of this drawable is the same as the size of the origin drawable.
 *
 * @see CutDrawable
 * @see CutAccurateDrawable
 */
public class ClipDrawable extends DrawableWrapper implements Clippable {

  private RectF clip = new RectF();
  private boolean drawRectDirty = true;

  private RectF srcRect = new RectF();
  private RectF dstRect = new RectF();

  @Override
  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    super.onSetWrappedDrawable(oldDrawable, newDrawable);
    drawRectDirty = true;
  }

  @Override
  public void clip(float left, float top, float right, float bottom) {
    clip.set(left, top, right, bottom);
    drawRectDirty = true;
    invalidateSelf();
  }

  private void updateDrawRect(Drawable drawable) {
    if (!drawRectDirty) return;
    drawRectDirty = false;

    Rect bounds = getBounds();
    RectF srcRect = this.srcRect;
    RectF dstRect = this.dstRect;

    dstRect.set(bounds);
    if (!dstRect.intersect(clip.left, clip.top, clip.right, clip.bottom)) {
      srcRect.setEmpty();
      dstRect.setEmpty();
      return;
    }

    int width = drawable.getIntrinsicWidth();
    int height = drawable.getIntrinsicHeight();
    float scaleX = (float) width / bounds.width();
    float scaleY = (float) height / bounds.height();
    srcRect.set((dstRect.left - bounds.left) * scaleX,
        (dstRect.top - bounds.top) * scaleY,
        (dstRect.right - bounds.left) * scaleX,
        (dstRect.bottom - bounds.top) * scaleY);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (clip.isEmpty()) return;

    Drawable drawable = getDrawable();
    if (drawable instanceof Accurate) {
      updateDrawRect(drawable);
      if (!srcRect.isEmpty() && !dstRect.isEmpty()) {
        ((Accurate) drawable).draw(canvas, srcRect, dstRect);
      }
    } else if (drawable != null) {
      int saved = canvas.save();
      canvas.clipRect(clip);
      drawable.draw(canvas);
      canvas.restoreToCount(saved);
    }
  }
}
