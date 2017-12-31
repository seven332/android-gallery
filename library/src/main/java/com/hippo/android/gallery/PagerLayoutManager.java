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

import android.content.Context;
import android.support.animation.FlingAnimation;
import android.support.animation.FloatPropertyCompat;
import android.support.animation.SpringAnimation;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * PagerLayoutManager lays pages like {@code ViewPager}.
 */
public class PagerLayoutManager extends GalleryLayoutManager {

  private static final String LOG_TAG = "PagerLayoutManager";

  @IntDef({POSITION_PREVIOUS, POSITION_CURRENT, POSITION_NEXT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Position {}

  public static final int POSITION_PREVIOUS = 0;
  public static final int POSITION_CURRENT = 1;
  public static final int POSITION_NEXT = 2;

  public static final int TURNING_THRESHOLD_DP = 48;

  // Current page index
  private int currentIndex = 0;

  // The interval between pages
  private int pageInterval = 0;

  /*
   * The offset of pages
   * From previous to next is positive
   * From next to previous is negative
   */
  private float pageOffset = 0.0f;

  /*
   * The threshold for turing page.
   * It's checked when the finger up.
   * If pageOffset is larger than turningThreshold,
   * turn to previous page.
   * If pageOffset is smaller than -turningThreshold,
   * turn to next page.
   */
  // TODO Add setTurningThreshold()?
  private final float turningThreshold;

  @Photo.ScaleType
  private int scaleType = Photo.SCALE_TYPE_FIT;

  @Photo.StartPosition
  private int startPosition = Photo.START_POSITION_TOP_LEFT;

  // Stores the remain offset x and y
  private float[] remain = new float[2];

  private PagerLayout pagerLayout;

  public static final FloatPropertyCompat<PagerLayoutManager> PAGE_OFFSET =
      new FloatPropertyCompat<PagerLayoutManager>("pageOffset") {
        @Override
        public float getValue(PagerLayoutManager plm) {
          return plm.pageOffset;
        }
        @Override
        public void setValue(PagerLayoutManager plm, float value) {
          plm.setPageOffset(value);
        }
  };
  private SpringAnimation turningAnimation = new SpringAnimation(this, PAGE_OFFSET, 0.0f);

  public static final FloatPropertyCompat<PagerLayoutManager> SCROLL_BY =
      new FloatPropertyCompat<PagerLayoutManager>("scrollBy") {
        @Override
        public void setValue(PagerLayoutManager plm, float value) {
          float d = value - plm.lastFling;
          plm.lastFling = value;
          // Fling animation should only be applied to the page, not the whole GalleryView
          plm.scrollPage(d * plm.flingScaleX, d * plm.flingScaleY);
        }
        @Override
        public float getValue(PagerLayoutManager slm) {
          return slm.lastFling;
        }
      };
  private float flingScaleX;
  private float flingScaleY;
  private float lastFling;
  private FlingAnimation flingAnimation = new FlingAnimation(this, SCROLL_BY);

  public PagerLayoutManager(Context context) {
    turningThreshold = context.getResources().getDisplayMetrics().density * TURNING_THRESHOLD_DP;
    turningAnimation.getSpring().setDampingRatio(1.0f);
  }

  /**
   * Sets the interval between pages.
   * Negative value is treated as {@code 0}.
   */
  public void setPageInterval(int pageInterval) {
    pageInterval = Math.max(0, pageInterval);

    if (this.pageInterval != pageInterval) {
      this.pageInterval = pageInterval;

      /*
       * Changing page interval changes page range.
       * It might make the final value of turning animation
       * mismatch page range.
       *
       * Canceling animation and resetting page offset to 0.0f
       * should avoid it.
       */
      cancelAnimations();
      pageOffset = 0.0f;

      requestLayout();
    }
  }

  /**
   * Sets scale type for each photo page.
   *
   * @throws IllegalStateException if the GalleryView it attached to is in layout.
   */
  public void setScaleType(@Photo.ScaleType int scaleType) {
    if (isInLayout()) throw new IllegalStateException("Can't set scale type during layout");

    if (this.scaleType == scaleType) return;
    this.scaleType = scaleType;

    GalleryNest nest = getNest();
    if (nest == null) return;

    for (GalleryPage page : nest.getPages()) {
      Photo photo = Utils.asPhoto(page);
      if (photo == null) continue;

      photo.setScaleType(scaleType);
    }
  }

  /**
   * Sets start position for each photo page.
   *
   * @throws IllegalStateException if the GalleryView it attached to is in layout.
   */
  public void setStartPosition(@Photo.StartPosition int startPosition) {
    if (isInLayout()) throw new IllegalStateException("Can't set start position during layout");

    if (this.startPosition == startPosition) return;
    this.startPosition = startPosition;

    GalleryNest nest = getNest();
    if (nest == null) return;

    for (GalleryPage page : nest.getPages()) {
      Photo photo = Utils.asPhoto(page);
      if (photo == null) continue;

      photo.setStartPosition(startPosition);
    }
  }

  /**
   * Sets PagerLayout to this PagerLayoutManager.
   *
   * @throws IllegalStateException if the GalleryView it attached to is in layout.
   */
  public void setPagerLayout(PagerLayout pagerLayout) {
    if (isInLayout()) throw new IllegalStateException("Can't set pager layout during layout");

    if (this.pagerLayout != pagerLayout) {
      this.pagerLayout = pagerLayout;

      /*
       * Reset state.
       */
      cancelAnimations();
      pageOffset = 0.0f;

      requestLayout();
    }
  }

  private GalleryPage pinPage(GalleryNest nest, int index) {
    boolean reset = !nest.containPage(index);

    GalleryPage page = nest.pinPage(index);

    Photo photo = Utils.asPhoto(page);
    if (reset && photo != null) {
      photo.setScaleType(scaleType);
      photo.setStartPosition(startPosition);
    }

    return page;
  }

  @Override
  public void layout(int width, int height) {
    if (pagerLayout == null) {
      Log.e(LOG_TAG, "Cannot layout without a PagerLayout set");
      return;
    }

    GalleryNest nest = getNest();
    if (nest == null) return;

    int pageCount = nest.getPageCount();

    // Ensure current index in the range
    int newIndex = Utils.clamp(currentIndex, 0, nest.getPageCount() - 1);
    if (currentIndex != newIndex) {
      currentIndex = newIndex;
      pageOffset = 0;
    }

    pagerLayout.start(width, height, pageInterval, scaleType, startPosition);

    // Ensure page offset in the range
    fixPageOffset(nest);

    // Layout current page
    GalleryPage current = pinPage(nest, currentIndex);
    pagerLayout.layoutPage(current.view, pageOffset, POSITION_CURRENT);
    // Layout previous page
    if (currentIndex > 0) {
      GalleryPage previous = pinPage(nest, currentIndex - 1);
      pagerLayout.layoutPage(previous.view, pageOffset, POSITION_PREVIOUS);
    }
    // Layout next page
    if (currentIndex < pageCount - 1) {
      GalleryPage next = pinPage(nest, currentIndex + 1);
      pagerLayout.layoutPage(next.view, pageOffset, POSITION_NEXT);
    }
  }

  /*
   * Returns true if a page is selected.
   * Namely pageOffset is 0.
   * It's hard to make a float to be 0.0f. So convert it to int,
   * and compare it with 0.
   */
  private boolean isPageSelected() {
    return (int) pageOffset == 0;
  }

  /*
   * Returns the photo if current page is selected and is photo.
   */
  @Nullable
  private Photo getSelectedPhoto(GalleryNest nest) {
    return isPageSelected() ? Utils.asPhoto(nest.getPageAt(currentIndex)) : null;
  }

  /*
   * Handle page turning and fix PageOffset to make it in range.
   */
  private void fixPageOffset(GalleryNest nest) {
    int pageRange = pagerLayout.getPageRange();

    // Try to turn to previous page
    while (pageOffset >= pageRange && currentIndex > 0) {
      currentIndex -= 1;
      pageOffset -= pageRange;
    }

    // Turn to next page
    while (pageOffset <= -pageRange && currentIndex < nest.getPageCount() - 1) {
      currentIndex += 1;
      pageOffset += pageRange;
    }

    // Ensure page offset in range
    if (currentIndex == 0 && pageOffset > 0.0f) {
      pageOffset = 0.0f;
    } else if (currentIndex == nest.getPageCount() - 1 && pageOffset < 0.0f) {
      pageOffset = 0.0f;
    }
  }

  /*
   * Set PageOffset directly. The value out of (-pageRange, pageRange) will be
   * treat as page turning.
   *
   * TurningAnimation require accurate PageOffset value at the end of the animation.
   * It's hard to set PageOffset accurately through scrollBy().
   * setPageOffset() works fine.
   */
  private void setPageOffset(float newPageOffset) {
    GalleryNest nest = getNest();
    if (nest == null) return;

    float oldPageOffset = pageOffset;
    pageOffset = newPageOffset;
    fixPageOffset(nest);

    // Only need layout if pageOffset changes
    if (pageOffset != oldPageOffset) {
      nest.layout(this);
    }
  }

  @Override
  public void scroll(float dx, float dy) {
    if (pagerLayout == null) return;
    GalleryNest nest = getNest();
    if (nest == null) return;

    boolean needLayout = false;

    while (dx != 0.0f && dy != 0.0f) {
      // Offset current selected photo
      Photo photo = getSelectedPhoto(nest);
      if (photo != null) {
        photo.offset(dx, dy, remain);
        dx = remain[0];
        dy = remain[1];
      }

      float oldPageOffset = pageOffset;

      // Offset all pages
      pageOffset = pagerLayout.scrollPage(pageOffset, dx, dy, remain);
      dx = remain[0];
      dy = remain[1];

      fixPageOffset(nest);

      // Only need layout if pageOffset changes
      if (pageOffset != oldPageOffset) {
        needLayout = true;
      }
    }

    if (needLayout) {
      nest.layout(this);
    }
  }

  @Override
  public void scale(float x, float y, float factor) {
    GalleryNest nest = getNest();
    if (nest == null) return;
    Photo photo = getSelectedPhoto(nest);
    if (photo == null) return;

    photo.scale(x, y, factor);
  }

  /*
   * Scrolls selected page directly.
   * Used by fling animation.
   */
  private void scrollPage(float dx, float dy) {
    GalleryNest nest = getNest();
    if (nest == null) return;
    Photo photo = getSelectedPhoto(nest);
    if (photo == null) return;

    photo.offset(dx, dy, remain);
  }

  @Override
  public void fling(float velocityX, float velocityY) {
    if (pagerLayout == null) return;
    GalleryNest nest = getNest();
    if (nest == null) return;
    Photo photo = getSelectedPhoto(nest);
    if (photo == null) return;

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

  /**
   * Starts a animation to turn to next or previous page, or turn back to the original page.
   */
  public void startTurningAnimation() {
    if (isInLayout()) throw new IllegalStateException("Can't start turning animation during layout");

    if (pagerLayout == null) return;
    GalleryNest nest = getNest();
    if (nest == null) return;
    if (isPageSelected()) return;

    float finalPageOffset;
    int pageRange = pagerLayout.getPageRange();
    int pageCount = nest.getPageCount();

    if (pageOffset >= turningThreshold && currentIndex > 0) {
      // Turn to previous page
      finalPageOffset = pageRange;
    } else if (pageOffset <= -turningThreshold && currentIndex < pageCount - 1) {
      // Turn to next page
      finalPageOffset = -pageRange;
    } else {
      // Keep current page
      finalPageOffset = 0.0f;
    }

    turningAnimation.setStartValue(pageOffset);
    turningAnimation.animateToFinalPosition(finalPageOffset);
  }

  @Override
  protected void cancelAnimations() {
    turningAnimation.cancel();
    flingAnimation.cancel();
  }

  /**
   * PagerLayout handle single page laying.
   */
  public interface PagerLayout {

    /**
     * Starts a new layout turn.
     */
    void start(int width, int height, int interval,
        @Photo.ScaleType int scaleType, @Photo.StartPosition int startPosition);

    /**
     * Returns the page range.
     *
     * The page offset must be in [-getPageRange(), getPageRange()].
     */
    int getPageRange();

    /**
     * Lays a single page.
     *
     * @param page the page to lay
     * @param offset the offset of the page,
     *               positive if the page move to next page position,
     *               negative if the page move to previous page position
     * @param position one of {@link #POSITION_PREVIOUS}, {@link #POSITION_CURRENT}
     *                 or {@link #POSITION_NEXT}
     */
    void layoutPage(View page, float offset, @Position int position);

    /**
     * Updates the page offset. The new page offset must be in
     * [-getPageRange(), getPageRange()]. So it's not necessary to consume all
     * dx and dy. Store the remain dx and dy to remain array.
     *
     * @param offset the origin offset
     * @param dx the delta x
     * @param dy the delta y
     * @param remain a two-length array, the remain delta x and y
     * @return new page offset, must in [-getPageRange(), getPageRange()]
     */
    float scrollPage(float offset, float dx, float dy, float[] remain);
  }
}
