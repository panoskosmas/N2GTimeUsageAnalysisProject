package kosmasn2g;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


class XYTimeMeasurements extends ApplicationFrame {

    XYTimeMeasurements(final String title,String filename,String word) throws IOException {

        super(title);
        BufferedReader br;
        String line = "";
        String [] data;
        String day = "";
        final XYSeries series = new XYSeries("Power Measurement");
        br = new BufferedReader(new FileReader(filename));
        //word 1234567-->Monday to Sunday

        while ((line = br.readLine()) != null) {
            data = line.split(",");
            Date d = new java.sql.Date(Long.parseLong(data[0]));
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(d);

            if( (Integer.parseInt(data[1]) > 0)) {
                if ((word.contains("1")) && (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)) {
                    series.add(Long.parseLong(data[0]), Integer.parseInt(data[1]));
                }
                if ((word.contains("2")) && (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY)) {
                    series.add(Long.parseLong(data[0]), Integer.parseInt(data[1]));
                }
                if ((word.contains("3")) && (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY)) {
                    series.add(Long.parseLong(data[0]), Integer.parseInt(data[1]));
                }
                if ((word.contains("4")) && (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY)) {
                    series.add(Long.parseLong(data[0]), Integer.parseInt(data[1]));
                }
                if ((word.contains("5")) && (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)) {
                    series.add(Long.parseLong(data[0]), Integer.parseInt(data[1]));
                }
                if ((word.contains("6")) && (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)) {
                    series.add(Long.parseLong(data[0]), Integer.parseInt(data[1]));
                }
                if ((word.contains("7")) && (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
                    series.add(Long.parseLong(data[0]), Integer.parseInt(data[1]));
                }
            }
            else { assert true;}
            }
        final XYSeriesCollection set = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYStepChart(
                "All Time DVD's Measurements" ,
                "X",
                "Y",
                set,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800,800));
        setContentPane(chartPanel);
        }
        }

