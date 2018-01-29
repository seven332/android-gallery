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
 * Created by Hippo on 2018/1/28.
 */

import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;

/**
 * Accurate provides a way to draw only a rectangle region of this drawable.
 */
public interface Accurate {

  /**
   * Draw a rectangle region {@code src} in the destination region {@code dst}.
   */
  void draw(@NonNull Canvas canvas, @NonNull RectF src, @NonNull RectF dst);
}
