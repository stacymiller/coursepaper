/**
 * Created by derketzer on 22.09.14.
 */
public class BroadieGlassermanEstimation extends Estimation {
    public static double upperEstimate(ImitatedAsset a, double strikePrice){
        if (a.lastChild) {
            return payoff(strikePrice, a.price);
        }
        double ans = 0;
        double len = a.children.length;
        for (ImitatedAsset child : a.children){
            ans += upperEstimate(child, strikePrice) / len;
        }
        return Math.max(payoff(strikePrice, a.price), ans);
    }

    public static double lowerEstimate(ImitatedAsset a, double strikePrice){
        if (a.lastChild) {
            return payoff(strikePrice, a.price);
        }

        int len = a.children.length;
        double[] childrenEstimates = new double[len];
        double sum = 0;
        for (int i = 0; i < len; i++){
            childrenEstimates[i] = lowerEstimate(a.children[i], strikePrice);
            sum += childrenEstimates[i];
        }

        double answer = 0;
        double po = payoff(strikePrice, a.price);
        for (int i = 0; i < len; i++){
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
}
