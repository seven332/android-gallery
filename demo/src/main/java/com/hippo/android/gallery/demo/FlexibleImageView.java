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
 * Created by Hippo on 2018/1/29.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import com.hippo.android.gallery.drawable.ClipDrawable;
import com.hippo.android.gallery.intf.Clippable;
import com.hippo.android.gallery.intf.Flexible;

public class FlexibleImageView extends AppCompatImageView implements Flexible, Clippable {

  private ClipDrawable clipDrawable = new ClipDrawable();

  public FlexibleImageView(Context context) {
    super(context);
    setScaleType(ScaleType.FIT_XY);
    super.setImageDrawable(clipDrawable);
  }

  public FlexibleImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setScaleType(ScaleType.FIT_XY);
    super.setImageDrawable(clipDrawable);
  }

  @Override
  public void setImageDrawable(@Nullable Drawable drawable) {
    Utils.recycle(clipDrawable.getDrawable());
    clipDrawable.setDrawable(drawable);
    requestLayout();
  }

  @Override
  public boolean isFlexible() {
    return true;
  }

  @Override
  public void clip(float left, float top, float right, float bottom) {
    clipDrawable.clip(left, top, right, bottom);
  }
}
