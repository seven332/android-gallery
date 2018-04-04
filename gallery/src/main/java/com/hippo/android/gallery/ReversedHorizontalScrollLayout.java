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

import android.support.annotation.Nullable;
import android.view.View;
import java.util.List;

// offset = -(target - base) = base - target
public class ReversedHorizontalScrollLayout extends BaseScrollLayout {

  private int totalLeft;
  private int totalRight;

  public ReversedHorizontalScrollLayout() {
    super(false, true);
  }

  @Override
  public void layoutAnchor(View page, float offset) {
    measure(page);

    int top = Utils.isFlexible(page) ? (int) deviate : 0;
    int bottom = top + page.getMeasuredHeight();
    int right = (int) (width - offset);
    int left = right - page.getMeasuredWidth();
    layout(page, left, top, right, bottom);

    totalLeft = left;
    totalRight = right;
  }

  @Override
  public boolean canLayoutNext(View last, float offset) {
    return last.getRight() > -offset;
  }

  @Override
  public void layoutNext(View page) {
    measure(page);

    int top = Utils.isFlexible(page) ? (int) deviate : 0;
    int bottom = top + page.getMeasuredHeight();
    int right = totalLeft - interval;
    int left = right - page.getMeasuredWidth();
    layout(page, left, top, right, bottom);

    totalLeft = left;
  }

  @Override
  public int getNextBlank(View last) {
    return Math.min(0, -last.getLeft());
  }

  @Override
  public boolean canLayoutPrevious(View first, float offset) {
    return first.getLeft() < width - offset;
  }

  @Override
  public void layoutPrevious(View page) {
    measure(page);

    int top = Utils.isFlexible(page) ? (int) deviate : 0;
    int bottom = top + page.getMeasuredHeight();
    int left = totalRight + interval;
    int right = left + page.getMeasuredWidth();
    layout(page, left, top, right, bottom);

    totalRight = right;
  }

  @Override
  public int getPreviousOffset(View first) {
    return width - first.getRight();
  }

  @Override
  public void offsetPages(List<GalleryPage> pages, int offset) {
    int actualOffset = -offset;

    for (GalleryPage page : pages) {
      page.view.offsetLeftAndRight(actualOffset);
      Utils.updateClipRegion(page.view, width, height);
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
  public void scrollBy(float anchorOffset, float pageDeviate, float dx, float dy,
      @Nullable View first, @Nullable View last, float[] result) {
    float newAnchorOffset = anchorOffset - dx;
    float newPageDeviate = pageDeviate + dy;
    float remainDx = 0;
    float remainDy = 0;

    if (first != null) {
      float newRight = first.getRight() + dx;
      if (newRight < width) {
        remainDx = newRight - width;
      }
    }

    if (last != null) {
      float newLeft = last.getLeft() + dx;
      float maxLeft = first != null ? Math.min(width - (first.getRight() - last.getLeft()), 0) : 0;
      if (newLeft > maxLeft) {
        remainDx = newLeft - maxLeft;
      }
    }

    result[0] = newAnchorOffset + remainDx;
    result[1] = newPageDeviate - remainDy;
    result[2] = remainDx;
    result[3] = remainDy;
  }

  @Override
  public void scaleBy(float anchorOffset, float pageDeviate, float x, float y, float factor, float[] result) {
    result[0] = (width - x) - (((width - x) - anchorOffset) * factor);
    result[1] = y - ((y - pageDeviate) * factor);
  }
}
