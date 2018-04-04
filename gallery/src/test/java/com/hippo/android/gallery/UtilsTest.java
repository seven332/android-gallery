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
 * Created by Hippo on 2018/1/5.
 */

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.graphics.RectF;
import android.view.View;
import com.hippo.android.gallery.intf.Clippable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class UtilsTest {

  @Test
  public void testEquals() {
    assertEquals(false, Utils.floatEquals(-0.001f, 0.0f));
    assertEquals(true, Utils.floatEquals(-0.0001f, 0.0f));
  }

  @Test
  public void testUpdateVisibleRect() {
    ClippableView view = new ClippableView(RuntimeEnvironment.application);

    view.layout(10, 10, 50, 50);
    Utils.updateClipRegion(view, 100, 100);
    assertEquals(new RectF(0, 0, 40, 40), view.clip);

    view.layout(-50, -50, 0, 0);
    Utils.updateClipRegion(view, 100, 100);
    assertEquals(new RectF(0, 0, 0, 0), view.clip);

    view.layout(-10, -20, 120, 130);
    Utils.updateClipRegion(view, 100, 100);
    assertEquals(new RectF(10, 20, 110, 120), view.clip);

    view.layout(-10, -20, 90, 90);
    Utils.updateClipRegion(view, 100, 100);
    assertEquals(new RectF(10, 20, 100, 110), view.clip);
  }

  private static class ClippableView extends View implements Clippable {

    public RectF clip = new RectF();

    public ClippableView(Context context) {
      super(context);
    }

    @Override
    public void clip(float left, float top, float right, float bottom) {
      clip.set(left, top, right, bottom);
    }
  }
}
