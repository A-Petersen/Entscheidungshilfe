import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultKeyedValues2DDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class World extends org.jfree.ui.ApplicationFrame {

    static final int danger = 7;
    static final int noDanger = 4;

    private double situationDistribution;
    private int numberOfSituations;

    private List<Agent> agents;

    public World(double situationDistribution, int numberOfSituations, int numberOfAgents, String title) {
        super(title);
        agents = new LinkedList<Agent>();
        this.situationDistribution = situationDistribution;
        this.numberOfSituations = numberOfSituations;
        while(numberOfAgents-- > 0) {
            agents.add(new Agent(this));
        }
    }

    public void runWorld() {
        int situation = noDanger;
        for (int i = 0; i < numberOfSituations; i++) {
            if ((i / (double)numberOfSituations) >= situationDistribution) situation = danger;
            for (Agent a : agents) {
                a.runPersonalIntention(situation);

                System.out.println(
                        "Agent[" + a.getId() +
                        "]:\tSD: [" + a.getStandardDeviation() + "]:\tTH: [" + a.getThreshold() + "]" +
                        "\tAgent Situation: [" + a.getAgentSituation() + "][" + situation + "]\t" + "\t\tRes[ " + ((a.getRunAwayIntention() && situation == 7) ? "CORRECT ]" : "FALSE ]") +
                        "\n\t\t\tTP_DANGER: [" + a.getTP_rate_DANGER() + "]" +
                        "\tTP_NO_DANGER: [" + a.getTP_rate_NO_DANGER() + "]\n");
            }
        }

        situation = noDanger;
        int correct = 0;
        int correct_MEAN_TH = 0;
        int numberOfSituations_ = 10000;
        for (int i = 0; i < numberOfSituations_; i++) {
            if ((i / (double) numberOfSituations_) >= situationDistribution) situation = danger;
            for (Agent a : agents) {
                a.runPersonalIntention(situation);
            }
            for (Agent a : agents) {
                a.runInfluencedReaction(situation);
                if (a.runAway() == (situation == 7)) correct++;
                if (a.isRunAwayThresholdMeanAgent() == (situation == 7)) correct_MEAN_TH++;
            }
        }
        System.out.println("Correct: " + (correct/((double)numberOfSituations_*agents.size())));
        System.out.println("Correct: " + (correct_MEAN_TH/((double)numberOfSituations_*agents.size())));
    }


    public List<Agent> getAgents() {
        return agents;
    }

    public void getXYagents() {
        int i = 1;
        XYSeriesCollection collection = new XYSeriesCollection();
        XYSeries TP_DANGER = new XYSeries("TP_DANGER");
        XYSeries FP_DANGER = new XYSeries("FP_DANGER");
        for (Agent a : agents) {
            TP_DANGER.add(i, a.getTP_rate_DANGER());
            FP_DANGER.add(i, 1 - a.getTP_rate_NO_DANGER());
            i++;
        }
        collection.addSeries(TP_DANGER);
        collection.addSeries(FP_DANGER);

        XYDotRenderer dot = new XYDotRenderer();
        dot.setDotHeight(5);
        dot.setDotWidth(5);

        NumberAxis xax = new NumberAxis("Agents");
        NumberAxis yax = new NumberAxis("FP / TP");

        XYPlot plot = new XYPlot(collection, xax, yax, dot);

        JFreeChart chart2 = new JFreeChart(plot);

        ApplicationFrame punkteframe = new ApplicationFrame("Punkte"); //"Punkte" entspricht der Ueberschrift des Fensters

        ChartPanel chartPanel2 = new ChartPanel(chart2);
        punkteframe.setContentPane(chartPanel2);
        punkteframe.pack();
        punkteframe.setVisible(true);
    }

    public void populationChart_FP_TP() {

        DefaultKeyedValues2DDataset data = new DefaultKeyedValues2DDataset();
        int i = 1;
        for (Agent a : agents) {
            data.addValue(100 * (1 - a.getTP_rate_NO_DANGER()), "FP", i + "");
            data.addValue((-100) * a.getTP_rate_DANGER(), "TP", i + "");
            i++;
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
                            "True and False Positives",
                            "Agent_ID",     // domain axis label
                            "Percent (%)", // range axis label
                            data,         // data
                            PlotOrientation.HORIZONTAL,
                            true,            // include legend
                            true,            // tooltips
                            false            // urls
               );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);
    }

    public void barChart_FP_TP() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int i = 1;
        for (Agent a : agents) {
            dataset.addValue(100 * (1 - a.getTP_rate_NO_DANGER()), "FP", i + "");
            dataset.addValue((100) * a.getTP_rate_DANGER(), "TP", i + "");

            dataset.addValue(100 * (1 - a.getTP_rate_NO_DANGER_INFLUENCED()), "FP_INFLUENCED", i + "");
            dataset.addValue((100) * a.getTP_rate_DANGER_INFLUENCED(), "TP_INFLUENCED", i + "");
            i++;
        }

        dataset.addValue(100 * (1 - agents.get(0).getTP_rate_NO_DANGER_INFLUENCED_AVG()), "FP_INFLUENCED_AVG", "AVG");
        dataset.addValue((100) * agents.get(0).getTP_rate_DANGER_INFLUENCED_AVG(), "TP_INFLUENCED_AVG", "AVG");

        JFreeChart chart = ChartFactory.createBarChart(
                "Danger, TP and FP", // chart title
                "Agent_ID", // domain axis label
                "Percent", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // URLs
        );

        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(chartPanel);

        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);

    }
}

