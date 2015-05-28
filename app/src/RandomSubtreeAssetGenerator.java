import org.apache.commons.math3.distribution.PoissonDistribution;

import static java.lang.Math.max;

/**
 * Created by stacymiller on 24.05.15.
 */
public class RandomSubtreeAssetGenerator extends AssetGenerator{
    static PoissonDistribution poisson = new PoissonDistribution(1);
    public static ImitatedAsset generateAssetTree(int branches, int steps, double initialPrice){
        if (timedelta == 1) {
            timedelta = 1. / steps;
            stepsTotal = steps;
        }
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, steps == 0, timedelta);
        if (steps > 0) {
            int needCalculation = 0;
            System.out.println((steps + 1.) / stepsTotal);
            for (int branch = 0; branch < branches; branch++) {
                if (rnd2.nextDouble() < (steps + 1.) / stepsTotal){
                    needCalculation++;
                }
            }
            needCalculation = max(needCalculation, 1);
            for (int branch = 0; branch < needCalculation; branch++) {
                double price = getRandomPrice(ans);
                ans.addChild(generateAssetTree(branches, steps - 1, price));
            }
            for (int branch = needCalculation; branch < branches; branch++){
                ans.addChild(new ImitatedAsset(getRandomPrice(ans), 0, true, timedelta));
            }
        }
        return ans;
    }
}
