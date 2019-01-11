import org.uncommons.maths.random.GaussianGenerator;

import java.util.List;
import java.util.Random;

public class Agent {
    /**
     * Random generator
     */
    private static Random rGen = new Random();

    /**
     * lower standard deviation
     */
    private static final double l_SD = 3.0;

    /**
     * upper standard deviation
     */
    private static final double u_SD = 5.0;

    /**
     * lower threshold
     */
    private static final double l_TH = 5.5;

    /**
     * upper threshold
     */
    private static final double u_TH = 8.5;

    /**
     * Agent id_counter
     */
    private static int agent_ids = 0;

    /**
     * ID
     */
    private int id;

    /**
     * World Class
     */
    private World world;

    /**
     * True if the situation is dangerous, influenced by the group of agents.
     */
    private boolean runAway;

    /**
     * True if the situation is dangerous, influenced and calculated with the mean threshold of the group of agents.
     */
    private boolean runAwayThresholdMeanAgent;

    /**
     * True if the situation is dangerous, personal intention.
     */
    private boolean runAwayIntention;

    /**
     * Counter of true positives for dangerous situations. Personal.
     */
    private int COUNT_TP_DANGER = 0;

    /**
     * True Positive rate for dangerous situations. Personal.
     */
    private double TP_rate_DANGER = 0;

    /**
     * Counter of true positives for harmless situations. Personal.
     */
    private int COUNT_TP_NO_DANGER = 0;

    /**
     * True Positive rate for harmless situations. Personal.
     */
    private double TP_rate_NO_DANGER = 0;

    /**
     * Counter of true positives for dangerous situations. Influenced by the group of agents.
     */
    private int COUNT_TP_DANGER_INFLUENCED = 0;

    /**
     * True Positive rate for dangerous situations. Influenced by the group of agents.
     */
    private double TP_rate_DANGER_INFLUENCED = 0;

    /**
     * Counter of true positives for harmless situations. Influenced by the group of agents.
     */
    private int COUNT_TP_NO_DANGER_INFLUENCED = 0;

    /**
     * True Positive rate for harmless situations. Influenced by the group of agents.
     */
    private double TP_rate_NO_DANGER_INFLUENCED = 0;
    /**
     * Counter of true positives for dangerous situations. Influenced and calculated with the mean threshold of the group of agents.
     */
    private int COUNT_TP_DANGER_INFLUENCED_AVG = 0;
    /**
     * True Positive rate for dangerous situations.  Influenced and calculated with the mean threshold of the group of agents.
     */
    private double TP_rate_DANGER_INFLUENCED_AVG = 0;
    /**
     * Counter of true positives for harmless situations. Influenced and calculated with the mean threshold of the group of agents.
     */
    private int COUNT_TP_NO_DANGER_INFLUENCED_AVG = 0;
    /**
     * True Positive rate for harmless situations. Influenced and calculated with the mean threshold of the group of agents.
     */
    private double TP_rate_NO_DANGER_INFLUENCED_AVG = 0;

    /**
     * Amount of dangerous situations simulated. Personal.
     */
    private int numberOfSituations_DANGER = 0;
    /**
     * Amount of harmless situations simulated. Personal.
     */
    private int numberOfSituations_NO_DANGER = 0;
    /**
     * Amount of dangerous situations simulated. Influenced by the group of agents.
     */
    private int numberOfSituations_DANGER_INFLUENCED = 0;
    /**
     * Amount of harmless situations simulated. Influenced by the group of agents.
     */
    private int numberOfSituations_NO_DANGER_INFLUENCED = 0;
    /**
     * Amount of dangerous situations simulated. Influenced and calculated with the mean threshold of the group of agents.
     */
    private int numberOfSituations_DANGER_INFLUENCED_AVG = 0;
    /**
     * Amount of harmless situations simulated. Influenced and calculated with the mean threshold of the group of agents.
     */
    private int numberOfSituations_NO_DANGER_INFLUENCED_AVG = 0;

    /**
     * Standard deviation of the GaussianGenerator.
     */
    private double standardDeviation;
    /**
     * Random threshold for personal decisions.
     */
    private double threshold;
    /**
     * Calculated threshold from True- and False-Positives.
     */
    private double TPFP_Treshold;

    /**
     * With RandomGaussianGenerator generated agent situation.
     */
    private double agentSituation;

    /**
     * RandomGaussianGenerator for dangerous situations.
     */
    private GaussianGenerator gGenDanger;
    /**
     * RandomGaussianGenerator for harmless situations.
     */
    private GaussianGenerator gGenNoDanger;

    /**
     * Constructor for Agent
     * @param world the world the agent exists in.
     */
    public Agent(World world) {
        this.world = world;
        this.id = agent_ids;
        this.standardDeviation = l_SD + ((u_SD - l_SD) * rGen.nextDouble());
        this.threshold = l_TH + ((u_TH - l_TH) * rGen.nextDouble());
        this.gGenNoDanger = new GaussianGenerator(World.noDanger, standardDeviation, new Random());
        this.gGenDanger = new GaussianGenerator(World.danger, standardDeviation, new Random());
        agent_ids++;
    }

    /**
     *
     * @param situation current situation in the World
     */
    void runPersonalIntention(int situation) {
        if (situation == World.danger) {
            agentSituation = gGenDanger.nextValue();
            if(runAwayIntention) COUNT_TP_DANGER++;
            numberOfSituations_DANGER++;
        } else {
            agentSituation = gGenNoDanger.nextValue();
            if (!runAwayIntention) COUNT_TP_NO_DANGER++;
            numberOfSituations_NO_DANGER++;
        }
        runAwayIntention = agentSituation > threshold;
        TP_rate_DANGER = getTP_rate_DANGER();
        TP_rate_NO_DANGER = getTP_rate_NO_DANGER();
        TPFP_Treshold = ( TP_rate_DANGER + (1 - TP_rate_NO_DANGER) ) / 2;
    }


    /**
     *
     * @param situation
     */
    void runInfluencedReaction(int situation) {
        List<Agent> agents = world.getAgents();
        double TP_rate_DANGER_AGENTS;
        double agentThreshold = 0;
        int dangerDetect = 0;
        for (Agent a : agents) {
            agentThreshold += a.getTPFP_Treshold();
            if (a.getRunAwayIntention()) dangerDetect++;
        }
        agentThreshold /= agents.size();

        TP_rate_DANGER_AGENTS = (double)dangerDetect / agents.size();

        runAway = false;
        runAwayThresholdMeanAgent = false;

        if (TP_rate_DANGER_AGENTS > agentThreshold) runAwayThresholdMeanAgent = true;
        if (TP_rate_DANGER_AGENTS > TPFP_Treshold) runAway = true;
        if (situation == World.danger) {
            if (runAway) COUNT_TP_DANGER_INFLUENCED++;
            numberOfSituations_DANGER_INFLUENCED++;
            if (runAwayThresholdMeanAgent) COUNT_TP_DANGER_INFLUENCED_AVG++;
            numberOfSituations_DANGER_INFLUENCED_AVG++;
        } else {
            if (!runAway) COUNT_TP_NO_DANGER_INFLUENCED++;
            numberOfSituations_NO_DANGER_INFLUENCED++;
            if (!runAwayThresholdMeanAgent) COUNT_TP_NO_DANGER_INFLUENCED_AVG++;
            numberOfSituations_NO_DANGER_INFLUENCED_AVG++;
        }
        System.out.println("AGENT[" + id + "]\tRUN: [" + runAway + "][" + situation + "]\tMEAN: [" + TP_rate_DANGER_AGENTS + "] - TH[" + TPFP_Treshold + "]\t - MEAN_TH[" + agentThreshold + "]");
    }

    public int getId() {
        return id;
    }


    public double getStandardDeviation() {
        return standardDeviation;
    }


    public double getThreshold() {
        return threshold;
    }


    public double getAgentSituation() {
        return agentSituation;
    }


    public boolean getRunAwayIntention() {
        return runAwayIntention;
    }


    public boolean isRunAwayThresholdMeanAgent() {
        return runAwayThresholdMeanAgent;
    }


    public boolean runAway() {
        return runAway;
    }


    double getTP_rate_DANGER () {
        TP_rate_DANGER = (double)COUNT_TP_DANGER / numberOfSituations_DANGER;
        return TP_rate_DANGER;
    }


    double getTP_rate_NO_DANGER () {
        TP_rate_NO_DANGER = (double)COUNT_TP_NO_DANGER / numberOfSituations_NO_DANGER;
        return TP_rate_NO_DANGER;
    }


    public double getTPFP_Treshold() {
        return TPFP_Treshold;
    }


    public double getTP_rate_DANGER_INFLUENCED() {
        TP_rate_DANGER_INFLUENCED = (double)COUNT_TP_DANGER_INFLUENCED / numberOfSituations_DANGER_INFLUENCED;
        return TP_rate_DANGER_INFLUENCED;
    }


    public double getTP_rate_NO_DANGER_INFLUENCED() {
        TP_rate_NO_DANGER_INFLUENCED = (double)COUNT_TP_NO_DANGER_INFLUENCED / numberOfSituations_NO_DANGER_INFLUENCED;
        return TP_rate_NO_DANGER_INFLUENCED;
    }


    public double getTP_rate_DANGER_INFLUENCED_AVG() {
        TP_rate_DANGER_INFLUENCED_AVG = (double)COUNT_TP_DANGER_INFLUENCED_AVG / numberOfSituations_DANGER_INFLUENCED_AVG;
        return TP_rate_DANGER_INFLUENCED_AVG;
    }


    public double getTP_rate_NO_DANGER_INFLUENCED_AVG() {
        TP_rate_NO_DANGER_INFLUENCED_AVG = (double)COUNT_TP_NO_DANGER_INFLUENCED_AVG / numberOfSituations_NO_DANGER_INFLUENCED_AVG;
        return TP_rate_NO_DANGER_INFLUENCED_AVG;
    }
}
