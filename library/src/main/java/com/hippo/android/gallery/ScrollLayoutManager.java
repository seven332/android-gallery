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

import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import java.util.LinkedList;

public class ScrollLayoutManager extends GalleryView.LayoutManager {


  // First shown page index
  private int currentIndex = 0;

  // First shown page offset
  private int currentOffset = -5000;

  // The interval between pages
  private int pageInterval = 0;

  private PageLayout pageLayout;

  public void setPageInterval(int pageInterval) {
    this.pageInterval = pageInterval;
  }

  public void setPageLayout(PageLayout pagelayout) {
    this.pageLayout = pagelayout;
  }

  @Override
  public void layout(GalleryView.Nest nest, int width, int height) {
    // Check page count
    int pageCount = nest.getPageCount();
    if (pageCount == 0) {
      return;
    }

    pageLayout.start(width, height, pageInterval);

    // Ensure current index in the range.
    int newIndex = Utils.clamp(currentIndex, 0, nest.getPageCount() - 1);
    if (currentIndex != newIndex) {
      currentIndex = newIndex;
      currentIndex = 0;
    }

    // Layout first visible page
    pageLayout.layoutCurrent(nest.pinPage(currentIndex), currentOffset);

    // Layout next pages
    int index = currentIndex;
    while (++index < pageCount && pageLayout.canLayoutNext()) {
      pageLayout.layoutNext(nest.pinPage(index));
    }
    int mostNextIndex = index - 1;

    // Layout previous pages
    int nextLess = pageLayout.getNextLess();
    int positiveNextLess = Math.max(0, nextLess);
    index = currentIndex;
    while (--index >= 0 && pageLayout.canLayoutPrevious(positiveNextLess)) {
      pageLayout.layoutPrevious(nest.pinPage(index));
    }

    // Adjust pages position
    int previousLess = pageLayout.getPreviousLess();
    if (previousLess > 0) {
      // Has previous blank, fill it
      pageLayout.offsetPages(-previousLess);
    } else if (nextLess > 0) {
      // Has next blank, fill it
      pageLayout.offsetPages(Math.min(nextLess, -previousLess));
    }

    // Continue layout next pages
    index = mostNextIndex;
    while (++index < pageCount && pageLayout.canLayoutNext()) {
      pageLayout.layoutNext(nest.pinPage(index));
    }

    // TODO Remove

    GalleryView.Page currentPage = pageLayout.end();
    if (currentPage != null) {
      currentIndex = currentPage.getIndex();
      currentOffset = pageLayout.getPageOffset(currentPage);
    } else {
      Log.e("ScrollLayoutManager", "Can't find current page");
    }
  }

  @Override
  public void scroll(GalleryView.Nest nest, int distanceX, int distanceY) {
    currentOffset = pageLayout.applyScroll(currentOffset, distanceX, distanceY);
    nest.layout(this, nest.getWidth(), nest.getHeight());
  }

  /**
   * TODO
   */
  public static abstract class PageLayout {

    /**
     * Starts new layout turn.
     */
    public abstract void start(int width, int height, int interval);

    /**
     * Ends this layout turn, return new current page.
     */
    @Nullable
    public abstract GalleryView.Page end();

    /**
     * Get the page offset.
     */
    public abstract int getPageOffset(GalleryView.Page page);

    /**
     * Layout current page.
     */
    public abstract void layoutCurrent(GalleryView.Page page, int offset);

    /**
     * Return {@code true} if {@link #layoutNext(GalleryView.Page)} should still be called.
     */
    public abstract boolean canLayoutNext();

    /**
     * Layout the page as next page.
     */
    public abstract void layoutNext(GalleryView.Page page);

    public abstract int getNextLess();

    /**
     * Return {@code true} if {@link #layoutPrevious(GalleryView.Page)} should still be called.
     */
    public abstract boolean canLayoutPrevious(int nextLess);

    /**
     * Layout the page as previous page.
     */
    public abstract void layoutPrevious(GalleryView.Page page);

    public abstract int getPreviousLess();

    /**
     * Offset all pages.
     * Positive: previous to next.
     * Negative: next to previous.
     */
    public abstract void offsetPages(int offset);

    public abstract void removeDumpPages();

    /**
     * TODO
     */
    public abstract int applyScroll(int currentOffset, int distanceX, int distanceY);
  }

  /**
   * TODO
   */
  public static class VerticallyPageLayout extends PageLayout {

    private int width;
    private int height;
    private int interval;

    private int widthMeasureSpec;
    private int heightMeasureSpec;

    private int totalTop;
    private int totalBottom;

    private LinkedList<GalleryView.Page> pages = new LinkedList<>();

    @Override
    public void start(int width, int height, int interval) {
      this.width = width;
      this.height = height;
      this.interval = interval;
      this.widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
      this.heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    @Override
    public GalleryView.Page end() {
      GalleryView.Page currentPage = null;
      for (GalleryView.Page page : pages) {
        if (page.view.getBottom() > 0) {
          currentPage = page;
          break;
        }
      }

      pages.clear();

      return currentPage;
    }

    @Override
    public int getPageOffset(GalleryView.Page page) {
      return page.view.getTop();
    }

    public void measureView(View view) {
      ViewGroup.LayoutParams lp = view.getLayoutParams();
      view.measure(widthMeasureSpec, ViewGroup.getChildMeasureSpec(heightMeasureSpec, 0, lp.height));
    }

    @Override
    public void layoutCurrent(GalleryView.Page page, int offset) {
      pages.add(page);

      View view = page.view;
      measureView(view);
      view.layout(0, offset, width, offset + view.getMeasuredHeight());

      totalTop = offset;
      totalBottom = offset + view.getMeasuredHeight();
    }

    @Override
    public boolean canLayoutNext() {
      return pages.getLast().view.getTop() < height;
    }

    @Override
    public void layoutNext(GalleryView.Page page) {
      pages.addLast(page);

      View view = page.view;
      measureView(view);
      int top = totalBottom + interval;
      int bottom = top + view.getMeasuredHeight();
      view.layout(0, top, width, bottom);

      totalBottom = bottom;
    }

    @Override
    public int getNextLess() {
      return height - pages.getLast().view.getBottom();
    }

    @Override
    public boolean canLayoutPrevious(int nextLess) {
      return pages.getFirst().view.getBottom() > -nextLess;
    }

    @Override
    public void layoutPrevious(GalleryView.Page page) {
      pages.addFirst(page);

      View view = page.view;
      measureView(view);
      int bottom = totalTop + interval;
      int top = bottom - view.getMeasuredHeight();
      view.layout(0, top, width, bottom);

      totalTop = top;
    }

    @Override
    public int getPreviousLess() {
      return pages.getFirst().view.getTop();
    }

    @Override
    public void offsetPages(int offset) {
      for (GalleryView.Page page : pages) {
        page.view.offsetTopAndBottom(offset);
      }
      totalTop += offset;
      totalBottom += offset;
    }

    @Override
    public void removeDumpPages() {
      // TODO
    }

    @Override
    public int applyScroll(int currentOffset, int distanceX, int distanceY) {
      return currentOffset - distanceY;
    }
  }











}
