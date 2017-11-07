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

package com.hippo.android.gallery;

/*
 * Created by Hippo on 2017/8/28.
 */

import android.view.View;

public class PagerLayoutManager extends GalleryView.LayoutManager {

  // Current screen first page index
  private int currentIndex = 0;

  // The percent of current screen to next screen or previous screen.
  // (-1.0, 1.0), positive for next screen, negative for previous screen.
  private float scrollPercent = 0.0f;

  // Number of images per screen
  private int screenVolume = 1;

  private PageLayout pageLayout;

  @Override
  public void layout(GalleryView.Nest nest, int width, int height) {


    // TODO Ensure currentIndex fits nest



    //nest.pinPage();


  }

  @Override
  public void scroll(GalleryView.Nest nest, int distanceX, int distanceY) {

  }



  private void layoutPage(GalleryView.Nest nest, int width, int height, int index) {
    // Ensure the page volume is not out of range
    int volume = Math.min(screenVolume, nest.getPageCount() - index);

    for (int i = 0; i < volume; ++i) {

      GalleryView.Page page = nest.pinPage(index);
      View view = page.view;


      //pageLayout.layout(view);


    }






  }

  public static abstract class PageLayout {



    public abstract void layout(View view, int left, int top, int right, int bottom);



  }













}
