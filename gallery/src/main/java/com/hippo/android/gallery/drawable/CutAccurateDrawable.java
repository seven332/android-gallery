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
 * Created by Hippo on 2018/1/28.
 */

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.hippo.android.gallery.intf.Accurate;

public class CutAccurateDrawable extends CutDrawable implements Accurate {

  private RectF rectF1 = new RectF();

  @Override
  public void onSetWrappedDrawable(@Nullable Drawable oldDrawable, @Nullable Drawable newDrawable) {
    super.onSetWrappedDrawable(oldDrawable, newDrawable);
    if (newDrawable != null && !(newDrawable instanceof Accurate)) {
      throw new IllegalArgumentException("CutAccurateDrawable only accepts Accurate");
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas, @NonNull RectF src, @NonNull RectF dst) {
    Drawable drawable = getDrawable();
    Rect cut = getCut();
    if (drawable != null && !cut.isEmpty()) {
      RectF source = rectF1;
      source.set(src);
      source.offset(cut.left, cut.top);
      ((Accurate) drawable).draw(canvas, source, dst);
    }
  }
}
