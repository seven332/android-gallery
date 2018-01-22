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

/**
 * BaseGestureHandler handles scrolling, flinging, scaling and other base gestures.
 */
public class BaseGestureHandler implements GalleryGestureHandler {

  @Override
  public void onDown(GalleryView view, float x, float y) {
    GalleryLayoutManager layoutManager = view.getLayoutManager();
    if (layoutManager != null) {
      layoutManager.cancelAnimations();
    }
  }

  @Override
  public void onUp(GalleryView view, float x, float y) {
    GalleryLayoutManager layoutManager = view.getLayoutManager();
    if (layoutManager instanceof PagerLayoutManager) {
      ((PagerLayoutManager) layoutManager).startTurningAnimation();
    }
  }

  @Override
  public void onCancel(GalleryView view) {
    GalleryLayoutManager layoutManager = view.getLayoutManager();
    if (layoutManager instanceof PagerLayoutManager) {
      ((PagerLayoutManager) layoutManager).startTurningAnimation();
    }
  }

  @Override
  public void onSingleTap(GalleryView view, float x, float y) {}

  @Override
  public void onDoubleTap(GalleryView view, float x, float y) {}

  @Override
  public void onLongPress(GalleryView view, float x, float y) {}

  @Override
  public void onScroll(GalleryView view, float dx, float dy, float totalX, float totalY, float x,
      float y) {
    GalleryLayoutManager layoutManager = view.getLayoutManager();
    if (layoutManager != null) {
      layoutManager.scroll(dx, dy);
    }
  }

  @Override
  public void onFling(GalleryView view, float velocityX, float velocityY) {
    GalleryLayoutManager layoutManager = view.getLayoutManager();
    if (layoutManager != null) {
      layoutManager.fling(velocityX, velocityY);
    }
  }

  @Override
  public void onScale(GalleryView view, float x, float y, float scale) {
    GalleryLayoutManager layoutManager = view.getLayoutManager();
    if (layoutManager != null) {
      layoutManager.scale(x, y, scale);
    }
  }

  @Override
  public void onRotate(GalleryView view, float x, float y, float angle) {}
}
