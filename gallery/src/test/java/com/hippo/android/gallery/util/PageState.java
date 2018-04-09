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

public class PageState {

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

  @Override
  public String toString() {
    return "PageState{index=" + index
        + ", left=" + left
        + ", top=" + top
        + ", right=" + right
        + ", bottom=" + bottom
        + "}";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PageState) {
      PageState state = (PageState) obj;
      return index == state.index &&
          left == state.left &&
          top == state.top &&
          right == state.right &&
          bottom == state.bottom;
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = hash * 31 + index;
    hash = hash * 31 + left;
    hash = hash * 31 + top;
    hash = hash * 31 + right;
    hash = hash * 31 + bottom;
    return hash;
  }
}
