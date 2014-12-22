public class Main {

    public static void main(String[] args) throws InterruptedException {
        int branches = 3;
        int steps = 150;
        int width = 50;
        int sectors = 7;
        double initialPrice = 100.;
//        ImitatedAsset ia = AssetGenerator.generateAssetByHistogram(10, branches, steps, 3, initialPrice);
//        System.out.println(ia);
//        System.out.println(BroadieGlassermanEstimation.upperEstimateHistogram(ia, 110));
//        System.out.println(BroadieGlassermanEstimation.lowerEstimateHistogram(ia, 110));

        System.out.println(String.format("branches=%d\tsteps=%d\twidth=%d\tsectors=%d\t", branches, steps, width, sectors));

        int converged = 0;
        int notConverged = 0;

        for (int i = 0; i < 100; i++) {
            if (testConvergence(initialPrice, sectors, steps, branches, width)) {
                converged++;
            } else {
                notConverged++;
            }
        }

        System.out.println(String.format("Converged: %d", converged));
        System.out.println(String.format("Not converged: %d", notConverged));
    }

    public static boolean testConvergence(double initialPrice, int sectors, int steps, int branches, int width) {
        int i;
        int stepLimit = 1000;
        double x0 = 0;
        double x1 = Double.MAX_VALUE;
        double eps = 0.001;
        long seed = System.currentTimeMillis();

        for (i = 0; i < stepLimit && (Math.abs(x0-x1) > eps); i++) {
            x0 = x1;
            AssetGenerator.rnd.setSeed(seed);
            ImitatedAsset ia = AssetGenerator.generateAssetByHistogram(width, branches, steps+i, sectors, initialPrice);
            x1 = BroadieGlassermanEstimation.upperEstimateHistogram(ia, 1.3*initialPrice);
        }
        System.out.println(String.format("%d\t%d\t%f", (i != stepLimit) ? 1 : 0, i, x1));

        // if we reached stepLimit therefore we did not converge
        return i != stepLimit;
    }
}
