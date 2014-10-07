package com.suan.flowlayout.flowlayout;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import com.suan.flowlayout.FlowLayout;


public class CrazyActivity extends Activity {

  /**
   * function to show :
   * 1.normal flow ,textView + Button + Layout
   * 2.gravity ,weight
   * 3.lineNum
   */

  public static int[] colors = {
      Color.parseColor("#4ac8b5"),
      Color.parseColor("#ffc145"),
      Color.parseColor("#4c9be6"),
      Color.parseColor("#c2b765"),
      Color.parseColor("#fe8f58"),
      Color.parseColor("#ff5940")

  };
  public static int[] widths = {
      400,
      300,
      400,
      500,
      600,
  };

  private FlowLayout mFlowLayout;
  private ScrollView mScrollView;
  private Handler mHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_crazy);
    mHandler = new Handler(Looper.getMainLooper());

    mFlowLayout = (FlowLayout) findViewById(R.id.flow_layout);
    mScrollView = (ScrollView) findViewById(R.id.scroll_view);

    for (int i = 0; i < 400; i++) {
      addChild(i);
    }
  }

  private void addChild(final int index) {
    mHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        Button textView = new Button(CrazyActivity.this);
        textView.setText(index + "");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(Color.WHITE);
        // textView.setBackgroundColor(colors[(int) (Math.random() * 6f)]);
        int width = (int) (Math.random() * 400f);
        int height = getResources().getDimensionPixelSize(R.dimen.child_height);
        textView.setTextSize(Math.min(width, height) / 4);
        FlowLayout.LayoutParam param =
            new FlowLayout.LayoutParam(width, height);
        textView.setScaleX(0f);
        textView.animate().scaleX(1f).setDuration(700).start();
        mFlowLayout.addView(textView, param);
        mFlowLayout.setGravity(index % 2 == 0
            ? FlowLayout.GRAVITY_CENTER
            : FlowLayout.GRAVITY_RIGHT);
        mScrollView.fullScroll(View.FOCUS_DOWN);
      }
    }, 100 * index);

  }
}
