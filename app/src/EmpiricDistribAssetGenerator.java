import java.util.Arrays;

/**
 * Created by stacymiller on 30.04.15.
 */
public class EmpiricDistribAssetGenerator extends AssetGenerator {
    public static ImitatedAsset generateAssetTree(int branches, int steps, double initialPrice){
        timedelta = 1. / steps;
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, false, timedelta);
        ImitatedAsset[] prevRow = getFirstRow(ans, branches);
        for (int step = 0; step < steps; step++) {
            boolean median = branches % 2 == 1;
            ImitatedAsset[] curRow = new ImitatedAsset[branches * prevRow.length];
            ImitatedAsset[] newRow = new ImitatedAsset[prevRow.length];
            for (int i = 0; i < prevRow.length; i++) {
                for (int b = 0; b < branches; b++) {
                    curRow[i * branches + b] = new ImitatedAsset(getRandomPrice(prevRow[i]), branches, false, timedelta);
                    curRow[i * branches + b].parent = prevRow[i];
                }
            }
            Arrays.sort(curRow);
            for (int i = 0; i < prevRow.length; i++) {
                ImitatedAsset v;
                if (median) {
                    v = curRow[i * branches + branches / 2 + 1];
                } else {
                    double price = (curRow[i * branches + branches / 2].price + curRow[i * branches + branches / 2 + 1].price) / 2;
                    v = new ImitatedAsset(price, branches, false, timedelta);
                }
                for (int b = 0; b < branches; b++) {
                    curRow[i * branches + b].parent.addChild(v);
                    curRow[i * branches + b] = null;
                }
                newRow[i] = v;
            }
            prevRow = newRow;
        }
        return ans;
    }

    private static ImitatedAsset[] getFirstRow(ImitatedAsset ans, int branches) {
        ImitatedAsset[] curRow = new ImitatedAsset[branches];
        for (int b = 0; b < branches; b++) {
            curRow[b] = new ImitatedAsset(getRandomPrice(ans), branches, false, timedelta);
            ans.addChild(curRow[b]);
        }
        return curRow;
    }
}

