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

package com.hippo.android.gallery.demo;

/*
 * Created by Hippo on 2017/11/4.
 */

import android.content.Context;

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
   * Converts dp value to pix value.
   *
   * @param context the context
   * @param dp the dp value to convert
   * @return the pix value
   * @throws NullPointerException if {@code context == null}
   */
  public static int dp2pix(Context context, float dp) throws NullPointerException {
    if (context == null) throw new NullPointerException("context == null");
    return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
  }

  /**
   * Converts pix value to dp value.
   *
   * @param context the context
   * @param pix the pix value to convert
   * @return the dp value
   * @throws NullPointerException if {@code context == null}
   */
  public static float pix2dp(Context context, int pix) throws NullPointerException {
    if (context == null) throw new NullPointerException("context == null");
    return pix / context.getResources().getDisplayMetrics().density;
  }
}
