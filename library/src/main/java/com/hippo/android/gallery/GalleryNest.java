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

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public final class GalleryNest {

  private static final String LOG_TAG = "GalleryNest";

  private static final int MAX_PAGE = 5;

  // Pages which are attached to GalleryView
  @SuppressLint("UseSparseArrays")
  private Map<Integer, GalleryPage> pages = new HashMap<>();
  // Page cache, key is page type
  @SuppressLint("UseSparseArrays")
  private Map<Integer, Stack<GalleryPage>> cache = new HashMap<>();

  final GalleryView view;

  @Nullable
  GalleryAdapter adapter;

  GalleryNest(GalleryView view) {
    this.view = view;
  }

  /**
   * Returns an unmodifiable collection of all attached page.
   */
  public Collection<GalleryPage> getPages() {
    if (view.inLayout) throw new IllegalStateException("Can't get pages during laying");
    return Collections.unmodifiableCollection(pages.values());
  }

  // Remove all page, clear cache
  void reset() {
    if (view.inLayout) throw new IllegalStateException("Can't reset Nest during laying");

    // Remove all views attached the GalleryView
    view.removeAllViews();

    if (adapter != null) {
      // Unbind and destroy all attached page
      for (GalleryPage page : pages.values()) {
        adapter.unbindPage(page);
        adapter.destroyPage(page);
      }
      pages.clear();

      // Destroy all cached page
      for (Stack<GalleryPage> stack : cache.values()) {
        for (GalleryPage page : stack) {
          adapter.destroyPage(page);
        }
      }
      cache.clear();
    }
  }

  /**
   * Layout the GalleryView attached to this Nest.
   * The GalleryView should be measured.
   */
  public void layout(@NonNull GalleryLayoutManager layoutManager) {
    if (view.inLayout) {
      Log.e(LOG_TAG, "Cannot layout GalleryView recursively");
      return;
    }

    if (adapter == null) {
      Log.e(LOG_TAG, "Cannot layout without a Adapter set");
      return;
    }

    int width = view.getWidth();
    int height = view.getHeight();

    startLayout();
    if (width > 0 && height > 0 && adapter.getPageCount() > 0) {
      layoutManager.layout(width, height);
    }
    endLayout();
  }

  private void startLayout() {
    view.inLayout = true;

    Iterator<GalleryPage> iterator = pages.values().iterator();
    while (iterator.hasNext()) {
      GalleryPage page = iterator.next();
      if (page.pinned) {
        // Mark the page unpinned if it's pinned
        page.pinned = false;
      } else {
        // Unpin the page if it's not pinned
        unpinPageInternal(page);
        iterator.remove();
      }
    }
  }

  private void endLayout() {
    Iterator<GalleryPage> iterator = pages.values().iterator();
    while (iterator.hasNext()) {
      GalleryPage page = iterator.next();
      // Remove all unpinned pages
      if (!page.pinned) {
        unpinPageInternal(page);
        iterator.remove();
      }
    }

    view.inLayout = false;
  }

  // pages still keeps the page
  private void unpinPageInternal(GalleryPage page) {
    page.pinned = false;
    view.removeView(page.view);
    //noinspection ConstantConditions
    adapter.unbindPage(page);

    Stack<GalleryPage> stack = cache.get(page.getType());
    if (stack == null) {
      stack = new Stack<>();
      cache.put(page.getType(), stack);
    }

    if (stack.size() < MAX_PAGE) {
      stack.push(page);
    } else {
      adapter.destroyPage(page);
    }
  }

  /**
   * Returns {@code true} if the view of the page is attached.
   */
  public boolean containPage(int index) {
    if (!view.inLayout) {
      throw new IllegalStateException("Cannot only call containPage() in layout");
    }

    if (adapter == null) {
      throw new IllegalStateException("Don't unset adapter in layout");
    }

    GalleryPage page = pages.get(index);
    return page != null && adapter.getPageType(index) == page.getType();
  }

  /**
   * Pins the page with the the index to gallery.
   * If the page is already pinned, just return it.
   */
  @NonNull
  public GalleryPage pinPage(int index) {
    if (!view.inLayout) {
      throw new IllegalStateException("Cannot only call pinPage() in layout");
    }

    if (adapter == null) {
      throw new IllegalStateException("Don't unset adapter in layout");
    }

    // Get from unpinned attached page
    GalleryPage page = pages.get(index);
    if (page != null && adapter.getPageType(index) == page.getType()) {
      page.pinned = true;
      return page;
    }

      /*
       * The page isn't attached.
       * Get the page, bind it and attach it.
       */

    // Get from cache
    page = null;
    int type = adapter.getPageType(index);
    Stack<GalleryPage> stack = cache.get(type);
    if (stack != null && !stack.empty()) {
      page = stack.pop();
    }

    // Create page
    if (page == null) {
      page = adapter.createPage(view, type);
    }

    // Bind and attach
    page.pinned = true;

    view.addView(page.view);
    adapter.bindPage(page, index);
    pages.put(page.getIndex(), page);

    return page;
  }

  /**
   * Unpin the page.
   */
  public void unpinPage(GalleryPage page) {
    if (!view.inLayout) {
      throw new IllegalStateException("Cannot only call unpinPage() in layout");
    }

    if (adapter == null) {
      throw new IllegalStateException("Don't unset adapter in layout");
    }

    pages.remove(page.getIndex());
    unpinPageInternal(page);
  }

  /**
   * Returns the count of pages.
   */
  public int getPageCount() {
    if (adapter == null) {
      throw new IllegalStateException("Can only call getPageCount() in layout");
    }
    return adapter.getPageCount();
  }

  /**
   * Returns the page with the specified index.
   * Returns {@code null} if the GalleryView is in laying.
   */
  @Nullable
  public GalleryPage getPageAt(int index) {
    if (view.inLayout) return null;

    GalleryPage page = pages.get(index);
    return page != null && page.pinned ? page : null;
  }

  void notifyPageChanged(int index) {
    if (view.inLayout) return;
    GalleryPage page = pages.get(index);
    if (page == null) return;

    page.pinned = false;
    view.requestLayout();
  }

  void notifyPageSetChanged() {
    if (view.inLayout) return;
    if (pages.isEmpty()) return;

    for (GalleryPage page : pages.values()) {
      page.pinned = false;
    }
    view.requestLayout();
  }
}
