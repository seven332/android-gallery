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

package com.hippo.android.gallery.util;

/*
 * Created by Hippo on 2018/4/9.
 */

import static org.junit.Assert.assertEquals;

import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.android.gallery.GalleryAdapter;
import com.hippo.android.gallery.GalleryLayoutManager;
import com.hippo.android.gallery.GalleryPage;
import com.hippo.android.gallery.GalleryView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.robolectric.RuntimeEnvironment;

public final class GalleryViewWrapper {

  private int size;
  private GalleryView galleryView;
  private GalleryLayoutManager layoutManager;
  private Transformer transformer;

  private RectF rect = new RectF();
  private PointF point = new PointF();

  private GalleryViewWrapper(GalleryViewWrapper.Builder builder) {
    size = builder.size;

    galleryView = new GalleryView(RuntimeEnvironment.application);
    galleryView.setLayoutManager(builder.layoutManager);
    galleryView.setAdapter(new Adapter(builder.pageInfos));

    layoutManager = builder.layoutManager;

    transformer = builder.transformer;
    transformer.setUp(size, size);
  }

  public GalleryLayoutManager getLayoutManager() {
    return layoutManager;
  }

  public PageState newPageState(int index, int left, int top, int right, int bottom) {
    rect.set(left, top, right, bottom);
    transformer.transformRect(rect);
    return new PageState(index, (int) rect.left, (int) rect.top, (int) rect.right, (int) rect.bottom);
  }

  public void offsetPageState(PageState state, int dx, int dy) {
    point.set(dx, dy);
    transformer.transformOffset(point);
    state.offset((int) point.x, (int) point.y);
  }

  public void scroll(float dx, float dy) {
    point.set(dx, dy);
    transformer.transformOffset(point);
    galleryView.scroll(point.x, point.y);
  }

  public void scale(int x, int y, float factor) {
    point.set(x, y);
    transformer.transformPoint(point);
    galleryView.scale(point.x, point.y, factor);
  }

  public void layout() {
    galleryView.forceLayout();
    galleryView.measure(
        View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY)
    );
    galleryView.layout(0, 0, size, size);
  }

  public void assertPages(List<PageState> expected) {
    assertPages(expected, null);
  }

  public void assertPages(List<PageState> expected, String message) {
    List<PageState> actual = new ArrayList<>(galleryView.getPages())
        .stream()
        .sorted((o1, o2) -> o1.getIndex() - o2.getIndex())
        .map(it -> {
          View view = it.view;
          return new PageState(it.getIndex(), view.getLeft(), view.getTop(),
              view.getRight(), view.getBottom());
        })
        .collect(Collectors.toList());
    assertEquals(message, expected, actual);
  }

  private static class Adapter extends GalleryAdapter {

    public List<PageInfo> pageInfos;

    public Adapter(List<PageInfo> pageInfos) {
      this.pageInfos = pageInfos;
    }

    @NonNull
    @Override
    public GalleryPage onCreatePage(GalleryView parent, int type) {
      return new GalleryPage(new FlexibleView(parent.getContext()));
    }

    @Override
    public void onDestroyPage(GalleryPage page) {}

    @Override
    public void onBindPage(GalleryPage page) {
      int index = page.getIndex();
      PageInfo info = pageInfos.get(index);
      ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(info.size, info.size);
      page.view.setLayoutParams(lp);
    }

    @Override
    public void onUnbindPage(GalleryPage page) {}

    @Override
    public int getPageCount() {
      return pageInfos.size();
    }
  }

  public static class Builder {

    private int size;
    private GalleryLayoutManager layoutManager;
    private Transformer transformer;
    private List<PageInfo> pageInfos = new ArrayList<>();

    public Builder size(int size) {
      this.size = size;
      return this;
    }

    public Builder layoutManager(GalleryLayoutManager layoutManager) {
      this.layoutManager = layoutManager;
      return this;
    }

    public Builder transformer(Transformer transformer) {
      this.transformer = transformer;
      return this;
    }

    public Builder add(int size, boolean flexible) {
      pageInfos.add(new PageInfo(size, flexible));
      return this;
    }

    public GalleryViewWrapper build() {
      return new GalleryViewWrapper(this);
    }
  }

  private static class PageInfo {

    int size;
    boolean flexible;

    public PageInfo(int size, boolean flexible) {
      this.size = size;
      this.flexible = flexible;
    }
  }
}
