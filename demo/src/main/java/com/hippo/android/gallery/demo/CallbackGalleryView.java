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

package com.hippo.android.gallery.demo;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.hippo.android.gallery.GalleryView;

public class CallbackGalleryView extends GalleryView {

  private AfterLayoutListener listener;

  public CallbackGalleryView(Context context) {
    super(context);
  }

  public CallbackGalleryView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setAfterLayoutListener(AfterLayoutListener listener) {
    this.listener = listener;
  }

  @Override
  public void layout() {
    super.layout();

    if (listener != null) {
      listener.onAfterLayout();
    }
  }

  public interface AfterLayoutListener {
    void onAfterLayout();
  }
}
