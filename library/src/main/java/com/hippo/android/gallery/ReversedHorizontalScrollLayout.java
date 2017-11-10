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
public class ReversedHorizontalScrollLayout extends BaseScrollLayout {

  private int totalLeft;
  private int totalRight;

  @Override
  protected int selfWidthMeasureSpec(int width) {
    return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
  }

  @Override
  protected int selfHeightMeasureSpec(int height) {
    return View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
  }

  protected void measurePage(View view) {
    view.measure(
        ViewGroup.getChildMeasureSpec(widthMeasureSpec, 0, view.getLayoutParams().width),
        heightMeasureSpec
    );
  }

  @Override
  public void layoutAnchor(View page, int offset) {
    measurePage(page);

    int right = width - offset;
    int left = right - page.getMeasuredWidth();
    page.layout(left, 0, right, height);

    totalLeft = left;
    totalRight = right;
  }

  @Override
  public boolean canLayoutNext(View last, int offset) {
    return last.getRight() > -offset;
  }

  @Override
  public void layoutNext(View page) {
    measurePage(page);

    int right = totalLeft - interval;
    int left = right - page.getMeasuredWidth();
    page.layout(left, 0, right, height);

    totalLeft = left;
  }

  @Override
  public int getNextBlank(View last) {
    return Math.min(0, -last.getLeft());
  }

  @Override
  public boolean canLayoutPrevious(View first, int offset) {
    return first.getLeft() < width - offset;
  }

  @Override
  public void layoutPrevious(View page) {
    measurePage(page);
    int left = totalRight + interval;
    int right = left + page.getMeasuredWidth();
    page.layout(left, 0, right, height);

    totalRight = right;
  }

  @Override
  public int getPreviousOffset(View first) {
    return width - first.getRight();
  }

  @Override
  public void offsetPages(List<GalleryView.Page> pages, int offset) {
    int actualOffset = -offset;

    for (GalleryView.Page page : pages) {
      page.view.offsetLeftAndRight(actualOffset);
    }

    totalLeft += actualOffset;
    totalRight += actualOffset;
  }

  @Override
  public boolean canBeAnchor(View page) {
    return Math.max(0, page.getLeft()) < Math.min(width, page.getRight());
  }

  @Override
  public int getAnchorOffset(View anchor) {
    return width - anchor.getRight();
  }

  @Override
  public void resetLayoutState(View view) {
    totalLeft = view.getLeft();
    totalRight = view.getRight();
  }

  @Override
  public int scrollBy(int oldAnchorOffset, int distanceX, int distanceY) {
    return oldAnchorOffset + distanceX;
  }
}
