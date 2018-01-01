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

import android.support.annotation.Nullable;

/**
 * Adapter handles Page creation, destroying, binding and unbinding.
 */
public abstract class GalleryAdapter {

  @Nullable
  private GalleryView view;

  void attach(GalleryView view) {
    if (this.view != null) {
      throw new IllegalStateException("This Adapter is already attached to a GalleryView.");
    }
    this.view = view;
  }

  void detach() {
    this.view = null;
  }

  GalleryPage createPage(GalleryView parent, int type) {
    GalleryPage page = onCreatePage(parent, type);
    page.type = type;
    return page;
  }

  /**
   * Creates a Page of the specified type.
   */
  public abstract GalleryPage onCreatePage(GalleryView parent, int type);

  void destroyPage(GalleryPage page) {
    onDestroyPage(page);
    page.type = GalleryView.INVALID_TYPE;
  }

  /**
   * Destroys the Page.
   *
   * The index of the Page is invalid.
   * The type of the Page is valid.
   */
  public abstract void onDestroyPage(GalleryPage page);

  void bindPage(GalleryPage page, int index) {
    page.index = index;
    onBindPage(page);
  }

  /**
   * Binds the Page.
   *
   * The view of the page is already attached to the GalleryView.
   *
   * The index of the Page is valid.
   * The type of the Page is valid.
   */
  public abstract void onBindPage(GalleryPage page);

  void unbindPage(GalleryPage page) {
    onUnbindPage(page);
    page.index = GalleryView.INVALID_INDEX;
  }

  /**
   * Unbinds the Page.
   *
   * The view of the page is already detached from the GalleryView.
   *
   * The index of the Page is valid.
   * The type of the Page is valid.
   */
  public abstract void onUnbindPage(GalleryPage page);

  /**
   * Returns the total count of all pages.
   */
  public abstract int getPageCount();

  /**
   * Returns the type of the page with the specified index.
   */
  public int getPageType(int index) {
    return 0;
  }

  /**
   * Notifies the page with the specified index is changed.
   */
  public final void notifyPageChanged(int index) {
    if (view != null) {
      view.notifyPageChanged(index);
    }
  }

  /**
   * Notifies all pages might be changed.
   */
  public final void notifyPageSetChanged() {
    if (view != null) {
      view.notifyPageSetChanged();
    }
  }
}
