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

public abstract class BaseScrollLayout implements ScrollLayoutManager.ScrollLayout {

  protected int width;
  protected int height;
  protected int interval;

  protected int widthMeasureSpec;
  protected int heightMeasureSpec;

  @Override
  public void start(int width, int height, int interval) {
    this.width = width;
    this.height = height;
    this.interval = interval;
    this.widthMeasureSpec = selfWidthMeasureSpec(width);
    this.heightMeasureSpec = selfHeightMeasureSpec(height);
  }

  /**
   * Simulate the WidthMeasureSpec of the GalleryView.
   */
  protected abstract int selfWidthMeasureSpec(int width);

  /**
   * Simulate the HeightMeasureSpec of the GalleryView.
   */
  protected abstract int selfHeightMeasureSpec(int height);
}
