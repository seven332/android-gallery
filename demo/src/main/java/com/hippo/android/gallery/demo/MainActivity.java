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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hippo.android.gallery.BaseGestureHandler;
import com.hippo.android.gallery.GalleryAdapter;
import com.hippo.android.gallery.GalleryPage;
import com.hippo.android.gallery.GalleryView;
import com.hippo.android.gallery.ScrollLayoutManager;
import com.hippo.android.gesture.GestureRecognizer;

public class MainActivity extends AppCompatActivity {

  private static final String[] IMAGE_URLS = {
      "http://www.gstatic.com/webp/gallery/1.jpg",
      "http://www.gstatic.com/webp/gallery/2.jpg",
      "http://www.gstatic.com/webp/gallery/3.jpg",
      "http://www.gstatic.com/webp/gallery/4.jpg",
      "http://www.gstatic.com/webp/gallery/5.jpg",
  };

  private GalleryView view;
  private GalleryViewStyle style = new GalleryViewStyle();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    view = findViewById(R.id.gallery_view);
    view.setAdapter(new Adapter(getLayoutInflater()));
    view.setGestureHandler(new BaseGestureHandler());
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

  private static class Adapter extends GalleryAdapter {

    private static final int TYPE_IMAGE_TRANSFORM = 0;
    private static final int TYPE_IMAGE_FLEXIBLE = 1;
    private static final int TYPE_TEXT = 2;

    private LayoutInflater inflater;

    public Adapter(LayoutInflater inflater) {
      this.inflater = inflater;
    }

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

    @Override
    public void onBindPage(GalleryPage page) {
      if (page.getType() != TYPE_TEXT) {
        ImageView imageView = page.view.findViewById(R.id.image);

        RequestOptions myOptions = new RequestOptions()
            .dontTransform()
            .override(Target.SIZE_ORIGINAL);

        GlideApp.with(inflater.getContext())
            .load(IMAGE_URLS[page.getIndex() / 2])
            .apply(myOptions)
            .into(imageView);
      } else {
        TextView textView = page.view.findViewById(R.id.text);
        textView.setText("Image " + (page.getIndex() / 2 + 1));
      }
    }

    @Override
    public void onUnbindPage(GalleryPage page) {}

    @Override
    public int getPageCount() {
      return IMAGE_URLS.length * 2;
    }

    @Override
    public int getPageType(int index) {
      return index % 2 == 0
          ? (getGalleryView().getLayoutManager() instanceof ScrollLayoutManager ? TYPE_IMAGE_FLEXIBLE : TYPE_IMAGE_TRANSFORM)
          : TYPE_TEXT;
    }
  }
}
