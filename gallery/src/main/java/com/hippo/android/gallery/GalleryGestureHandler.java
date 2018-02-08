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

import android.support.annotation.Nullable;

// TODO let GalleryGestureHandler handles common animations, like fling animation.
/**
 * GalleryGestureHandler handles gestures from GalleryView.
 */
public class GalleryGestureHandler {

  @Nullable
  private GalleryView view;

  void attach(GalleryView view) {
    if (this.view != null) {
      throw new IllegalStateException("This GestureHandler is already attached to a GalleryView.");
    }
    this.view = view;
  }

  void detach() {
    this.view = null;
  }

  @Nullable
  private GalleryLayoutManager getLayoutManager() {
    if (view != null) {
      return view.getLayoutManager();
    } else {
      return null;
    }
  }

  public void onDown(float x, float y) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager != null) {
      layoutManager.cancelAnimations();
    }
  }

  public void onUp(float x, float y) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager instanceof PagerLayoutManager) {
      ((PagerLayoutManager) layoutManager).startTurningAnimation();
    }
  }

  public void onCancel() {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager instanceof PagerLayoutManager) {
      ((PagerLayoutManager) layoutManager).startTurningAnimation();
    }
  }

  public void onSingleTap(float x, float y) {}

  public void onDoubleTap(float x, float y) {}

  public void onLongPress(float x, float y) {}

  public void onScroll(float dx, float dy, float totalX, float totalY, float x, float y) {
    GalleryLayoutManager layoutManager = getLayoutManager();
    if (layoutManager != null) {
      layoutManager.scroll(dx, dy);
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
      layoutManager.scale(x, y, scale);
    }
  }

  public void onRotate(float x, float y, float angle) {}
}
