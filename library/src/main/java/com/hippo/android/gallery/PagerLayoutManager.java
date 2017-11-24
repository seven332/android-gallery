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
 * Created by Hippo on 2017/8/28.
 */

import android.support.annotation.IntDef;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class PagerLayoutManager extends GalleryView.LayoutManager {

  // SCALE_MIN MUST BE 1.0f
  private static final float SCALE_MIN = 1.0f;
  private static final float SCALE_MAX = 3.0f;

  public static final float NO_SCALE = 0.0f;
  public static final int NO_OFFSET = Integer.MIN_VALUE;

  @IntDef({POSITION_PREVIOUS, POSITION_CURRENT, POSITION_NEXT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Position {}

  public static final int POSITION_PREVIOUS = 0;
  public static final int POSITION_CURRENT = 1;
  public static final int POSITION_NEXT = 2;

  // Current page index
  private int currentIndex = 0;

  // The interval between pages
  private int pageInterval = 0;

  // The offset of pages
  // From previous to next is positive
  // From next To previous is negative
  private int pageOffset = 0;

  @Photo.ScaleType
  private int scaleType = Photo.SCALE_TYPE_FIT;

  @Photo.StartPosition
  private int startPosition = Photo.START_POSITION_TOP_LEFT;

  private int[] temp = new int[2];

  private PagerLayout pagerLayout;

  public void setPageInterval(int pageInterval) {
    this.pageInterval = pageInterval;
  }

  public void setScaleType(@Photo.ScaleType int scaleType) {
    this.scaleType = scaleType;
  }

  public void setStartPosition(@Photo.StartPosition int startPosition) {
    this.startPosition = startPosition;
  }

  public void setPagerLayout(PagerLayout pagerLayout) {
    this.pagerLayout = pagerLayout;
  }

  @Override
  public void layout(GalleryView.Nest nest, int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }

    // Check page count
    int pageCount = nest.getPageCount();
    if (pageCount == 0) {
      return;
    }

    // Ensure current index in the range
    int newIndex = Utils.clamp(currentIndex, 0, nest.getPageCount() - 1);
    if (currentIndex != newIndex) {
      currentIndex = newIndex;
      pageOffset = 0;
    }

    pagerLayout.start(width, height, pageInterval, scaleType, startPosition);

    // Handle turning page
    int pageRange = pagerLayout.getPageRange();
    // Turn to previous page
    while (pageOffset >= pageRange && currentIndex > 0) {
      currentIndex -= 1;
      pageOffset -= pageRange;
    }
    // Turn to next page
    while (pageOffset <= -pageRange && currentIndex < pageCount - 1) {
      currentIndex += 1;
      pageOffset += pageRange;
    }

    // Fix page offset
    if (currentIndex == 0 && pageOffset > 0) {
      pageOffset = 0;
    } else if (currentIndex == pageCount - 1 && pageOffset < 0) {
      pageOffset = 0;
    }

    // Layout current page
    GalleryView.Page current = nest.pinPage(currentIndex);
    pagerLayout.layoutPage(current.view, pageOffset, POSITION_CURRENT);
    // Layout previous page
    if (currentIndex > 0) {
      GalleryView.Page previous = nest.pinPage(currentIndex - 1);
      pagerLayout.layoutPage(previous.view, pageOffset, POSITION_PREVIOUS);
    }
    // Layout next page
    if (currentIndex < pageCount - 1) {
      GalleryView.Page next = nest.pinPage(currentIndex + 1);
      pagerLayout.layoutPage(next.view, pageOffset, POSITION_NEXT);
    }
  }

  @Override
  public void scrollBy(GalleryView.Nest nest, int dx, int dy) {
    pageOffset = pagerLayout.scrollPage(pageOffset, dx, dy);
    nest.layout(this, nest.getWidth(), nest.getHeight());
  }

  @Override
  public void scaleBy(GalleryView.Nest nest, int x, int y, float factor) {
    // Scale only works when pageOffset == 0
    if (pageOffset != 0) {
      return;
    }


    // TODO
  }

  public interface PagerLayout {

    void start(int width, int height, int interval,
        @Photo.ScaleType int scaleType, @Photo.StartPosition int startPosition);

    int getPageRange();

    void layoutPage(View page, int offset, @Position int position);

    int scrollPage(int offset, int dx, int dy);
  }
}
