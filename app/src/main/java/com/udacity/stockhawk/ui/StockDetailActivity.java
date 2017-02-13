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

        lineChart = (LineChartView) findViewById(R.id.chart);
        tvDes = (TextView) findViewById(R.id.detail_point_describe);

        String symbol = getIntent().getStringExtra("symbol");

        if (symbol != null) {
            drawChart(symbol);
        }

        lineChart.setOnValueTouchListener(new ValueTouchListener());
    }

    private void drawChart(String symbol) {
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

    }

    private void initLineChart() {
        Line line = new Line(mPointValues).setColor(Color.parseColor("#FFCD41"));
        List<Line> lines = new ArrayList<Line>();
        line.setShape(ValueShape.CIRCLE);
        line.setCubic(false);
        line.setFilled(false);
        line.setHasLabelsOnlyForSelected(true);
        line.setHasLines(true);
        line.setHasPoints(true);

        lines.add(line);
        final LineChartData data = new LineChartData();
        data.setLines(lines);

        //坐标轴
        Axis axisX = new Axis(); //X轴
        axisX.setHasTiltedLabels(true);
        axisX.setTextColor(Color.WHITE);
//        axisX.setName("DATE");
        axisX.setTextSize(10);
        axisX.setMaxLabelChars(8);
        axisX.setValues(mAxisXValues);
        data.setAxisXBottom(axisX);
        axisX.setHasLines(true);

        // Y轴是根据数据的大小自动设置Y轴上限
        Axis axisY = new Axis();  //Y轴
        axisY.setName("PRICE");
        axisY.setTextSize(10);
        data.setAxisYLeft(axisY);

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
