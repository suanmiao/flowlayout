# Android FlowLayout

## Introduction

  Enhanced LinearLayout , child will auto flow when there is no enough space in current line
Also , you can be more efficient when you want to develop some layout pattern (see "Demonstration" for more detail )

![](https://github.com/suanmiao/flowlayout/raw/master/imgs/normal_mode.png =240*400)

## Demonstration

###1.orientation

child will be place in specific orientation , horizontal or vertical
when there is no enough space for more child , it will be placed in  another line

###2.weight###

child with parameter "weight" will share the rest of space in current line
> notice:

> when you use "weight" ,please ensure that "width"/"height" is zero
> only children in same line can share rest of space on current line

###3.gravity###

child can be placed in different gravity. for example when orientation is horizontal ,
children can be placed from left or right edge flow layout

![](https://github.com/suanmiao/flowlayout/raw/master/imgs/gravity_weight.png =240x400)

> notice:

> "gravity=left" in horizontal mode equals "gravity=top" in horizontal mode
> "gravity=right" in horizontal mode equals "gravity=bottom" in horizontal mode

###4.line number###

this is a amazing function !
when you want to create layout that kind:

![](https://github.com/suanmiao/flowlayout/raw/master/imgs/line_number_usage.png =240x400)

if you use LinearLayout,you should write code:

    <LinearLayout
        android:orientation="vertical"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="xxdip">

            <View
                android:id="@+id/fist_child_in_first_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

            <View
                android:id="@+id/second_child_in_first_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

        </LinearLayout>


        <LinearLayout
            android:orientation="horizontal"
            android:layout_width="match_parent"
            android:layout_height="xxdip">

            <View
                android:id="@+id/fist_child_in_second_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

            <View
                android:id="@+id/second_child_in_second_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

        </LinearLayout>
    </LinearLayout>

but when you use FlowLayout with line number,it's much more easier!

    <com.suan.flowlayout.FlowLayout
        android:orientation="horizontal"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

            <View
                flow:lineNum="1"
                flow:weight="1"
                android:id="@+id/fist_child_in_first_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

            <View
                flow:lineNum="1"
                flow:weight="1"
                android:id="@+id/second_child_in_first_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

            <View
                flow:lineNum="2"
                flow:weight="1"
                android:id="@+id/fist_child_in_second_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

            <View
                flow:lineNum="2"
                flow:weight="1"
                android:id="@+id/second_child_in_second_line"
                android:layout_width="0dp"
                android:layout_height="xxdip"/>

    </com.suan.flowlayout.FlowLayout>

## Usage

sample usage:

        <com.suan.flowlayout.FlowLayout
            flow:orientation="horizontal"
            flow:gravity="left"
            flow:horizontalSpacing="10dp"
            flow:verticalSpacing="10dp"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <Button
                .../>

            <...
                .../>

        </com.suan.flowlayout.FlowLayout>

## Parameters

if you want to use custom attribute of FlowLayout
you should define the name space first:

xmlns:f="http://schemas.android.com/apk/res/your.namespace"

or

xmlns:flow="http://schemas.android.com/apk/res-auto"

then you can use custom attribute below

###1.layout parameters

flow:orientation

> specific child orientation for this layout "horizontal" or "vertical"
> default value is "horizontal"

flow:gravity

> specific child gravity for this layout "left"/"top" or "right"/"bottom"
> default value is "left"/"top"

flow:horizontalSpacing

> specific child spacing for this layout , spacing between child horizontally
> default value is "0"

flow:verticalSpacing

> specific child spacing for this layout , spacing between child vertically
> default value is "0"

###2.child layout parameters

flow:weight

> set weight for this child ,and child with attribute "weight" in same line will share the rest of space according to weight

flow:childHorizontalSpacing

> same function as "horizontalSpacing" ,but this will only has effect on this child

flow:childVerticalSpacing

> same function as "verticalSpacing" ,but this will only has effect on this child

flow:lineNum

> specific line number for this child

> notice:

> same line number does't always means same line or row ,only if there are coherent and space is enough

## Copyrights

Copyright 2014, suanmiao

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

