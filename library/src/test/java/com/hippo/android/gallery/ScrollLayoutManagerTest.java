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
 * Created by Hippo on 2017/11/28.
 */

import static org.junit.Assert.assertEquals;

import android.view.View;
import android.view.ViewGroup;
import com.hippo.android.gallery.util.View1;
import com.hippo.android.gallery.util.View2;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ScrollLayoutManagerTest {

  private static final int PAGE_SIZE = 50;
  private static final int PAGE_INTERVAL = 10;
  private static final int GALLERY_SIZE = 150;

  private GalleryView galleryView;

  @Before
  public void setup() {
    galleryView = new GalleryView(RuntimeEnvironment.application);
  }

  @Test
  public void testEmpty() {
    TestParameters params = new TestParameters();
    params.pageCount = 0;
    testLayout(params);
  }

  @Test
  public void testShort() {
    for (int offset = -1000; offset < 1000; offset++) {
      testShort(offset);
    }
  }

  private void testShort(int offset) {
    for (int i = 0; i < 2; i++) {
      TestParameters params = new TestParameters();
      params.anchorIndex = i;
      params.anchorOffset = offset + i * (params.pageSize + params.pageInterval);
      params.pageCount = 2;
      params.pageStates.add(new PageState(0, 0, 0, GALLERY_SIZE, 50));
      params.pageStates.add(new PageState(1, 0, 60, GALLERY_SIZE, 110));
      testLayout(params);
    }
  }

  @Test
  public void testLong() {
    for (int offset = -1000; offset < 1000; offset++) {
      int firstIndex = -1;
      int firstOffset = Utils.clamp(offset, -140, 0) - PAGE_SIZE - PAGE_INTERVAL;
      for (int i = 1; i < 5; i++) {
        firstIndex++;
        firstOffset += PAGE_SIZE + PAGE_INTERVAL;
        if (firstOffset + PAGE_SIZE > 0) {
          if (firstIndex > 0) {
            firstIndex--;
            firstOffset -= PAGE_SIZE + PAGE_INTERVAL;
          }
          break;
        }
      }
      testLong(offset, firstIndex, firstOffset);
    }
  }

  private void testLong(int offset, int firstIndex, int firstOffset) {
    for (int i = 0; i < 5; i++) {
      TestParameters params = new TestParameters();
      params.anchorIndex = i;
      params.anchorOffset = offset + i * (params.pageSize + params.pageInterval);
      params.pageCount = 5;

      for (int j = 0; firstIndex + j < 5; j++) {
        int top = firstOffset + j * (params.pageSize + params.pageInterval);
        int bottom = top + params.pageSize;
        params.pageStates.add(new PageState(firstIndex + j, 0, top, GALLERY_SIZE, bottom));

        if (top >= params.gallerySize) {
          break;
        }
      }

      testLayout(params);
    }
  }

  private static class TestParameters {
    public int anchorIndex = 0;
    public int anchorOffset = 0;
    public int pageSize = PAGE_SIZE;
    public int pageInterval = PAGE_INTERVAL;
    public int pageCount = 0;
    public int gallerySize = GALLERY_SIZE;
    public List<PageState> pageStates = new LinkedList<>();
  }

  private static class PageState {
    public int index;
    public int left;
    public int top;
    public int right;
    public int bottom;
    public PageState(int index, int left, int top, int right, int bottom) {
      this.index = index;
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
    }
  }

  private void testLayout(TestParameters params) {
    ScrollLayoutManager layoutManager = new ScrollLayoutManager();
    layoutManager.setPageInterval(params.pageInterval);
    layoutManager.setScrollLayout(new VerticalScrollLayout());
    layoutManager.setAnchor(params.anchorIndex, params.anchorOffset);

    galleryView.setLayoutManager(layoutManager);
    galleryView.setAdapter(new TestAdapter(params.pageCount, params.pageSize));
    galleryView.forceLayout();
    galleryView.measure(
        View.MeasureSpec.makeMeasureSpec(params.gallerySize, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(params.gallerySize, View.MeasureSpec.EXACTLY)
    );
    galleryView.layout(0, 0, params.gallerySize, params.gallerySize);

    assertEquals(params.pageStates.size(), galleryView.getChildCount());
    for (PageState state : params.pageStates) {
      GalleryView.Page page = galleryView.getPageAt(state.index);
      View view = page.view;
      assertEquals(state.index, page.getIndex());
      assertEquals(state.left, view.getLeft());
      assertEquals(state.top, view.getTop());
      assertEquals(state.right, view.getRight());
      assertEquals(state.bottom, view.getBottom());
    }
  }

  public static class TestAdapter extends GalleryView.Adapter {

    private int count;
    private int size;

    public TestAdapter(int count, int size) {
      this.count = count;
      this.size = size;
    }

    @Override
    public GalleryView.Page onCreatePage(GalleryView parent, int type) {
      View view = type == 0 ? new View1(parent.getContext()) : new View2(parent.getContext());
      ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(size, size);
      view.setLayoutParams(lp);
      return new GalleryView.Page(view);
    }

    @Override
    public void onDestroyPage(GalleryView.Page page) {}

    @Override
    public void onBindPage(GalleryView.Page page) {}

    @Override
    public void onUnbindPage(GalleryView.Page page) {}

    @Override
    public int getPageCount() {
      return count;
    }
  }
}
