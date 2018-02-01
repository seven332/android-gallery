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
import android.widget.ImageView;
import com.hippo.android.gallery.drawable.TransformDrawable;
import com.hippo.android.gallery.intf.Transformable;

public class TransformImageView extends AppCompatImageView implements Transformable {

  private TransformDrawable transformDrawable = new TransformDrawable();

  public TransformImageView(Context context) {
    super(context);
    setScaleType(ImageView.ScaleType.FIT_XY);
    super.setImageDrawable(transformDrawable);
  }

  public TransformImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setScaleType(ImageView.ScaleType.FIT_XY);
    super.setImageDrawable(transformDrawable);
  }

  @Override
  public void setImageDrawable(@Nullable Drawable drawable) {
    Utils.recycle(transformDrawable.getDrawable());
    transformDrawable.setDrawable(drawable);
    requestLayout();
  }

  @Override
  public void scale(float x, float y, float factor, @Nullable float[] remain) {
    transformDrawable.scale(x, y, factor, remain);
  }

  @Override
  public void scroll(float dx, float dy, @Nullable float[] remain) {
    transformDrawable.scroll(dx, dy, remain);
  }

  @Override
  public void setScale(float scale) {
    transformDrawable.setScale(scale);
  }

  @Override
  public void setScaleType(int scaleType) {
    transformDrawable.setScaleType(scaleType);
  }

  @Override
  public void setStartPosition(int startPosition) {
    transformDrawable.setStartPosition(startPosition);
  }
}
