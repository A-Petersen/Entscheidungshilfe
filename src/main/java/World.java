import java.util.LinkedList;
import java.util.List;

public class World {

    static final int danger = 7;
    static final int noDanger = 4;

    private double situationDistribution;
    private int numberOfSituations;

    private List<Agent> agents;

    public World(double situationDistribution, int numberOfSituations, int numberOfAgents) {
        agents = new LinkedList<Agent>();
        this.situationDistribution = situationDistribution;
        this.numberOfSituations = numberOfSituations;
        while(numberOfAgents-- > 0) {
            agents.add(new Agent());
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
                        "\tAgent Situation: [" + a.getAgentSituation() + "][" + situation + "]" +
                        "\n\t\t\tTP_DANGER: [" + a.getTP_rate_DANGER() + "]" +
                        "\tTP_NO_DANGER: [" + a.getTP_rate_NO_DANGER() + "]\n");
            }
        }
    }
}
