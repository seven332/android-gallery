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
 * Created by Hippo on 2017/11/4.
 */

import android.support.annotation.Nullable;
import android.view.View;

final class Utils {

  /**
   * Returns the input value x clamped to the range [bound1, bound2] if bound2 &gt;= bound1,
   * otherwise [bound2, bound1].
   *
   * @param x the input
   * @param bound1 the first bound
   * @param bound2 the second bound
   * @return the result which has been clamped
   */
  public static int clamp(int x, int bound1, int bound2) {
    if (bound2 >= bound1) {
      if (x > bound2) return bound2;
      if (x < bound1) return bound1;
    } else {
      if (x > bound1) return bound1;
      if (x < bound2) return bound2;
    }
    return x;
  }

  /**
   * Returns the input value x clamped to the range [bound1, bound2] if bound2 &gt;= bound1,
   * otherwise [bound2, bound1].
   *
   * @param x the input
   * @param bound1 the first bound
   * @param bound2 the second bound
   * @return the result which has been clamped
   */
  public static float clamp(float x, float bound1, float bound2) {
    if (bound2 >= bound1) {
      if (x > bound2) return bound2;
      if (x < bound1) return bound1;
    } else {
      if (x > bound1) return bound1;
      if (x < bound2) return bound2;
    }
    return x;
  }

  /**
   * Returns the page as photo. Returns {@code null} if the view isn't a photo or it's disabled.
   */
  @Nullable
  public static Photo asPhoto(GalleryPage page) {
    return page != null ? asPhoto(page.view) : null;
  }

  /**
   * Returns the view as photo. Returns {@code null} if the view isn't a photo or it's disabled.
   */
  @Nullable
  public static Photo asPhoto(View view) {
    if (view instanceof Photo) {
      Photo photo = (Photo) view;
      if (photo.isPhotoEnabled()) {
        return photo;
      }
    }
    return null;
  }

  /**
   * Returns {@code true} if the arguments are equal or within the range of allowed
   * error (inclusive).
   */
  public static boolean equals(float x, float y, float eps) {
    return Math.abs(y - x) <= eps;
  }
}
