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
 * Created by Hippo on 2017/11/10.
 */

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

class GestureRecognizer {

  public interface Listener {
    void onSingleTapUp(float x, float y);
    void onSingleTapConfirmed(float x, float y);
    void onDoubleTap(float x, float y);
    void onDoubleTapConfirmed(float x, float y);
    void onLongPress(float x, float y);
    void onScroll(float dx, float dy, float totalX, float totalY, float x, float y);
    void onFling(float velocityX, float velocityY);
    void onScaleBegin(float focusX, float focusY);
    void onScale(float focusX, float focusY, float scale);
    void onScaleEnd();
    void onDown(float x, float y);
  }

  private final Listener listener;
  private final GestureDetectorCompat gestureDetector;
  private final ScaleGestureDetector scaleDetector;

  public GestureRecognizer(Context context, Listener listener) {
    this.listener = listener;
    this.gestureDetector = new GestureDetectorCompat(context, new GestureListener(listener));
    this.scaleDetector = new ScaleGestureDetector(context, new ScaleListener(listener));
  }

  public boolean onTouchEvent(MotionEvent event) {
    gestureDetector.onTouchEvent(event);
    scaleDetector.onTouchEvent(event);

    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
      listener.onDown(event.getX(), event.getY());
    }

    return true;
  }

  private static class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private Listener listener;

    public GestureListener(Listener listener) {
      this.listener = listener;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      listener.onScroll(distanceX, distanceY, e2.getX() - e1.getX(), e2.getY() - e1.getY(), e2.getX(), e2.getY());
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      listener.onFling(velocityX, velocityY);
      return true;
    }
  }

  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private Listener listener;

    public ScaleListener(Listener listener) {
      this.listener = listener;
    }
  }
}
