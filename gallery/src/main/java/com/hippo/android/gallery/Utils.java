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

import android.graphics.RectF;
import android.view.View;
import com.hippo.android.gallery.intf.Clippable;
import com.hippo.android.gallery.intf.Flexible;

public final class Utils {

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
   * Returns the input value n clamped to the range [bound1, bound2] if bound2 &gt;= bound1,
   * otherwise [bound2, bound1].
   *
   * @param n the input
   * @param bound1 the first bound
   * @param bound2 the second bound
   * @return the result which has been clamped
   */
  public static float clamp(float n, float bound1, float bound2) {
    if (bound2 >= bound1) {
      if (n > bound2) return bound2;
      if (n < bound1) return bound1;
    } else {
      if (n > bound1) return bound1;
      if (n < bound2) return bound2;
    }
    return n;
  }

  /**
   * Returns {@code true} if the arguments are equal or within the range of allowed
   * error (inclusive).
   */
  public static boolean equals(float x, float y, float eps) {
    return Math.abs(y - x) <= eps;
  }

  /**
   * Returns {@code true} if the view is flexible.
   */
  public static boolean isFlexible(View view) {
    return view instanceof Flexible && ((Flexible) view).isFlexible();
  }

  /**
   * Updates the view's clip region if the view is a clipper.
   */
  public static void updateClipRegion(View view, int parentWidth, int parentHeight) {
    Clippable clippable = view instanceof Clippable ? (Clippable) view : null;
    if (clippable == null) return;

    int left = Math.max(0, -view.getLeft());
    int top = Math.max(0, -view.getTop());
    int right = Math.min(view.getWidth(), parentWidth - view.getLeft());
    int bottom = Math.min(view.getHeight(), parentHeight - view.getTop());

    if (left < right && top < bottom) {
      clippable.clip(left, top, right, bottom);
    } else {
      clippable.clip(0, 0, 0, 0);
    }
  }

  /**
   * Returns the result which is the smallest (closest to zero)
   * {@code int} value that is greater than or equal to the input
   * and is power of 2. n is treated as unsigned int.
   *
   * @param n the input
   * @return the result
   */
  public static int nextPow2(int n) {
    if (n == 0) return 1;
    n -= 1;
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return n + 1;
  }

  /**
   * Returns the result which is the biggest (closest to positive infinity)
   * {@code long} value that is smaller than or equal to the input
   * and is power of 2. n is treated as unsigned int.
   * <p>
   * Return 0 if {@code n} is 0.
   *
   * @param n the input
   * @return the result
   */
  public static int prevPow2(int n) {
    n |= n >> 1;
    n |= n >> 2;
    n |= n >> 4;
    n |= n >> 8;
    n |= n >> 16;
    return n - (n >>> 1);
  }

  /**
   * Returns the largest (closest to positive infinity)
   * {@code int} value that is less than or equal to the algebraic quotient.
   *
   * @param a the dividend
   * @param b the divisor
   * @return the quotient
   */
  public static int floorDiv(int a, int b) {
    int r = a / b;
    // if the signs are different and modulo not zero, round down
    if ((a ^ b) < 0 && (r * b != a)) {
      --r;
    }
    return r;
  }

  /**
   * Returns the smallest (closest to positive infinity)
   * {@code int} value that is greater than or equal to the algebraic quotient.
   *
   * @param a the dividend
   * @param b the divisor
   * @return the quotient
   */
  public static int ceilDiv(int a, int b) {
    return -floorDiv(-a, b);
  }

  /**
   *                                            dst
   *        src                          +---------------+
   *    +----------+                     |               |
   *    |   s      |                     |    d          |
   *    | +--+     |    linear map       | +----+        |
   *    | +--+     |    ---------->      | |    |        |
   *    |          |                     | +----+        |
   *    +----------+                     |               |
   *                                     +---------------+
   */
  public static void mapRect(RectF src, RectF dst, RectF s, RectF d) {
    final float sX = src.left;
    final float sY = src.top;
    final float dX = dst.left;
    final float dY = dst.top;
    final float scaleX = dst.width() / src.width();
    final float scaleY = dst.height() / src.height();
    d.set(dX + (s.left - sX) * scaleX,
        dY + (s.top - sY) * scaleY,
        dX + (s.right - sX) * scaleX,
        dY + (s.bottom - sY) * scaleY);
  }
}
