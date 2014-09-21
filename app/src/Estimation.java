/**
 * Created by derketzer on 22.09.14.
 */
public class Estimation {
    enum OptionType {PUT, CALL}

    static OptionType optionType = OptionType.CALL;

    static double payoff(double strikePrice, double currentPrice) {
        double ans = currentPrice - strikePrice;
        if (optionType == OptionType.CALL) {
            return ans > 0 ? ans : 0;
        } else if (optionType == OptionType.PUT) {
            return ans < 0 ? -ans : 0;
        } else {
            throw new IllegalArgumentException(String.format("Payoff calculation method is unknown for %s option",
                    optionType.toString()));
        }
    }
}
