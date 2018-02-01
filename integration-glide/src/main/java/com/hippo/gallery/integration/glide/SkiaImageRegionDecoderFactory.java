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

package com.hippo.gallery.integration.glide;

/*
 * Created by Hippo on 2018/2/1.
 */

import android.graphics.BitmapRegionDecoder;
import android.support.annotation.Nullable;
import com.hippo.android.gallery.drawable.ImageRegionDecoder;
import com.hippo.android.gallery.drawable.SkiaImageRegionDecoder;
import java.io.IOException;
import java.io.InputStream;

public class SkiaImageRegionDecoderFactory implements ImageRegionDecoderFactory {

  @Nullable
  @Override
  public ImageRegionDecoder create(InputStream is) throws IOException {
    BitmapRegionDecoder bitmapDecoder = BitmapRegionDecoder.newInstance(is, false);
    if (bitmapDecoder != null) {
      return new SkiaImageRegionDecoder(bitmapDecoder);
    }
    return null;
  }
}
