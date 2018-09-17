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

// offset = target - base
public class VerticalScrollLayout extends BaseScrollLayout {

  private int totalTop;
  private int totalBottom;

  public VerticalScrollLayout() {
    super(true, false);
  }

  @Override
  public void layoutAnchor(View page, float offset, float keep) {
    int oldHeight = page.getHeight();
    measure(page);
    int newHeight = page.getMeasuredHeight();

    int left = Utils.isFlexible(page) ? (int) deviate : 0;
    int right = left + page.getMeasuredWidth();
    int top = (int) offset;
    if (keep > 0 && oldHeight != 0) {
      top += Math.min(keep, page.getHeight()) * (1 - (float) newHeight / (float) oldHeight);
    }
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

    int left = Utils.isFlexible(page) ? (int) deviate : 0;
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

    int left = Utils.isFlexible(page) ? (int) deviate : 0;
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
      Utils.updateClipRegion(page.view, width, height);
    }

    totalTop += offset;
    totalBottom += offset;
  }

  @Override
  public GalleryPage selectAnchor(List<GalleryPage> pages) {
    GalleryPage last = null;

    for (GalleryPage page : pages) {
      last = page;

      View view = page.view;

      if (view.getTop() >= 0) {
        return page;
      }

      if (view.getBottom() >= height / 4) {
        return page;
      }
    }

    return last;
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
  public void scrollBy(float anchorOffset, float pageDeviate, float dx, float dy,
      @Nullable View first, @Nullable View last, float[] result) {
    float newAnchorOffset = anchorOffset + dy;
    float newPageDeviate = pageDeviate + dx;
    float remainDx = 0;
    float remainDy = 0;

    if (first != null) {
      float newTop = first.getTop() + dy;
      if (newTop > 0) {
        remainDy = newTop;
      }
    }

    if (last != null) {
      float newBottom = last.getBottom() + dy;
      float minBottom = first != null ? Math.min(last.getBottom() - first.getTop(), height) : height;
      if (newBottom < minBottom) {
        remainDy = newBottom - minBottom;
      }
    }

    result[0] = newAnchorOffset - remainDy;
    result[1] = newPageDeviate - remainDx;
    result[2] = remainDx;
    result[3] = remainDy;
  }

  @Override
  public void scaleBy(float anchorOffset, float pageDeviate, float x, float y, float factor,
      GalleryView gallery, int anchorIndex, float[] result) {
    int newAnchorIndex = anchorIndex;
    float newAnchorOffset = anchorOffset;
    float newAnchorKeep = 0;

    boolean forward = y > anchorOffset;
    int index = anchorIndex;
    int increment = forward ? 1: -1;
    for (;;) {
      GalleryPage page = gallery.getPageAt(index);
      if (page == null) {
        // Reach the end of attached pages
        break;
      }

      View view = page.view;
      newAnchorIndex = index;
      newAnchorOffset = view.getTop();
      newAnchorKeep = y - view.getTop();

      if (y > view.getTop() - interval && y <= view.getBottom()) {
        // The y is in the range of this view
        break;
      }

      index += increment;
    }

    result[0] = newAnchorIndex;
    result[1] = newAnchorOffset;
    result[2] = newAnchorKeep;
    result[3] = x - ((x - pageDeviate) * factor);
  }
}
