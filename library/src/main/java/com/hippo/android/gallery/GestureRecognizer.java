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
    void onUp(float x, float y);
    void onCancel();
    void onPointerDown(float x, float y);
    void onPointerUp(float x, float y);
  }

  private final Listener listener;
  private final DownUpDetector downUpDetector;
  private final GestureDetectorCompat gestureDetector;
  private final ScaleGestureDetector scaleDetector;

  private boolean scaling = false;

  public GestureRecognizer(Context context, Listener listener) {
    this.listener = listener;
    this.downUpDetector = new DownUpDetector(new DownUpListener());
    this.gestureDetector = new GestureDetectorCompat(context, new GestureListener());
    this.scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
  }

  public boolean onTouchEvent(MotionEvent event) {
    downUpDetector.onTouchEvent(event);
    scaleDetector.onTouchEvent(event);
    gestureDetector.onTouchEvent(event);
    return true;
  }

  private class DownUpListener implements DownUpDetector.DownUpListener {

    @Override
    public void onDown(float x, float y) {
      scaling = false;
      listener.onDown(x, y);
    }

    @Override
    public void onUp(float x, float y) {
      listener.onUp(x, y);
    }

    @Override
    public void onCancel() {
      listener.onCancel();
    }

    @Override
    public void onPointerDown(float x, float y) {
      listener.onPointerDown(x, y);
    }

    @Override
    public void onPointerUp(float x, float y) {
      listener.onPointerUp(x, y);
    }
  }

  private class GestureListener extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      // Scroll action is easier caught than Scale action
      // Only catch scroll action when no scaling and point count is one
      if (!scaling && e1.getPointerCount() == 1 && e2.getPointerCount() == 1) {
        listener.onScroll(-distanceX, -distanceY, e2.getX() - e1.getX(), e2.getY() - e1.getY(),
            e2.getX(), e2.getY());
      }
      return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      listener.onFling(velocityX, velocityY);
      return true;
    }
  }

  private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
      scaling = true;
      listener.onScaleBegin(detector.getFocusX(), detector.getFocusY());
      return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
      if (scaling) {
        listener.onScale(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
      }
      return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
      if (scaling) {
        listener.onScaleEnd();
        scaling = false;
      }
    }
  }
}
