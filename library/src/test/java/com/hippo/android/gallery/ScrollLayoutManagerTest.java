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

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import com.hippo.android.gallery.util.AxisSwap;
import com.hippo.android.gallery.util.HorizontalFlip;
import com.hippo.android.gallery.util.NoOp;
import com.hippo.android.gallery.util.Transformer;
import com.hippo.android.gallery.util.TransformerChain;
import com.hippo.android.gallery.util.VerticalFlip;
import com.hippo.android.gallery.util.View1;
import com.hippo.android.gallery.util.View2;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ScrollLayoutManagerTest {

  private static final int PAGE_SIZE = 50;
  private static final int PAGE_INTERVAL = 10;
  private static final int GALLERY_SIZE = 150;

  private GalleryView galleryView;

  private ScrollLayoutManager.ScrollLayout scrollLayout = new ReversedHorizontalScrollLayout();
  private Transformer transformer = new TransformerChain(new AxisSwap(), new HorizontalFlip());

  private Rect rect = new Rect();
  private Point point = new Point();

  @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-{2}")
  public static List<Object[]> data() {
    List<Object[]> parameters = new LinkedList<>();

    parameters.add(new Object[]{
        new VerticalScrollLayout(),
        new NoOp(),
        VerticalScrollLayout.class.getSimpleName(),
    });

    parameters.add(new Object[]{
        new ReversedVerticalScrollLayout(),
        new VerticalFlip(),
        ReversedVerticalScrollLayout.class.getSimpleName(),
    });

    parameters.add(new Object[]{
        new HorizontalScrollLayout(),
        new HorizontalFlip(),
        HorizontalScrollLayout.class.getSimpleName(),
    });

    parameters.add(new Object[]{
        new ReversedHorizontalScrollLayout(),
        new TransformerChain(new AxisSwap(), new HorizontalFlip()),
        HorizontalScrollLayout.class.getSimpleName(),
    });

    return parameters;
  }

  public ScrollLayoutManagerTest(
      ScrollLayoutManager.ScrollLayout scrollLayout,
      Transformer transformer,
      String testName
  ) {
    this.scrollLayout = new VerticalScrollLayout();
    this.transformer = new NoOp();
  }

  @Before
  public void setup() {
    galleryView = new GalleryView(RuntimeEnvironment.application);
    transformer.setUp(GALLERY_SIZE, GALLERY_SIZE);
  }

  private PageState newPageState(int index, int left, int top, int right, int bottom) {
    rect.set(left, top, right, bottom);
    transformer.transformRect(rect);
    return new PageState(index, rect.left, rect.top, rect.right, rect.bottom);
  }

  private void offsetPageState(PageState state, int dx, int dy) {
    point.set(dx, dy);
    transformer.transformOffset(point);
    state.offset(point.x, point.y);
  }

  private void scrollView(View view, int dx, int dy) {
    point.set(dx, dy);
    transformer.transformOffset(point);
    view.scrollBy(point.x, point.y);
  }

  @Test
  public void testEmpty() {
    TestParameters params = new TestParameters();
    params.pageCount = 0;
    layout(params);

    assertPages(Collections.emptyList());
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
      layout(params);

      List<PageState> states = new LinkedList<>();
      states.add(newPageState(0, 0, 0, GALLERY_SIZE, 50));
      states.add(newPageState(1, 0, 60, GALLERY_SIZE, 110));
      assertPages(states);
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
      layout(params);

      List<PageState> states = new LinkedList<>();
      for (int j = 0; firstIndex + j < 5; j++) {
        int top = firstOffset + j * (params.pageSize + params.pageInterval);
        int bottom = top + params.pageSize;
        states.add(newPageState(firstIndex + j, 0, top, GALLERY_SIZE, bottom));
        if (top >= params.gallerySize) {
          break;
        }
      }
      assertPages(states);
    }
  }

  @Test
  public void testScroll() {
    TestParameters params = new TestParameters();
    params.anchorIndex = 1;
    params.anchorOffset = -10;
    params.pageCount = 8;
    layout(params);

    List<PageState> states = new LinkedList<>();
    states.add(newPageState(0, 0, -70, GALLERY_SIZE, -20));
    states.add(newPageState(1, 0, -10, GALLERY_SIZE, 40));
    states.add(newPageState(2, 0, 50, GALLERY_SIZE, 100));
    states.add(newPageState(3, 0, 110, GALLERY_SIZE, 160));
    states.add(newPageState(4, 0, 170, GALLERY_SIZE, 220));
    assertPages(states);

    scrollView(galleryView, 0, 40);
    for (PageState state : states) {
      offsetPageState(state, 0, 40);
    }
    states.remove(states.size() - 1);
    assertPages(states);

    scrollView(galleryView, 0, -140);
    for (PageState state : states) {
      offsetPageState(state, 0, -140);
    }
    states.remove(0);
    states.remove(0);
    states.add(newPageState(4, 0, 70, GALLERY_SIZE, 120));
    states.add(newPageState(5, 0, 130, GALLERY_SIZE, 180));
    states.add(newPageState(6, 0, 190, GALLERY_SIZE, 240));
    assertPages(states);

    scrollView(galleryView, 0, -140);
    for (PageState state : states) {
      offsetPageState(state, 0, -140);
    }
    states.remove(0);
    states.remove(0);
    states.add(newPageState(7, 0, 110, GALLERY_SIZE, 160));
    assertPages(states);

    scrollView(galleryView, 0, -10);
    for (PageState state : states) {
      offsetPageState(state, 0, -10);
    }
    assertPages(states);

    scrollView(galleryView, 0, -10);
    assertPages(states);
  }

  @Test
  public void testSmallScroll() {
    // TODO Test multiple small scrolls whose step is smaller than 1
  }

  private static class TestParameters {
    public int anchorIndex = 0;
    public int anchorOffset = 0;
    public int pageSize = PAGE_SIZE;
    public int pageInterval = PAGE_INTERVAL;
    public int pageCount = 0;
    public int gallerySize = GALLERY_SIZE;
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

    public void offset(int dx, int dy) {
      left += dx;
      right += dx;
      top += dy;
      bottom += dy;
    }
  }

  private void layout(TestParameters params) {
    ScrollLayoutManager layoutManager = new ScrollLayoutManager();
    layoutManager.setPageInterval(params.pageInterval);
    layoutManager.setScrollLayout(scrollLayout);
    layoutManager.setAnchor(params.anchorIndex, params.anchorOffset);

    galleryView.setLayoutManager(layoutManager);
    galleryView.setAdapter(new TestAdapter(params.pageCount, params.pageSize));
    galleryView.forceLayout();
    galleryView.measure(
        View.MeasureSpec.makeMeasureSpec(params.gallerySize, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(params.gallerySize, View.MeasureSpec.EXACTLY)
    );
    galleryView.layout(0, 0, params.gallerySize, params.gallerySize);
  }

  private void assertPages(List<PageState> states) {
    assertEquals(states.size(), galleryView.getChildCount());
    for (PageState state : states) {
      GalleryPage page = galleryView.getPageAt(state.index);
      View view = page.view;
      assertEquals(state.index, page.getIndex());
      assertEquals(state.left, view.getLeft());
      assertEquals(state.top, view.getTop());
      assertEquals(state.right, view.getRight());
      assertEquals(state.bottom, view.getBottom());
    }
  }

  public static class TestAdapter extends GalleryAdapter {

    private int count;
    private int size;

    public TestAdapter(int count, int size) {
      this.count = count;
      this.size = size;
    }

    @Override
    public GalleryPage onCreatePage(GalleryView parent, int type) {
      View view = type == 0 ? new View1(parent.getContext()) : new View2(parent.getContext());
      ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(size, size);
      view.setLayoutParams(lp);
      return new GalleryPage(view);
    }

    @Override
    public void onDestroyPage(GalleryPage page) {}

    @Override
    public void onBindPage(GalleryPage page) {}

    @Override
    public void onUnbindPage(GalleryPage page) {}

    @Override
    public int getPageCount() {
      return count;
    }
  }
}
