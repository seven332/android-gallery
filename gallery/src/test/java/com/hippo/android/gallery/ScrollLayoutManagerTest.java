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

import com.hippo.android.gallery.util.AxisSwap;
import com.hippo.android.gallery.util.GalleryViewWrapper;
import com.hippo.android.gallery.util.HorizontalFlip;
import com.hippo.android.gallery.util.NoOp;
import com.hippo.android.gallery.util.PageState;
import com.hippo.android.gallery.util.Transformer;
import com.hippo.android.gallery.util.TransformerChain;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ScrollLayoutManagerTest {

  private static final int PAGE_SIZE = 50;
  private static final int PAGE_INTERVAL = 10;
  private static final int GALLERY_SIZE = 150;

  @ParameterizedRobolectricTestRunner.Parameters(name = "{index}-{2}")
  public static List<Object[]> tests() {
    List<Object[]> parameters = new LinkedList<>();

    parameters.add(new Object[]{
        new VerticalScrollLayout(),
        new NoOp(),
        VerticalScrollLayout.class.getSimpleName(),
    });

    parameters.add(new Object[]{
        new HorizontalScrollLayout(),
        new AxisSwap(),
        HorizontalScrollLayout.class.getSimpleName(),
    });

    parameters.add(new Object[]{
        new ReversedHorizontalScrollLayout(),
        new TransformerChain(new AxisSwap(), new HorizontalFlip()),
        ReversedHorizontalScrollLayout.class.getSimpleName(),
    });

    return parameters;
  }

  private ScrollLayoutManager.ScrollLayout scrollLayout;
  private Transformer transformer;

  public ScrollLayoutManagerTest(
      ScrollLayoutManager.ScrollLayout scrollLayout,
      Transformer transformer,
      @SuppressWarnings("unused") String testName
  ) {
    this.scrollLayout = scrollLayout;
    this.transformer = transformer;
  }

  private GalleryViewWrapper.Builder newBuilder() {
    ScrollLayoutManager slm = new ScrollLayoutManager();
    slm.setPageInterval(PAGE_INTERVAL);
    slm.setScrollLayout(scrollLayout);

    return new GalleryViewWrapper.Builder()
        .size(GALLERY_SIZE)
        .layoutManager(slm)
        .transformer(transformer);
  }

  @Test
  public void testNoPage() {
    GalleryViewWrapper gallery = newBuilder().build();
    gallery.layout();
    gallery.assertPages(Collections.emptyList(), "No pages");
  }

  /**
   * A few pages.
   */
  @Test
  public void testLayoutShort() {
    for (int offset = -1000; offset < 1000; offset++) {
      for (int anchorIndex = 0; anchorIndex < 2; anchorIndex++) {
        GalleryViewWrapper gallery = newBuilder()
            .add(PAGE_SIZE, false)
            .add(PAGE_SIZE, false)
            .build();

        ScrollLayoutManager slm = (ScrollLayoutManager) gallery.getLayoutManager();
        slm.setAnchor(
            anchorIndex,
            offset + anchorIndex * (PAGE_SIZE + PAGE_INTERVAL)
        );

        gallery.layout();

        List<PageState> states = new LinkedList<>();
        states.add(gallery.newPageState(0,
            0, 0, GALLERY_SIZE, PAGE_SIZE));
        states.add(gallery.newPageState(1,
            0, PAGE_SIZE + PAGE_INTERVAL, GALLERY_SIZE, PAGE_SIZE + PAGE_INTERVAL + PAGE_SIZE));
        gallery.assertPages(states, "offset=" + offset + ", anchorIndex=" + anchorIndex);
      }
    }
  }

  /**
   * A lot of pages.
   */
  @Test
  public void testLayoutLong() {
    for (int offset = -1000; offset < 1000; offset++) {
      // Find first attached page index and offset
      int firstIndex = -1;
      int firstOffset = Utils.clamp(offset, GALLERY_SIZE - 5 * PAGE_SIZE - 4 * PAGE_INTERVAL, 0) - PAGE_SIZE - PAGE_INTERVAL;
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

      for (int anchorIndex = 0; anchorIndex < 5; anchorIndex++) {
        GalleryViewWrapper gallery = newBuilder()
            .add(PAGE_SIZE, false)
            .add(PAGE_SIZE, false)
            .add(PAGE_SIZE, false)
            .add(PAGE_SIZE, false)
            .add(PAGE_SIZE, false)
            .build();

        ScrollLayoutManager slm = (ScrollLayoutManager) gallery.getLayoutManager();
        slm.setAnchor(
            anchorIndex,
            offset + anchorIndex * (PAGE_SIZE + PAGE_INTERVAL)
        );

        gallery.layout();

        List<PageState> states = new LinkedList<>();
        for (int i = 0; firstIndex + i < 5; i++) {
          int top = firstOffset + i * (PAGE_SIZE + PAGE_INTERVAL);
          int bottom = top + PAGE_SIZE;
          states.add(gallery.newPageState(firstIndex + i, 0, top, GALLERY_SIZE, bottom));
          if (top >= GALLERY_SIZE) {
            break;
          }
        }
        gallery.assertPages(states, "offset=" + offset + ", anchorIndex=" + anchorIndex);
      }
    }
  }

  @Test
  public void testScroll() {
    GalleryViewWrapper gallery = newBuilder()
        .add(PAGE_SIZE, false)
        .add(PAGE_SIZE, false)
        .add(PAGE_SIZE, false)
        .add(PAGE_SIZE, false)
        .add(PAGE_SIZE, false)
        .add(PAGE_SIZE, false)
        .add(PAGE_SIZE, false)
        .add(PAGE_SIZE, false)
        .build();
    ScrollLayoutManager slm = (ScrollLayoutManager) gallery.getLayoutManager();
    slm.setAnchor(1, -10);
    gallery.layout();

    List<PageState> states = new LinkedList<>();
    states.add(gallery.newPageState(0, 0, -70, GALLERY_SIZE, -20));
    states.add(gallery.newPageState(1, 0, -10, GALLERY_SIZE, 40));
    states.add(gallery.newPageState(2, 0, 50, GALLERY_SIZE, 100));
    states.add(gallery.newPageState(3, 0, 110, GALLERY_SIZE, 160));
    states.add(gallery.newPageState(4, 0, 170, GALLERY_SIZE, 220));
    gallery.assertPages(states);

    gallery.scroll(0, 40);
    for (PageState state : states) {
      gallery.offsetPageState(state, 0, 40);
    }
    states.remove(states.size() - 1);
    gallery.assertPages(states);

    gallery.scroll(0, -140);
    for (PageState state : states) {
      gallery.offsetPageState(state, 0, -140);
    }
    states.remove(0);
    states.remove(0);
    states.add(gallery.newPageState(4, 0, 70, GALLERY_SIZE, 120));
    states.add(gallery.newPageState(5, 0, 130, GALLERY_SIZE, 180));
    states.add(gallery.newPageState(6, 0, 190, GALLERY_SIZE, 240));
    gallery.assertPages(states);

    gallery.scroll(0, -140);
    for (PageState state : states) {
      gallery.offsetPageState(state, 0, -140);
    }
    states.remove(0);
    states.remove(0);
    states.add(gallery.newPageState(7, 0, 110, GALLERY_SIZE, 160));
    gallery.assertPages(states);

    gallery.scroll(0, -10);
    for (PageState state : states) {
      gallery.offsetPageState(state, 0, -10);
    }
    gallery.assertPages(states);

    gallery.scroll(0, -10);
    gallery.assertPages(states);
  }

  @Test
  public void testSmallScroll() {
    // TODO Test multiple small scrolls whose step is smaller than 1
  }

  @Test
  public void testScale() {
    GalleryViewWrapper gallery = newBuilder()
        .add(PAGE_SIZE, true)
        .add(PAGE_SIZE, true)
        .add(PAGE_SIZE, true)
        .add(PAGE_SIZE, true)
        .add(PAGE_SIZE, true)
        .build();
    ScrollLayoutManager slm = (ScrollLayoutManager) gallery.getLayoutManager();
    slm.setAnchor(1, -10);
    gallery.layout();

    List<PageState> states = new LinkedList<>();
    states.add(gallery.newPageState(0, 0, -70, GALLERY_SIZE, -20));
    states.add(gallery.newPageState(1, 0, -10, GALLERY_SIZE, 40));
    states.add(gallery.newPageState(2, 0, 50, GALLERY_SIZE, 100));
    states.add(gallery.newPageState(3, 0, 110, GALLERY_SIZE, 160));
    states.add(gallery.newPageState(4, 0, 170, GALLERY_SIZE, 220));
    gallery.assertPages(states);

    gallery.scale(50, 70, 1.5f);
    states = new LinkedList<>();
    states.add(gallery.newPageState(0, -25, -130, 200, -55));
    states.add(gallery.newPageState(1, -25, -45, 200, 30));
    states.add(gallery.newPageState(2, -25, 40, 200, 115));
    states.add(gallery.newPageState(3, -25, 125, 200, 200));
    states.add(gallery.newPageState(4, -25, 210, 200, 285));
    gallery.assertPages(states);
  }
}
