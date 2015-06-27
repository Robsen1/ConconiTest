package at.fhooe.mc.conconii;

import android.app.Activity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class EvaluationActivity extends Activity {

    private LineChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);

        //get chart
        mChart = (LineChart) findViewById(R.id.evaluationScreen_linechart);

        //calculate the points to display
        EvaluationCalculator calc = new EvaluationCalculator(DataManager.getInstance().getDataList());
        ArrayList<ActualData> finalData = calc.getFinalData();
        finalData.add(new ActualData(80, 5, 0));
        finalData.add(new ActualData(80, 5, 200));
        finalData.add(new ActualData(88, 5, 400));
        finalData.add(new ActualData(90, 5, 600));
        finalData.add(new ActualData(120, 5, 800));
        finalData.add(new ActualData(130, 5, 1000));
        finalData.add(new ActualData(140, 5, 1200));
        finalData.add(new ActualData(150, 5, 1400));
        finalData.add(new ActualData(150, 5, 1600));
        finalData.add(new ActualData(160, 5, 1800));
        finalData.add(new ActualData(170, 5, 2000));
        finalData.add(new ActualData(190, 5, 2200));
        finalData.add(new ActualData(190, 5, 2400));
        finalData.add(new ActualData(190, 5, 2600));


        // actual date and time
        Date dNow = new Date();
        SimpleDateFormat ft =
                new SimpleDateFormat("E dd.MM.yyyy 'at' HH:mm:ss");

        // create the chart
        ArrayList<Entry> chartData = new ArrayList<>();
        for (int i = 0; i < finalData.size(); i++) {
            Entry e = new Entry(finalData.get(i).getHeartRate(), i);
            chartData.add(e);
        }

        LineDataSet dataSet = new LineDataSet(chartData, ft.format(dNow));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(dataSet);

        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < finalData.size(); i++) {
            xVals.add(Integer.toString((int) finalData.get(i).getTargetSpeed()));
        }

        LineData data = new LineData(xVals, dataSets);
        mChart.setData(data);
        mChart.invalidate(); // refresh

        // formatting
        //Chart
        mChart.setDescription("");


        //X Axis
        XAxis xAx = mChart.getXAxis();
        xAx.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAx.setTextSize(14f);
        //set text km/h
        TextView textXAxis = (TextView) findViewById(R.id.evaluation_xAxis_text);
//        textXAxis.layout(xAxisXPos, 0,xAxisYPos, 0);


        //Y Axis
        mChart.getAxisRight().setEnabled(false);
        YAxis yAx = mChart.getAxisLeft();
        yAx.setStartAtZero(false);
        yAx.setTextSize(14f);
        yAx.setValueFormatter(new MyValueFormatter());
        //set text bpm
        TextView textYAxis = (TextView) findViewById(R.id.evaluation_yAxis_text);
//        textYAxis.layout(yAxisXPos, 0, yAxisYPos, 0);


    }

    // formatter
    private class MyValueFormatter implements ValueFormatter {
        private DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value); // append a dollar-sign
        }
    }

}