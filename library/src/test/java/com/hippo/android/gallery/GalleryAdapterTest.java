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

package com.hippo.android.gallery;

/*
 * Created by Hippo on 2018/1/9.
 */

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class GalleryAdapterTest {

  @Test
  public void testNotifyPageChanged() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 100, 2, 3, 4});
    layout.adapter.notifyPageChanged(1);
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testNotifyPageRangeChanged() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 100, 200, 300, 4});
    layout.adapter.notifyPageRangeChanged(1, 3);
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testNotifyPageInserted() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 9, 1, 2, 3, 4});
    layout.adapter.notifyPageInserted(1);
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testNotifyPageRangeInserted() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 9, 8, 7, 1, 2, 3, 4});
    layout.adapter.notifyPageRangeInserted(1, 3);
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testNotifyPageRemoved() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 2, 3, 4});
    layout.adapter.notifyPageRemoved(1);
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testNotifyPageRangeRemoved() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 4});
    layout.adapter.notifyPageRangeRemoved(1, 3);
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testNotifyPageMoved() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 2, 3, 1, 4});
    layout.adapter.notifyPageMoved(1, 3);
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testNotifyPageSetChanged() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {324, 1, 322, 44, 11, 432});
    layout.adapter.notifyPageSetChanged();
    layout.layout();
    layout.assertPages();
  }

  @Test
  public void testMultipleNotifyPageXXX() {
    GalleryLayout layout = new GalleryLayout(RuntimeEnvironment.application);
    layout.set(new int[] {0, 1, 2, 3, 4});
    layout.layout();
    layout.assertPages();

    layout.set(new int[] {0, 9, 1, 2, 3, 4});
    layout.adapter.notifyPageInserted(1);
    layout.set(new int[] {0, 8, 9, 1, 2, 3, 4});
    layout.adapter.notifyPageInserted(1);
    layout.set(new int[] {0, 8, 9, 1, 2, 3});
    layout.adapter.notifyPageRemoved(6);
    layout.set(new int[] {8, 9, 1, 2, 3, 0});
    layout.adapter.notifyPageMoved(0, 5);
    layout.layout();
    layout.assertPages();
  }

  private static class GalleryLayout {

    private GalleryView view;
    private TestAdapter adapter;

    public GalleryLayout(Context context) {
      view = new GalleryView(context);
      adapter = new TestAdapter();
      ScrollLayoutManager layoutManager = new ScrollLayoutManager();
      layoutManager.setScrollLayout(new HorizontalScrollLayout());
      view.setLayoutManager(layoutManager);
      view.setAdapter(adapter);
    }

    public void set(int[] data) {
      adapter.data = data;
    }

    public void layout() {
      view.measure(
          View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(1000, View.MeasureSpec.EXACTLY)
      );
      view.layout(0, 0, 1000, 1000);
    }

    public void assertPages() {
      int[] data = adapter.data;
      for (int i = 0, n = data.length; i < n; i++) {
        GalleryPage page = view.getPageAt(i);
        assertEquals("" + data[i], ((TextView) page.view).getText().toString());
      }
    }
  }

  private static class TestAdapter extends GalleryAdapter {

    public int[] data;

    @Override
    public GalleryPage onCreatePage(GalleryView parent, int type) {
      View view = new TextView(parent.getContext());
      ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(1, 1);
      view.setLayoutParams(lp);
      return new GalleryPage(view);
    }

    @Override
    public void onDestroyPage(GalleryPage page) {}

    @Override
    public void onBindPage(GalleryPage page) {
      ((TextView) page.view).setText("" + data[page.getIndex()]);
    }

    @Override
    public void onUnbindPage(GalleryPage page) {
      ((TextView) page.view).setText(null);
    }

    @Override
    public int getPageCount() {
      return data.length;
    }
  }
}
