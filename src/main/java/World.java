import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

/**
 * The class World, extends jfree for the barchart.
 */
public class World extends org.jfree.ui.ApplicationFrame {
    /**
     * Int representation of a danger situation
     */
    static final int danger = 7;

    /**
     * Int representation of a harmless situation
     */
    static final int noDanger = 4;

    /**
     * The distribution of harmless and danger situations
     */
    private double situationDistribution;

    /**
     * Amount of trainings situations simulated
     */
    private int nTrainSituations;

    /**
     * Amount of test situations simulated
     */
    private int nTestSituations;

    /**
     * List of agent
     */
    private List<Agent> agents;

    private boolean verbose = false;

    /**
     * Constructor to create the world with specified data.
     * @param situationDistribution the distribution of harmless and danger situations
     * @param numberOfTrainSituations amount of the trainings situations
     * @param numberOfTestSituations amount of the test situations
     * @param nAgents amount of agents
     * @param verbose debugging
     */
    public World(double situationDistribution, int numberOfTrainSituations, int numberOfTestSituations, int nAgents, boolean verbose) {
        super("");
        this.verbose = verbose;
        agents = new LinkedList<Agent>();
        this.situationDistribution = situationDistribution;
        this.nTrainSituations = numberOfTrainSituations;
        this.nTestSituations = numberOfTestSituations;
        while(nAgents-- > 0) { // create Agents
            agents.add(new Agent(this));
        }
    }

    /**
     * Starts the World within its specified parameters.
     */
    public void runWorld() {
        // Trainings cycles
        int situation = noDanger; // start with harmless situations
        for (int i = 0; i < nTrainSituations; i++) {
            if ((i / (double) nTrainSituations) >= situationDistribution) situation = danger; // check if following situations have to be dangerous based on the distribution
            for (Agent a : agents) { // run personal intentions of the agents (pure training)
                a.runPersonalIntention(situation);

                if (verbose) System.out.println(
                        "Agent[" + a.getId() +
                        "]:\tSD: [" + a.getStandardDeviation() + "]:\tTH: [" + a.getThreshold() + "]" +
                        "\tAgent Situation: [" + a.getAgentSituation() + "][" + situation + "]\t" + "\t\tRes[ " + ((a.getRunAwayIntention() && situation == 7) ? "CORRECT ]" : "FALSE ]") +
                        "\n\t\t\tTP_DANGER: [" + a.getTP_rate_DANGER() + "]" +
                        "\tTP_NO_DANGER: [" + a.getTP_rate_NO_DANGER() + "]\n");
            }
        }

        // Test cycles
        situation = noDanger; // start with harmless situations
        int correct = 0;
        int correct_MEAN_TH = 0;
        int numberOfSituations_ = nTestSituations;
        for (int i = 0; i < numberOfSituations_; i++) {
            if ((i / (double) numberOfSituations_) >= situationDistribution) situation = danger; // check if following situations have to be dangerous based on the distribution
            for (Agent a : agents) {
                a.runPersonalIntention(situation); // run personal intentions of the agents
            }
            for (Agent a : agents) {
                a.runInfluencedReaction(situation); // run personal influenced reactions of the agents
                if (a.runAway() == (situation == 7)) correct++; // count if the influenced reaction was correct
                if (a.isRunAwayThresholdMeanAgent() == (situation == 7)) correct_MEAN_TH++; // count if the influenced incl. average threshold reaction was correct
            }
        }
        System.out.println("Correct: " + (correct/((double)numberOfSituations_*agents.size())));
        System.out.println("Correct: " + (correct_MEAN_TH/((double)numberOfSituations_*agents.size())));
    }

    /**
     * Get a list of all Agents
     * @return List of Agents
     */
    public List<Agent> getAgents() {
        return agents;
    }

    /**
     * It creates a BarChart for all Agents. Representing thr situation danger with True and False Positives in percent.
     * All three cases will be shown. Not influenced, influenced and influenced plus group average for calculations.
     */
    public void barChart_FP_TP() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int i = 1;
        for (Agent a : agents) {
            // personal intention Bars
            dataset.addValue(100 * (1 - a.getTP_rate_NO_DANGER()), "FP", i + "");
            dataset.addValue((100) * a.getTP_rate_DANGER(), "TP", i + "");
            // Influenced Bars
            dataset.addValue(100 * (1 - a.getTP_rate_NO_DANGER_INFLUENCED()), "FP_INFLUENCED", i + "");
            dataset.addValue((100) * a.getTP_rate_DANGER_INFLUENCED(), "TP_INFLUENCED", i + "");
            i++;
        }

        // Influenced and average threshold Bars
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

