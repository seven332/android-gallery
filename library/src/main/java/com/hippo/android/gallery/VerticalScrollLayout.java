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
public class VerticalScrollLayout extends BaseScrollLayout {

  private int totalTop;
  private int totalBottom;

  public VerticalScrollLayout() {
    super(true, false);
  }

  @Override
  public void layoutAnchor(View page, float offset) {
    measure(page);

    int left = Utils.asPhoto(page) != null ? (int) deviate : 0;
    int right = left + page.getMeasuredWidth();
    int top = (int) offset;
    int bottom = top + page.getMeasuredHeight();
    layout(page, left, top, right, bottom);

    totalTop = top;
    totalBottom = bottom;
  }

  @Override
  public boolean canLayoutNext(View last, float offset) {
    return last.getTop() < height + offset;
  }

  @Override
  public void layoutNext(View page) {
    measure(page);

    int left = Utils.asPhoto(page) != null ? (int) deviate : 0;
    int right = left + page.getMeasuredWidth();
    int top = totalBottom + interval;
    int bottom = top + page.getMeasuredHeight();
    layout(page, left, top, right, bottom);

    totalBottom = bottom;
  }

  @Override
  public int getNextBlank(View last) {
    return Math.min(0, last.getBottom() - height);
  }

  @Override
  public boolean canLayoutPrevious(View first, float offset) {
    return first.getBottom() > offset;
  }

  @Override
  public void layoutPrevious(View page) {
    measure(page);

    int left = Utils.asPhoto(page) != null ? (int) deviate : 0;
    int right = left + page.getMeasuredWidth();
    int bottom = totalTop - interval;
    int top = bottom - page.getMeasuredHeight();
    layout(page, left, top, right, bottom);

    totalTop = top;
  }

  @Override
  public int getPreviousOffset(View first) {
    return first.getTop();
  }

  @Override
  public void offsetPages(List<GalleryPage> pages, int offset) {
    for (GalleryPage page : pages) {
      page.view.offsetTopAndBottom(offset);
    }

    totalTop += offset;
    totalBottom += offset;
  }

  @Override
  public boolean canBeAnchor(View page) {
    return Math.max(0, page.getTop()) < Math.min(height, page.getBottom());
  }

  @Override
  public int getAnchorOffset(View anchor) {
    return anchor.getTop();
  }

  @Override
  public void resetLayoutState(View view) {
    totalTop = view.getTop();
    totalBottom = view.getBottom();
  }

  @Override
  public void scrollBy(float anchorOffset, float pageDeviate, float dx, float dy, float[] result) {
    result[0] = anchorOffset + dy;
    result[1] = pageDeviate + dx;
  }

  @Override
  public void scaleBy(float anchorOffset, float pageDeviate, float x, float y, float factor, float[] result) {
    result[0] = y - ((y - anchorOffset) * factor);
    result[1] = x - ((x - pageDeviate) * factor);
  }
}
