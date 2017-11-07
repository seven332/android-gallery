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
import android.widget.TextView;
import com.hippo.android.gallery.GalleryView;
import com.hippo.android.gallery.ScrollLayoutManager;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);




    ScrollLayoutManager layoutManager = new ScrollLayoutManager();
    layoutManager.setPageLayout(new ScrollLayoutManager.VerticallyPageLayout());

    GalleryView galleryView = findViewById(R.id.gallery_view);
    galleryView.setLayoutManager(layoutManager);
    galleryView.setAdapter(new Adapter(getLayoutInflater()));
  }


  private static class Adapter extends GalleryView.Adapter {

    private LayoutInflater inflater;

    public Adapter(LayoutInflater inflater) {
      this.inflater = inflater;
    }

    @Override
    public GalleryView.Page onCreatePage(GalleryView parent, int type) {
      return new GalleryView.Page(inflater.inflate(R.layout.page, parent, false));
    }

    @Override
    public void onDestroyPage(GalleryView.Page page) {

    }

    @Override
    public void onBindPage(GalleryView.Page page) {
      ((TextView) page.view).setText(Integer.toString(page.getIndex()));
    }

    @Override
    public void onUnbindPage(GalleryView.Page page) {

    }

    @Override
    public int getPageCount() {
      return 100;
    }
  }
}
