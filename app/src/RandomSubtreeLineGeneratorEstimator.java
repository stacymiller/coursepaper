import java.util.Random;

import static java.lang.Math.*;

/**
 * Created by stacymiller on 03.06.15.
 */
public class RandomSubtreeLineGeneratorEstimator extends Estimation {
    static Random rnd = new Random();
    static Random rnd2 = new Random();
    static int stepsTotal;
    public static int elCompUpper;
    public static int elCompLower;

    static double timedelta = 1;
// calculate возвращает A(gamma) -- оценку по одному случайному поддереву. случайное дерево генерируется след.образом:
//    если данная вершина не поглощена, у неё порождается b детей, иначе -- она поглощается
    public static double calculate_gamma(int branches, int steps, double initialPrice, double strikePrice){
        double upperEstimate = 0;
        ImitatedAsset ia = new ImitatedAsset(initialPrice, branches, steps==0, timedelta);
        String offset = " ";
//        for(int j = 0; j < stepsTotal-steps; j++)
//            offset += offset;
//        разыгрываем событие поглощения вершины
        double alpha = rnd.nextDouble();
        if ((steps == 0) || (alpha < g(steps, branches))){
//            swallowed vertex
            upperEstimate = exp(-ia.discountFactor*(steps-stepsTotal))*payoff(strikePrice, ia);
//            System.out.println(offset + String.format("vertex %f, no children", initialPrice));
        } else {
//            System.out.println(offset + String.format("vertex %f, %d children", initialPrice, branches));
            for (int b = 0; b < branches; b++){
                upperEstimate += calculate_gamma(branches, steps - 1, getRandomPrice(ia), strikePrice) / branches;
            }
        }
        return upperEstimate;
    }

    public static double calculate(double initialPrice, double strikePrice, int steps, int branches, int n){
        double ans = 0;
        stepsTotal = steps;
        timedelta = 1./steps;
        for (int i = 0; i < n; i++){
            ans = max(ans, calculate_gamma(branches, steps, initialPrice, strikePrice));
        }
        return ans;
    }

    private static double g(int steps, int branches) {
        return 1./branches;/*1. - ((double)steps) / stepsTotal;*/
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
