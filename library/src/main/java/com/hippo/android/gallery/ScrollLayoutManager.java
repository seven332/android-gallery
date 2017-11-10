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

import android.view.View;
import java.util.LinkedList;
import java.util.List;

public class ScrollLayoutManager extends GalleryView.LayoutManager {

  // First anchor page index
  private int anchorIndex;

  // First anchor page offset
  private int anchorOffset;

  // The interval between pages
  private int pageInterval;

  private ScrollLayout scrollLayout;

  public void setPageInterval(int pageInterval) {
    this.pageInterval = pageInterval;
  }

  public void setScrollLayout(ScrollLayout pageLayout) {
    this.scrollLayout = pageLayout;
    this.anchorOffset = 0;
  }

  // Layout next pages one by one, until first invisible page
  private void layoutNextPages(GalleryView.Nest nest, LinkedList<GalleryView.Page> pages) {
    int pageCount = nest.getPageCount();
    int nextIndex = pages.getLast().getIndex();

    while (++nextIndex < pageCount && scrollLayout.canLayoutNext(pages.getLast().view, 0)) {
      GalleryView.Page nextPage = nest.pinPage(nextIndex);
      pages.addLast(nextPage);
      scrollLayout.layoutNext(nextPage.view);

      // If the last pages can't has previous, remove the second last page
      if (!scrollLayout.canLayoutPrevious(nextPage.view, 0)) {
        // pages.size() must be 2
        if (pages.size() != 2) {
          throw new IllegalStateException("If canLayoutPrevious() is true in layoutNextPages(), "
              + "pages.size() must be 2");
        }
        // Remove it
        nest.unpinPage(pages.getFirst());
        pages.removeFirst();
        scrollLayout.resetLayoutState(nextPage.view);
      }
    }
  }

  // Layout previous pages one by one, until first invisible page
  private void layoutPreviousPages(GalleryView.Nest nest, LinkedList<GalleryView.Page> pages, int nextBlank) {
    int previousIndex = pages.getFirst().getIndex();

    while (--previousIndex >= 0 && scrollLayout.canLayoutPrevious(pages.getFirst().view, nextBlank)) {
      GalleryView.Page previousPage = nest.pinPage(previousIndex);
      pages.addFirst(previousPage);
      scrollLayout.layoutPrevious(previousPage.view);

      // If the first pages can't has next, remove the second first page
      if (!scrollLayout.canLayoutNext(previousPage.view, nextBlank)) {
        // pages.size() must be 2
        if (pages.size() != 2) {
          throw new IllegalStateException("If canLayoutNext() is true in layoutPreviousPages(), "
              + "pages.size() must be 2");
        }
        // Remove it
        nest.unpinPage(pages.getLast());
        pages.removeLast();
        scrollLayout.resetLayoutState(previousPage.view);
      }
    }
  }

  private void adjustPagesPosition(int nextBlank, int previousOffset, LinkedList<GalleryView.Page> pages) {
    int pageOffset = 0;
    if (previousOffset > 0) {
      // There is blank in previous area.
      pageOffset = -previousOffset;
    } else if (nextBlank < 0) {
      pageOffset = Math.min(-nextBlank, -previousOffset);
    }
    if (pageOffset != 0) {
      scrollLayout.offsetPages(pages, pageOffset);
    }
  }

  @Override
  public void layout(GalleryView.Nest nest, int width, int height) {
    // Check page count
    int pageCount = nest.getPageCount();
    if (pageCount == 0) {
      return;
    }

    // Ensure current index in the range.
    int newIndex = Utils.clamp(anchorIndex, 0, nest.getPageCount() - 1);
    if (anchorIndex != newIndex) {
      anchorIndex = newIndex;
      anchorOffset = 0;
    }

    scrollLayout.start(width, height, pageInterval);

    LinkedList<GalleryView.Page> pages = new LinkedList<>();

    /*
     * 1. Layout anchor page
     */
    GalleryView.Page anchorPage = nest.pinPage(anchorIndex);
    pages.add(anchorPage);
    scrollLayout.layoutAnchor(anchorPage.view, anchorOffset);

    /*
     * 2. Layout next pages one by one, until the first out-of-screen page
     */
    layoutNextPages(nest, pages);

    int nextBlank = scrollLayout.getNextBlank(pages.getLast().view);

    /*
     * 3. Layout previous pages one by one, until the first out-of-screen page
     */
    layoutPreviousPages(nest, pages, nextBlank);

    int previousOffset = scrollLayout.getPreviousOffset(pages.getFirst().view);

    /*
     * 4. Adjust pages position to avoid blank
     */
    adjustPagesPosition(nextBlank, previousOffset, pages);

    /*
     * 5. Continue layout next pages
     */
    layoutNextPages(nest, pages);

    /*
     * 6. Update anchorIndex and anchorOffset
     */
    for (GalleryView.Page page : pages) {
      if (scrollLayout.canBeAnchor(page.view)) {
        anchorIndex = page.getIndex();
        anchorOffset = scrollLayout.getAnchorOffset(page.view);
        break;
      }
    }
  }

  @Override
  public void scrollBy(GalleryView.Nest nest, int distanceX, int distanceY) {
    anchorOffset = scrollLayout.applyScroll(anchorOffset, distanceX, distanceY);
    nest.layout(this, nest.getWidth(), nest.getHeight());
  }

  public interface ScrollLayout {

    /**
     * Starts new layout turn.
     */
    void start(int width, int height, int interval);

    /**
     * Layout the anchor page.
     *
     * @param offset the offset of the page to the baseline.
     */
    void layoutAnchor(View page, int offset);

    /**
     * Returns {@code true} if a page can be layout after the current last page.
     *
     * @param last current last page
     * @param offset the offset to correct the baseline
     */
    boolean canLayoutNext(View last, int offset);

    /**
     * Layout the page as a next page.
     */
    void layoutNext(View page);

    /**
     * Returns the blank remains in next direction.
     *
     * @param last the last view
     */
    int getNextBlank(View last);

    /**
     * Returns {@code true} if a view can be layout after the first view.
     *
     * @param first current first view
     * @param offset the offset to correct the baseline
     */
    boolean canLayoutPrevious(View first, int offset);

    /**
     * Layout the page as a previous page.
     */
    void layoutPrevious(View page);

    /**
     * Returns the offset to baseline in previous direction.
     */
    int getPreviousOffset(View first);

    /**
     * Offset all pages.
     */
    void offsetPages(List<GalleryView.Page> pages, int offset);

    /**
     * Whether the page can be a anchor.
     */
    boolean canBeAnchor(View page);

    /**
     * Return the offset ot the anchor.
     */
    int getAnchorOffset(View anchor);

    /**
     * Reset current layout state as the view.
     */
    void resetLayoutState(View view);

    /**
     * Apply scroll to current layout state.
     */
    int applyScroll(int oldAnchorOffset, int distanceX, int distanceY);
  }
}
