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

        for (int b = 1; b < 5; b++)
            testConvergenceToTrueValue(4, 100, b*100);
//        testConvergenceToAmericanOption(100, 10);
    }

    private static String testConvergenceToTrueValue(int n, double initialPrice, int states) throws FileNotFoundException, UnsupportedEncodingException {
        String filename = String.format(Locale.ENGLISH, "test_convergence_to_true_value_%d.txt", states);
        double ansUp;
        double ansLow;
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        writer.println("branches,states,upper_estimator,lower_estimator");
        for (int branches = 3; branches < 100; branches++) {
            try {
                for (int sample = 0; sample < 100; sample++) {
                    ImitatedAsset ia;
                    ia = FiniteStateAssetGenerator.generateAssetTree(500, branches, n, initialPrice);
                    ansUp = BroadieGlassermanEstimation.upperEstimateHistogram(ia, 1.3 * initialPrice);
                    ansLow = BroadieGlassermanEstimation.lowerEstimateHistogram(ia, 1.3 * initialPrice);
                    writer.println(String.format(Locale.ENGLISH, "%d, %d, %f, %f", branches, states, ansUp, ansLow));
                    ia = null;
                }
            } catch (OutOfMemoryError oom) {
                break;
            }
        }
        writer.close();
        return filename;
    }

    private static String testConvergenceToAmericanOption(double initialPrice, int branches) throws FileNotFoundException, UnsupportedEncodingException {
        String filename = "test_convergence_to_american_option.txt";
        double ansUp;
        double ansLow;
        PrintWriter writer = new PrintWriter(filename, "UTF-8");
        writer.println("execution_times,upper_estimator,lower_estimator");
        for (int n = 4; n < 1000; n++) {
            try {
                for (int sample = 0; sample < 100; sample++) {
                    ImitatedAsset ia;
                    ia = EmpiricDistribAssetGenerator.generateAssetTree(branches, n, initialPrice);
                    ansUp = BroadieGlassermanEstimation.upperEstimate(ia, 1.3 * initialPrice);
                    ansLow = BroadieGlassermanEstimation.lowerEstimate(ia, 1.3 * initialPrice);
                    writer.println(String.format(Locale.ENGLISH, "%d, %f, %f", n, ansUp, ansLow));
                    System.out.println(String.format(Locale.ENGLISH, "%d, %f, %f", n, ansUp, ansLow));
                    ia = null;
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
            x1 = BroadieGlassermanEstimation.upperEstimateHistogram(ia, 1.3*initialPrice);
        }
        System.out.println(String.format("%d\t%d\t%f", (i != stepLimit) ? 1 : 0, i, x1));

        // if we reached stepLimit therefore we did not converge
        return i != stepLimit;
    }
}
