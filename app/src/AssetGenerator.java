import java.util.Random;

/**
 * Created by stacymiller on 26.03.15.
 */
public class AssetGenerator {
    static Random rnd = new Random();
    static double lambda = 10.;
    static double volatility = 0.31436183;
    static double profitability = 0.3;
    static double timedelta = 1;
    static double eps = 10e-6;

    public static ImitatedAsset generateAssetTree(int branches, int steps, double initialPrice){
        if (timedelta == 1) {
            timedelta = 1. / steps;
        }
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, steps == 0);
        if (steps > 0) {
            for (int branch = 0; branch < branches; branch++) {
                double price = getRandomPrice(initialPrice);
                ans.children.add(generateAssetTree(branches, steps - 1, price));
            }
        }
        return ans;
    }

    protected static double getRandomPrice(double initialPrice) {
        return initialPrice * (1 + profitability*timedelta + volatility*rnd.nextGaussian()*Math.sqrt(timedelta));
    }
}
