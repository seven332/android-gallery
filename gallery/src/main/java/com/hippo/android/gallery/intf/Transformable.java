/*
 * Copyright 2018 Hippo Seven
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

package com.hippo.android.gallery.intf;

/*
 * Created by Hippo on 2018/1/27.
 */

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Transformable can scale and scroll it's content without changing it's size.
 */
public interface Transformable {

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
   * Scale the content of this transformer.
   *
   * The size of the content can be under some limitation. The factor may not take effort
   * or not take all effort. The remain effort of the factor should be return.
   *
   * @param x focus x
   * @param y focus y
   * @param factor the factor of scale, greater than 1 for zoom in, smaller than 1 for zoom in
   */
  void scale(float x, float y, float factor, @Nullable float[] remain);

  /**
   * Scroll the content of this transformer.
   *
   * The position of the content can be under some limitation. The offset may not take effort
   * or not take all effort. The remain effort of the offset should be return.
   *
   * @param dx the distance to offset, positive for right to left
   * @param dy the distance to offset, positive for bottom to top
   */
  void scroll(float dx, float dy, @Nullable float[] remain);

  /**
   * Set scale for the content of this transformer.
   * Scale type will be set to {@link #SCALE_TYPE_FIXED}.
   */
  void setScale(float scale);

  /**
   * Set scale type for the content of this transformer.
   */
  void setScaleType(@ScaleType int scaleType);

  /**
   * Set start position for the content of this transformer.
   */
  void setStartPosition(@StartPosition int startPosition);
}
