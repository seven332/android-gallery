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
 * Created by Hippo on 2017/11/17.
 */

import android.graphics.Rect;

public class HorizontalPagerLayout extends BasePagerLayout {

  @Override
  public int getPageRange() {
    return width + interval;
  }

  @Override
  public int scrollPage(int offset, int dx, int dy) {
    return offset - dx;
  }

  @Override
  public void offsetRect(Rect rect, int offset, int position) {
    int pageRange = getPageRange();
    switch (position) {
      case PagerLayoutManager.POSITION_PREVIOUS:
        offset -= pageRange;
        break;
      case PagerLayoutManager.POSITION_CURRENT:
        // Keep offset
        break;
      case PagerLayoutManager.POSITION_NEXT:
        offset += pageRange;
        break;
    }
    rect.offset(offset, 0);
  }
}
