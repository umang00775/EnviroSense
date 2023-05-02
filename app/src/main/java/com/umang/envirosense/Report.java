package com.umang.envirosense;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class Report extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.WhiteNotificationBar);
        setContentView(R.layout.activity_report);

        BarChart barChart = findViewById(R.id.barchart);
        PieChart pieChart = findViewById(R.id.piechart);

        ArrayList<BarEntry> barEntries = new ArrayList<>();
        ArrayList<PieEntry> pieEntries = new ArrayList<>();

        for (int i=1; i<=5; i++){
            float val = (float) (i*10.0);
            BarEntry barEntry = new BarEntry(i, val+20);
            PieEntry pieEntry = new PieEntry(i*10, val);
            barEntries.add(barEntry);
            pieEntries.add(pieEntry);
        }


        // Bar chart set up
        BarDataSet barDataSet = new BarDataSet(barEntries, "temperature");
        barDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        barDataSet.setDrawValues(false);


        barChart.setData(new BarData(barDataSet));
        barChart.animateY(2000);
        barChart.getDescription().setEnabled(false);


        // Pie chart set up
        PieDataSet pieDataSet =  new PieDataSet(pieEntries, "Humidity in last 24 hrs");
        pieDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        pieChart.setData(new PieData(pieDataSet));
        pieChart.animateXY(2000,2000);
        pieChart.getDescription().setEnabled(false);
    }
}