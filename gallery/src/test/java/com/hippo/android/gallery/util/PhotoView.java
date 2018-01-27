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

package com.hippo.android.gallery.util;

/*
 * Created by Hippo on 2018/1/7.
 */

import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import com.hippo.android.gallery.Photo;

public class PhotoView extends View implements Photo {

  private Rect visibleRect = new Rect();

  public PhotoView(Context context) {
    super(context);
  }

  public Rect getVisibleRect() {
    return visibleRect;
  }

  @Override
  public boolean isPhotoEnabled() {
    return true;
  }

  @Override
  public void setVisibleRect(int left, int top, int right, int bottom) {
    visibleRect.set(left, top, right, bottom);
  }

  @Override
  public void scale(float x, float y, float factor) {}

  @Override
  public void setScaleType(int scaleType) {}

  @Override
  public void offset(float dx, float dy, float[] remain) {}

  @Override
  public void setStartPosition(int startPosition) {}
}
