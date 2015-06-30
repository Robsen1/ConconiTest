package at.fhooe.mc.conconii;

import android.app.Activity;
import android.os.Bundle;

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

/**
 * This Activity displays a human readable representation of data collected during the test.
 * This class uses the MPAndroid Chart package.
 *
 * @author Robsen & Gix
 */
public class EvaluationActivity extends Activity {
    private LineChart mChart;

    /**
     * Creates the Line Chart for displaying Data.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_evaluation);
        mChart = (LineChart) findViewById(R.id.evaluationScreen_linechart);

        //calculate the points to display
        EvaluationCalculator calc = new EvaluationCalculator(DataManager.getInstance().getDataList());
        ArrayList<ActualData> finalData = calc.getFinalData();

        //#############Testingdata#################
        finalData.add(new ActualData(130, 5, 0));
        finalData.add(new ActualData(136, 5, 200));//6
        finalData.add(new ActualData(143, 5, 400));
        finalData.add(new ActualData(151, 5, 600));
        finalData.add(new ActualData(157, 5, 800));
        finalData.add(new ActualData(163, 5, 1000));//8
        finalData.add(new ActualData(171, 5, 1200));
        finalData.add(new ActualData(176, 5, 1400));
        finalData.add(new ActualData(180, 5, 1600));
        finalData.add(new ActualData(183, 5, 1800));//10
        finalData.add(new ActualData(186, 5, 2000));
        finalData.add(new ActualData(190, 5, 2200));
        finalData.add(new ActualData(192, 5, 2400));
        finalData.add(new ActualData(196, 5, 2600));//12
        finalData.add(new ActualData(198, 5, 2800));
        finalData.add(new ActualData(200, 5, 3000));
        finalData.add(new ActualData(203, 5, 2800));
        finalData.add(new ActualData(204, 5, 3000));//14

        // actual date and time
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("E dd.MM.yyyy 'at' HH:mm:ss");

        // create the chart
        ArrayList<Entry> chartData = new ArrayList<>();
        for (int i = 0; i < finalData.size(); i++) {
            Entry e = new Entry(finalData.get(i).getHeartRate(), i);
            chartData.add(e);
        }

        LineDataSet dataSet = new LineDataSet(chartData, ft.format(dNow));
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        ArrayList<LineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        ArrayList<String> xVals = new ArrayList<>();
        for (int i = 0; i < finalData.size(); i++) {
            xVals.add(Integer.toString((int) finalData.get(i).getTargetSpeed()));
        }

        LineData data = new LineData(xVals, dataSets);
        mChart.setData(data);
        mChart.invalidate();

        //formatting the chart
        mChart.setDescription("");

        //x-axis
        XAxis xAx = mChart.getXAxis();
        xAx.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAx.setTextSize(14f);

        //y-axis
        mChart.getAxisRight().setEnabled(false);
        YAxis yAx = mChart.getAxisLeft();
        yAx.setStartAtZero(false);
        yAx.setTextSize(14f);
        yAx.setValueFormatter(new MyValueFormatter());
    }

    /**
     * A Formatter class for individual formatting purposes.
     */
    private class MyValueFormatter implements ValueFormatter {
        private final DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value) {
            return mFormat.format(value); // append a dollar-sign
        }
    }

}