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
 * Created by Hippo on 2017/11/17.
 */

import android.graphics.Rect;
import android.view.View;

public abstract class BasePagerLayout implements PagerLayoutManager.PagerLayout {

  protected int width;
  protected int height;
  protected int interval;

  private int widthMeasureSpec;
  private int heightMeasureSpec;

  private Rect rect = new Rect();

  @Override
  public void start(int width, int height, int interval) {
    this.width = width;
    this.height = height;
    this.interval = interval;

    this.widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
    this.heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
  }

  private void measure(View view) {
    view.measure(widthMeasureSpec, heightMeasureSpec);
  }

  private void layout(View view, int left, int top, int right, int bottom) {
    view.layout(left, top, right, bottom);
    Utils.updateVisibleRect(view, width, height);
  }

  @Override
  public void layoutPage(View page, float offset, @PagerLayoutManager.Position int position) {
    measure(page);

    Rect rect = this.rect;
    rect.set(0, 0, width, height);
    offsetRect(rect, offset, position);

    layout(page, rect.left, rect.top, rect.right, rect.bottom);
  }

  /**
   * Offset the rectangle.
   *
   * @param rect the rectangle of page's position
   * @param offset the offset of the page,
   *               positive if the page move to next page position,
   *               negative if the page move to previous page position
   * @param position one of {@link PagerLayoutManager#POSITION_PREVIOUS},
   *                 {@link PagerLayoutManager#POSITION_CURRENT} or
   *                 {@link PagerLayoutManager#POSITION_NEXT}
   */
  public abstract void offsetRect(Rect rect, float offset, @PagerLayoutManager.Position int position);
}
