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

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.ResourceDecoder;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.util.ByteBufferUtil;
import com.hippo.android.gallery.drawable.ImageRegionDecoder;
import com.hippo.android.gallery.drawable.TiledDrawable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

/**
 * Decodes {@link TiledDrawable TiledDrawables} from {@link ByteBuffer ByteBuffers}.
 */
public class ByteBufferTiledDrawableDecoder implements ResourceDecoder<ByteBuffer, TiledDrawable> {

  public static final Option<Boolean> ENABLE = Option.memory(
      "com.hippo.gallery.integration.glide.ByteBufferTiledDrawableDecoder.ENABLE", false);

  private static final int MARK_POSITION = 10 * 1024 * 1024;

  private ImageRegionDecoderFactory factory;
  private Executor executor;

  public ByteBufferTiledDrawableDecoder(
      @NonNull ImageRegionDecoderFactory factory,
      @NonNull Executor executor
  ) {
    this.factory = factory;
    this.executor = executor;
  }

  @Override
  public boolean handles(@NonNull ByteBuffer source, @NonNull Options options) throws IOException {
    return options.get(ENABLE);
  }

  @Nullable
  @Override
  public Resource<TiledDrawable> decode(@NonNull ByteBuffer source, int width, int height,
      @NonNull Options options) throws IOException {
    InputStream is = ByteBufferUtil.toStream(source);
    if (!needToBeTiled(is)) return null;

    ImageRegionDecoder decoder = factory.create(is);
    if (decoder == null) return null;

    try {
      TiledDrawableResource resource = TiledDrawableResource.create(decoder, executor);
      if (resource != null) {
        decoder = null;
      }
      return resource;
    } finally {
      if (decoder != null) {
        decoder.recycle();
      }
    }
  }

  private boolean needToBeTiled(InputStream is) throws IOException {
    is.mark(MARK_POSITION);

    try {
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(is, null, options);

      int maxTextureSize = TiledDrawable.getMaxTextureSize();
      return options.outWidth > maxTextureSize || options.outHeight > maxTextureSize;
    } finally {
      is.reset();
    }
  }
}
