package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.io.BufferedReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.ChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;
import lecho.lib.hellocharts.view.PreviewLineChartView;

import static lecho.lib.hellocharts.gesture.ZoomType.HORIZONTAL_AND_VERTICAL;

/**
 * Created by dh on 17-2-5.
 */

public class StockDetailActivity extends AppCompatActivity {

    private Context mContext;
    private LineChartView lineChart;
    private TextView tvDes;

    List<PointValue> mPointValues;
    List<AxisValue> mAxisXValues;
    List<History> mHistoryList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mContext = this;

        Intent intent = getIntent();
        String symbol = intent.getStringExtra("symbol");

        lineChart = (LineChartView) findViewById(R.id.chart);
        tvDes = (TextView) findViewById(R.id.detail_point_describe);

        Uri uri = Contract.Quote.makeUriForStock(symbol);

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {

            String historyString = cursor.getString(Contract.Quote.POSITION_HISTORY);

            if (historyString != null) {

                mPointValues = new ArrayList<PointValue>();
                mAxisXValues = new ArrayList<AxisValue>();
                mHistoryList = new ArrayList<History>();

                String[] historyArray = historyString.split("\n");
                if (historyArray != null) {
                    int length = historyArray.length;
                    for (int i = 0; i < length; i++) {
                        String[] historyData = historyArray[i].split(", ");
                        History history = new History(Long.valueOf(historyData[0]), historyData[1]);
                        mAxisXValues.add(new AxisValue(i).setLabel(history.historyDate));
                        mPointValues.add(new PointValue(i, history.historyPrice));
                        mHistoryList.add(history);
                    }
                }
                initLineChart();
            }
            cursor.close();
        }

        lineChart.setOnValueTouchListener(new ValueTouchListener());
    }

    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));  //折线的颜色（橙色）
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);//折线图上每个数据点的形状  这里是圆形 （有三种 ：ValueShape.SQUARE  ValueShape.CIRCLE  ValueShape.DIAMOND）
        line.setCubic(false);//曲线是否平滑，即是曲线还是折线
        line.setFilled(false);//是否填充曲线的面积
//        line.setHasLabels(true);//曲线的数据坐标是否加上备注
        line.setHasLabelsOnlyForSelected(true);//点击数据坐标提示数据（设置了这个line.setHasLabels(true);就无效）
        line.setHasLines(true);//是否用线显示。如果为false 则没有曲线只有点显示
        line.setHasPoints(true);//是否显示圆点 如果为false 则没有原点只有点显示（每个数据点都是个大的圆点）

        lines.add(line);
        final LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);  //X坐标轴字体是斜的显示还是直的，true是斜的显示
        axisX.setTextColor(Color.WHITE);  //设置字体颜色
//        axisX.setName("DATE");  //表格名称
        axisX.setTextSize(10);//设置字体大小
        axisX.setMaxLabelChars(8); //最多几个X轴坐标，意思就是你的缩放让X轴上数据的个数7<=x<=mAxisXValues.length
        axisX.setValues(mAxisXValues);  //填充X轴的坐标名称
        data.setAxisXBottom(axisX); //x 轴在底部
        //data.setAxisXTop(axisX);  //x 轴在顶部
        axisX.setHasLines(true); //x 轴分割线

        // Y轴是根据数据的大小自动设置Y轴上限(在下面我会给出固定Y轴数据个数的解决方案)
        Axis axisY = new Axis();  //Y轴
        axisY.setName("PRICE");//y轴标注
        axisY.setTextSize(10);//设置字体大小
        data.setAxisYLeft(axisY);  //Y轴设置在左边
//        data.setAxisYRight(axisY);  //y轴设置在右边

        //设置行为属性，支持缩放、滑动以及平移
        lineChart.setInteractive(true);
        lineChart.setZoomType(ZoomType.HORIZONTAL);
        lineChart.setMaxZoom((float) 5);//最大方法比例
        lineChart.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
        lineChart.setLineChartData(data);
        lineChart.setVisibility(View.VISIBLE);

        Viewport v = new Viewport(lineChart.getMaximumViewport());
        v.left = 0;
        v.right= 7;
        lineChart.setCurrentViewport(v);
    }


    private class History {
        public String historyDate;
        public float historyPrice;
        public String fullDate;

        History(long date, String price) {
            Date d = new Date(date);
            this.fullDate = new SimpleDateFormat("yyyy-MM-dd").format(d);
            this.historyDate = new SimpleDateFormat("MM.dd").format(d);
            this.historyPrice = new BigDecimal(price).floatValue();
        }
    }

    private class ValueTouchListener implements LineChartOnValueSelectListener {
        @Override
        public void onValueSelected(int lineIndex, int pointIndex, PointValue value) {
            //tvDes.setText("Date: " + mHistoryList.get(pointIndex).fullDate + ", Price: $" + value.getY());
            tvDes.setText(mContext.getString(R.string.detail_point_describe, mHistoryList.get(pointIndex).fullDate, value.getY()));
        }

        @Override
        public void onValueDeselected() {

        }
    }
}
