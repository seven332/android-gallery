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
import java.util.List;

// offset = -(target - base) = base - target
public class ReversedVerticalScrollLayout extends BaseScrollLayout {

  private int totalTop;
  private int totalBottom;

  protected void measurePage(View view) {
    int widthMeasureSpec = isScalable(view)
        ? View.MeasureSpec.makeMeasureSpec((int) (width * scale), View.MeasureSpec.EXACTLY)
        : View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
    int heightMeasureSpec = getPageMeasureSpec(height, view.getLayoutParams().height);
    view.measure(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  public void layoutAnchor(View page, int offset) {
    measurePage(page);

    int deviate = isScalable(page) ? this.deviate : 0;
    int right = deviate + page.getMeasuredWidth();
    int bottom = height - offset;
    int top = bottom - page.getMeasuredHeight();
    page.layout(deviate, top, right, bottom);

    totalTop = top;
    totalBottom = bottom;
  }

  @Override
  public boolean canLayoutNext(View last, int offset) {
    return last.getBottom() > -offset;
  }

  @Override
  public void layoutNext(View page) {
    measurePage(page);

    int deviate = isScalable(page) ? this.deviate : 0;
    int right = deviate + page.getMeasuredWidth();
    int bottom = totalTop - interval;
    int top = bottom - page.getMeasuredHeight();
    page.layout(deviate, top, right, bottom);

    totalTop = top;
  }

  @Override
  public int getNextBlank(View last) {
    return Math.min(0, -last.getTop());
  }

  @Override
  public boolean canLayoutPrevious(View first, int offset) {
    return first.getTop() < height - offset;
  }

  @Override
  public void layoutPrevious(View page) {
    measurePage(page);

    int deviate = isScalable(page) ? this.deviate : 0;
    int right = deviate + page.getMeasuredWidth();
    int top = totalBottom + interval;
    int bottom = top + page.getMeasuredHeight();
    page.layout(deviate, top, right, bottom);

    totalBottom = bottom;
  }

  @Override
  public int getPreviousOffset(View first) {
    return height - first.getBottom();
  }

  @Override
  public void offsetPages(List<GalleryView.Page> pages, int offset) {
    int actualOffset = -offset;

    for (GalleryView.Page page : pages) {
      page.view.offsetTopAndBottom(actualOffset);
    }

    totalTop += actualOffset;
    totalBottom += actualOffset;
  }

  @Override
  public boolean canBeAnchor(View page) {
    return Math.max(0, page.getTop()) < Math.min(height, page.getBottom());
  }

  @Override
  public int getAnchorOffset(View anchor) {
    return height - anchor.getBottom();
  }

  @Override
  public void resetLayoutState(View view) {
    totalTop = view.getTop();
    totalBottom = view.getBottom();
  }

  @Override
  public void scrollBy(int dx, int dy, int[] result) {
    result[0] = dy;
    result[1] = -dx;
  }

  @Override
  public void scaleBy(int anchorOffset, int pageDeviate, int x, int y, float factor, int[] result) {
    result[0] = (height - y) - (int) (((height - y) - anchorOffset) * factor);
    result[1] = x - (int) ((x - pageDeviate) * factor);
  }
}
