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
 * Created by Hippo on 2017/12/31.
 */

import android.view.View;

/**
 * Page of a GalleryView, the base element to lay.
 */
public class GalleryPage {

  /**
   * The view of this Page.
   */
  public final View view;

  int index = GalleryView.INVALID_INDEX;
  int type = GalleryView.INVALID_TYPE;

  /*
   * In layout, the pinned of all attached valid page are set to false at the beginning,
   * pining a page to set the pinned to true.
   * Remove the pages whose pinned is false.
   */
  boolean pinned = false;

  public GalleryPage(View view) {
    this.view = view;
  }

  /**
   * Returns the index of this Page.
   * It's valid from {@link GalleryAdapter#onBindPage(GalleryPage)}
   * to {@link GalleryAdapter#onUnbindPage(GalleryPage)},
   * except {@link GalleryAdapter#onUnbindPage(GalleryPage)}.
   */
  public int getIndex() {
    return index;
  }

  /**
   * Returns the type of this Page.
   * It's valid until the page destroyed.
   */
  public int getType() {
    return type;
  }

  @Override
  public String toString() {
    return "Page{" + Integer.toHexString(hashCode()) + " index=" + index + ", type=" + type +
        ", pinned=" + pinned + "}";
  }
}
