public class main {
    public static void main(String [ ] args) {
        World world = new World(0.5, 10000, 25, "Titel");
        world.runWorld();
//        world.getXYagents();
//        world.populationChart_FP_TP();
        world.barChart_FP_TP();
    }
}
