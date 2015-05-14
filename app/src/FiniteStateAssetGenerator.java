/**
 * Created by stacymiller on 05.04.15.
 */
public class FiniteStateAssetGenerator extends AssetGenerator {
    int states = 10;

    public FiniteStateAssetGenerator(int states) {

    }
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
