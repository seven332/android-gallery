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
 * Created by Hippo on 2017/11/15.
 */

import android.support.annotation.IntDef;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The direct child of {@link GalleryView}.
 */
public interface Photo {

  @IntDef({SCALE_TYPE_ORIGIN, SCALE_TYPE_FIT_WIDTH, SCALE_TYPE_FIT_HEIGHT,
      SCALE_TYPE_FIT, SCALE_TYPE_FIXED})
  @Retention(RetentionPolicy.SOURCE)
  @interface ScaleType {}

  int SCALE_TYPE_ORIGIN = 0;
  int SCALE_TYPE_FIT_WIDTH = 1;
  int SCALE_TYPE_FIT_HEIGHT = 2;
  int SCALE_TYPE_FIT = 3;
  int SCALE_TYPE_FIXED = 4;

  @IntDef({START_POSITION_TOP_LEFT, START_POSITION_TOP_RIGHT, START_POSITION_BOTTOM_LEFT,
      START_POSITION_BOTTOM_RIGHT, START_POSITION_CENTER})
  @Retention(RetentionPolicy.SOURCE)
  @interface StartPosition {}

  int START_POSITION_TOP_LEFT = 0;
  int START_POSITION_TOP_RIGHT = 1;
  int START_POSITION_BOTTOM_LEFT = 2;
  int START_POSITION_BOTTOM_RIGHT = 3;
  int START_POSITION_CENTER = 4;

  /**
   * Returns {@code false} if the photo should be treated as a normal page.
   *
   * If it returns {@code false}, none of the function would be called.
   *
   * If the return value changed, call {@link GalleryView.Adapter#notifyPageChanged(int)} first.
   */
  boolean isPhotoEnabled();

  /**
   * Set visible rect of the photo. The remain area should <b>NOT</b> be drawn.
   */
  void setVisibleRect(float left, float top, float right, float bottom);

  /**
   * Scale the content of the photo.
   *
   * @param x focus x
   * @param y focus y
   * @param factor the factor of scale, greater than 1 for zoom in, smaller than 1 for zoom in
   */
  void scale(float x, float y, float factor);

  /**
   * Set scale type for the content of the photo.
   */
  void setScaleType(@ScaleType int scaleType);

  /**
   * Offset the content of the photo.
   *
   * @param dx the distance to offset, positive for right to left
   * @param dy the distance to offset, positive for bottom to top
   */
  void offset(float dx, float dy, float[] remain);

  /**
   * Set start position for the content of the photo.
   */
  void setStartPosition(@StartPosition int startPosition);
}
