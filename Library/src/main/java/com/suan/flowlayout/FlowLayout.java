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
 * more flexible than LinearLayout
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

  public static final int GRAVITY_NONE = -1;
  public static final int GRAVITY_LEFT = 1;
  public static final int GRAVITY_CENTER = 2;
  public static final int GRAVITY_RIGHT = 3;

  public static final int GRAVITY_TOP = 4;
  public static final int GRAVITY_BOTTOM = 5;

  private static final int SPACING_NONE = -1;

  // horizontal
  private int availableWidth;
  private List<List<Integer>> lineChildIndex;
  private List<Integer> lineHeightList;
  private List<Integer> lineWidthList;
  private int currentLineWidth = 0;
  private int currentLineHeight = 0;
  private int maxWidth = 0;
  private List<Integer> currentLineChildIndex;

  // vertical
  private int availableHeight;
  private List<List<Integer>> rowChildIndex;
  private List<Integer> rowWidthList;
  private List<Integer> rowHeightList;
  private int currentRowWidth = 0;
  private int currentRowHeight = 0;
  private int maxHeight = 0;
  private List<Integer> currentRowChildIndex;

  // common
  private List<Integer> weightChildList;
  private float totalWeight = 0f;
  private int orientation = ORIENTATION_HORIZONTAL;
  private int gravity = GRAVITY_NONE;
  private int lineNum = LayoutParam.LINE_NUM_INVALID;
  public int horizontalSpacing = 0;
  public int verticalSpacing = 0;

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
      orientation = a.getInt(R.styleable.FlowLayout_orientation, ORIENTATION_HORIZONTAL);
      gravity = a.getInt(R.styleable.FlowLayout_gravity, GRAVITY_NONE);
      verticalSpacing =
          a.getDimensionPixelSize(R.styleable.FlowLayout_verticalSpacing, SPACING_NONE);
      horizontalSpacing =
          a.getDimensionPixelSize(R.styleable.FlowLayout_horizontalSpacing, SPACING_NONE);
    } finally {
      a.recycle();
    }
  }

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
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    availableWidth = widthSize - getPaddingLeft() - getPaddingRight();
    /**
     * two kind of type should be taken into account:
     * 1. dimen = 0 && weight !=0
     * 2. MATCH_PARENT
     */
    lineChildIndex = new ArrayList<List<Integer>>();
    lineHeightList = new ArrayList<Integer>();
    lineWidthList = new ArrayList<Integer>();
    weightChildList = new ArrayList<Integer>();
    maxWidth = 0;
    lineNum = LayoutParam.LINE_NUM_INVALID;
    newLine();
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      LayoutParam lp = (LayoutParam) child.getLayoutParams();

      int childWidthMode = MeasureSpec.EXACTLY;
      int childHeightMode = MeasureSpec.EXACTLY;

      int childWidthSize = lp.width;
      int childHeightSize = lp.height;

      int childHorizontalSpacing = getChildHorizontalSpacing(child);
      int childVerticalSpacing = getChildVerticalSpacing(child);

      if (heightMode == MeasureSpec.UNSPECIFIED && childHeightSize == 0) {
        childHeightMode = MeasureSpec.UNSPECIFIED;
      }
      if (lp.lineNum != lineNum) {
        endLine(availableWidth - currentLineWidth);
        newLine();
        lineNum = lp.lineNum;
      }

      if (lp.width == LayoutParams.MATCH_PARENT) {
        if (currentLineWidth + lp.leftMargin + lp.rightMargin + childHorizontalSpacing <= availableWidth) {
          // take place all the extra spacing
          childWidthSize =
              availableWidth - currentLineWidth - lp.leftMargin - lp.rightMargin
                  - childHorizontalSpacing;
          currentLineChildIndex.add(i);
          currentLineWidth = widthSize - getPaddingRight();
          // child with WRAP_CONTENT has been measured
          if (lp.width != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          // set line height after measure ,in case that height is WRAP_CONTENT
          currentLineHeight =
              Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin
                  + childVerticalSpacing);

          // end last line
          endLine(0);
          newLine();
        } else {
          // end last line
          endLine(0);
          newLine();
          childWidthSize = availableWidth - lp.leftMargin - lp.rightMargin - childHorizontalSpacing;
          currentLineChildIndex.add(i);
          currentLineWidth = widthSize - getPaddingRight();
          // child with WRAP_CONTENT has been measured
          if (lp.width != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          // set line height after measure ,in case that height is WRAP_CONTENT
          currentLineHeight =
              Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin
                  + childVerticalSpacing);
          endLine(0);
          newLine();
        }
      } else if (lp.width == 0 && lp.weight != 0) {
        // add to weight child list ,waiting for measure when whole line end ,so it will has extra
        // place to be placed
        totalWeight += lp.weight;
        weightChildList.add(i);
        currentLineChildIndex.add(i);
      } else {
        if (childWidthSize == LayoutParams.WRAP_CONTENT) {
          child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
              MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
          childWidthSize = child.getMeasuredWidth();
          childHeightSize = child.getMeasuredHeight();
        }

        if (currentLineWidth + lp.leftMargin + childWidthSize + lp.rightMargin
            + childHorizontalSpacing <= availableWidth) {
          // current line
          currentLineChildIndex.add(i);
          currentLineWidth +=
              childWidthSize + lp.leftMargin + lp.rightMargin + childHorizontalSpacing;
          // child with WRAP_CONTENT has been measured
          if (lp.width != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          // set line height after measure ,in case that height is WRAP_CONTENT
          if (childWidthSize + lp.leftMargin + lp.rightMargin != 0) {
            currentLineHeight =
                Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin
                    + childVerticalSpacing);
          }
        } else {
          // end last line
          endLine(availableWidth - currentLineWidth);
          newLine();

          currentLineChildIndex.add(i);
          currentLineWidth =
              childWidthSize + lp.leftMargin + lp.rightMargin + childHorizontalSpacing;
          // child with WRAP_CONTENT has been measured
          if (lp.width != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          // set line height after measure ,in case that height is WRAP_CONTENT
          currentLineHeight =
              Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin
                  + childVerticalSpacing);
        }
      }

    }

    // end last line
    endLine(availableWidth - currentLineWidth);

    // then set position for all child
    int totalHeight = getPaddingTop();
    for (int i = 0; i < lineChildIndex.size(); i++) {
      List<Integer> currentLineIndexList = lineChildIndex.get(i);
      currentLineHeight = lineHeightList.get(i);
      int currentLineTotalWidth = lineWidthList.get(i);
      switch (gravity) {
        case GRAVITY_CENTER:
          currentLineWidth = getPaddingLeft() + (availableWidth - currentLineTotalWidth) / 2;
          break;
        case GRAVITY_RIGHT:
        case GRAVITY_BOTTOM:
          currentLineWidth = getPaddingLeft() + (availableWidth - currentLineTotalWidth);
          break;
        default:
          currentLineWidth = getPaddingLeft();
          break;
      }
      for (int childIndex : currentLineIndexList) {
        View child = getChildAt(childIndex);
        LayoutParam lp = (LayoutParam) child.getLayoutParams();
        int childWidth = child.getMeasuredWidth();
        lp.top = totalHeight + lp.topMargin + getChildVerticalSpacing(child) / 2;
        lp.left = currentLineWidth + lp.leftMargin + getChildHorizontalSpacing(child) / 2;

        currentLineWidth += (lp.leftMargin + childWidth + lp.rightMargin);
      }
      totalHeight += currentLineHeight;
    }

    int measuredWidth = (widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST)
        ? maxWidth + getPaddingRight() + getPaddingLeft() : widthSize;
    int measuredHeight =
        (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST)
            ? totalHeight + getPaddingBottom() : heightSize;

    setMeasuredDimension(measuredWidth, measuredHeight);
  }

  private void endLine(int extraSpacing) {
    // when every line ends , child in weight list will be calculated
    measureWeightChildHorizontal(weightChildList, extraSpacing,
        totalWeight);
    int currentLineTotalWidth = 0;
    for (int childIndex : currentLineChildIndex) {
      View child = getChildAt(childIndex);
      currentLineTotalWidth += child.getMeasuredWidth();
    }
    lineWidthList.add(currentLineTotalWidth);
    lineChildIndex.add(currentLineChildIndex);
    lineHeightList.add(currentLineHeight);
    maxWidth = Math.max(currentLineWidth, maxWidth);
  }

  private void newLine() {
    // create new
    currentLineChildIndex = new ArrayList<Integer>();
    currentLineHeight = 0;
    currentLineWidth = getPaddingLeft();
    totalWeight = 0;
  }

  private void measureWeightChildHorizontal(List<Integer> weightChildList, int extraSpacing,
      float totalWeight) {
    while (weightChildList.size() > 0) {
      int childIndex = weightChildList.get(0);
      View weightChild = getChildAt(childIndex);
      LayoutParam lp = (LayoutParam) weightChild.getLayoutParams();

      int childWidthMode = MeasureSpec.EXACTLY;
      int childHeightMode = MeasureSpec.EXACTLY;
      int childWidthSize = (int) (lp.weight / totalWeight * extraSpacing);
      int childHeightSize = lp.height;

      if (childWidthSize + lp.leftMargin + lp.rightMargin != 0) {
        currentLineHeight =
            Math.max(currentLineHeight, childHeightSize + lp.topMargin + lp.bottomMargin);
      }
      weightChild.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
          MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
      weightChildList.remove(0);
    }
  }

  private void measureVertically(int widthMeasureSpec, int heightMeasureSpec) {
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    availableHeight = heightSize - getPaddingTop() - getPaddingBottom();

    weightChildList = new ArrayList<Integer>();
    rowChildIndex = new ArrayList<List<Integer>>();
    rowWidthList = new ArrayList<Integer>();
    rowHeightList = new ArrayList<Integer>();

    maxHeight = 0;
    newRow();
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      LayoutParam lp = (LayoutParam) child.getLayoutParams();

      int childWidthMode = MeasureSpec.EXACTLY;
      int childHeightMode = MeasureSpec.EXACTLY;

      int childWidthSize = lp.width;
      int childHeightSize = lp.height;

      int childHorizontalSpacing = getChildHorizontalSpacing(child);
      int childVerticalSpacing = getChildVerticalSpacing(child);

      if (widthMode == MeasureSpec.UNSPECIFIED && childWidthSize == 0) {
        childWidthMode = MeasureSpec.UNSPECIFIED;
      }
      if (lp.lineNum != lineNum) {
        endRow(availableHeight - currentRowHeight);
        newRow();
        lineNum = lp.lineNum;
      }
      if (lp.height == LayoutParams.MATCH_PARENT) {
        if (currentRowHeight + lp.topMargin + lp.height + lp.bottomMargin + childVerticalSpacing <= availableHeight) {
          // take place all the extra spacing
          childHeightSize =
              availableHeight - currentRowHeight - lp.topMargin - lp.bottomMargin
                  - childVerticalSpacing;
          currentRowChildIndex.add(i);
          currentRowHeight = heightSize - getPaddingBottom();

          if (lp.height != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          currentRowWidth =
              Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin
                  + childHorizontalSpacing);
          endRow(0);
          newRow();
        } else {
          endRow(0);
          newRow();

          childHeightSize = availableHeight - lp.topMargin - lp.bottomMargin - childVerticalSpacing;
          currentRowChildIndex.add(i);
          currentRowHeight = heightSize - getPaddingBottom();
          if (lp.height != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          currentRowWidth =
              Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin
                  + childHorizontalSpacing);
          endRow(0);
          newRow();
        }
      } else if (lp.height == 0 && lp.weight != 0) {
        // add to weight child list ,waiting for measure when whole row end ,so it will has extra
        // place to be placed
        currentRowChildIndex.add(i);
        totalWeight += lp.weight;
        weightChildList.add(i);
      } else {
        if (childHeightSize == LayoutParams.WRAP_CONTENT) {
          child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
              MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
          childWidthSize = child.getMeasuredWidth();
          childHeightSize = child.getMeasuredHeight();
        }
        if (currentRowHeight + lp.topMargin + childHeightSize + lp.bottomMargin
            + childVerticalSpacing <= availableHeight) {
          // current row
          currentRowChildIndex.add(i);
          currentRowHeight +=
              childHeightSize + lp.topMargin + lp.bottomMargin + childVerticalSpacing;

          if (lp.height != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          if (childHeightSize + lp.topMargin + lp.bottomMargin != 0) {
            currentRowWidth =
                Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin
                    + childHorizontalSpacing);
          }
        } else {
          endRow(availableHeight - currentRowHeight);
          newRow();

          currentRowChildIndex.add(i);
          currentRowHeight =
              childHeightSize + lp.topMargin + lp.bottomMargin + childVerticalSpacing;

          if (lp.height != LayoutParams.WRAP_CONTENT) {
            child.measure(MeasureSpec.makeMeasureSpec(childWidthSize, childWidthMode),
                MeasureSpec.makeMeasureSpec(childHeightSize, childHeightMode));
            childWidthSize = child.getMeasuredWidth();
            childHeightSize = child.getMeasuredHeight();
          }
          currentRowWidth =
              Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin
                  + childHorizontalSpacing);
        }
      }
    }
    // end last row
    endRow(availableHeight - currentRowHeight);

    // then set position for all child

    int totalWidth = getPaddingLeft();
    for (int i = 0; i < rowChildIndex.size(); i++) {
      List<Integer> currentRowIndexList = rowChildIndex.get(i);
      currentRowWidth = rowWidthList.get(i);

      int currentRowTotalHeight = rowHeightList.get(i);
      switch (gravity) {
        case GRAVITY_CENTER:
          currentRowHeight = getPaddingTop() + (availableHeight - currentRowTotalHeight) / 2;
          break;
        case GRAVITY_RIGHT:
        case GRAVITY_BOTTOM:
          currentRowHeight = getPaddingTop() + (availableHeight - currentRowTotalHeight);
          break;
        default:
          currentRowHeight = getPaddingTop();
          break;
      }

      for (int childIndex : currentRowIndexList) {
        View child = getChildAt(childIndex);
        LayoutParam lp = (LayoutParam) child.getLayoutParams();
        int childHeight = child.getMeasuredHeight();
        lp.top = currentRowHeight + lp.topMargin + getChildVerticalSpacing(child) / 2;
        lp.left = totalWidth + lp.leftMargin + getChildHorizontalSpacing(child) / 2;

        currentRowHeight += (lp.topMargin + childHeight + lp.bottomMargin);
      }
      totalWidth += currentRowWidth;
    }

    setMeasuredDimension((widthMode == MeasureSpec.UNSPECIFIED || widthMode == MeasureSpec.AT_MOST)
        ? totalWidth + getPaddingRight() : widthSize,
        (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.AT_MOST)
            ? maxHeight + getPaddingLeft() + getPaddingBottom() : heightSize);
  }

  private void endRow(int extraSpacing) {
    // end last row
    measureWeightChildVertically(weightChildList, extraSpacing, totalWeight);
    int currentRowTotalHeight = 0;
    for (int childIndex : currentRowChildIndex) {
      View child = getChildAt(childIndex);
      currentRowTotalHeight += child.getMeasuredHeight();
    }
    rowHeightList.add(currentRowTotalHeight);

    rowChildIndex.add(currentRowChildIndex);
    rowWidthList.add(currentRowWidth);
    maxHeight = Math.max(currentRowHeight, maxHeight);
  }

  private void newRow() {
    // take new row
    currentRowChildIndex = new ArrayList<Integer>();
    currentRowWidth = 0;
    currentRowHeight = getPaddingTop();
    totalWeight = 0;
  }

  private void measureWeightChildVertically(List<Integer> weightChildList, int extraSpacing,
      float totalWeight) {
    while (weightChildList.size() > 0) {
      int childIndex = weightChildList.get(0);
      View weightChild = getChildAt(childIndex);
      LayoutParam lp = (LayoutParam) weightChild.getLayoutParams();

      int childWidthMode = MeasureSpec.EXACTLY;
      int childHeightMode = MeasureSpec.EXACTLY;
      int childHeightSize = (int) (lp.weight / totalWeight * extraSpacing);
      int childWidthSize = lp.width;

      if (childHeightSize + lp.topMargin + lp.bottomMargin != 0) {
        currentRowWidth =
            Math.max(currentRowWidth, childWidthSize + lp.leftMargin + lp.rightMargin);
      }
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
      LayoutParam lp = (LayoutParam) child.getLayoutParams();

      int left = lp.left;
      int top = lp.top;
      child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());
    }
  }

  public void setGravity(int gravity) {
    boolean changed = gravity == this.gravity;
    this.gravity = gravity;
    if (changed) {
      requestLayout();
    }
  }

  private int getChildHorizontalSpacing(View child) {
    LayoutParam lp = (LayoutParam) child.getLayoutParams();
    int childHorizontalSpacing = horizontalSpacing == SPACING_NONE ? 0 : horizontalSpacing;

    childHorizontalSpacing =
        lp.horizontalSpacing == SPACING_NONE ? childHorizontalSpacing : lp.horizontalSpacing;
    return childHorizontalSpacing;
  }

  private int getChildVerticalSpacing(View child) {
    LayoutParam lp = (LayoutParam) child.getLayoutParams();
    int childVerticalSpacing = verticalSpacing == SPACING_NONE ? 0 : verticalSpacing;

    childVerticalSpacing =
        lp.verticalSpacing == SPACING_NONE ? childVerticalSpacing : lp.verticalSpacing;
    return childVerticalSpacing;
  }

  @Override
  protected LayoutParam generateDefaultLayoutParams() {
    return new LayoutParam(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
  }

  @Override
  protected LayoutParam generateLayoutParams(LayoutParams p) {
    return new LayoutParam(p);
  }

  @Override
  public LayoutParam generateLayoutParams(AttributeSet attrs) {
    return new LayoutParam(getContext(), attrs);
  }

  public static class LayoutParam extends MarginLayoutParams {

    public static final int LINE_NUM_INVALID = Integer.MIN_VALUE;
    public float weight = -1;
    public int lineNum = LINE_NUM_INVALID;
    public int horizontalSpacing = 0;
    public int verticalSpacing = 0;
    public int left = -1;
    public int top = -1;

    public LayoutParam(int width, int height) {
      super(width, height);
    }

    public LayoutParam(Context context, AttributeSet attributeSet) {
      super(context, attributeSet);

      TypedArray a = context.obtainStyledAttributes(attributeSet,
          R.styleable.FlowLayout, 0, 0);
      try {
        weight = a.getInt(R.styleable.FlowLayout_weight, 0);
        lineNum = a.getInt(R.styleable.FlowLayout_lineNum, LINE_NUM_INVALID);
        horizontalSpacing =
            a.getDimensionPixelSize(R.styleable.FlowLayout_childHorizontalSpacing, SPACING_NONE);
        verticalSpacing =
            a.getDimensionPixelSize(R.styleable.FlowLayout_childVerticalSpacing, SPACING_NONE);
      } finally {
        a.recycle();
      }
    }

    public LayoutParam(LayoutParams source) {
      super(source);
    }
  }

}
