import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by stacymiller on 06/10/14.
 */
public class AssetGenerator {
    static Random rnd = new Random(100);
    static double lambda = 10.;
    static double volatility = 0.31436183;
    static double profitability = 0.3;
    static double timedelta = 1;
    static double eps = 10e-6;

    public static ImitatedAsset generateTreeAssets(int branches, int steps, double initialPrice){
        if (timedelta == 1) {
            timedelta = 1. / steps;
        }
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
        return initialPrice * (1 + profitability*timedelta + volatility*rnd.nextGaussian()*Math.sqrt(timedelta));
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

    private static void generateBlock(ImitatedAsset[] nodes, ImitatedAsset[] new_nodes, boolean lastRow, int start, int end, int children, double price, int new_start){
        ImitatedAsset asset = new ImitatedAsset(price, children, false); // intermediate asset will definitely have children
        for (int i = start; i < end; i++ ){ // assign average node as a child to the previous generation
            nodes[i].children[0] = asset;
        }
        for (int i = 0; i < children; i++){ // generating new nodes
            asset.children[i] = new ImitatedAsset(getRandomPrice(asset.price), 1, lastRow);
            new_nodes[new_start+i] = asset.children[i];
//            System.out.print(String.format("\nnew_nodes[%d] = %s", new_start+i, asset.children[i].toString()));
//                    System.out.println(String.format("new_nodes[%d] = asset.children[%d] = %s", new_nodes_i, i, asset.children[i].toString()));
        }
    }
    private static void generateBlock(ImitatedAsset[] nodes, ImitatedAsset[] new_nodes, int start, int end, int children, double price, int new_start){
        generateBlock(nodes, new_nodes, false, start, end, children, price, new_start);
    }

    private static void generateBlock(ImitatedAsset[] nodes, ImitatedAsset[] new_nodes, boolean lastRow, int start, int end, int children, double price){
        generateBlock(nodes, new_nodes, lastRow, start, end, children, price, start);
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
    public static ImitatedAsset generateAssetByHistogram(int width, int branch, int steps, int sectors, double initialPrice) throws InterruptedException {
        timedelta = 1. / steps;
        int expSteps = (int) Math.floor(Math.log(width) / Math.log(branch));
        ImitatedAsset[] nodes = new ImitatedAsset[width];
        ImitatedAsset ans = generateTreeAssetsToModeling(branch, expSteps, initialPrice, nodes);

        sortArrayWithNulls(nodes);
        double sector = getSectorWidth(sectors, nodes);
        double min = extremalValue(nodes, -1);
        int len = countLength(nodes);
        // generate ImitatedAsset array of desired width
        int k = 0;
        double rest = 0.;
        double sum = 0;
        int amount = 0; // amount of nodes in the sector
        int new_nodes_i = 0;
        ImitatedAsset[] new_nodes = new ImitatedAsset[width];

        int i1 = 0;
        do {
            if ((nodes[i1].price > min + (k+1) * sector)){
                int children = (int)(((double)amount * width) / len);
                rest += ((double)amount * width) / len - children;
                if (rest > 1){
                    children++;
                    rest -= 1;
                }
                generateBlock(nodes, new_nodes, i1-amount, i1, children, sum/amount, new_nodes_i);
                new_nodes_i += children;
                sum = nodes[i1].price;
                amount = 1;
                k++;
            } else {
                amount++;
                sum += nodes[i1].price;
            }
            i1++;
        }while (i1 < len);
        int children = (int)(((double)amount * width) / len);
        rest += ((double)amount * width) / len - children;
        if (rest > 0){
            children++;
        }
        generateBlock(nodes, new_nodes, i1-amount, i1, children, sum/amount, new_nodes_i);

        if(countLength(new_nodes) != width){
            throw new AssertionError(String.format("Generated %d nodes instead of %d", countLength(new_nodes), width));
        }
        nodes = new_nodes;

        for (int step = expSteps; step < steps; step++) {
            sortArrayWithNulls(nodes);
            sector = getSectorWidth(sectors, nodes);
            min = extremalValue(nodes, -1);
            sum = 0;
            amount = 0;
            k = 0;
            new_nodes = new ImitatedAsset[width];
            for (int j = 0; j < width; j++){ // iterating over {{nodes}}
                if (nodes[j].price > min + (k+1) * sector) { // reached the end of the sector
                    generateBlock(nodes, new_nodes, (step + 1 == steps), j-amount, j, amount, sum/amount);
                    k++;
                    amount = 0;
                    sum = 0;
                }
                sum += nodes[j].price;
                amount++;
            }
            generateBlock(nodes, new_nodes, (step + 1 == steps), width-amount, width, amount, sum/amount);
            nodes = new_nodes;
        }
        return ans;
    }

    private static double getSectorWidth(int sectors, ImitatedAsset[] nodes) {
        double min = extremalValue(nodes, -1);
        double max = extremalValue(nodes, 1);
        return (max - min) / sectors;
    }

    private static void sortArrayWithNulls(ImitatedAsset[] nodes) {
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
