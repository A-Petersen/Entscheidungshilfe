import org.uncommons.maths.random.GaussianGenerator;

import java.util.List;
import java.util.Random;

public class Agent {

    //  range 0.0d (inclusive) to 1.0d (exclusive)
    private static Random rGen = new Random();
    private static final double l_SD = 3.0;
    private static final double u_SD = 5.0;
    private static final double l_TH = 5.5;
    private static final double u_TH = 8.5;
    private static int agent_ids = 0;

    private int id;
    private World world;

    private boolean runAway;
    private boolean runAwayThresholdMeanAgent;
    private boolean runAwayIntention;

    private int COUNT_TP_DANGER = 0;
    private double TP_rate_DANGER = 0;
    private int COUNT_TP_NO_DANGER = 0;
    private double TP_rate_NO_DANGER = 0;
    private int COUNT_TP_DANGER_INFLUENCED = 0;
    private double TP_rate_DANGER_INFLUENCED = 0;
    private int COUNT_TP_NO_DANGER_INFLUENCED = 0;
    private double TP_rate_NO_DANGER_INFLUENCED = 0;
    private int COUNT_TP_DANGER_INFLUENCED_AVG = 0;
    private double TP_rate_DANGER_INFLUENCED_AVG = 0;
    private int COUNT_TP_NO_DANGER_INFLUENCED_AVG = 0;
    private double TP_rate_NO_DANGER_INFLUENCED_AVG = 0;

    private int numberOfSituations_DANGER = 0;
    private int numberOfSituations_NO_DANGER = 0;
    private int numberOfSituations_DANGER_INFLUENCED = 0;
    private int numberOfSituations_NO_DANGER_INFLUENCED = 0;
    private int numberOfSituations_DANGER_INFLUENCED_AVG = 0;
    private int numberOfSituations_NO_DANGER_INFLUENCED_AVG = 0;

    private double standardDeviation;
    private double threshold;
    private double TPFP_Treshold;

    private double agentSituation;

    private GaussianGenerator gGenDanger;
    private GaussianGenerator gGenNoDanger;

    /**
     *
     * @param world
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
     * @param situation
     */
    void runPersonalIntention(int situation) {
        if (situation == World.danger) {
            agentSituation = gGenDanger.nextValue();
        } else {
            agentSituation = gGenNoDanger.nextValue();
        }
        runAwayIntention = agentSituation > threshold;

        if (situation == World.danger) {
            if (runAwayIntention) COUNT_TP_DANGER++;
            numberOfSituations_DANGER++;
        } else {
            if (!runAwayIntention) COUNT_TP_NO_DANGER++;
            numberOfSituations_NO_DANGER++;
        }

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
        if (TP_rate_DANGER_AGENTS > TPFP_Treshold) runAway = true;
        if (situation == World.danger) {
            if (runAway) COUNT_TP_DANGER_INFLUENCED++;
            numberOfSituations_DANGER_INFLUENCED++;
        } else {
            if (!runAway) COUNT_TP_NO_DANGER_INFLUENCED++;
            numberOfSituations_NO_DANGER_INFLUENCED++;
        }
        if (TP_rate_DANGER_AGENTS > agentThreshold) runAwayThresholdMeanAgent = true;
        if (situation == World.danger) {
            if (runAwayThresholdMeanAgent) COUNT_TP_DANGER_INFLUENCED_AVG++;
            numberOfSituations_DANGER_INFLUENCED_AVG++;
        } else {
            if (!runAwayThresholdMeanAgent) COUNT_TP_NO_DANGER_INFLUENCED_AVG++;
            numberOfSituations_NO_DANGER_INFLUENCED_AVG++;
        }
        System.out.println("AGENT[" + id + "]\tRUN: [" + runAway + "][" + situation + "]\tMEAN: [" + TP_rate_DANGER_AGENTS + "] - TH[" + TPFP_Treshold + "]\t - MEAN_TH[" + agentThreshold + "]");
    }

    /**
     * The calculation of True Positive rate that is Danger
     * @return True Positiv Rate
     */
    double getTP_rate_DANGER () {
        TP_rate_DANGER = (double)COUNT_TP_DANGER / numberOfSituations_DANGER;
        return TP_rate_DANGER;
    }

    /**
     * The calculation of True Positive rate that is not Danger
     * @return False Positive
     */
    double getTP_rate_NO_DANGER () {
        TP_rate_NO_DANGER = (double)COUNT_TP_NO_DANGER / numberOfSituations_NO_DANGER;
        return TP_rate_NO_DANGER;
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

    /**
     *
     * @return
     */
    public double getAgentSituation() {
        return agentSituation;
    }

    /**
     *
     * @return
     */
    public boolean getRunAwayIntention() {
        return runAwayIntention;
    }

    /**
     *
     * @return
     */
    public boolean isRunAwayThresholdMeanAgent() {
        return runAwayThresholdMeanAgent;
    }

    /**
     *
     * @return
     */
    public boolean runAway() {
        return runAway;
    }

    /**
     *
     * @return
     */
    public double getTPFP_Treshold() {
        return TPFP_Treshold;
    }

    /**
     *
     * @return
     */
    public double getTP_rate_DANGER_INFLUENCED() {
        TP_rate_DANGER_INFLUENCED = (double)COUNT_TP_DANGER_INFLUENCED / numberOfSituations_DANGER_INFLUENCED;
        return TP_rate_DANGER_INFLUENCED;
    }

    /**
     *
     * @return
     */
    public double getTP_rate_NO_DANGER_INFLUENCED() {
        TP_rate_NO_DANGER_INFLUENCED = (double)COUNT_TP_NO_DANGER_INFLUENCED / numberOfSituations_NO_DANGER_INFLUENCED;
        return TP_rate_NO_DANGER_INFLUENCED;
    }

    /**
     *
     * @return
     */
    public double getTP_rate_DANGER_INFLUENCED_AVG() {
        TP_rate_DANGER_INFLUENCED_AVG = (double)COUNT_TP_DANGER_INFLUENCED_AVG / numberOfSituations_DANGER_INFLUENCED_AVG;
        return TP_rate_DANGER_INFLUENCED_AVG;
    }

    /**
     * The Average of influenced from True Positiv with no Danger
     * @return the average of True Positive(influenced) Rate
     */
    public double getTP_rate_NO_DANGER_INFLUENCED_AVG() {
        TP_rate_NO_DANGER_INFLUENCED_AVG = (double)COUNT_TP_NO_DANGER_INFLUENCED_AVG / numberOfSituations_NO_DANGER_INFLUENCED_AVG;
        return TP_rate_NO_DANGER_INFLUENCED_AVG;
    }
}
