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

package com.hippo.android.gallery.util;

/*
 * Created by Hippo on 2017/12/15.
 */

import android.graphics.Point;
import android.graphics.Rect;

public class AxisSwap implements Transformer {

  private int width;
  private int height;

  @Override
  public void setUp(int width, int height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public void transformOffset(Point point) {
    point.x = point.x ^ point.y;
    point.y = point.x ^ point.y;
    point.x = point.x ^ point.y;
  }

  @Override
  public void transformPoint(Point point) {
    // TODO
  }

  @Override
  public void transformRect(Rect rect) {
    rect.left = rect.left ^ rect.top;
    rect.top = rect.left ^ rect.top;
    rect.left = rect.left ^ rect.top;

    rect.right = rect.right ^ rect.bottom;
    rect.bottom = rect.right ^ rect.bottom;
    rect.right = rect.right ^ rect.bottom;
  }
}
