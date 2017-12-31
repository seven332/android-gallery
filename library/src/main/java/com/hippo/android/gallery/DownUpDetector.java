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
 * Created by Hippo on 2017/11/16.
 */

import android.view.MotionEvent;

class DownUpDetector {

  public interface DownUpListener {
    void onDown(float x, float y);
    void onUp(float x, float y);
    void onCancel();
    void onPointerDown(float x, float y);
    void onPointerUp(float x, float y);
  }

  private final DownUpListener listener;

  public DownUpDetector(DownUpListener listener) {
    this.listener = listener;
  }

  public void onTouchEvent(MotionEvent ev) {
    switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        listener.onDown(ev.getX(), ev.getY());
        break;
      case MotionEvent.ACTION_UP:
        listener.onUp(ev.getX(), ev.getY());
        break;
      case MotionEvent.ACTION_CANCEL:
        listener.onCancel();
        break;
      case MotionEvent.ACTION_POINTER_DOWN:
        listener.onPointerDown(ev.getX(), ev.getY());
        break;
      case MotionEvent.ACTION_POINTER_UP:
        listener.onPointerUp(ev.getX(), ev.getY());
        break;
    }
  }
}
