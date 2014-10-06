package com.suan.flowlayout.flowlayout;

import android.app.Activity;
import android.os.Bundle;


public class NormalFlowActivity extends Activity {

  /**
   * function to show :
   * 1.normal flow ,textView + Button + Layout
   * 2.gravity ,weight
   * 3.lineNum
   */

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_normal_flow);
  }

}
