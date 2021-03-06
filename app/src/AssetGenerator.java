import java.util.Random;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

/**
 * Created by stacymiller on 26.03.15.
 */
public class AssetGenerator {
    static Random rnd = new Random();
    static Random rnd2 = new Random();
    static double timedelta = 1;
    static int stepsTotal;

    public static ImitatedAsset generateAssetTree(int branches, int steps, double initialPrice){
        if (timedelta == 1) {
            timedelta = 1. / steps;
            stepsTotal = steps;
        }
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, steps == 0, timedelta);
        if (steps > 0) {
            for (int branch = 0; branch < branches; branch++) {
                double price = getRandomPrice(ans);
                ans.children.add(generateAssetTree(branches, steps - 1, price));
            }
        }
        return ans;
    }

    protected static double getRandomPrice(ImitatedAsset asset, double g) {
        // TODO: http://en.wikipedia.org/wiki/Optimal_stopping#Option_trading
        return asset.price * exp(timedelta * (
                asset.discountRate - asset.dividendRate - asset.volatility * asset.volatility / 2
        ) + asset.volatility * sqrt(timedelta) * g);
    }

    protected static double getRandomPrice(ImitatedAsset asset) {
        // TODO: http://en.wikipedia.org/wiki/Optimal_stopping#Option_trading
        return getRandomPrice(asset, rnd.nextGaussian());
    }
}
