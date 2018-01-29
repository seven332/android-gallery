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
 * Created by Hippo on 2018/1/24.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import com.hippo.android.gallery.Utils;
import com.hippo.android.gallery.intf.Accurate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * TiledDrawable is designed for displaying huge images without OutOfMemoryErrors.
 * It can only be used with TransformDrawable.
 */
public class TiledDrawable extends Drawable implements Accurate {

  private static final String LOG_TAG = "TiledDrawable";

  private static final Bitmap.Config DEFAULT_CONFIG = Bitmap.Config.ARGB_8888;

  private static int MAX_TEXTURE_SIZE = 1024;

  private final ImageRegionDecoder decoder;
  private final Bitmap preview;
  private final int previewSample;
  private final Executor executor;
  private final boolean shared;

  private final SparseArray<List<Tile>> tilesMap;
  private final Matrix matrix;

  private final int width;
  private final int height;

  private int currentSample;
  private boolean recycled;

  private final RectF rectF1 = new RectF();
  private final RectF rectF2 = new RectF();
  private final List<Tile> list1 = new ArrayList<>();

  /**
   * Sets the max texture size for all TiledDrawables.
   *
   * It determines the preview size and tile size.
   */
  public static void setMaxTextureSize(int size) {
    MAX_TEXTURE_SIZE = size;
  }

  /**
   * Returns the max texture size.
   *
   * It determines the preview size and tile size.
   */
  public static int getMaxTextureSize() {
    return MAX_TEXTURE_SIZE;
  }

  private static int previewSample(ImageRegionDecoder decoder) {
    float maxSize = (float) MAX_TEXTURE_SIZE;
    int widthScale = (int) Math.ceil((float) decoder.getWidth() / maxSize);
    int heightScale = (int) Math.ceil((float) decoder.getHeight() / maxSize);
    return Math.max(1, Math.max(Utils.nextPow2(widthScale), Utils.nextPow2(heightScale)));
  }

  /**
   * Generates preview for this ImageRegionDecoder.
   *
   * It only need be called once in the instance factory.
   */
  @Nullable
  public static Bitmap generatePreview(ImageRegionDecoder decoder) {
    Rect rect = new Rect(0, 0, decoder.getWidth(), decoder.getHeight());
    int sample = previewSample(decoder);
    return decoder.decode(rect, DEFAULT_CONFIG, sample);
  }

  /**
   * Create a TiledDrawable.
   *
   * @param decoder the decoder to decode the image
   * @param executor The executor to execute decode tasks.
   *                 Usually, ImageRegionDecoder can't be used parallel.
   *                 One serial executor for one TiledDrawable should be enough.
   * @param shared If it's true, the decoder and the preview will not be recycled
   *               in {@link #recycle()}, and the decoder and the preview must not be
   *               recycled before {@link #recycle()} called. If it's false,
   *               the decoder and the preview should only be referenced by this
   *               TiledDrawable, and the decoder and the preview will be recycled
   *               in {@link #recycle()}.
   */
  public TiledDrawable(
      @NonNull ImageRegionDecoder decoder,
      @NonNull Bitmap preview,
      @NonNull Executor executor,
      boolean shared
  ) {
    this.decoder = decoder;
    this.preview = preview;
    this.previewSample = previewSample(decoder);
    this.executor = executor;
    this.shared = shared;

    tilesMap = new SparseArray<>();
    matrix = new Matrix();

    width = decoder.getWidth();
    height = decoder.getHeight();

    initTileMap(MAX_TEXTURE_SIZE);
  }

  private void initTileMap(int maxTileSize) {
    // The other sample levels except preview sample
    int sample = previewSample;
    for (;;) {
      if (sample == 1) {
        break;
      }

      // Update sample
      sample = sample / 2;
      int maxTileMappingSize = maxTileSize * sample;

      // Calculate tile count among x and y axis
      int xTiles = Utils.ceilDiv(width, maxTileMappingSize);
      int yTiles = Utils.ceilDiv(height, maxTileMappingSize);

      List<Tile> tiles = new ArrayList<>(xTiles * yTiles);
      for (int x = 0; x < xTiles; x++) {
        for (int y = 0; y < yTiles; y++) {
          Tile tile = new Tile();
          tile.drawable = this;
          tile.rect = new Rect(
              x * maxTileMappingSize,
              y * maxTileMappingSize,
              x == xTiles - 1 ? width : (x + 1) * maxTileMappingSize,
              y == yTiles - 1 ? height : (y + 1) * maxTileMappingSize
          );
          tiles.add(tile);
        }
      }
      tilesMap.put(sample, tiles);
    }
  }

  /**
   * Recycles this TiledDrawable. It will draw nothing.
   */
  @MainThread
  public void recycle() {
    if (recycled) {
      return;
    }
    recycled = true;

    for (int i = 0, len = tilesMap.size(); i < len; i++) {
      for (Tile tile : tilesMap.valueAt(i)) {
        tile.drawable = null;
        if (tile.task != null) {
          tile.task.cancel(false);
        }
        if (tile.bitmap != null) {
          tile.bitmap.recycle();
          tile.bitmap = null;
        }
      }
    }
    tilesMap.clear();

    if (!shared) {
      // decoder.recycle() might block the main thread
      new RecycleDecoderTask(decoder).executeOnExecutor(executor);
      preview.recycle();
    }
  }

  @Override
  public int getIntrinsicWidth() {
    return width;
  }

  @Override
  public int getIntrinsicHeight() {
    return height;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    throw new IllegalStateException("Please call draw(Canvas, RectF, RectF)!");
  }

  /*
   * Returns the result which is the biggest value
   * that is smaller than or equal to the sample
   * and is power of 2.
   */
  private int calculateSample(RectF src, RectF dst) {
    float scaleX = src.width() / dst.width();
    float scaleY = src.height() / dst.height();
    int sample = Math.min((int) scaleX, (int) scaleY);
    sample = Math.max(1, sample);
    return Utils.prevPow2(sample);
  }

  private void drawPreview(Canvas canvas, RectF src, RectF dst) {
    RectF pSrc = rectF1;
    RectF pDst = rectF2;
    pSrc.set(0, 0, width, height);
    Utils.mapRect(src, dst, pSrc, pDst);
    pSrc.set(0, 0, preview.getWidth(), preview.getHeight());
    matrix.setRectToRect(pSrc, pDst, Matrix.ScaleToFit.FILL);
    canvas.drawBitmap(preview, matrix, null);
  }

  private void decodeTile(Tile tile, int sample) {
    if (tile.task == null && !tile.failed) {
      new DecodeTileTask(decoder, tile, sample).executeOnExecutor(executor);
    }
  }

  private void drawTiles(Canvas canvas, RectF src, RectF dst, List<Tile> tiles, int sample) {
    List<Tile> toDraw = this.list1;
    boolean missTiles = false;

    for (Tile tile : tiles) {
      Rect rect = tile.rect;
      tile.visible = src.intersects(rect.left, rect.top, rect.right, rect.bottom);
      if (!tile.visible) {
        continue;
      }

      if (tile.bitmap == null) {
        missTiles = true;
        decodeTile(tile, sample);
      } else {
        toDraw.add(tile);
      }
    }

    if (missTiles) {
      // TODO doesn't work fine with image with alpha channel
      drawPreview(canvas, src, dst);
    }

    RectF tSrc = rectF1;
    RectF tDst = rectF2;
    for (final Tile tile : toDraw) {
      Bitmap bitmap = tile.bitmap;
      if (bitmap == null) {
        continue;
      }

      tSrc.set(tile.rect);
      Utils.mapRect(src, dst, tSrc, tDst);
      tSrc.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
      matrix.setRectToRect(tSrc, tDst, Matrix.ScaleToFit.FILL);
      canvas.drawBitmap(bitmap, matrix, null);
    }

    toDraw.clear();
  }

  private void gc() {
    for (int i = 0, len = tilesMap.size(); i < len; i++) {
      int sample = tilesMap.keyAt(i);
      List<Tile> list = tilesMap.valueAt(i);

      for (Tile tile : list) {
        // Skip current sample and visible
        if (sample == currentSample && tile.visible) {
          continue;
        }

        if (tile.bitmap != null) {
          decoder.recycle(tile.bitmap);
          tile.bitmap = null;
        }

        if (tile.task != null) {
          tile.task.cancel(false);
        }

        if (sample != currentSample) {
          tile.failed = false;
        }
      }
    }
  }

  @Override
  public void draw(@NonNull Canvas canvas, @NonNull RectF src, @NonNull RectF dst) {
    if (recycled) {
      return;
    }

    int sample = calculateSample(src, dst);
    sample = Math.min(previewSample, sample);
    currentSample = sample;

    int saved = canvas.save();
    canvas.clipRect(dst);
    if (sample == previewSample) {
      drawPreview(canvas, src, dst);
    } else {
      drawTiles(canvas, src, dst, tilesMap.get(sample, Collections.<Tile>emptyList()), sample);
    }
    canvas.restoreToCount(saved);

    gc();
  }

  @Override
  public void setAlpha(int alpha) {}

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {}

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  private static class Tile {
    private Drawable drawable;
    // Source rect, the rect of the source image
    private Rect rect;
    private Bitmap bitmap;
    // Mark the tile can be seen in the screen
    // It's only valid for the tiles in current sample
    private boolean visible;
    private AsyncTask task;
    private boolean failed;
  }

  private static class DecodeTileTask extends AsyncTask<Void, Void, Bitmap> {

    private final ImageRegionDecoder decoder;
    private final Tile tile;
    private final int sample;

    public DecodeTileTask(ImageRegionDecoder decoder, Tile tile, int sample) {
      this.decoder = decoder;
      this.tile = tile;
      this.sample = sample;
    }

    @Override
    protected void onPreExecute() {
      tile.task = this;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
      return decoder.decode(tile.rect, Bitmap.Config.ARGB_8888, sample);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      tile.task = null;
      tile.bitmap = bitmap;
      if (bitmap != null) {
        if (tile.drawable != null) {
          tile.drawable.invalidateSelf();
        }
      } else {
        tile.failed = true;
        Log.e(LOG_TAG, "Can't decode region " + tile.rect + " at sample " + sample);
      }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
      tile.task = null;
      if (bitmap != null) {
        decoder.recycle(bitmap);
      }
    }
  }

  private static class RecycleDecoderTask extends AsyncTask<Void, Void, Void> {

    private ImageRegionDecoder decoder;

    private RecycleDecoderTask(ImageRegionDecoder decoder) {
      this.decoder = decoder;
    }

    @Override
    protected Void doInBackground(Void... voids) {
      decoder.recycle();
      return null;
    }
  }
}
