public class main {
    public static void main(String [ ] args) {
        World world = new World(0.5, 1000, 1000, 25, true);
        world.runWorld();
        world.barChart_FP_TP();
    }
}
