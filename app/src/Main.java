public class Main {

    public static void main(String[] args) throws InterruptedException {
        int branches = 5;
        int steps = 1;
        int width = 50;
        int sectors = 10;
        double initialPrice = 100.;
//        ImitatedAsset ia = AssetGenerator.generateTreeAssets(branches, steps, initialPrice);
//        ImitatedAsset ia = AssetGenerator.generateAssetByHistogram(width, branches, steps, sectors, initialPrice);
//        System.out.println(ia);
//        System.out.println(BroadieGlassermanEstimation.upperEstimateHistogram(ia, 130));
//        System.out.println(BroadieGlassermanEstimation.lowerEstimateHistogram(ia, 130));
//        System.out.println(BroadieGlassermanEstimation.upperEstimate(ia, 130));
//        System.out.println(BroadieGlassermanEstimation.lowerEstimate(ia, 130));
//        ia = null;
//        System.gc();
        int converge = 0;
        int notConverge = 0;
        for(int k = 0; k < 200; k++){
            if (testConvergence(width, branches, steps, sectors, initialPrice)){
                converge++;
            } else {
                notConverge++;
            }
        }
        System.out.println(String.format("converges: %d", converge));
        System.out.println(String.format("does not converge: %d", notConverge));
    }

    static boolean testConvergence(int width, int branches, int steps, int sectors, double initialPrice){
        double x0 = Double.MAX_VALUE;
        double eps = 0.001;
        int i;
        long seed = System.currentTimeMillis();

        AssetGenerator.rnd.setSeed(seed);
        ImitatedAsset ia = AssetGenerator.generateAssetByHistogram(width, branches, steps, sectors, initialPrice);

//        if (ia.equals(AssetGenerator.generateAssetByHistogram(width, branches, steps, sectors, initialPrice))){
//            System.out.println("Equals");
//        } else {
//            System.out.println("Not equals");
//        }
//        return false;

//        double x1 = BroadieGlassermanEstimation.upperEstimateHistogram(ia, 1.3 * initialPrice);
        double x1 = BroadieGlassermanEstimation.lowerEstimateHistogram(ia, 1.3 * initialPrice);
        for (i = 1; (i < 1000) && Math.abs(x0-x1) > eps; i++){
            x0 = x1;
//            ia = null;
//            AssetGenerator.rnd.setSeed(seed);
//            ia = AssetGenerator.generateAssetByHistogram(width, branches, steps+i, sectors, initialPrice);

            x1 = BroadieGlassermanEstimation.lowerEstimateHistogram(ia, 1.3 * initialPrice);
//            System.out.println(x0);
//            System.out.println(x1);
//            System.out.println();
        }
//        System.out.println();
        return i != 1000; // true if converged, false otherwise
    }
}
