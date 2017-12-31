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
 * GalleryGestureHandler handles gestures from GalleryView.
 */
public interface GalleryGestureHandler {
  void onSingleTapUp(GalleryView view, float x, float y);
  void onSingleTapConfirmed(GalleryView view, float x, float y);
  void onDoubleTap(GalleryView view, float x, float y);
  void onDoubleTapConfirmed(GalleryView view, float x, float y);
  void onLongPress(GalleryView view, float x, float y);
  void onScroll(GalleryView view, float dx, float dy, float totalX, float totalY, float x, float y);
  void onFling(GalleryView view, float velocityX, float velocityY);
  void onScaleBegin(GalleryView view, float focusX, float focusY);
  void onScale(GalleryView view, float focusX, float focusY, float scale);
  void onScaleEnd(GalleryView view);
  void onDown(GalleryView view, float x, float y);
  void onUp(GalleryView view, float x, float y);
  void onCancel(GalleryView view);
  void onPointerDown(GalleryView view, float x, float y);
  void onPointerUp(GalleryView view, float x, float y);
}
