import ea.Evolver;
import ea.Settings;
import nsga.Individual;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class Main {

    // The evolution
    private Evolver evo;

    // Java SWING!
    private JLabel generationLabel;
    private JLabel allCountLabel;
    private JLabel frontCountLabel;
    private JLabel frontNumberCount;

    //private Timeline timeline;
    private boolean running;

    // For "all" chart
    private ChartPanel allChartPanel;
    private XYSeries allPlotDataAll;
    private XYSeries allPlotDataBest;
    private XYSeries allPlotDataWorst;
    private JFreeChart allChart;

    // For "front" chart
    private ChartPanel frontChartPanel;
    private XYSeries frontPlotDataAll;
    private XYSeries frontPlotDataBest;
    private XYSeries frontPlotDataWorst;
    private JFreeChart frontChart;

    // Pause
    private boolean pause = false;

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
        if (Settings.weirdPlotting) {
            this.weirdPlotting();
        } else {
            this.run();
        }
    }

    /**
     * Create the "all" chart
     */

    private void createAllChart() {
        // Create the initial plot data
        this.allPlotDataAll = new XYSeries("");
        this.allPlotDataBest = new XYSeries("");
        this.allPlotDataWorst = new XYSeries("");

        // Fetch the data set
        XYDataset dc = newAllDataset();

        // Create the chart
        this.allChart = ChartFactory.createScatterPlot(
                "Entire population",
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
        plot.setRangeCrosshairVisible(false);
        plot.setDomainCrosshairVisible(false);
        plot.setBackgroundPaint(new Color(221, 221, 221));
        plot.setDomainGridlinePaint(new Color(119, 119, 199));
        plot.setRangeGridlinePaint(new Color(119, 119, 119));
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
        this.frontPlotDataAll = new XYSeries("");
        this.frontPlotDataBest = new XYSeries("");
        this.frontPlotDataWorst = new XYSeries("");

        // Fetch the data set
        XYDataset dc = newFrontDataset();

        // Create the chart
        this.frontChart = ChartFactory.createScatterPlot(
                "Pareto-front",
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
        plot.setRangeCrosshairVisible(false);
        plot.setDomainCrosshairVisible(false);
        plot.setBackgroundPaint(new Color(221, 221, 221));
        plot.setDomainGridlinePaint(new Color(119, 119, 199));
        plot.setRangeGridlinePaint(new Color(119, 119, 119));
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
        f.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                // Toggle pause
                pause = !pause;
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        f.setTitle("Kristian Ekle & Thomas Gautvedt :: IT3708 :: Project 5");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Create panels
        JPanel outerContainer = new JPanel();
        JPanel chartContainer = new JPanel();
        JPanel allChartPanel = new JPanel();
        JPanel frontChartPanel = new JPanel();

        // Add charts
        allChartPanel.add(this.allChartPanel);
        frontChartPanel.add(this.frontChartPanel);

        // Add panels to container
        chartContainer.setLayout(new GridLayout(1, 2));
        chartContainer.add(allChartPanel);
        chartContainer.add(frontChartPanel);

        // Create labels
        generationLabel = new JLabel("Generation: 0 / " + Settings.maxGeneration);
        generationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        allCountLabel = new JLabel("Population Size: ");
        allCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        frontNumberCount = new JLabel("Number of fronts: ");
        frontNumberCount.setAlignmentX(Component.CENTER_ALIGNMENT);

        frontCountLabel = new JLabel("Non Dominated Solutions: ");
        frontCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add to outer layout
        outerContainer.setLayout(new BoxLayout(outerContainer, BoxLayout.Y_AXIS));
        outerContainer.add(generationLabel);
        outerContainer.add(allCountLabel);
        outerContainer.add(frontNumberCount);
        outerContainer.add(frontCountLabel);
        outerContainer.add(chartContainer);

        // Add container to frame and display
        f.add(outerContainer);
        f.pack();
        f.setVisible(true);
    }

    /**
     * Start the interval that updates the view every nth ms
     */

    private void run() {
        // Set running
        this.running = true;

        // Start the evolver
        this.evo = new Evolver();
        this.evo.initialize();

        Timer timer = new Timer(Settings.tick, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!pause) {
                    tick();
                }
            }
        });
        timer.start();
    }

    private void weirdPlotting() {

        //
        // FIRST RUN
        //

        this.evo = new Evolver();
        this.evo.initialize();
        this.evo.solve();

        ArrayList<Individual> runOnePareto = new ArrayList<>(this.evo.getParetoFronts().get(0).getAllMembers());

        //
        // RUN TWO
        //

        Settings.populationSize = 1000;
        Settings.maxGeneration = 500;
        Settings.crossover = 0.8;
        Settings.mutation = 0.5;
        Settings.tournamentSize = 50;

        this.evo = new Evolver();
        this.evo.initialize();
        this.evo.solve();

        ArrayList<Individual> runTwoPareto = new ArrayList<>(this.evo.getParetoFronts().get(0).getAllMembers());

        //
        // RUN THREE
        //

        Settings.populationSize = 2000;
        Settings.maxGeneration = 250;
        Settings.crossover = 0.9;
        Settings.mutation = 0.7;
        Settings.tournamentSize = 100;

        this.evo = new Evolver();
        this.evo.initialize();
        this.evo.solve();

        ArrayList<Individual> runThreePareto = new ArrayList<>(this.evo.getParetoFronts().get(0).getAllMembers());

        //
        // PLOTTING
        //

        // The values
        ArrayList<Individual>[] values = (ArrayList<Individual>[]) new ArrayList[3];
        values[0] = runOnePareto;
        values[1] = runTwoPareto;
        values[2] = runThreePareto;

        // The series
        XYSeries[] series = new XYSeries[3];
        series[0] = frontPlotDataAll;
        series[1] = frontPlotDataBest;
        series[2] = frontPlotDataWorst;

        // Used for scaling
        double bestX = Double.MIN_VALUE;
        double worstX = Double.MAX_VALUE;
        double bestY = Double.MIN_VALUE;
        double worstY = Double.MAX_VALUE;

        System.out.println(runOnePareto.size());
        System.out.println(runTwoPareto.size());

        // Loop all the value lists
        for (int i = 0; i < values.length; i++) {
            // Loop all members of this value
            for (Individual member : values[i]) {
                series[i].add(new XYDataItem(member.getDistance(), member.getCost()));
            }

            // Get best and worst from the data set
            ArrayList<Individual> bestAndWorst = Evolver.getBestAndWorst(values[i]);

            // Calculate the values
            double tempWorstX = Math.max(0, bestAndWorst.get(1).getDistance() - (bestAndWorst.get(1).getDistance() * 0.1));
            double tempBestX = bestAndWorst.get(0).getDistance() * 1.1;

            double tempWorstY = Math.max(0, bestAndWorst.get(3).getCost() - (bestAndWorst.get(3).getCost() * 0.1));
            double tempBestY = bestAndWorst.get(2).getCost() * 1.1;

            // Check if we should update the scaling
            if (tempWorstX < worstX) {
                worstX = tempWorstX;
            }
            if (tempBestX > bestX) {
                bestX = tempBestX;
            }

            if (tempWorstY < worstY) {
                worstY = tempWorstY;
            }
            if (tempBestY > bestY) {
                bestY = tempBestY;
            }

        }

        // Change colorz
        frontChart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE); // All
        frontChart.getXYPlot().getRenderer().setSeriesPaint(1, Color.GREEN); // Best
        frontChart.getXYPlot().getRenderer().setSeriesPaint(2, Color.RED); // Worst

        // Set range
        NumberAxis frontDomainAxis = (NumberAxis) frontChart.getXYPlot().getDomainAxis();
        frontDomainAxis.setRange(worstX, bestX);
        NumberAxis frontRangeAxis = (NumberAxis) frontChart.getXYPlot().getRangeAxis();
        frontRangeAxis.setRange(worstY, bestY);

        // Change draw order
        frontChart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

        // Set the dataset
        frontChartPanel.getChart().getXYPlot().setDataset(frontChart.getXYPlot().getDataset());

        // Update the UI
        frontChartPanel.updateUI();
    }

    /**
     * Run a single tick
     */

    public void tick() {
        // Make sure we are still running
        if (running) {
            // Reset the dataset
            this.allChart.getXYPlot().setDataset(newAllDataset());
            this.frontChart.getXYPlot().setDataset(newFrontDataset());

            // Run one generation
            boolean state = this.evo.runGeneration();

            // Update generation label
            generationLabel.setText("Generation: " + this.evo.getGeneration() + " / " + Settings.maxGeneration);
            allCountLabel.setText("Population Size: " + this.evo.getParents().size());
            frontNumberCount.setText("Number of fronts: " + this.evo.getParetoFronts().size());
            frontCountLabel.setText("Non Dominated Solutions: " + this.evo.getParetoFronts().get(0).getSize());

            // Remove legends
            allChart.removeLegend();
            frontChart.removeLegend();

            //
            // ALL CHART
            //

            // Store the best and worst for each member in this front
            ArrayList<Individual> allBestAndWorst = Evolver.getBestAndWorst(this.evo.getParents());

            // Loop all the individuals
            for (Individual member : this.evo.getParents()) {
                // Avoid plotting the best/worst because they overdraw each other
                if (!allBestAndWorst.contains(member)) {
                    // Add current member to plot
                    allPlotDataAll.add(new XYDataItem(member.getDistance(), member.getCost()));
                }
            }

            // Add best (0th and 2nd element)
            allPlotDataBest.add(new XYDataItem(allBestAndWorst.get(0).getDistance(), allBestAndWorst.get(0).getCost()));
            allPlotDataBest.add(new XYDataItem(allBestAndWorst.get(2).getDistance(), allBestAndWorst.get(2).getCost()));

            // Add worst (1st and 3rd element)
            allPlotDataWorst.add(new XYDataItem(allBestAndWorst.get(1).getDistance(), allBestAndWorst.get(1).getCost()));
            allPlotDataWorst.add(new XYDataItem(allBestAndWorst.get(3).getDistance(), allBestAndWorst.get(3).getCost()));

            // Change colorz
            allChart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE); // All
            allChart.getXYPlot().getRenderer().setSeriesPaint(1, Color.GREEN); // Best
            allChart.getXYPlot().getRenderer().setSeriesPaint(2, Color.RED); // Worst

            // Set range
            NumberAxis allDomainAxis = (NumberAxis) allChart.getXYPlot().getDomainAxis();
            allDomainAxis.setRange(Math.max(0, allBestAndWorst.get(1).getDistance() -
                    (allBestAndWorst.get(1).getDistance() * 0.1)), allBestAndWorst.get(0).getDistance() * 1.1);
            NumberAxis allRangeAxis = (NumberAxis) allChart.getXYPlot().getRangeAxis();
            allRangeAxis.setRange(Math.max(0, allBestAndWorst.get(3).getCost() -
                    (allBestAndWorst.get(3).getCost() * 0.1)), allBestAndWorst.get(2).getCost() * 1.1);

            // Change draw order
            allChart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

            // Set the dataset
            allChartPanel.getChart().getXYPlot().setDataset(allChart.getXYPlot().getDataset());

            // Update the UI
            allChartPanel.updateUI();

            //
            // FRONT CHART
            //

            // Store the best and worst for each member in this front
            ArrayList<Individual> frontBestAndWorst = Evolver.getBestAndWorst(this.evo.getParetoFronts().get(0).getAllMembers());

            // Loop all members in the first front
            for (Individual member : this.evo.getParetoFronts().get(0).getAllMembers()) {
                // Avoid plotting the best/worst because they overdraw each other
                if (!frontBestAndWorst.contains(member)) {
                    // Add current member to plot
                    frontPlotDataAll.add(new XYDataItem(member.getDistance(), member.getCost()));
                }
            }

            // Add best (0th and 2nd element)
            frontPlotDataBest.add(new XYDataItem(frontBestAndWorst.get(0).getDistance(), frontBestAndWorst.get(0).getCost()));
            frontPlotDataBest.add(new XYDataItem(frontBestAndWorst.get(2).getDistance(), frontBestAndWorst.get(2).getCost()));

            // Add worst (1st and 3rd element)
            frontPlotDataWorst.add(new XYDataItem(frontBestAndWorst.get(1).getDistance(), frontBestAndWorst.get(1).getCost()));
            frontPlotDataWorst.add(new XYDataItem(frontBestAndWorst.get(3).getDistance(), frontBestAndWorst.get(3).getCost()));

            // Change colorz
            frontChart.getXYPlot().getRenderer().setSeriesPaint(0, Color.BLUE); // All
            frontChart.getXYPlot().getRenderer().setSeriesPaint(1, Color.GREEN); // Best
            frontChart.getXYPlot().getRenderer().setSeriesPaint(2, Color.RED); // Worst

            // Set range
            NumberAxis frontDomainAxis = (NumberAxis) frontChart.getXYPlot().getDomainAxis();
            frontDomainAxis.setRange(Math.max(0, frontBestAndWorst.get(1).getDistance() -
                    (frontBestAndWorst.get(1).getDistance() * 0.1)), frontBestAndWorst.get(0).getDistance() * 1.1);
            NumberAxis frontRangeAxis = (NumberAxis) frontChart.getXYPlot().getRangeAxis();
            frontRangeAxis.setRange(Math.max(0, frontBestAndWorst.get(3).getCost() -
                    (frontBestAndWorst.get(3).getCost() * 0.1)), frontBestAndWorst.get(2).getCost() * 1.1);

            // Change draw order
            frontChart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);

            // Set the dataset
            frontChartPanel.getChart().getXYPlot().setDataset(frontChart.getXYPlot().getDataset());

            // Update the UI
            frontChartPanel.updateUI();

            // Check if we should break
            if (!state) {
                running = false;
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
        allPlotDataAll.clear();
        allPlotDataBest.clear();
        allPlotDataWorst.clear();

        // Create a new dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Add the empty plots as the series
        dataset.addSeries(allPlotDataAll);
        dataset.addSeries(allPlotDataBest);
        dataset.addSeries(allPlotDataWorst);

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
        frontPlotDataAll.clear();
        frontPlotDataBest.clear();
        frontPlotDataWorst.clear();

        // Create a new dataset
        XYSeriesCollection dataset = new XYSeriesCollection();

        // Add the empty plots as the series
        dataset.addSeries(frontPlotDataAll);
        dataset.addSeries(frontPlotDataBest);
        dataset.addSeries(frontPlotDataWorst);

        // Return the dataset
        return dataset;
    }

}
