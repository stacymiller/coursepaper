import java.util.HashMap;
import java.util.Map;

/**
 * Created by derketzer on 22.09.14.
 */
public class BroadieGlassermanEstimation extends Estimation {
    public static double upperEstimate(ImitatedAsset a, double strikePrice){
        Map<ImitatedAsset, Double> estimated = new HashMap<ImitatedAsset, Double>();
        return upperEstimate(a, strikePrice, estimated);
    }

    private static double upperEstimate(ImitatedAsset a, double strikePrice, Map<ImitatedAsset, Double> estimated){
        Double ans = estimated.get(a);
        if (ans == null) { // this child wasn't calculated yet
            if (a.isLastChild) {
                ans = payoff(strikePrice, a);
                estimated.put(a, ans);
                return ans;
            }
            ans = 0.;
            int len = a.children.size();
            for (ImitatedAsset child : a.children){
                ans += upperEstimate(child, strikePrice, estimated) / len;
            }
            estimated.put(a, ans);
            return Math.max(payoff(strikePrice, a), ans);
        } else {
            return estimated.get(a);
        }
    }

    public static double lowerEstimate(ImitatedAsset a, double strikePrice){
        Map<ImitatedAsset, Double> estimated = new HashMap<ImitatedAsset, Double>();
        return lowerEstimate(a, strikePrice, estimated);
    }

    private static double lowerEstimate(ImitatedAsset a, double strikePrice, Map<ImitatedAsset, Double> estimated){
        Double mappedAns = estimated.get(a);
        if (mappedAns == null) {
            if (a.isLastChild) {
                mappedAns = payoff(strikePrice, a);
                estimated.put(a, mappedAns);
                return mappedAns;
            }

            int len = a.children.size();
            double[] childrenEstimates = new double[len];
            int i = 0;
            double sum = 0;
            for (ImitatedAsset child: a.children){
                childrenEstimates[i] = lowerEstimate(child, strikePrice, estimated);
                sum += childrenEstimates[i++];
            }

            double answer = 0;
            double po = payoff(strikePrice, a);
            for (i = 0; i < len; i++) {
                double ans;
                if ((sum - childrenEstimates[i]) / (len - 1) < po) {
                    ans = po;
                } else {
                    ans = childrenEstimates[i];
                }
                answer += ans / len;
            }
            estimated.put(a, answer);
            return answer;
        } else {
            return mappedAns;
        }
    }
}
