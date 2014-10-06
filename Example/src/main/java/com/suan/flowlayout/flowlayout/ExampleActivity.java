package com.suan.flowlayout.flowlayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.suan.flowlayout.FlowLayout;


public class ExampleActivity extends Activity {

  /**
   * function to show :
   * 1.normal flow ,textView + Button + Layout
   * 2.gravity ,weight
   * 3.lineNum
   */

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_example);
    findViewById(R.id.example_btn_normal_mode).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(ExampleActivity.this, NormalFlowActivity.class));
      }
    });
    findViewById(R.id.example_btn_weight_gravity).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(ExampleActivity.this, GravityWeightActivity.class));
      }
    });
    findViewById(R.id.example_btn_line_num).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(ExampleActivity.this, LineNumActivity.class));
      }
    });
    findViewById(R.id.example_btn_crazy).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(ExampleActivity.this, CrazyActivity.class));
      }
    });
  }

}
