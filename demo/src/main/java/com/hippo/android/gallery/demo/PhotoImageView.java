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
 * Created by Hippo on 2017/11/16.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.hippo.android.gallery.Photo;

public class PhotoImageView extends AppCompatImageView implements Photo {

  private final TransformedDrawable transformedDrawable = new TransformedDrawable(null);

  public PhotoImageView(Context context) {
    this(context, null);
  }

  public PhotoImageView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    setScaleType(ImageView.ScaleType.FIT_XY);
    super.setImageDrawable(transformedDrawable);
  }

  @Override
  public void setImageDrawable(@Nullable Drawable drawable) {
    transformedDrawable.setWrappedDrawable(drawable);
    requestLayout();
  }

  @Override
  public boolean isPhotoEnabled() {
    return true;
  }

  @Override
  public void setVisibleRect(int left, int top, int right, int bottom) {}

  @Override
  public void scale(float x, float y, float factor) {
    transformedDrawable.scale(x, y, factor);
  }

  @Override
  public void setScaleType(int scaleType) {
    transformedDrawable.setScaleType(scaleType);
  }

  @Override
  public void offset(float dx, float dy, float[] remain) {
    transformedDrawable.offset(dx, dy, remain);
  }

  @Override
  public void setStartPosition(int startPosition) {
    transformedDrawable.setStartPosition(startPosition);
  }
}
