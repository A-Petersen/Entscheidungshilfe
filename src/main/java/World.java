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
    /**
     * The value of danger
     */
    static final int danger = 7;

    /**
     * The value of no danger
     */
    static final int noDanger = 4;

    /**
     * The distribution of the situation
     */
    private double situationDistribution;

    /**
     * The number of the situation
     */
    private int numberOfSituations;

    /**
     * List of agent
     */
    private List<Agent> agents;

    /**
     * Constructor to create agent with specified data
     * @param situationDistribution the distribution of the situation
     * @param numberOfSituations the number of the situations
     * @param numberOfAgents the number of agent
     * @param title title for jfreechart
     */
    public World(double situationDistribution, int numberOfSituations, int numberOfAgents, String title) {
        super(title);
        agents = new LinkedList<Agent>();
        this.situationDistribution = situationDistribution;
        this.numberOfSituations = numberOfSituations;
        while(numberOfAgents-- > 0) {
            agents.add(new Agent(this));
        }
    }

    // Runaway gefahr erkannt

    /**
     * Starts to train the Agents multi times. Each Agent get train to check if it is danger or not danger.
     */
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

    /**
     * Get a list of all Agents
     * @return
     */
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * Create a graph about Agents with False or True Positive
     */
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

    /**
     * It creates a statistic of all Agents of danger, True and False Positiv in percent.
     */
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

