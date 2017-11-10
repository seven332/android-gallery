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
import java.util.List;

// offset = -(target - base) = base - target
public class ReversedVerticalScrollLayout extends BaseScrollLayout {

  private int totalTop;
  private int totalBottom;

  @Override
  protected int selfWidthMeasureSpec(int width) {
    return View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
  }

  @Override
  protected int selfHeightMeasureSpec(int height) {
    return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
  }

  protected void measurePage(View view) {
    view.measure(
        widthMeasureSpec,
        ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, view.getLayoutParams().height)
    );
  }

  @Override
  public void layoutAnchor(View page, int offset) {
    measurePage(page);

    int bottom = height - offset;
    int top = bottom - page.getMeasuredHeight();
    page.layout(0, top, width, bottom);

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

    int bottom = totalTop - interval;
    int top = bottom - page.getMeasuredHeight();
    page.layout(0, top, width, bottom);

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
    int top = totalBottom + interval;
    int bottom = top + page.getMeasuredHeight();
    page.layout(0, top, width, bottom);

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
  public int scrollBy(int oldAnchorOffset, int distanceX, int distanceY) {
    return oldAnchorOffset + distanceY;
  }
}
