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

import android.content.Context;
import android.view.View;
import com.hippo.android.gallery.intf.Flexible;

public class FlexibleView extends View implements Flexible {

  private boolean flexible;
  private int gallerySize;
  private int viewSize;

  public FlexibleView(Context context) {
    super(context);
  }

  public void setFlexible(boolean flexible) {
    this.flexible = flexible;
  }

  public void setSize(int gallerySize, int viewSize) {
    this.gallerySize = gallerySize;
    this.viewSize = viewSize;
  }

  @Override
  public boolean isFlexible() {
    return flexible;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    if (widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.UNSPECIFIED) {
      float scale = (float) widthSize / (float) gallerySize;
      setMeasuredDimension(widthSize, (int) (viewSize * scale));
    } else if (widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.EXACTLY) {
      float scale = (float) heightSize / (float) gallerySize;
      setMeasuredDimension((int) (viewSize * scale), heightSize);
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }
}
