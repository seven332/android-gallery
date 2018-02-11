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
 * Created by Hippo on 2017/12/31.
 */

import android.support.animation.FloatPropertyCompat;
import android.support.animation.SpringAnimation;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * GalleryGestureHandler handles gestures from GalleryView.
 */
public class GalleryGestureHandler {

  private static final int MAX_OVER_SCROLL = 48;

  @Nullable
  private GalleryView view;

  private float[] remain = new float[2];

  private float maxOverScroll = MAX_OVER_SCROLL;
  private float overScrollX;
  private float overScrollY;

  private float overScrollXAnimationFactor;
  private float overScrollYAnimationFactor;
  public static final FloatPropertyCompat<GalleryGestureHandler> OVER_SCROLL =
      new FloatPropertyCompat<GalleryGestureHandler>("overScroll") {
        @Override
        public float getValue(GalleryGestureHandler ggh) {
          // It should never be called
          return 0;
        }
        @Override
        public void setValue(GalleryGestureHandler ggh, float value) {
          ggh.setOverScroll(value * ggh.overScrollXAnimationFactor,
              value * ggh.overScrollYAnimationFactor);
        }
      };
  private SpringAnimation overScrollAnimation = new SpringAnimation(this, OVER_SCROLL, 0.0f);

  public GalleryGestureHandler() {
    overScrollAnimation.getSpring().setDampingRatio(1.0f);
  }

  void attach(GalleryView view) {
    if (this.view != null) {
      throw new IllegalStateException("This GestureHandler is already attached to a GalleryView.");
    }
    this.view = view;

    maxOverScroll = view.getContext().getResources().getDisplayMetrics().density * MAX_OVER_SCROLL;
  }

  void detach() {
    this.view = null;
    overScrollAnimation.skipToEnd();
  }

  @Nullable
  private GalleryLayoutManager getLayoutManager() {
    if (view != null) {
      return view.getLayoutManager();
    } else {
      return null;
    }
  }

  private boolean canOverScroll() {
    return view != null && view.getOverScrollMode() != View.OVER_SCROLL_NEVER;
  }

  public void onDown(float x, float y) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager != null) {
      layoutManager.cancelAnimations();
    }
    overScrollAnimation.cancel();
  }

  private void startOverScrollAnimation() {
    // This section starts an animation to reduces over scroll, no need to check over scroll mode
    if (!Utils.floatEquals(overScrollX, 0.0f) || !Utils.floatEquals(overScrollY, 0.0f)) {
      float startValue = Math.max(Math.abs(overScrollX), Math.abs(overScrollY));
      overScrollXAnimationFactor = overScrollX / startValue;
      overScrollYAnimationFactor = overScrollY / startValue;
      overScrollAnimation.setStartValue(startValue);
      overScrollAnimation.animateToFinalPosition(0.0f);
    }
  }

  private void setOverScroll(float overScrollX, float overScrollY) {
    this.overScrollX = overScrollX;
    this.overScrollY = overScrollY;
    if (view != null) {
      view.setOverScroll(overScrollX, overScrollY);
    }
  }

  public void onUp(float x, float y) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager instanceof PagerLayoutManager) {
      ((PagerLayoutManager) layoutManager).startTurningAnimation();
    }

    startOverScrollAnimation();
  }

  public void onCancel() {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager instanceof PagerLayoutManager) {
      ((PagerLayoutManager) layoutManager).startTurningAnimation();
    }

    startOverScrollAnimation();
  }

  public void onSingleTap(float x, float y) {}

  public void onDoubleTap(float x, float y) {}

  public void onLongPress(float x, float y) {}

  private float toOverScroll(float from) {
    boolean opposite = false;
    if (from < 0.0f) {
      from = -from;
      opposite = true;
    }
    float result = maxOverScroll * from / (from + 2 * maxOverScroll);
    return opposite ? -result : result;
  }

  private float fromOverScroll(float to) {
    boolean opposite = false;
    if (to < 0.0f) {
      to = -to;
      opposite = true;
    }
    float result = 2 * maxOverScroll * to / (maxOverScroll - to);
    return opposite ? -result : result;
  }

  private void overScrollBefore(float value, float offset, float[] result) {
    if (Utils.floatEquals(value, 0.0f)) {
      result[0] = 0.0f;
      result[1] = offset;
    } else if (Utils.floatEquals(offset, 0.0f)) {
      result[0] = value;
      result[1] = 0.0f;
    } else if (Utils.oppositeSigns(value, offset)) {
      float newValue = value + offset;
      if (Utils.oppositeSigns(value, newValue)) {
        result[0] = 0.0f;
        result[1] = newValue;
      } else {
        result[0] = newValue;
        result[1] = 0.0f;
      }
    } else {
      result[0] = toOverScroll(fromOverScroll(value) + offset);
      result[1] = 0.0f;
    }
  }

  public void onScroll(float dx, float dy, float totalX, float totalY, float x, float y) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager != null) {
      // This section reduces over scroll, no need to check over scroll mode
      overScrollBefore(overScrollX, dx, remain);
      float newOverScrollX = remain[0];
      dx = remain[1];
      overScrollBefore(overScrollY, dy, remain);
      float newOverScrollY = remain[0];
      dy = remain[1];

      if (!Utils.floatEquals(dx, 0.0f) || !Utils.floatEquals(dy, 0.0f)) {
        layoutManager.scroll(dx, dy, remain);
        dx = remain[0];
        dy = remain[1];
      }

      if (canOverScroll()) {
        if (!Utils.floatEquals(dx, 0.0f)) {
          newOverScrollX = toOverScroll(fromOverScroll(newOverScrollX) + dx);
        }
        if (!Utils.floatEquals(dy, 0.0f)) {
          newOverScrollY = toOverScroll(fromOverScroll(newOverScrollY) + dy);
        }
      }

      setOverScroll(newOverScrollX, newOverScrollY);
    }
  }

  public void onFling(float velocityX, float velocityY) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager != null) {
      layoutManager.fling(velocityX, velocityY);
    }
  }

  public void onScale(float x, float y, float scale) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager != null) {
      layoutManager.scale(x, y, scale, null);
    }
  }

  public void onRotate(float x, float y, float angle) {}
}
