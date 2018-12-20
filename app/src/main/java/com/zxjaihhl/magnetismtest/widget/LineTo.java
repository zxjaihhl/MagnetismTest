package com.zxjaihhl.magnetismtest.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by zxjaihhl on 2018/1/22.
 */

public class LineTo extends View {
    Paint paint;
    private int point_one_x,point_one_y, point_tow_x, point_tow_y;

    public LineTo(Context context,int point_one_x,int point_one_y,int point_tow_x,int point_tow_y) {
        super(context);
        this.point_one_x = point_one_x;
        this.point_one_y = point_one_y;
        this.point_tow_x = point_tow_x;
        this.point_tow_y = point_tow_y;
        paint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
        paint.setColor(Color.RED);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(2);
    }

    //在这里我们将测试canvas提供的绘制图形方法
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(point_one_x, point_one_y, point_tow_x, point_tow_y, paint);
    }
}

