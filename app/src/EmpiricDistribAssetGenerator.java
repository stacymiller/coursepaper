import java.util.Arrays;

/**
 * Created by stacymiller on 30.04.15.
 */
public class EmpiricDistribAssetGenerator extends AssetGenerator {
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

    private static ImitatedAsset generateAssetTree(int branches, int steps, double initialPrice, ImitatedAsset[] prevRow){
        ImitatedAsset[] curRow = new ImitatedAsset[branches*prevRow.length];
        for(int i = 0; i < prevRow.length; i++){
            for(int b = 0; b < branches; b++){
                curRow[i*branches + b] = new ImitatedAsset(getRandomPrice(prevRow[i].price), branches);
                prevRow[i].addChild(curRow[i*branches + b]);
            }
        }
        Arrays.sort(curRow);

    }

    protected static double getRandomPrice(double initialPrice) {
        return initialPrice * (1 + profitability*timedelta + volatility*rnd.nextGaussian()*Math.sqrt(timedelta));
    }
}

