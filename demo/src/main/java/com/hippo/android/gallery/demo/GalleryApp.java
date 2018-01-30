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

/*
 * Created by Hippo on 2018/1/5.
 */

import android.app.Application;
import android.util.DisplayMetrics;
import com.github.anrwatchdog.ANRWatchDog;
import com.hippo.android.gallery.drawable.TiledDrawable;

public class GalleryApp extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    new ANRWatchDog().start();

    DisplayMetrics metrics = getResources().getDisplayMetrics();
    int maxTextureSize = Math.max(metrics.widthPixels, metrics.heightPixels);
    TiledDrawable.setMaxTextureSize(maxTextureSize);
  }
}
