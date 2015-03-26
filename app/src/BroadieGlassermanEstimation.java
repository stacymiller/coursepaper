import java.util.HashMap;
import java.util.Map;

/**
 * Created by derketzer on 22.09.14.
 */
public class BroadieGlassermanEstimation extends Estimation {
    public static double upperEstimate(ImitatedAsset a, double strikePrice){
        if (a == null) {
            return 0;
        }
        if (a.isLastChild) {
//            System.out.println(String.format("reached last child with price %.2f, returned payoff %.2f", a.price, payoff(strikePrice, a.price)));
            return payoff(strikePrice, a.price);
        }
        double ans = 0;
        int len = a.children.size();

//        System.out.println(String.format("estimating child with %d children, price %.2f", len, a.price));
        for (ImitatedAsset child : a.children){
            ans += upperEstimate(child, strikePrice) / len;
        }
//        System.out.println(String.format("estimated option price %.2f, payoff %.2f, returned %.2f", ans, payoff(strikePrice, a.price), Math.max(payoff(strikePrice, a.price), ans)));
        return Math.max(payoff(strikePrice, a.price), ans);
    }

    public static double upperEstimateHistogram(ImitatedAsset a, double strikePrice){
        Map<ImitatedAsset, Double> estimated = new HashMap<ImitatedAsset, Double>();
        return upperEstimateHistogram(a, strikePrice, estimated);
    }

    public static double upperEstimateHistogram(ImitatedAsset a, double strikePrice, Map<ImitatedAsset, Double> estimated){
        Double ans = estimated.get(a);
        if (ans == null) {
            if (a.isLastChild) {
                ans = payoff(strikePrice, a.price);
                estimated.put(a, ans);
                return ans;
            }
            ans = 0.;
            int len = a.children.size();
//        System.out.println(String.format("estimating child with %d children, price %.2f", len, a.price));
            for (ImitatedAsset child : a.children){
                ans += upperEstimateHistogram(child, strikePrice, estimated) / len;
            }
            estimated.put(a, ans);
//        System.out.println(String.format("estimated option price %.2f, payoff %.2f, returned %.2f", ans, payoff(strikePrice, a.price), Math.max(payoff(strikePrice, a.price), ans)));
            return Math.max(payoff(strikePrice, a.price), ans);
        } else {
            return estimated.get(a);
        }
    }

    public static double lowerEstimate(ImitatedAsset a, double strikePrice){
        if (a == null) {
            return 0;
        }
        if (a.isLastChild) {
            return payoff(strikePrice, a.price);
        }

        int len = a.children.size();
        double[] childrenEstimates = new double[len];
        int i = 0;
        double sum = 0;
        for (ImitatedAsset child: a.children){
            childrenEstimates[i] = lowerEstimate(child, strikePrice);
            sum += childrenEstimates[i++];
        }

        double answer = 0;
        double po = payoff(strikePrice, a.price);
        for (i = 0; i < len; i++){
            double ans;
            if ((sum - childrenEstimates[i]) / (len-1) < po){
                ans = po;
            } else {
                ans = childrenEstimates[i];
            }
            answer += ans / len;
        }
        return answer;
    }

    public static double lowerEstimateHistogram(ImitatedAsset a, double strikePrice){
        Map<ImitatedAsset, Double> estimated = new HashMap<ImitatedAsset, Double>();
        return lowerEstimateHistogram(a, strikePrice, estimated);
    }

    public static double lowerEstimateHistogram(ImitatedAsset a, double strikePrice, Map<ImitatedAsset, Double> estimated){
        Double mappedAns = estimated.get(a);
        if (mappedAns == null) {
            if (a.isLastChild) {
                mappedAns = payoff(strikePrice, a.price);
                estimated.put(a, mappedAns);
                return mappedAns;
            }

            int len = a.children.size();
            double[] childrenEstimates = new double[len];
            int i = 0;
            double sum = 0;
            for (ImitatedAsset child: a.children){
                childrenEstimates[i] = lowerEstimateHistogram(child, strikePrice, estimated);
                sum += childrenEstimates[i++];
            }

            double answer = 0;
            double po = payoff(strikePrice, a.price);
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
