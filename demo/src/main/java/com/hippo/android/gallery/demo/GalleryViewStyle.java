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

package com.hippo.android.gallery.demo;

/*
 * Created by Hippo on 2017/12/20.
 */

import com.hippo.android.gallery.GalleryView;
import com.hippo.android.gallery.HorizontalPagerLayout;
import com.hippo.android.gallery.HorizontalScrollLayout;
import com.hippo.android.gallery.PagerLayoutManager;
import com.hippo.android.gallery.Photo;
import com.hippo.android.gallery.ReversedHorizontalPagerLayout;
import com.hippo.android.gallery.ReversedHorizontalScrollLayout;
import com.hippo.android.gallery.ScrollLayoutManager;
import com.hippo.android.gallery.VerticalScrollLayout;

public class GalleryViewStyle {

  public static final int LAYOUT_MANAGER_SCROLL = 0;
  public static final int LAYOUT_MANAGER_PAGER = 1;

  public static final int SCROLL_LAYOUT_VERTICAL = 0;
  public static final int SCROLL_LAYOUT_HORIZONTAL = 1;
  public static final int SCROLL_LAYOUT_REVERSED_HORIZONTAL = 2;

  public static final int PAGER_LAYOUT_HORIZONTAL = 0;
  public static final int PAGER_LAYOUT_REVERSED_HORIZONTAL = 1;

  private int layoutManager = LAYOUT_MANAGER_SCROLL;
  private int scrollLayout = SCROLL_LAYOUT_VERTICAL;
  private int pagerLayout = PAGER_LAYOUT_HORIZONTAL;
  private int pageInterval = 0;
  private int scaleType = Photo.SCALE_TYPE_FIT;
  private int startPosition = Photo.START_POSITION_TOP_LEFT;

  private boolean changed = true;

  public boolean isChanged() {
    return changed;
  }

  public int getLayoutManager() {
    return layoutManager;
  }

  public void setLayoutManager(int layoutManager) {
    if (this.layoutManager != layoutManager) {
      this.layoutManager = layoutManager;
      changed = true;
    }
  }

  public int getScrollLayout() {
    return scrollLayout;
  }

  public void setScrollLayout(int scrollLayout) {
    if (this.scrollLayout != scrollLayout) {
      this.scrollLayout = scrollLayout;
      changed = true;
    }
  }

  public int getPagerLayout() {
    return pagerLayout;
  }

  public void setPagerLayout(int pagerLayout) {
    if (this.pagerLayout != pagerLayout) {
      this.pagerLayout = pagerLayout;
      changed = true;
    }
  }

  public int getPageInterval() {
    return pageInterval;
  }

  public void setPageInterval(int pageInterval) {
    if (this.pageInterval != pageInterval) {
      this.pageInterval = pageInterval;
      changed = true;
    }
  }

  public int getScaleType() {
    return scaleType;
  }

  public void setScaleType(int scaleType) {
    if (this.scaleType != scaleType) {
      this.scaleType = scaleType;
      changed = true;
    }
  }

  public int getStartPosition() {
    return startPosition;
  }

  public void setStartPosition(int startPosition) {
    if (this.startPosition != startPosition) {
      this.startPosition = startPosition;
      changed = true;
    }
  }

  public void apply(GalleryView view) {
    if (!isChanged()) return;

    changed = false;
    GalleryView.LayoutManager lm = view.getLayoutManager();
    if (layoutManager == LAYOUT_MANAGER_SCROLL) {
      ScrollLayoutManager slm;
      if (lm instanceof ScrollLayoutManager) {
        slm = (ScrollLayoutManager) lm;
      } else {
        slm = new ScrollLayoutManager();
      }

      switch (scrollLayout) {
        case SCROLL_LAYOUT_VERTICAL:
          slm.setScrollLayout(new VerticalScrollLayout());
          break;
        case SCROLL_LAYOUT_HORIZONTAL:
          slm.setScrollLayout(new HorizontalScrollLayout());
          break;
        case SCROLL_LAYOUT_REVERSED_HORIZONTAL:
          slm.setScrollLayout(new ReversedHorizontalScrollLayout());
          break;
      }
      slm.setPageInterval(pageInterval);
      view.setLayoutManager(slm);
    } else {
      PagerLayoutManager plm;
      if (lm instanceof PagerLayoutManager) {
        plm = (PagerLayoutManager) lm;
      } else {
        plm = new PagerLayoutManager(view.getContext());
      }

      switch (pagerLayout) {
        case PAGER_LAYOUT_HORIZONTAL:
          plm.setPagerLayout(new HorizontalPagerLayout());
          break;
        case PAGER_LAYOUT_REVERSED_HORIZONTAL:
          plm.setPagerLayout(new ReversedHorizontalPagerLayout());
          break;
      }
      plm.setPageInterval(pageInterval);
      plm.setScaleType(scaleType);
      plm.setStartPosition(startPosition);
      view.setLayoutManager(plm);
    }
  }
}
