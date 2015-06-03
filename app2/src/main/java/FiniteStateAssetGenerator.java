/**
 * Created by stacymiller on 05.04.15.
 */

import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

public class FiniteStateAssetGenerator extends AssetGenerator {
    static double[][] stateBorders;
    static double[][] stateRepresentatives;
    static double infinityState;

    public static ImitatedAsset generateAssetTree(int branches, int steps, double initialPrice){
        throw new UnsupportedOperationException("You must pass number of underlying asset states or use superclass");
    }

    public static ImitatedAsset generateAssetTree(int statesNumber, int branches, int steps, double initialPrice) {
        timedelta = 1. / steps;
        calculateBorders(statesNumber, steps, new ImitatedAsset(initialPrice, branches));
//        System.out.println(Arrays.toString(stateBorders));
//        System.out.println(Arrays.toString(stateRepresentatives));
        Map[] states = new Map[steps+1]; // because x_0 is also a step
        for (int i = 0; i <= steps; i++){
            states[i] = new HashMap<Integer, ImitatedAsset>(statesNumber+1, (float) 0.999);
        }
        return generateAssetTree(states, branches, steps, (statesNumber % 2 == 1 ? statesNumber / 2 + 1 : statesNumber / 2));
    }

    private static void calculateBorders(int statesNumber, int steps, ImitatedAsset param){
        NormalDistribution nd = new NormalDistribution();
        stateBorders = new double[steps+1][statesNumber];
        stateRepresentatives = new double[steps+1][statesNumber];
        for (int i = 0; i < statesNumber; i++){
            double median = nd.inverseCumulativeProbability((i+0.5) / (statesNumber));
            double border = nd.inverseCumulativeProbability((i+1.) / (statesNumber));
            for(int j = 0; j <= steps; j++){
                stateRepresentatives[j][i] =
                        param.price * exp((
                                param.discountRate - param.dividendRate + param.volatility*param.volatility / 2
                        )*j*timedelta + param.volatility*sqrt(j*timedelta)*median);
                stateBorders[j][i] =
                        param.price * exp((
                                param.discountRate - param.dividendRate + param.volatility*param.volatility / 2
                        )*j*timedelta + param.volatility*sqrt(j*timedelta)*border);
            }
        }
//        for(int i = 0; i < stateBorders.length; i++){
//            System.out.println(Arrays.toString(stateBorders[i]));
//            System.out.println(Arrays.toString(stateRepresentatives[i]));
//        }
    }

    private static ImitatedAsset generateAssetTree(Map[] states, int branches, int steps, int curState){
        ImitatedAsset ans = (ImitatedAsset)states[steps].get(curState);
        if (ans == null){
            ans = new ImitatedAsset(stateRepresentatives[stateRepresentatives.length - steps - 1][curState], branches, steps == 0, timedelta);
            states[steps].put(curState, ans);
            if (steps > 0) {
                for (int branch = 0; branch < branches; branch++) {
                    int nextState = getRandomState(ans, stateRepresentatives.length - steps);
                    ans.children.add(generateAssetTree(states, branches, steps - 1, nextState));
                }
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

    protected static int getRandomState(ImitatedAsset asset, int step) {
        double price = getRandomPrice(asset);
        for(int i = 0; i < stateBorders[step].length; i++){
            if (price < stateBorders[step][i]) {
                return i;
            }
        }
        throw new AssertionError(String.format(
                Locale.ENGLISH, "Asset price %f is not less than any of %s", price, Arrays.toString(stateBorders[step])));
    }
}
