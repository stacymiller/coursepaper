import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by stacymiller on 06/10/14.
 */
public class AssetGenerator {
    static Random rnd = new Random();
    static double lambda = 10.;

    public static ImitatedAsset generateTreeAssets(int branches, int steps, double initialPrice){
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, steps == 0);
        if (steps > 0) {
            for (int branch = 0; branch < branches; branch++) {
                double price = getRandomPrice(initialPrice);
                ans.children[branch] = generateTreeAssets(branches, steps - 1, price);
            }
        }
        return ans;
    }

    private static double getRandomPrice(double initialPrice) {
        return initialPrice + rnd.nextGaussian() * lambda;
    }

    private static double extremalValue(ImitatedAsset[] a, int sign){
        double ans = (- sign) * Double.MAX_VALUE - 2;
        for (ImitatedAsset i: a){
            if ((i != null) && (sign * i.price > sign * ans)){
                ans = i.price;
            }
        }
        return ans;
    }

    /** Creates new generaions of assets based on distribution of the previous generation
     *
     * @param width
     * @param branch
     * @param steps
     * @param sectors
     * @param initialPrice
     * @return
     */
    public static ImitatedAsset generateAssetByHistogram(int width, int branch, int steps, int sectors, double initialPrice){
        int expSteps = (int) Math.floor(Math.log(width) / Math.log(branch));
        ImitatedAsset[] nodes = new ImitatedAsset[width];
        ImitatedAsset ans = generateTreeAssetsToModeling(branch, expSteps, initialPrice, nodes);

        for (int step = expSteps; step < steps; step++) {
            for (ImitatedAsset n: nodes){
                System.out.print((n == null ? "null" : n.price) + " ");
            }
            Arrays.sort(nodes, new Comparator<ImitatedAsset>() {
                @Override
                public int compare(ImitatedAsset o1, ImitatedAsset o2) {
                    if (o1 != null) {
                        return o1.compareTo(o2);
                    } else if (o2 != null) {
                        return -o2.compareTo(null); // because o1 is null
                    } else {
                        return 0;
                    }
                }
            });
            int len = countLength(nodes);
            System.out.println(String.format("length of nodes: %d, step: %d < %d", len, step, steps));

            double min = extremalValue(nodes, -1);
            double max = extremalValue(nodes, 1);
            double sector = (max - min) / sectors;
            System.out.println(sector);
            // split [min(nodes); max(nodes)] in {{sectors}} parts and link nodes from each part to their average node
            double sum = 0;
            int k = 0;
            int amount = 0;
            ImitatedAsset[] new_nodes = new ImitatedAsset[width]; // possible ArrayIndexOutOfBounds
            int new_nodes_i = 0;
            for (int j = 0; j < len; j++){ // iterating over {{nodes}}
                if (nodes[j].price < max - (k+1) * sector) { // reached the end of the sector
                    System.out.print(" | ");
                    int children = (int)(((double)amount / len) * width);
                    ImitatedAsset asset = new ImitatedAsset(sum / amount, children, false); // intermediate asset will definitely have children
                    for (int l = 0; l < amount; l++ ){ // assign average node as a child to the previous generation
                        nodes[j-l-1].children[0] = asset;
                    }
                    k++;
                    amount = 0;
                    sum = 0.;
                    boolean lastRow = (step + 1 == steps);
                    for (int i = 0; i < children; i++, new_nodes_i++){ // generating new nodes
                        asset.children[i] = new ImitatedAsset(getRandomPrice(asset.price), 1, lastRow);
                        new_nodes[new_nodes_i] = asset.children[i];
                    }
                }
                System.out.print(String.format("%.2f ", nodes[j].price));
                sum += nodes[j].price;
                amount++;
            }
            System.out.print("\n");
            nodes = new_nodes;
            // OR take all the averages and produce new {{nodes}} from them here
        }
        return ans;
    }

    private static int countLength(Object[] sortedArrayWithNullsAtRight) {
        int len = 0;
        for (Object ia: sortedArrayWithNullsAtRight){
            if (ia == null){
                break;
            }
            len++;
        }
        return len;
    }

    private static ImitatedAsset generateTreeAssetsToModeling(int branches, int steps, double initialPrice, ImitatedAsset[] lastRow) {
        ImitatedAsset ans;
        if (steps > 0) {
            ans = new ImitatedAsset(initialPrice, branches, false);
            for (int branch = 0; branch < branches; branch++) {
                double price = getRandomPrice(initialPrice);
                ans.children[branch] = generateTreeAssetsToModeling(branches, steps - 1, price, lastRow);
            }
        } else {
            ans = new ImitatedAsset(initialPrice, 1, false); // these assets will have a child "intermediate" asset
            for (int i = 0; i < lastRow.length; i++){
                if (lastRow[i] == null) {
                    lastRow[i] = ans;
                    return ans;
                }
            }
        }
        return ans;
    }
}
