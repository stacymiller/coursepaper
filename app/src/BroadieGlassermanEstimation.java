/**
 * Created by derketzer on 22.09.14.
 */
public class BroadieGlassermanEstimation extends Estimation {
    public static double upperEstimate(ImitatedAsset a, double strikePrice){
        if (a == null) {
            return 0;
        }
        if (a.lastChild) {
            System.out.println(String.format("reached last child with price %.2f, returned payoff %.2f", a.price, payoff(strikePrice, a.price)));
            return payoff(strikePrice, a.price);
        }
        double ans = 0;
        int len = a.children_length();

        System.out.println(String.format("estimating child with %d children, price %.2f", len, a.price));
        for (ImitatedAsset child : a.children){
            ans += upperEstimate(child, strikePrice) / len;
        }
        System.out.println(String.format("estimated option price %.2f, payoff %.2f, returned %.2f", ans, payoff(strikePrice, a.price), Math.max(payoff(strikePrice, a.price), ans)));
        return Math.max(payoff(strikePrice, a.price), ans);
    }

    public static double lowerEstimate(ImitatedAsset a, double strikePrice){
        if (a == null) {
            return 0;
        }
        if (a.lastChild) {
            return payoff(strikePrice, a.price);
        }

        int len = a.children_length();
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
