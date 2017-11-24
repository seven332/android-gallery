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
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import com.hippo.android.gallery.Photo;

public class PhotoImageView extends AppCompatImageView implements Photo {

  public PhotoImageView(Context context) {
    super(context);
  }

  public PhotoImageView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean isPhotoEnabled() {
    // TODO
    return true;
  }

  @Override
  public void setVisibleRect(float left, float top, float right, float bottom) {
    // TODO
    Log.d("TAG", left + "-" + top + "-" + right + "-" + bottom);
  }

  @Override
  public void scale(float x, float y, float factor) {
    // TODO
  }

  @Override
  public void setScaleType(int scaleType) {
    // TODO
  }

  @Override
  public void offset(float dx, float dy) {
    // TODO
  }

  @Override
  public void setStartPosition(int startPosition) {
    // TODO
  }
}
