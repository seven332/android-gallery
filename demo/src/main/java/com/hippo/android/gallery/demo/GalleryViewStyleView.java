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

package com.hippo.android.gallery.demo;

/*
 * Created by Hippo on 2017/12/20.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.hippo.android.gallery.GalleryView;
import com.hippo.android.gallery.Photo;

public class GalleryViewStyleView extends NestedScrollView {

  private static SparseIntArray LAYOUT_MANAGER_MAP = new SparseIntArray();
  private static SparseIntArray SCROLL_LAYOUT_MAP = new SparseIntArray();
  private static SparseIntArray PAGER_LAYOUT_MAP = new SparseIntArray();
  private static SparseIntArray SCALE_TYPE_MAP = new SparseIntArray();
  private static SparseIntArray START_POSITION_MAP = new SparseIntArray();

  static {
    LAYOUT_MANAGER_MAP.put(GalleryViewStyle.LAYOUT_MANAGER_SCROLL, R.id.layout_manager_scroll);
    LAYOUT_MANAGER_MAP.put(GalleryViewStyle.LAYOUT_MANAGER_PAGER, R.id.layout_manager_pager);

    SCROLL_LAYOUT_MAP.put(GalleryViewStyle.SCROLL_LAYOUT_VERTICAL, R.id.scroll_layout_vertical);
    SCROLL_LAYOUT_MAP.put(GalleryViewStyle.SCROLL_LAYOUT_HORIZONTAL, R.id.scroll_layout_horizontal);
    SCROLL_LAYOUT_MAP.put(GalleryViewStyle.SCROLL_LAYOUT_REVERSED_HORIZONTAL, R.id.scroll_layout_reversed_horizontal);

    PAGER_LAYOUT_MAP.put(GalleryViewStyle.PAGER_LAYOUT_HORIZONTAL, R.id.pager_layout_horizontal);
    PAGER_LAYOUT_MAP.put(GalleryViewStyle.PAGER_LAYOUT_REVERSED_HORIZONTAL, R.id.pager_layout_reversed_horizontal);

    SCALE_TYPE_MAP.put(Photo.SCALE_TYPE_ORIGIN, R.id.scale_type_origin);
    SCALE_TYPE_MAP.put(Photo.SCALE_TYPE_FIT_WIDTH, R.id.scale_type_fit_width);
    SCALE_TYPE_MAP.put(Photo.SCALE_TYPE_FIT_HEIGHT, R.id.scale_type_fit_height);
    SCALE_TYPE_MAP.put(Photo.SCALE_TYPE_FIT, R.id.scale_type_fit);
    SCALE_TYPE_MAP.put(Photo.SCALE_TYPE_FIXED, R.id.scale_type_fixed);

    START_POSITION_MAP.put(Photo.START_POSITION_TOP_LEFT, R.id.start_position_top_left);
    START_POSITION_MAP.put(Photo.START_POSITION_TOP_RIGHT, R.id.start_position_top_right);
    START_POSITION_MAP.put(Photo.START_POSITION_BOTTOM_LEFT, R.id.start_position_bottom_left);
    START_POSITION_MAP.put(Photo.START_POSITION_BOTTOM_RIGHT, R.id.start_position_bottom_right);
    START_POSITION_MAP.put(Photo.START_POSITION_CENTER, R.id.start_position_center);
  }

  private static int getValueByKey(SparseIntArray map, int key) {
    return map.get(key);
  }

  private static int getKeyByValue(SparseIntArray map, int value) {
    return map.keyAt(map.indexOfValue(value));
  }

  private GalleryView view;
  private GalleryViewStyle style;

  private RadioGroup layoutManager;
  private TextView scrollLayoutText;
  private RadioGroup scrollLayout;
  private TextView pagerLayoutText;
  private RadioGroup pagerLayout;
  private TextView pageIntervalValue;
  private SeekBar pageInterval;
  private TextView scaleTypeText;
  private RadioGroup scaleType;
  private TextView startPositionText;
  private RadioGroup startPosition;

  public GalleryViewStyleView(@NonNull Context context) {
    super(context);
    init(context);
  }

  public GalleryViewStyleView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  private void init(Context context) {
    LayoutInflater.from(context).inflate(R.layout.widget_gallery_view_style, this);

    layoutManager = findViewById(R.id.layout_manager);
    scrollLayoutText = findViewById(R.id.scroll_layout_text);
    scrollLayout = findViewById(R.id.scroll_layout);
    pagerLayoutText = findViewById(R.id.pager_layout_text);
    pagerLayout = findViewById(R.id.pager_layout);
    pageIntervalValue = findViewById(R.id.page_interval_value);
    pageInterval = findViewById(R.id.page_interval);
    scaleTypeText = findViewById(R.id.scale_type_text);
    scaleType = findViewById(R.id.scale_type);
    startPositionText = findViewById(R.id.start_position_text);
    startPosition = findViewById(R.id.start_position);

    layoutManager.setOnCheckedChangeListener((group, checkedId) -> {
      if (checkedId == R.id.layout_manager_scroll) {
        scrollLayoutText.setVisibility(VISIBLE);
        scrollLayout.setVisibility(VISIBLE);

        pagerLayoutText.setVisibility(GONE);
        pagerLayout.setVisibility(GONE);
        scaleTypeText.setVisibility(GONE);
        scaleType.setVisibility(GONE);
        startPositionText.setVisibility(GONE);
        startPosition.setVisibility(GONE);
      } else if (checkedId == R.id.layout_manager_pager) {
        scrollLayoutText.setVisibility(GONE);
        scrollLayout.setVisibility(GONE);

        pagerLayoutText.setVisibility(VISIBLE);
        pagerLayout.setVisibility(VISIBLE);
        scaleTypeText.setVisibility(VISIBLE);
        scaleType.setVisibility(VISIBLE);
        startPositionText.setVisibility(VISIBLE);
        startPosition.setVisibility(VISIBLE);
      }
      style.layoutManager = getKeyByValue(LAYOUT_MANAGER_MAP, checkedId);
      style.apply(view);
    });

    scrollLayout.setOnCheckedChangeListener((group, checkedId) -> {
      style.scrollLayout = getKeyByValue(SCROLL_LAYOUT_MAP, checkedId);
      style.apply(view);
    });

    pagerLayout.setOnCheckedChangeListener((group, checkedId) -> {
      style.pagerLayout = getKeyByValue(PAGER_LAYOUT_MAP, checkedId);
      style.apply(view);
    });

    pageIntervalValue.setText("0dp");
    pageInterval.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        pageIntervalValue.setText(progress + "dp");
        style.pageInterval = Utils.dp2pix(seekBar.getContext(), progress);
        style.apply(view);
      }
      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });

    scaleType.setOnCheckedChangeListener((group, checkedId) -> {
      style.scaleType = getKeyByValue(SCALE_TYPE_MAP, checkedId);
      style.apply(view);
    });

    startPosition.setOnCheckedChangeListener((group, checkedId) -> {
      style.startPosition = getKeyByValue(START_POSITION_MAP, checkedId);
      style.apply(view);
    });
  }

  public void setStyle(GalleryView view, GalleryViewStyle style) {
    this.view = view;
    this.style = style;

    layoutManager.check(getValueByKey(LAYOUT_MANAGER_MAP, style.layoutManager));
    scrollLayout.check(getValueByKey(SCROLL_LAYOUT_MAP, style.scrollLayout));
    pagerLayout.check(getValueByKey(PAGER_LAYOUT_MAP, style.pagerLayout));
    pageInterval.setProgress((int) Utils.pix2dp(view.getContext(), style.pageInterval));
    scaleType.check(getValueByKey(SCALE_TYPE_MAP, style.scaleType));
    startPosition.check(getValueByKey(START_POSITION_MAP, style.startPosition));
  }
}
