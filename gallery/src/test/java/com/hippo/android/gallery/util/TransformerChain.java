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

public class TransformerChain implements Transformer {

  private final Transformer[] transformers;

  public TransformerChain(Transformer... transformers) {
    this.transformers = transformers;
  }

  @Override
  public void setUp(int width, int height) {
    for (Transformer transformer : transformers) {
      transformer.setUp(width, height);
    }
  }

  @Override
  public void transformOffset(Point point) {
    for (Transformer transformer : transformers) {
      transformer.transformOffset(point);
    }
  }

  @Override
  public void transformPoint(Point point) {
    for (Transformer transformer : transformers) {
      transformer.transformPoint(point);
    }
  }

  @Override
  public void transformRect(Rect rect) {
    for (Transformer transformer : transformers) {
      transformer.transformRect(rect);
    }
  }
}
