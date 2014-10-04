/*
 * Copyright 2013 Blaz Solar
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.suan.flowlayout;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by suanmiao on 14-10-3.
 * function for this class:
 * ensure a container for type display that child can flow both horizontal and vertical,
 * more flexiable than LinearLayout
 * also ,I want to make it more smart in placing in child , more efficient in child placing
 *
 * target for this layout is every child can take as much place it wants
 * so ,In LinearLayout ,child will overflow when total width is bigger than one line
 * but this situation will not happen here
 * difference from LinearLayout
 * 1.child will flow when size is bigger than extra spacing
 * 2.weightSum attribute is no use cause multiple line exists in this layout
 */
public class FlowLayout extends ViewGroup {

  public static final int ORIENTATION_HORIZONTAL = 2;
  public static final int ORIENTATION_VERTICAL = 3;

  private int orientation = ORIENTATION_HORIZONTAL;
  // variable for efficient mode
  /**
   * flag whether child placing more efficient ,
   * if true ,less place will be used
   */
  private boolean efficientMode = false;

  public FlowLayout(Context context) {
    this(context, null);
  }

  public FlowLayout(Context context, AttributeSet attributeSet) {
    this(context, attributeSet, 0);
  }

  public FlowLayout(Context context, AttributeSet attributeSet, int defStyle) {
    super(context, attributeSet, defStyle);
    initAttr(context, attributeSet, defStyle);
  }

  private void initAttr(Context context, AttributeSet attributeSet, int defStyle) {

    TypedArray a =
        context.obtainStyledAttributes(attributeSet, R.styleable.FlowLayout, defStyle, 0);
    try {
      orientation = a.getInt(R.styleable.FlowLayout_flow_orientation, ORIENTATION_HORIZONTAL);
      // efficientMode = a.getBoolean(R.styleable.FlowLayout_flow_efficient_mode, false);
    } finally {
      a.recycle();
    }
  }

  /**
   * meaning of "weight" and "MATCH_PARENT":
   * 1. "weight":works when there is extra space in parent , and the value decide the percent
   * that child can take
   * 2. "MATCH_PARENT": means that the view wants to be as big as its parent
   * >>> so the order for space allocate is : normal/MATCH_PARENT then weight
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    switch (orientation) {
      case ORIENTATION_HORIZONTAL:
        measureHorizontally(widthMeasureSpec, heightMeasureSpec);
        break;
      case ORIENTATION_VERTICAL:
        measureVertically(widthMeasureSpec, heightMeasureSpec);
        break;
    }
  }

  private void measureHorizontally(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
    int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int availableWidth = widthSize - getPaddingLeft() - getPaddingRight();
    /**
     * two kind of type should be taken into account:
     * 1. dimen = 0 && weight !=0
     * 2. MATCH_PARENT
     */
    List<List<Integer>> lineChildIndex = new ArrayList<List<Integer>>();
    List<Integer> lineHeight = new ArrayList<Integer>();
    int currentLineWidth = 0;
    int currentLineHeight = 0;
    float totalWeight = 0f;
    int maxWidth = 0;
    List<Integer> currentLineChildIndex = new ArrayList<Integer>();
    List<Integer> weightChildList = new ArrayList<Integer>();
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      FlowLayoutParam lp = (FlowLayoutParam) child.getLayoutParams();

      int childWidthMode = MeasureSpec.EXACTLY;
      int childHeightMode = MeasureSpec.EXACTLY;

      int childWidthSize = widthSize;
      int childHeightSize = heightSize;

      if (lp.height > 0) {
        childHeightSize = lp.height;
      } else if (heightMode == MeasureSpec.UNSPECIFIED) {
        childHeightMode = MeasureSpec.UNSPECIFIED;
        childHeightSize = 0;
      }
      if (lp.width == LayoutParams.MATCH_PARENT) {
        if (currentLineWidth + lp.leftMargin + lp.width + lp.rightMargin <= availableWidth) {
          currentLineChildIndex.add(i);
          // take place all the extra spacing
          childWidthSize = availableWidth - currentLineWidth - lp.leftMargin - lp.rightMargin;
          currentLineHeight =
              Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin);

          // end last line
          /**
           * when every line ends , child in weight list will be calculated
           */
          measureWeightChildHorizontal(weightChildList, 0, totalWeight);
          lineChildIndex.add(currentLineChildIndex);
          lineHeight.add(currentLineHeight);
          // new line
          currentLineChildIndex = new ArrayList<Integer>();
          currentLineHeight = 0;
          currentLineWidth = 0;
          totalWeight = 0;
        } else {
          // end last line
          measureWeightChildHorizontal(weightChildList, 0, totalWeight);
          lineChildIndex.add(currentLineChildIndex);
          lineHeight.add(currentLineHeight);
          // take new line
          currentLineChildIndex = new ArrayList<Integer>();
          currentLineHeight = 0;
          currentLineChildIndex.add(i);
          currentLineHeight =
              Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin);
          childWidthSize =
              availableWidth - lp.leftMargin - lp.rightMargin;
          lineHeight.add(currentLineHeight);
          // new line
          currentLineChildIndex = new ArrayList<Integer>();
          currentLineHeight = 0;
          currentLineWidth = 0;
          totalWeight = 0;
          currentLineChildIndex.add(i);
        }
      } else if (lp.width == 0 && lp.weight != 0) {
        // add to weight child list ,waiting for measure when whole line end ,so it will has extra
        // place to be placed
        currentLineHeight =
            Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin);
        currentLineChildIndex.add(i);
        totalWeight += lp.weight;
        weightChildList.add(i);
        continue;
      } else {
        if (currentLineWidth + lp.leftMargin + lp.width + lp.rightMargin <= availableWidth) {
          // current line
          currentLineChildIndex.add(i);
          childWidthSize = lp.width;
          currentLineHeight =
              Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin);
          currentLineWidth += lp.width + lp.leftMargin + lp.rightMargin;
          maxWidth = Math.max(currentLineWidth, maxWidth);
        } else {
          childWidthSize = lp.width;
          // end last line
          measureWeightChildHorizontal(weightChildList, availableWidth - currentLineWidth,
              totalWeight);
          lineChildIndex.add(currentLineChildIndex);
          lineHeight.add(currentLineHeight);
          // new line
          currentLineChildIndex = new ArrayList<Integer>();
          currentLineHeight = 0;
          currentLineChildIndex.add(i);
          currentLineHeight =
              Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin);
          lineHeight.add(currentLineHeight);

          currentLineWidth = lp.width + lp.leftMargin + lp.rightMargin;
          maxWidth = Math.max(currentLineWidth, maxWidth);
        }
      }

      child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
          MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
    }

    // end last line
    measureWeightChildHorizontal(weightChildList, availableWidth - currentLineWidth, totalWeight);
    lineChildIndex.add(currentLineChildIndex);
    lineHeight.add(currentLineHeight);

    /**
     * then set position for all child
     */
    int totalHeight = getPaddingTop();
    for (int i = 0; i < lineChildIndex.size(); i++) {
      List<Integer> currentLineIndexList = lineChildIndex.get(i);
      currentLineHeight = lineHeight.get(i);
      currentLineWidth = getPaddingLeft();
      for (int j = 0; j < currentLineIndexList.size(); j++) {
        View child = getChildAt(currentLineIndexList.get(j));
        FlowLayoutParam lp = (FlowLayoutParam) child.getLayoutParams();
        int childWidth = child.getMeasuredWidth();
        lp.top = totalHeight + lp.topMargin;
        lp.left = currentLineWidth + lp.leftMargin;

        currentLineWidth += (lp.leftMargin + childWidth + lp.rightMargin);
      }
      totalHeight += currentLineHeight;
    }

    setMeasuredDimension((widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST)
        ? maxWidth : widthSize,
        (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST)
            ? totalHeight : heightSize);
  }

  private void measureWeightChildHorizontal(List<Integer> weightChildList, int extraSpacing,
      float totalWeight) {
    while (weightChildList.size() > 0) {
      int childIndex = weightChildList.get(0);
      View weightChild = getChildAt(childIndex);
      FlowLayoutParam lp = (FlowLayoutParam) weightChild.getLayoutParams();

      int childWidthMode = MeasureSpec.EXACTLY;
      int childHeightMode = MeasureSpec.EXACTLY;
      int childWidthSize = (int) (lp.weight / totalWeight * extraSpacing);
      int childHeightSize = lp.height;

      weightChild.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
          MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
      weightChildList.remove(0);
    }
  }


  private void measureVertically(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSize = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
    int heightSize = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int availableHeight = heightSize - getPaddingTop() - getPaddingBottom();

    List<List<Integer>> rowChildIndex = new ArrayList<List<Integer>>();
    List<Integer> rowWidth = new ArrayList<Integer>();
    int currentRowWidth = 0;
    int currentRowHeight = 0;
    float totalWeight = 0f;
    int maxHeight = 0;
    List<Integer> currentRowChildIndex = new ArrayList<Integer>();
    List<Integer> weightChildList = new ArrayList<Integer>();
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      FlowLayoutParam lp = (FlowLayoutParam) child.getLayoutParams();

      int childWidthMode = MeasureSpec.EXACTLY;
      int childHeightMode = MeasureSpec.EXACTLY;

      int childWidthSize = widthSize;
      int childHeightSize = heightSize;

      if (lp.width > 0) {
        childWidthSize = lp.width;
      } else if (widthMode == MeasureSpec.UNSPECIFIED) {
        childWidthMode = MeasureSpec.UNSPECIFIED;
        childWidthSize = 0;
      }
      if (lp.height == LayoutParams.MATCH_PARENT) {
        if (currentRowHeight + lp.topMargin + lp.height + lp.bottomMargin <= availableHeight) {
          currentRowChildIndex.add(i);
          // take place all the extra spacing
          childHeightSize = availableHeight - currentRowHeight - lp.topMargin - lp.bottomMargin;
          currentRowWidth =
              Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin);

          // end last row
          /**
           * when every row ends , child in weight list will be calculated
           */
          measureWeightChildVertically(weightChildList, 0, totalWeight);
          rowChildIndex.add(currentRowChildIndex);
          rowWidth.add(currentRowWidth);
          // new row
          currentRowChildIndex = new ArrayList<Integer>();
          currentRowWidth = 0;
          currentRowHeight = 0;
          totalWeight = 0;
        } else {
          // end last row
          measureWeightChildVertically(weightChildList, 0, totalWeight);
          rowChildIndex.add(currentRowChildIndex);
          rowWidth.add(currentRowWidth);
          // take new row
          currentRowChildIndex = new ArrayList<Integer>();
          currentRowWidth = 0;
          currentRowChildIndex.add(i);
          currentRowWidth =
              Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin);
          childHeightSize =
              availableHeight - lp.topMargin - lp.bottomMargin;
          rowWidth.add(currentRowWidth);
          // new row
          currentRowChildIndex = new ArrayList<Integer>();
          currentRowWidth = 0;
          currentRowHeight = 0;
          totalWeight = 0;
          currentRowChildIndex.add(i);
        }
      } else if (lp.height == 0 && lp.weight != 0) {
        // add to weight child list ,waiting for measure when whole row end ,so it will has extra
        // place to be placed
        currentRowWidth =
            Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin);
        currentRowChildIndex.add(i);
        totalWeight += lp.weight;
        weightChildList.add(i);
        continue;
      } else {
        if (currentRowHeight + lp.topMargin + lp.height + lp.bottomMargin <= availableHeight) {
          // current row
          currentRowChildIndex.add(i);
          childHeightSize = lp.height;
          currentRowWidth =
              Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin);
          currentRowHeight += lp.height + lp.topMargin + lp.bottomMargin;
          maxHeight = Math.max(currentRowHeight, maxHeight);
        } else {
          childHeightSize = lp.height;
          // end last row
          measureWeightChildVertically(weightChildList, availableHeight - currentRowHeight,
              totalWeight);
          rowChildIndex.add(currentRowChildIndex);
          rowWidth.add(currentRowWidth);
          // new row
          currentRowChildIndex = new ArrayList<Integer>();
          currentRowWidth = 0;
          currentRowChildIndex.add(i);
          currentRowWidth =
              Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin);
          rowWidth.add(currentRowWidth);

          currentRowHeight = lp.height + lp.topMargin + lp.bottomMargin;
          maxHeight = Math.max(currentRowHeight, maxHeight);
        }
      }

      child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
          MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));

    }
    // end last row
    measureWeightChildVertically(weightChildList, availableHeight - currentRowHeight, totalWeight);
    rowChildIndex.add(currentRowChildIndex);
    rowWidth.add(currentRowWidth);

    /**
     * then set position for all child
     */
    int totalWidth = getPaddingLeft();
    for (int i = 0; i < rowChildIndex.size(); i++) {
      List<Integer> currentRowIndexList = rowChildIndex.get(i);
      currentRowWidth = rowWidth.get(i);
      currentRowHeight = getPaddingTop();
      for (int j = 0; j < currentRowIndexList.size(); j++) {
        View child = getChildAt(currentRowIndexList.get(j));
        FlowLayoutParam lp = (FlowLayoutParam) child.getLayoutParams();
        int childHeight = child.getMeasuredHeight();
        lp.top = currentRowHeight + lp.topMargin;
        lp.left = totalWidth + lp.leftMargin;

        currentRowHeight += (lp.topMargin + childHeight + lp.bottomMargin);
      }
      totalWidth += currentRowWidth;
    }

    setMeasuredDimension((widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST)
        ? totalWidth : widthSize,
        (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST)
            ? maxHeight : heightSize);
  }



  private void measureWeightChildVertically(List<Integer> weightChildList, int extraSpacing,
      float totalWeight) {
    while (weightChildList.size() > 0) {
      int childIndex = weightChildList.get(0);
      View weightChild = getChildAt(childIndex);
      FlowLayoutParam lp = (FlowLayoutParam) weightChild.getLayoutParams();

      int childWidthMode = MeasureSpec.AT_MOST;
      int childHeightMode = MeasureSpec.AT_MOST;
      int childHeightSize = (int) (lp.weight / totalWeight * extraSpacing);
      int childWidthSize = lp.width;

      weightChild.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
          MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
      weightChildList.remove(0);
    }
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    layoutChild(changed, l, t, r, b);
  }

  private void layoutChild(boolean changed, int l, int t, int r, int b) {
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      FlowLayoutParam lp = (FlowLayoutParam) child.getLayoutParams();

      int left = getPaddingLeft() + lp.left;
      int top = getPaddingTop() + lp.top;
      child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }
  }

  @Override
  protected FlowLayoutParam generateDefaultLayoutParams() {
    return new FlowLayoutParam(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override
  protected FlowLayoutParam generateLayoutParams(LayoutParams p) {
    return new FlowLayoutParam(p);
  }

  @Override
  public FlowLayoutParam generateLayoutParams(AttributeSet attrs) {
    return new FlowLayoutParam(getContext(), attrs);
  }

  public class FlowLayoutParam extends MarginLayoutParams {

    public float weight = -1;
    public int left = -1;
    public int top = -1;

    public FlowLayoutParam(int width, int height) {
      super(width, height);
    }

    public FlowLayoutParam(Context context, AttributeSet attributeSet) {
      super(context, attributeSet);

      TypedArray a = context.obtainStyledAttributes(attributeSet,
          R.styleable.FlowLayout, 0, 0);
      try {
        weight = a.getInt(R.styleable.FlowLayout_flow_weight, 0);
      } finally {
        a.recycle();
      }
    }

    public FlowLayoutParam(LayoutParams source) {
      super(source);
    }
  }

}
