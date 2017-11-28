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

// offset = target - base
public class HorizontalScrollLayout extends BaseScrollLayout {

  private int totalLeft;
  private int totalRight;

  public HorizontalScrollLayout() {
    super(false, true);
  }

  @Override
  public void layoutAnchor(View page, float offset) {
    measure(page);

    int top = Utils.asPhoto(page) != null ? (int) this.deviate : 0;
    int bottom = top + page.getMeasuredHeight();
    int left = (int) offset;
    int right = left + page.getMeasuredWidth();
    layout(page, left, top, right, bottom);

    totalLeft = left;
    totalRight = right;
  }

  @Override
  public boolean canLayoutNext(View last, float offset) {
    return last.getLeft() < width + offset;
  }

  @Override
  public void layoutNext(View page) {
    measure(page);

    int top = Utils.asPhoto(page) != null ? (int) this.deviate : 0;
    int bottom = top + page.getMeasuredHeight();
    int left = totalRight + interval;
    int right = left + page.getMeasuredWidth();
    layout(page, left, top, right, bottom);

    totalRight = right;
  }

  @Override
  public int getNextBlank(View last) {
    return Math.min(0, last.getRight() - width);
  }

  @Override
  public boolean canLayoutPrevious(View first, float offset) {
    return first.getRight() > offset;
  }

  @Override
  public void layoutPrevious(View page) {
    measure(page);

    int top = Utils.asPhoto(page) != null ? (int) this.deviate : 0;
    int bottom = top + page.getMeasuredHeight();
    int right = totalLeft - interval;
    int left = right - page.getMeasuredWidth();
    layout(page, left, top, right, bottom);

    totalLeft = left;
  }

  @Override
  public int getPreviousOffset(View first) {
    return first.getLeft();
  }

  @Override
  public void offsetPages(List<GalleryView.Page> pages, int offset) {
    for (GalleryView.Page page : pages) {
      page.view.offsetLeftAndRight(offset);
    }

    totalLeft += offset;
    totalRight += offset;
  }

  @Override
  public boolean canBeAnchor(View page) {
    return Math.max(0, page.getLeft()) < Math.min(width, page.getRight());
  }

  @Override
  public int getAnchorOffset(View anchor) {
    return anchor.getLeft();
  }

  @Override
  public void resetLayoutState(View view) {
    totalLeft = view.getLeft();
    totalRight = view.getRight();
  }

  @Override
  public void scrollBy(float anchorOffset, float pageDeviate, float dx, float dy, float[] result) {
    result[0] = anchorOffset + dx;
    result[1] = pageDeviate + dy;
  }

  @Override
  public void scaleBy(float anchorOffset, float pageDeviate, float x, float y, float factor, float[] result) {
    result[0] = x - ((x - anchorOffset) * factor);
    result[1] = y - ((y - pageDeviate) * factor);
  }
}
