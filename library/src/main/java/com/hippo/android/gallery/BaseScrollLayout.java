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

import android.view.View;
import android.view.ViewGroup;

public abstract class BaseScrollLayout implements ScrollLayoutManager.ScrollLayout {

  protected int width;
  protected int height;
  protected float scale;
  protected int deviate;
  protected int interval;

  @Override
  public void start(int width, int height, float scale, int deviate, int interval) {
    this.width = width;
    this.height = height;
    this.scale = scale;
    this.deviate = deviate;
    this.interval = interval;
  }

  protected static boolean isScalable(View view) {
    return view instanceof Scalable && ((Scalable) view).isScalable();
  }

  protected static int getPageMeasureSpec(int parentSize, int childSize) {
    int resultSize;
    int resultMode;

    if (childSize == ViewGroup.LayoutParams.MATCH_PARENT) {
      resultSize = parentSize;
      resultMode = View.MeasureSpec.EXACTLY;
    } else if (childSize == ViewGroup.LayoutParams.WRAP_CONTENT) {
      resultSize = 0;
      resultMode = View.MeasureSpec.UNSPECIFIED;
    } else {
      resultSize = childSize;
      resultMode = View.MeasureSpec.EXACTLY;
    }

    return View.MeasureSpec.makeMeasureSpec(resultSize, resultMode);
  }
}
