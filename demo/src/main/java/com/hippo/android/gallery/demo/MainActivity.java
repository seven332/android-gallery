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
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hippo.android.gallery.GalleryView;
import com.hippo.android.gallery.ReversedHorizontalScrollLayout;
import com.hippo.android.gallery.ScrollLayoutManager;

public class MainActivity extends AppCompatActivity {

  private static final String[] IMAGE_URLS = {
      "http://www.gstatic.com/webp/gallery/1.jpg",
      "http://www.gstatic.com/webp/gallery/2.jpg",
      "http://www.gstatic.com/webp/gallery/3.jpg",
      "http://www.gstatic.com/webp/gallery/4.jpg",
      "http://www.gstatic.com/webp/gallery/5.jpg",
  };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ScrollLayoutManager layoutManager = new ScrollLayoutManager();
    layoutManager.setScrollLayout(new ReversedHorizontalScrollLayout());
    layoutManager.setPageInterval((int) (24 * getResources().getDisplayMetrics().density));

//    PagerLayoutManager layoutManager = new PagerLayoutManager();
//    layoutManager.setPagerLayout(new HorizontalPagerLayout());
//    layoutManager.setPageInterval((int) (24 * getResources().getDisplayMetrics().density));
//    layoutManager.setScaleType(PagerLayoutManager.SCALE_FIT_HEIGHT);

    GalleryView galleryView = findViewById(R.id.gallery_view);
    galleryView.setLayoutManager(layoutManager);
    galleryView.setAdapter(new Adapter(getLayoutInflater()));
  }

  private static class Adapter extends GalleryView.Adapter {

    private static final int TYPE_IMAGE = 0;
    private static final int TYPE_TEXT = 1;

    private LayoutInflater inflater;

    public Adapter(LayoutInflater inflater) {
      this.inflater = inflater;
    }

    @Override
    public GalleryView.Page onCreatePage(GalleryView parent, int type) {
      View view;
      if (type == TYPE_IMAGE) {
        view = inflater.inflate(R.layout.page_image, parent, false);
      } else {
        view = inflater.inflate(R.layout.page_text, parent, false);
      }
      return new GalleryView.Page(view);
    }

    @Override
    public void onDestroyPage(GalleryView.Page page) {}

    @Override
    public void onBindPage(GalleryView.Page page) {
      if (page.getType() == TYPE_IMAGE) {
        ImageView imageView = page.view.findViewById(R.id.image);

        RequestOptions myOptions = new RequestOptions()
            .dontTransform()
            .override(Target.SIZE_ORIGINAL);

        Glide.with(inflater.getContext())
            .load(IMAGE_URLS[page.getIndex() / 2])
            .apply(myOptions)
            .into(imageView);
      } else {
        TextView textView = page.view.findViewById(R.id.text);
        textView.setText("Image " + (page.getIndex() / 2 + 1));
      }
    }

    @Override
    public void onUnbindPage(GalleryView.Page page) {}

    @Override
    public int getPageCount() {
      return IMAGE_URLS.length * 2;
    }

    @Override
    public int getPageViewType(int index) {
      return index % 2 == 0 ? TYPE_IMAGE : TYPE_TEXT;
    }
  }
}
