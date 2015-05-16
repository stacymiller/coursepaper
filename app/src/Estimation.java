import static java.lang.Math.exp;

/**
 * Created by derketzer on 22.09.14.
 */
public class Estimation {
    enum OptionType {PUT, CALL}

    static OptionType optionType = OptionType.CALL;
//    static interestRate =

    static double payoff(double strikePrice, ImitatedAsset asset) {
        double ans = asset.price - strikePrice;
        if (optionType == OptionType.CALL) {
            ans = ans > 0 ? ans : 0;
        } else if (optionType == OptionType.PUT) {
            ans = ans < 0 ? -ans : 0;
        } else {
            throw new IllegalArgumentException(String.format("Payoff calculation method is unknown for %s option",
                    optionType.toString()));
        }
        return exp(-asset.discountFactor) * ans;
    }
}
