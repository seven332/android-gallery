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

package com.hippo.android.gallery.demo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.bumptech.glide.request.RequestOptions;
import com.hippo.android.gallery.GalleryAdapter;
import com.hippo.android.gallery.GalleryGestureHandler;
import com.hippo.android.gallery.GalleryPage;
import com.hippo.android.gallery.GalleryView;
import com.hippo.android.gallery.ScrollLayoutManager;
import com.hippo.android.gallery.drawable.CutAccurateDrawable;
import com.hippo.android.gallery.drawable.CutDrawable;
import com.hippo.android.gallery.drawable.TiledDrawable;
import com.hippo.android.gesture.GestureRecognizer;
import com.hippo.gallery.integration.glide.ByteBufferTiledDrawableDecoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  private static final String[] IMAGE_URLS = {
      "http://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Flowers_blooming_outside_Dunvegan_Castle_during_summer.png/398px-Flowers_blooming_outside_Dunvegan_Castle_during_summer.png",
      "http://www.gstatic.com/webp/gallery/1.jpg",
      "http://www.gstatic.com/webp/gallery/2.jpg",
      "http://www.gstatic.com/webp/gallery/3.jpg",
      "http://www.gstatic.com/webp/gallery/4.jpg",
      "http://www.gstatic.com/webp/gallery/5.jpg",
      "https://upload.wikimedia.org/wikipedia/commons/4/4e/Macaca_nigra_self-portrait_large.jpg",
      "https://upload.wikimedia.org/wikipedia/commons/3/3d/LARGE_elevation.jpg",
  };

  private CallbackGalleryView view;
  private GalleryViewStyle style = new GalleryViewStyle();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Adapter adapter = new Adapter(getLayoutInflater());

    view = findViewById(R.id.gallery_view);
    view.setAdapter(adapter);
    view.setAfterLayoutListener(adapter);
    view.setGestureHandler(new GalleryGestureHandler());
    view.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
    GestureRecognizer gestureRecognizer = view.getGestureRecognizer();
    gestureRecognizer.setScaleEnabled(true);

    style.apply(view);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        GalleryViewStyleView view = new GalleryViewStyleView(this);
        view.setStyle(MainActivity.this.view, style);
        dialog.setContentView(view);
        dialog.show();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private static class Adapter extends GalleryAdapter implements CallbackGalleryView.AfterLayoutListener {

    private List<Object> items = new ArrayList<>();

    private Context context;
    private LayoutInflater inflater;

    private List<Operation> pendingOperations = new LinkedList<>();

    public Adapter(LayoutInflater inflater) {
      this.context = inflater.getContext();
      this.inflater = inflater;
      for (String url : IMAGE_URLS) {
        items.add(new TextItem(url));
        items.add(new ImageItem(url, ImageItem.WHOLE));
      }
    }

    @NonNull
    @Override
    public GalleryPage onCreatePage(GalleryView parent, int type) {
      View view;
      if (type == TYPE_IMAGE_TRANSFORM) {
        view = inflater.inflate(R.layout.page_image_transform, parent, false);
      } else if (type == TYPE_IMAGE_FLEXIBLE) {
        view = inflater.inflate(R.layout.page_image_flexible, parent, false);
      } else {
        view = inflater.inflate(R.layout.page_text, parent, false);
      }
      return new GalleryPage(view);
    }

    @Override
    public void onDestroyPage(GalleryPage page) {}

    private static boolean needToBeCut(Drawable drawable) {
      int width = drawable.getIntrinsicWidth();
      int height = drawable.getIntrinsicHeight();
      if (width <= 0 || height <= 0) {
        return false;
      }

      return width > height;
    }

    @Override
    public void onBindPage(GalleryPage page) {
      Object item = items.get(page.getIndex());

      if (item instanceof ImageItem) {
        ImageItem imageItem = (ImageItem) item;

        DrawableView imageView = page.view.findViewById(R.id.image);

        RequestOptions options = new RequestOptions()
            .dontTransform()
            .override(Target.SIZE_ORIGINAL)
            .placeholder(AppCompatResources.getDrawable(context, R.drawable.ic_image_black_24dp))
            .error(AppCompatResources.getDrawable(context, R.drawable.ic_broken_image_black_24dp))
            .set(ByteBufferTiledDrawableDecoder.ENABLE, true);

        GlideApp.with(inflater.getContext())
            .asDrawable()
            .load(imageItem.url)
            .apply(options)
            .into(new Target(imageView));
      } else {
        TextView textView = page.view.findViewById(R.id.text);
        textView.setText(((TextItem) item).text);
      }
    }

    @Override
    public void onUnbindPage(GalleryPage page) {}

    @Override
    public int getPageCount() {
      return items.size();
    }

    @Override
    public int getPageType(int index) {
      Object item = items.get(index);
      return item instanceof ImageItem
          ? (getGalleryView().getLayoutManager() instanceof ScrollLayoutManager ? TYPE_IMAGE_FLEXIBLE : TYPE_IMAGE_TRANSFORM)
          : TYPE_TEXT;
    }

    private int fixIndex(int index) {
      for (Operation operation : pendingOperations) {
        if (operation.mode == Operation.ADD && index >= operation.index) {
          index = index + 1;
        } else if (operation.mode == Operation.REMOVE && index < operation.index) {
          index = index - 1;
        }
      }

      return index;
    }

    private void addItem(int index, Object item) {
      if (getGalleryView().isInLayout2()) {
        index = fixIndex(index);
        pendingOperations.add(new Operation(Operation.ADD, index, item));
      } else {
        items.add(index, item);
        notifyPageInserted(index);
      }
    }

    private void removeItem(int index) {
      if (getGalleryView().isInLayout2()) {
        index = fixIndex(index);
        pendingOperations.add(new Operation(Operation.REMOVE, index, null));
      } else {
        items.remove(index);
        notifyPageRemoved(index);
      }
    }

    @Override
    public void onAfterLayout() {
      for (Operation operation : pendingOperations) {
        if (operation.mode == Operation.ADD) {
          items.add(operation.index, operation.item);
          notifyPageInserted(operation.index);
        } else {
          items.remove(operation.index);
          notifyPageRemoved(operation.index);
        }
      }

      pendingOperations.clear();
    }

    private class Target extends DrawableViewTarget {

      public Target(DrawableView view) {
        super(view);
      }

      @Override
      protected void setResource(@Nullable Drawable resource) {
        setResourceInternal(resource);
      }

      private void setResourceInternal(@Nullable Drawable resource) {
        if (resource == null) {
          //Log.d("TAG", "resource = null");
          super.setResource(resource);
          return;
        }

        GalleryPage page = getGalleryView().getPageByView(getView());
        if (page == null) {
          Utils.recycle(resource);
          return;
        }

        int index = page.getIndex();
        if (index == GalleryView.INVALID_INDEX) {
          Utils.recycle(resource);
          return;
        }

        ImageItem item = (ImageItem) items.get(index);
        boolean needToBeCut = needToBeCut(resource);
        boolean hasBeenCut = item.part != ImageItem.WHOLE;

        if (needToBeCut && !hasBeenCut) {
          item.part = ImageItem.LEFT;
          addItem(index + 1, new ImageItem(item.url, ImageItem.RIGHT));
        } else if (!needToBeCut && hasBeenCut) {
          item.part = ImageItem.WHOLE;
          if (index > 0 && items.get(index - 1) instanceof ImageItem &&
              ((ImageItem) items.get(index - 1)).url.equals(item.url)) {
            removeItem(index - 1);
          }
          if (index < items.size() - 1 && items.get(index + 1) instanceof ImageItem &&
              ((ImageItem) items.get(index + 1)).url.equals(item.url)) {
            removeItem(index + 1);
          }
        }

        if (item.part != ImageItem.WHOLE) {
          CutDrawable cutDrawable;
          if (resource instanceof TiledDrawable) {
            cutDrawable = new CutAccurateDrawable();
          } else {
            cutDrawable = new CutDrawable();
          }
          if (item.part == ImageItem.LEFT) {
            cutDrawable.cutPercent(0.0f, 0.0f, 0.5f, 1.0f);
          } else if (item.part == ImageItem.RIGHT) {
            cutDrawable.cutPercent(0.5f, 0.0f, 1.0f, 1.0f);
          }
          cutDrawable.setDrawable(resource);
          resource = cutDrawable;
        }

        super.setResource(resource);
      }
    }
  }

  private static final int TYPE_TEXT = 0;
  private static final int TYPE_IMAGE_FLEXIBLE = 1;
  private static final int TYPE_IMAGE_TRANSFORM = 2;

  static class ImageItem {

    static final int WHOLE = 0;
    static final int LEFT = 1;
    static final int RIGHT = 2;

    String url;
    int part;

    public ImageItem(String url, int part) {
      this.url = url;
      this.part = part;
    }
  }

  static class TextItem {
    String text;

    public TextItem(String text) {
      this.text = text;
    }
  }

  static class Operation {

    private static final int ADD = 0;
    private static final int REMOVE = 0;

    int mode;
    int index;
    Object item;

    public Operation(int mode, int index, Object item) {
      this.mode = mode;
      this.index = index;
      this.item = item;
    }
  }
}
