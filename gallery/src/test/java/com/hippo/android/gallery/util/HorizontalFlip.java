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

import android.graphics.PointF;
import android.graphics.RectF;

public class HorizontalFlip implements Transformer {

  private float width;

  @Override
  public void setUp(float width, float height) {
    this.width = width;
  }

  @Override
  public void transformOffset(PointF point) {
    point.x = -point.x;
  }

  @Override
  public void transformPoint(PointF point) {
    point.x = width - point.x;
  }

  @Override
  public void transformRect(RectF rect) {
    float oldLeft = rect.left;
    float oldRight = rect.right;
    rect.left = width - oldRight;
    rect.right = width - oldLeft;
  }
}
