import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws InterruptedException, FileNotFoundException, UnsupportedEncodingException {
        int branches = 10;
        int steps = 150;
        int width = 50;
        int sectors = 7;
        double initialPrice = 100.;

//        ImitatedAsset ia = EmpiricDistribAssetGenerator.generateAssetTree(branches, 5, 100);

//        RandomSubtreeGeneratorEstimator.calculate(branches, 5, initialPrice, 100);
//        System.out.println(RandomSubtreeLineGeneratorEstimator.calculate(100, 100, 3, 3, 1000));
//        testConvergenceToTrueValue(5, 100);
        testConvergenceToAmericanOption(100, 15);
    }

    private static void calculateTruePrice(double initialPrice, int steps) {
        double true_value = 0.;
        for (int i = 0; i < 100; i++) {
            ImitatedAsset ia = AssetGenerator.generateAssetTree(15, steps-1, initialPrice);
            double[] ans = new double[]{BroadieGlassermanEstimation.upperEstimate(ia, 100),
                    BroadieGlassermanEstimation.lowerEstimate(ia, 100)};
            true_value += (ans[0] + ans[1]) / 2;
        }
        System.out.println(true_value / 100);
    }

    private static String testConvergenceToTrueValue(int n, double initialPrice) throws FileNotFoundException, UnsupportedEncodingException {
        String filename = "test_convergence_to_true_value_empiric_distrib_6_steps.txt";
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        writer.println("branches,upper_estimator,lower_estimator,elem_comp_upper_est,elem_comp_lower_est");
        for (int branches = 3; branches < 50; branches++) {
            try {
                for (int sample = 0; sample < 100; sample++) {
//                    double[] ans = RandomSubtreeGeneratorEstimator.calculate(branches, n, initialPrice, 100);
//                    writer.println(String.format(Locale.ENGLISH, "%d, %f, %f, %d, %d", branches, ans[0], ans[1], RandomSubtreeGeneratorEstimator.elCompUpper, RandomSubtreeGeneratorEstimator.elCompLower));
//                    System.out.println(String.format(Locale.ENGLISH, "%d, %f, %f, %d, %d", branches, ans[0], ans[1], RandomSubtreeGeneratorEstimator.elCompUpper, RandomSubtreeGeneratorEstimator.elCompLower));
                    ImitatedAsset ia = EmpiricDistribAssetGenerator.generateAssetTree(branches, n, initialPrice);
                    double[] ans = new double[]{BroadieGlassermanEstimation.upperEstimate(ia, 100),
                                                BroadieGlassermanEstimation.lowerEstimate(ia, 100)};
                    ia = null;
                    writer.println(String.format(Locale.ENGLISH,
                            "%d, %f, %f, %d, %d", branches, ans[0], ans[1],
                            BroadieGlassermanEstimation.elCompUpper, BroadieGlassermanEstimation.elCompLower));
                }
            } catch (OutOfMemoryError oom) {
                break;
            }
        }
        writer.close();
        return filename;
    }

    private static String testConvergenceToAmericanOption(double initialPrice, int branches) throws FileNotFoundException, UnsupportedEncodingException {
        String filename = "test_convergence_to_american_option_standard.txt";
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        writer.println("execution_times,upper_estimator,lower_estimator,elem_comp_upper_est,elem_comp_lower_est");
        for (int n = 3; n < 15; n++) {
            try {
                for (int sample = 0; sample < 100; sample++) {
//                    double[] ans = RandomSubtreeGeneratorEstimator.calculate(branches, n, initialPrice, 1.3*initialPrice);
//                    writer.println(String.format(Locale.ENGLISH, "%d, %f, %f, %d, %d", n, ans[0], ans[1],
//                            RandomSubtreeGeneratorEstimator.elCompUpper, RandomSubtreeGeneratorEstimator.elCompLower));
//                    System.out.println(String.format(Locale.ENGLISH, "%d, %f, %f, %d, %d", n, ans[0], ans[1],
//                            RandomSubtreeGeneratorEstimator.elCompUpper, RandomSubtreeGeneratorEstimator.elCompLower));

                    ImitatedAsset ia = AssetGenerator.generateAssetTree(branches, n, initialPrice);
                    double[] ans = new double[]{BroadieGlassermanEstimation.upperEstimate(ia, 1.3*initialPrice),
                                                BroadieGlassermanEstimation.lowerEstimate(ia, 1.3*initialPrice)};
                    ia = null;
                    writer.println(String.format(Locale.ENGLISH,
                            "%d, %f, %f, %d, %d", n, ans[0], ans[1],
                            BroadieGlassermanEstimation.elCompUpper, BroadieGlassermanEstimation.elCompLower));
                    System.out.println(String.format(Locale.ENGLISH, "%d, %f, %f, %d, %d", n, ans[0], ans[1],
                            BroadieGlassermanEstimation.elCompUpper, BroadieGlassermanEstimation.elCompLower));
                }
            } catch (OutOfMemoryError oom) {
                break;
            }
        }
        writer.close();
        return filename;
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
            ImitatedAsset ia = HistogramAssetGenerator.generateAssetTreeByHistogram(width, branches, steps + i, sectors, initialPrice);
            x1 = BroadieGlassermanEstimation.upperEstimate(ia, 1.3 * initialPrice);
        }
        System.out.println(String.format("%d\t%d\t%f", (i != stepLimit) ? 1 : 0, i, x1));

        // if we reached stepLimit therefore we did not converge
        return i != stepLimit;
    }
}
