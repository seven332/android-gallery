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
 * Created by Hippo on 2018/1/29.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class SkiaImageRegionDecoder implements ImageRegionDecoder {

  private static final String LOG_TAG = "SkiaImageRegionDecoder";

  private BitmapRegionDecoder decoder;
  private int width;
  private int height;

  public SkiaImageRegionDecoder(BitmapRegionDecoder decoder) {
    this.decoder = decoder;
    this.width = decoder.getWidth();
    this.height = decoder.getHeight();
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Nullable
  @Override
  public Bitmap decode(@NonNull Rect rect, Bitmap.Config preferredConfig, int sample) {
    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = sample;
    options.inPreferredConfig = preferredConfig;
    try {
      return decoder.decodeRegion(rect, options);
    } catch (IllegalStateException | IllegalArgumentException e) {
      Log.e(LOG_TAG, "Can't decode region " + rect + " with preferred config " +
          preferredConfig + " sample " + sample, e);
      return null;
    }
  }

  @Override
  public void recycle(@NonNull Bitmap bitmap) {
    bitmap.recycle();
  }

  @Override
  public boolean isRecycled() {
    return decoder.isRecycled();
  }

  @Override
  public void recycle() {
    decoder.recycle();
  }
}
