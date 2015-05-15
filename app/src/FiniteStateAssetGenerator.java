/**
 * Created by stacymiller on 05.04.15.
 */

import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FiniteStateAssetGenerator extends AssetGenerator {
    static double[] stateBorders;
    static double[] stateRepresentatives;
    static double infinityState;

    public static ImitatedAsset generateAssetTree(int statesNumber, int branches, int steps, double initialPrice) {
        timedelta = 1. / steps;
        NormalDistribution nd = new NormalDistribution();
        stateBorders = new double[statesNumber];
        stateRepresentatives = new double[statesNumber];
        for (int i = 0; i < statesNumber; i++){
            stateRepresentatives[i] = nd.inverseCumulativeProbability((i+0.5) / (statesNumber));
            stateBorders[i] = nd.inverseCumulativeProbability((i+1.) / (statesNumber));
        }
        System.out.println(Arrays.toString(stateBorders));
        System.out.println(Arrays.toString(stateRepresentatives));
        Map[] states = new Map[steps+1]; // because x_0 is also a step
        for (int i = 0; i <= steps; i++){
            states[i] = new HashMap<Double, ImitatedAsset>(statesNumber+1, (float) 0.999);
        }
        System.out.println(Arrays.toString(states));
        ImitatedAsset ans = generateAssetTree(states, branches, steps, initialPrice);
        System.out.println(Arrays.toString(states));
        return ans;
    }

    private static ImitatedAsset generateAssetTree(Map[] states, int branches, int steps, double initialPrice){
        ImitatedAsset ans = (ImitatedAsset)states[steps].get(initialPrice);
        System.out.println(ans);
        if (ans == null){
            ans = new ImitatedAsset(initialPrice, branches, steps == 0);
            states[steps].put(initialPrice, ans);
        }
        if (steps > 0 && ans.children.isEmpty()) {
            for (int branch = 0; branch < branches; branch++) {
                double price = getRandomPrice(initialPrice);
                ans.children.add(generateAssetTree(states, branches, steps - 1, price));
            }
        }
        return ans;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    protected static double getRandomPrice(double initialPrice) {
        double e = rnd.nextGaussian();
        int l = stateBorders.length;
        int i;
        for (i = 0; i < l; i++){
            if (e < stateBorders[i]){
                e = stateRepresentatives[i];
                break;
            }
        }
        return round(getRandomPrice(initialPrice, e), 5);
    }
}
