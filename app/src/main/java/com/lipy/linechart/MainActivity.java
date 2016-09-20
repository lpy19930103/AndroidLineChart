package com.lipy.linechart;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {
    LineChartView lineChartView;
    DoubleLineChartView doubleLineChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLineChartView();
        lineChartViewDataLeft();
    }

    private void initLineChartView() {
        lineChartView = (LineChartView) findViewById(R.id.line_chart);
        doubleLineChartView = (DoubleLineChartView) findViewById(R.id.double_line_chart);
    }

    private void lineChartViewDataLeft() {
        String[] xdate = new String[]{"1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7"};
        String[] xdate1 = new String[]{"1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7", "1-8", "1-8", "1-10"};
        String[] ydata = lineChartView.getFundWeekYdata("5.00", "1.00");
        String[] ydata1 = doubleLineChartView.getWeekYdata("5.00", "0.00");
        float[] data1 = new float[]{4.00f, 2.00f, 3.40f, 2.50f, 5.00f, 1.50f, 5f};
        float[] data = new float[]{1.00f, 1.50f, 2.40f, 2.50f, 3.50f, 4.30f, 4.7f, 4.90f, 4.00f, 5.00f};
        float[] data2 = new float[]{1.00f, 1.30f, 1.40f, 1.50f, 2.00f, 3f, 3.2f, 3.6f, 4.5f, 5.0f};
        lineChartView.setData(xdate, ydata, data1);
        doubleLineChartView.setData(xdate1, ydata1, data, data2);

    }

}
