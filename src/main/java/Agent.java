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
    private boolean runAwayIntention;
    private int COUNT_TP_DANGER = 0;
    private double TP_rate_DANGER = 0;
    private int COUNT_TP_NO_DANGER = 0;
    private double TP_rate_NO_DANGER = 0;
    private int numberOfSituations_DANGER = 0;
    private int numberOfSituations_NO_DANGER = 0;

    private double standardDeviation;
    private double threshold;

    private double agentSituation;

    private GaussianGenerator gGenDanger;
    private GaussianGenerator gGenNoDanger;

    public Agent(World world) {
        this.world = world;
        this.id = agent_ids;
        this.standardDeviation = l_SD + ((u_SD - l_SD) * rGen.nextDouble());
        this.threshold = l_TH + ((u_TH - l_TH) * rGen.nextDouble());
        this.gGenNoDanger = new GaussianGenerator(World.noDanger, standardDeviation, new Random());
        this.gGenDanger = new GaussianGenerator(World.danger, standardDeviation, new Random());
        agent_ids++;
    }

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
    }

    void runInfluencedReaction(int situation) {
        List<Agent> agents = world.getAgents();

        double TP_rate_DANGER_AGENTS;
        int dangerDetect = 0;
        for (Agent a : agents) {
            if (a.getRunAwayIntention()) dangerDetect++;
        }
        TP_rate_DANGER_AGENTS = (double)dangerDetect / agents.size();

        double TPFP_Treshold = ( TP_rate_DANGER + (1 - TP_rate_NO_DANGER) ) / 2;
        if (TP_rate_DANGER_AGENTS > TPFP_Treshold) runAway = true;

        System.out.println("AGENT[" + id + "]\tRUN: [" + runAway + "][" + situation + "]\tD: [" + TP_rate_DANGER_AGENTS + "]- TH[" + TPFP_Treshold + "]");
    }

    double getTP_rate_DANGER () {
        TP_rate_DANGER = (double)COUNT_TP_DANGER / numberOfSituations_DANGER;
        return TP_rate_DANGER;
    }

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

    public double getAgentSituation() {
        return agentSituation;
    }

    public boolean getRunAwayIntention() {
        return runAwayIntention;
    }

    public boolean runAway() {
        return runAway;
    }
}
