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

  public int layoutManager = LAYOUT_MANAGER_SCROLL;
  public int scrollLayout = SCROLL_LAYOUT_VERTICAL;
  public int pagerLayout = PAGER_LAYOUT_HORIZONTAL;
  public int pageInterval = 0;
  public int scaleType = Photo.SCALE_TYPE_FIT;
  public int startPosition = Photo.START_POSITION_TOP_LEFT;

  public void apply(GalleryView view) {
    if (layoutManager == LAYOUT_MANAGER_SCROLL) {
      ScrollLayoutManager slm = new ScrollLayoutManager();
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
      PagerLayoutManager plm = new PagerLayoutManager(view.getContext());
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
