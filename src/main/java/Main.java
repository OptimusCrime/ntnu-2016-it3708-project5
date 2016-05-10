import ea.Evolver;
import nsga.Individual;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class Main {

    private ChartPanel chartPanel;
    private XYSeries plotData;
    private JFreeChart chart;

    /**
     * Main method
     *
     * @param args From sys
     */

    public static void main(String[] args) {
        new Main();
    }

    /**
     * Constructor
     */

    public Main() {
        // Create the chart
        this.createChart();

        // Create the frame
        this.createFrame();

        // Run stuff
        this.run();
    }

    /**
     * Create the chart
     */

    private void createChart() {
        // Create the initial plot data
        plotData = new XYSeries("");

        // Fetch the data set
        XYDataset dc = newDataset();

        // Create the chart
        this.chart = ChartFactory.createScatterPlot(
                "",
                "Distance",
                "Cost",
                dc,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Fix some stuff
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);

        // Create the chart panel
        this.chartPanel = new ChartPanel(chart);

        // Set various settings to chart panel
        this.chartPanel.setMouseWheelEnabled(false);
        this.chartPanel.setHorizontalAxisTrace(true);
        this.chartPanel.setVerticalAxisTrace(true);
    }

    /**
     * Create the frame
     */

    private void createFrame() {
        // Create the frame
        JFrame f = new JFrame();
        f.setTitle("Kristian Ekle & Thomas Gautvedt :: IT3708 :: Project 5");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout(0, 5));
        f.add(chartPanel);
        f.pack();
        f.setVisible(true);
    }

    /**
     * Run the Evolver/plotter
     */

    private void run() {
        // Start the evolver
        Evolver evo = new Evolver();
        evo.initialize();

        // Loop until we are finished
        while (true) {
            // Reset the dataset
            this.chart.getXYPlot().setDataset(newDataset());

            // Run one generation
            boolean state = evo.runGeneration();

            // Populate the dataset
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    chart.removeLegend();

                    // Loop all the individuals
                    for (Individual member : evo.getChildren()) {
                        // Add current member to plot
                        plotData.add(new XYDataItem(member.getDistance(), member.getCost()));
                    }

                    // Set the dataset
                    chartPanel.getChart().getXYPlot().setDataset(chart.getXYPlot().getDataset());

                    // Update the UI
                    chartPanel.updateUI();
                }
            });

            // Check if we should break
            if (!state) {
                break;
            }
        }
    }

    /**
     * Create a new data set
     *
     * @return The clean data set
     */

    private XYDataset newDataset() {
        // Reset the plots
        plotData.clear();

        // Create a new dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Add the empty plots as the series
        dataset.addSeries(plotData);

        // Return the dataset
        return dataset;
    }
}
