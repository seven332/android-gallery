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
 * Created by Hippo on 2018/1/31.
 */

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.util.Util;
import com.hippo.android.gallery.drawable.ImageRegionDecoder;
import com.hippo.android.gallery.drawable.TiledDrawable;
import java.util.concurrent.Executor;

/**
 * TiledDrawableResource stores a ImageRegionDecoder.
 * It returns a new TiledDrawable in {@link #get()}.
 */
public class TiledDrawableResource implements Resource<TiledDrawable> {

  private ImageRegionDecoder decoder;
  private Bitmap preview;
  private Executor executor;

  @Nullable
  public static TiledDrawableResource create(
      ImageRegionDecoder decoder,
      Executor executor
  ) {
    if (decoder == null) return null;

    Bitmap preview = TiledDrawable.generatePreview(decoder);
    if (preview == null) return null;

    return new TiledDrawableResource(decoder, preview, executor);
  }

  private TiledDrawableResource(ImageRegionDecoder decoder, Bitmap preview, Executor executor) {
    this.decoder = decoder;
    this.preview = preview;
    this.executor = executor;
  }

  @NonNull
  @Override
  public Class<TiledDrawable> getResourceClass() {
    return TiledDrawable.class;
  }

  @NonNull
  @Override
  public TiledDrawable get() {
    return new TiledDrawable(decoder, preview, executor, true);
  }

  @Override
  public int getSize() {
    // TODO How to get the size of ImageRegionDecoder
    return Util.getBitmapByteSize(preview);
  }

  @Override
  public void recycle() {
    decoder.recycle(preview);
    decoder.recycle();
  }
}
