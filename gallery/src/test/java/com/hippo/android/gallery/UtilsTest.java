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

import android.graphics.Rect;
import com.hippo.android.gallery.util.PhotoView;
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
    assertEquals(false, Utils.equals(-0.001f, 0.0f, 1e-4f));
    assertEquals(true, Utils.equals(-0.0001f, 0.0f, 1e-4f));
  }

  @Test
  public void testUpdateVisibleRect() {
    PhotoView view = new PhotoView(RuntimeEnvironment.application);

    view.layout(10, 10, 50, 50);
    Utils.updateVisibleRect(view, 100, 100);
    assertEquals(new Rect(0, 0, 40, 40), view.getVisibleRect());

    view.layout(-50, -50, 0, 0);
    Utils.updateVisibleRect(view, 100, 100);
    assertEquals(new Rect(0, 0, 0, 0), view.getVisibleRect());

    view.layout(-10, -20, 120, 130);
    Utils.updateVisibleRect(view, 100, 100);
    assertEquals(new Rect(10, 20, 110, 120), view.getVisibleRect());

    view.layout(-10, -20, 90, 90);
    Utils.updateVisibleRect(view, 100, 100);
    assertEquals(new Rect(10, 20, 100, 110), view.getVisibleRect());
  }
}
