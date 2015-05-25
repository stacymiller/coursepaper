import org.apache.commons.math3.distribution.PoissonDistribution;

/**
 * Created by stacymiller on 24.05.15.
 */
public class RandomSubtreeAssetGenerator extends AssetGenerator{
    static PoissonDistribution poisson = new PoissonDistribution(1);
    public static ImitatedAsset generateAssetTree(int branches, int steps, double initialPrice){
        if (timedelta == 1) {
            timedelta = 1. / steps;
        }
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, steps == 0, timedelta);
        if (steps > 0) {
            for (int branch = 0; branch < branches; branch++) {
                double price = getRandomPrice(ans);
                double alpha = rnd2.nextDouble();

                System.out.println(1.2 / branches);
                if (alpha < 1.2 / branches) {
                    ans.addChild(generateAssetTree(branches, steps - 1, price));
                } else {
                    ans.addChild(new ImitatedAsset(price, 0, true, timedelta, true));
                }
            }
        }
        return ans;
    }
}
