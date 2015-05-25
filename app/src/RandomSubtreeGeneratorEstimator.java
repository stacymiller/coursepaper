import org.apache.commons.math3.distribution.PoissonDistribution;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import static java.lang.Math.*;

/**
 * Created by stacymiller on 25.05.15.
 */
public class RandomSubtreeGeneratorEstimator extends Estimation{
    /**
     * Created by stacymiller on 24.05.15.
     */
    static PoissonDistribution poisson = new PoissonDistribution(1);
    static Random rnd = new Random();
    static Random rnd2 = new Random();

    static double timedelta = 1;
    public static double[] calculate(int branches, int steps, double initialPrice, double strikePrice){
        if (timedelta == 1) {
            timedelta = 1. / steps;
        }
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, steps == 0, timedelta);
        if (steps > 0) {
            int needCalculation = 0;
            for (int branch = 0; branch < branches; branch++) {
                if (rnd2.nextDouble() < 1.2 / branches){
                    needCalculation++;
                }
            }
            needCalculation = max(needCalculation, 1);
            double[][] estimators = new double[branches][2];
            double upperEstimator = 0;
            double lowerSum = 0;
            for (int b = 0; b < needCalculation; b++){
                estimators[b] = calculate(branches, steps-1, getRandomPrice(ans), strikePrice);
                upperEstimator += estimators[b][0] / branches;
                lowerSum += estimators[b][1];
            }
            for (int b = needCalculation; b < branches; b++){
                estimators[b] = estimators[(int)floor(rnd2.nextDouble()*needCalculation)];
                upperEstimator += estimators[b][0] / branches;
                lowerSum += estimators[b][1];
            }
            double lowerEstimator = 0;
            double po = payoff(strikePrice, ans);
            for (int i = 0; i < branches; i++) {
                double currentDecision;
                if ((lowerSum - estimators[i][1]) / (branches - 1) < po) {
                    currentDecision = po;
                } else {
                    currentDecision = estimators[i][1];
                }
                lowerEstimator += currentDecision / branches;
            }
            return new double[]{upperEstimator, lowerEstimator};
        } else {
            double po = payoff(strikePrice, ans);
            return new double[]{po, po};
        }
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
