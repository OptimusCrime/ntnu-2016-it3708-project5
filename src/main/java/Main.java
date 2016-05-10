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

    // For "all" chart
    private ChartPanel allChartPanel;
    private XYSeries allPlotData;
    private JFreeChart allChart;

    // For "front" chart
    private ChartPanel frontChartPanel;
    private XYSeries frontPlotData;
    private JFreeChart frontChart;

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
        // Create the charts
        this.createAllChart();
        this.createFrontChart();

        // Create the frame
        this.createFrame();

        // Run stuff
        this.run();
    }

    /**
     * Create the "all" chart
     */

    private void createAllChart() {
        // Create the initial plot data
        this.allPlotData = new XYSeries("");

        // Fetch the data set
        XYDataset dc = newAllDataset();

        // Create the chart
        this.allChart = ChartFactory.createScatterPlot(
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
        XYPlot plot = this.allChart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);

        // Create the chart panel
        this.allChartPanel = new ChartPanel(this.allChart);

        // Set various settings to chart panel
        this.allChartPanel.setMouseWheelEnabled(false);
        this.allChartPanel.setHorizontalAxisTrace(true);
        this.allChartPanel.setVerticalAxisTrace(true);
    }

    /**
     * Create the "front" chart
     */

    private void createFrontChart() {
        // Create the initial plot data
        this.frontPlotData = new XYSeries("");

        // Fetch the data set
        XYDataset dc = newFrontDataset();

        // Create the chart
        this.frontChart = ChartFactory.createScatterPlot(
                "",
                "Distance",
                "Cost",
                dc,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        // Remove legends
        allChart.removeLegend();
        frontChart.removeLegend();

        // Fix some stuff
        XYPlot plot = this.frontChart.getXYPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesVisible(true);

        // Create the chart panel
        this.frontChartPanel = new ChartPanel(this.frontChart);

        // Set various settings to chart panel
        this.frontChartPanel.setMouseWheelEnabled(false);
        this.frontChartPanel.setHorizontalAxisTrace(true);
        this.frontChartPanel.setVerticalAxisTrace(true);
    }

    /**
     * Create the frame
     */

    private void createFrame() {
        // Create the frame
        JFrame f = new JFrame();
        f.setTitle("Kristian Ekle & Thomas Gautvedt :: IT3708 :: Project 5");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Create panels
        JPanel chartContainer = new JPanel();
        JPanel allChartPanel = new JPanel();
        JPanel frontChartPanel = new JPanel();

        // Add charts
        allChartPanel.add(this.allChartPanel);
        frontChartPanel.add(this.frontChartPanel);

        // Add panels to container
        chartContainer.setLayout(new GridLayout(1,2));
        chartContainer.add(allChartPanel);
        chartContainer.add(frontChartPanel);

        // Add container to frame and display
        f.add(chartContainer);
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
            this.allChart.getXYPlot().setDataset(newAllDataset());
            this.frontChart.getXYPlot().setDataset(newFrontDataset());

            // Run one generation
            boolean state = evo.runGeneration();

            // Populate the dataset
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    // Remove legends
                    allChart.removeLegend();
                    frontChart.removeLegend();

                    //
                    // ALL CHART
                    //

                    // Loop all the individuals
                    for (Individual member : evo.getChildren()) {
                        // Add current member to plot
                        allPlotData.add(new XYDataItem(member.getDistance(), member.getCost()));
                    }

                    // Set the dataset
                    allChartPanel.getChart().getXYPlot().setDataset(allChart.getXYPlot().getDataset());

                    // Update the UI
                    allChartPanel.updateUI();

                    //
                    // FRONT CHART
                    //

                    // Loop all members in the first front
                    for (Individual member : evo.getParetoFronts().get(0).getAllMembers()) {
                        // Add current member to plot
                        frontPlotData.add(new XYDataItem(member.getDistance(), member.getCost()));
                    }

                    // Set the dataset
                    frontChartPanel.getChart().getXYPlot().setDataset(frontChart.getXYPlot().getDataset());

                    // Update the UI
                    frontChartPanel.updateUI();
                }
            });

            // Check if we should break
            if (!state) {
                break;
            }
        }
    }

    /**
     * Create a new data set for "all" chart
     *
     * @return The clean data set
     */

    private XYDataset newAllDataset() {
        // Reset the plots
        allPlotData.clear();

        // Create a new dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Add the empty plots as the series
        dataset.addSeries(allPlotData);

        // Return the dataset
        return dataset;
    }

    /**
     * Create a new data set for "front" chart
     *
     * @return The clean data set
     */

    private XYDataset newFrontDataset() {
        // Reset the plots
        frontPlotData.clear();

        // Create a new dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Add the empty plots as the series
        dataset.addSeries(frontPlotData);

        // Return the dataset
        return dataset;
    }
}
