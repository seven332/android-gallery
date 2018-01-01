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

import android.support.animation.FlingAnimation;
import android.support.animation.FloatPropertyCompat;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import java.util.LinkedList;
import java.util.List;

/**
 * ScrollLayoutManager lays pages like {@code ScrollView}.
 */
public class ScrollLayoutManager extends GalleryLayoutManager {

  // SCALE_MIN MUST BE 1.0f
  private static final float SCALE_MIN = 1.0f;
  private static final float SCALE_MAX = 3.0f;

  // First anchor page index
  private int anchorIndex = 0;

  // First anchor page offset
  private float anchorOffset = 0;

  // The interval between pages
  private int pageInterval = 0;

  // Scale factor for the scalable pages
  // Full width is 1.0f
  private float pageScale = 1.0f;

  // The offset against scroll direction
  private float pageDeviate;

  private float[] temp = new float[2];

  private ScrollLayout scrollLayout;

  public static final FloatPropertyCompat<ScrollLayoutManager> SCROLL_BY = new FloatPropertyCompat<ScrollLayoutManager>("scrollBy") {
    @Override
    public void setValue(ScrollLayoutManager slm, float value) {
      float d = value - slm.lastFling;
      slm.lastFling = value;
      slm.scroll(d * slm.flingScaleX, d * slm.flingScaleY);
    }
    @Override
    public float getValue(ScrollLayoutManager slm) {
      return slm.lastFling;
    }
  };
  private float flingScaleX;
  private float flingScaleY;
  private float lastFling;
  private FlingAnimation flingAnimation = new FlingAnimation(this, SCROLL_BY);

  /**
   * Sets the interval between pages.
   * Negative value is treated as {@code 0}.
   */
  public void setPageInterval(int pageInterval) {
    pageInterval = Math.max(0, pageInterval);

    if (this.pageInterval != pageInterval) {
      this.pageInterval = pageInterval;
      requestLayout();
    }
  }

  /**
   * Sets ScrollLayout to this ScrollLayoutManager.
   *
   * @throws IllegalStateException if the GalleryView it attached to is in layout.
   */
  public void setScrollLayout(ScrollLayout pageLayout) {
    if (isInLayout()) throw new IllegalStateException();

    if (this.scrollLayout != pageLayout) {
      this.scrollLayout = pageLayout;
      requestLayout();
    }
  }

  @VisibleForTesting
  void setAnchor(int index, int offset) {
    anchorIndex = index;
    anchorOffset = offset;
  }

  /*
   * Layout next pages one by one,
   * until first invisible page.
   */
  private void layoutNextPages(GalleryNest nest, LinkedList<GalleryPage> pages) {
    int pageCount = nest.getPageCount();
    int nextIndex = pages.getLast().getIndex();

    while (++nextIndex < pageCount && scrollLayout.canLayoutNext(pages.getLast().view, 0)) {
      GalleryPage nextPage = nest.pinPage(nextIndex);
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

  /*
   * Layout previous pages one by one,
   * until first invisible page.
   */
  private void layoutPreviousPages(GalleryNest nest, LinkedList<GalleryPage> pages, int nextBlank) {
    int previousIndex = pages.getFirst().getIndex();

    while (--previousIndex >= 0 && scrollLayout.canLayoutPrevious(pages.getFirst().view, nextBlank)) {
      GalleryPage previousPage = nest.pinPage(previousIndex);
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

  private void adjustPagesPosition(int nextBlank, int previousOffset, LinkedList<GalleryPage> pages) {
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
  public void layout(int width, int height) {
    GalleryNest nest = getNest();
    if (nest == null) return;

    // Ensure anchor index in the range
    int newIndex = Utils.clamp(anchorIndex, 0, nest.getPageCount() - 1);
    if (anchorIndex != newIndex) {
      anchorIndex = newIndex;
      anchorOffset = 0;
    }

    // Ensure page scale and deviate in the range
    pageScale = Utils.clamp(pageScale, SCALE_MIN, SCALE_MAX);
    pageDeviate = Utils.clamp(pageDeviate, -(pageScale - 1.0f) * width, 0);

    scrollLayout.start(width, height, pageScale, pageDeviate, pageInterval);

    LinkedList<GalleryPage> pages = new LinkedList<>();

    /*
     * 1. Layout anchor page
     */
    GalleryPage anchorPage = nest.pinPage(anchorIndex);
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
    for (GalleryPage page : pages) {
      if (scrollLayout.canBeAnchor(page.view)) {
        anchorIndex = page.getIndex();
        anchorOffset = scrollLayout.getAnchorOffset(page.view);
        break;
      }
    }
  }

  @Override
  public void scroll(float dx, float dy) {
    GalleryNest nest = getNest();
    if (nest == null) return;

    scrollLayout.scrollBy(anchorOffset, pageDeviate, dx, dy, temp);
    anchorOffset = temp[0];
    pageDeviate = temp[1];
    // It's hard to fix anchorOffset, let layout() fix it
    nest.layout(this);
  }

  @Override
  public void scale(float x, float y, float factor) {
    GalleryNest nest = getNest();
    if (nest == null) return;

    float oldPageScale = pageScale;
    pageScale = Utils.clamp(factor * pageScale, SCALE_MIN, SCALE_MAX);

    if (pageScale != oldPageScale) {
      // TODO Need a better way to fix anchorOffset and pageDeviate
      scrollLayout.scaleBy(anchorOffset, pageDeviate, x, y, pageScale / oldPageScale, temp);
      anchorOffset = temp[0];
      pageDeviate = temp[1];
      nest.layout(this);
    }
  }

  @Override
  public void fling(float velocityX, float velocityY) {
    GalleryNest nest = getNest();
    if (nest == null) return;

    float velocity;
    lastFling = 0.0f;
    if (Math.abs(velocityX) > Math.abs(velocityY)) {
      velocity = velocityX;
      flingScaleX = 1.0f;
      flingScaleY = velocityY / velocityX;
    } else {
      velocity = velocityY;
      flingScaleY = 1.0f;
      flingScaleX = velocityX / velocityY;
    }

    flingAnimation.cancel();
    flingAnimation.setStartVelocity(velocity)
        .setMinValue(-Float.MAX_VALUE)
        .setMaxValue(Float.MAX_VALUE)
        .start();
  }

  @Override
  public void cancelAnimations() {
    flingAnimation.cancel();
  }

  /**
   * ScrollLayout handle single page laying.
   */
  public interface ScrollLayout {

    /**
     * Starts new layout turn.
     *
     * @param deviate this param is used to layout view, {@code int} is enough
     */
    void start(int width, int height, float scale, float deviate, int interval);

    /**
     * Layout the anchor page.
     *
     * @param offset the offset of the page to the baseline.
     */
    void layoutAnchor(View page, float offset);

    /**
     * Returns {@code true} if a page can be layout after the current last page.
     *
     * @param last current last page
     * @param offset the offset to correct the baseline
     */
    boolean canLayoutNext(View last, float offset);

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
    boolean canLayoutPrevious(View first, float offset);

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
    void offsetPages(List<GalleryPage> pages, int offset);

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
     *
     * @param result a two-size array to store the offset of
     *               anchorOffset and pageDeviate
     */
    void scrollBy(float anchorOffset, float pageDeviate, float dx, float dy, float[] result);

    /**
     * Apply scale to current layout state.
     *
     * @param result a two-size to store new anchorOffset and pageDeviate
     */
    void scaleBy(float anchorOffset, float pageDeviate, float x, float y, float factor, float[] result);
  }
}
