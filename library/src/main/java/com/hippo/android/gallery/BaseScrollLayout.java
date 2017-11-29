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

package com.hippo.android.gallery;

/*
 * Created by Hippo on 2017/11/10.
 */

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseScrollLayout implements ScrollLayoutManager.ScrollLayout {

  protected int width;
  protected int height;
  protected float scale;
  protected float deviate;
  protected int interval;

  private boolean widthFixed;
  private boolean heightFixed;

  private Rect temp = new Rect();

  public BaseScrollLayout(boolean widthFixed, boolean heightFixed) {
    this.widthFixed = widthFixed;
    this.heightFixed = heightFixed;
  }

  @Override
  public void start(int width, int height, float scale, float deviate, int interval) {
    this.width = width;
    this.height = height;
    this.scale = scale;
    this.deviate = deviate;
    this.interval = interval;
  }

  private static int getPageMeasureSpec(int parentSize, int childDimension, boolean fixed) {
    if (fixed) {
      return View.MeasureSpec.makeMeasureSpec(parentSize, View.MeasureSpec.EXACTLY);
    } else {
      int resultSize;
      int resultMode;

      if (childDimension == ViewGroup.LayoutParams.MATCH_PARENT) {
        resultSize = parentSize;
        resultMode = View.MeasureSpec.EXACTLY;
      } else if (childDimension == ViewGroup.LayoutParams.WRAP_CONTENT) {
        resultSize = 0;
        resultMode = View.MeasureSpec.UNSPECIFIED;
      } else {
        resultSize = childDimension;
        resultMode = View.MeasureSpec.EXACTLY;
      }

      return View.MeasureSpec.makeMeasureSpec(resultSize, resultMode);
    }
  }

  protected void measure(View view) {
    float scale = Utils.asPhoto(view) != null ? this.scale : 1.0f;
    ViewGroup.LayoutParams lp = view.getLayoutParams();
    int widthMeasureSpec = getPageMeasureSpec(Utils.toPixelSize(width * scale), lp.width, widthFixed);
    int heightMeasureSpec = getPageMeasureSpec(Utils.toPixelSize(height * scale), lp.height, heightFixed);
    view.measure(widthMeasureSpec, heightMeasureSpec);
  }

  protected void layout(View view, int left, int top, int right, int bottom) {
    view.layout(left, top, right, bottom);

    Photo photo = Utils.asPhoto(view);
    if (photo != null) {
      Rect rect = temp;
      rect.left = -left;
      rect.top = -top;
      rect.right = width - left;
      rect.bottom = height - top;

      if (!rect.intersect(0, 0, right - left, bottom - top)) {
        rect.setEmpty();
      }

      photo.setVisibleRect(rect.left, rect.top, rect.right, rect.bottom);
    }
  }
}
