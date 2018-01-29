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

package com.hippo.android.gallery.drawable;

/*
 * Created by Hippo on 2018/1/11.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * ImageRegionDecoder can be used to decode a rectangle region from an image.
 *
 * It's a interface-version {@link android.graphics.BitmapRegionDecoder}.
 */
public interface ImageRegionDecoder {

  /**
   * Returns the width of the image.
   */
  int getWidth();

  /**
   * Returns the height of the image.
   */
  int getHeight();

  /**
   * Decodes a region for the image.
   */
  @Nullable
  Bitmap decode(@NonNull Rect rect, Bitmap.Config preferredConfig, int sample);

  /**
   * Recycles the bitmap generated in {@link #decode(Rect, Bitmap.Config, int)}.
   */
  void recycle(@NonNull Bitmap bitmap);

  /**
   * Returns true if this decoder has been recycled.
   */
  boolean isRecycled();

  /**
   * Recycles this ImageRegionDecoder.
   */
  void recycle();
}
