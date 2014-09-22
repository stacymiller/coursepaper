import com.sun.istack.internal.NotNull;

import java.lang.reflect.Array;
import java.util.*;

class ImitatedAsset implements Comparable{
    double price;
    ImitatedAsset[] children;
    boolean lastChild;

    ImitatedAsset(double newPrice, int branches){
        price = newPrice;
        children = new ImitatedAsset[branches];
        lastChild = true;
    }

    ImitatedAsset(double newPrice, int branches, boolean isLastChild){
        price = newPrice;
        children = new ImitatedAsset[branches];
        lastChild = isLastChild;
    }

    public String toString(){
        String ans = "";
        if (!lastChild) {
            for (ImitatedAsset child: children){
                ans = ans + child.toString() + "\n";
            }
            ans = ans.trim() + "\t";
        }
        ans = ans + price + "\n";
        return ans;
    }

    @Override
    @NotNull
    public int compareTo(Object o) {
        if (o instanceof ImitatedAsset){
            return (price < ((ImitatedAsset)o).price) ? 1 : -1;
        } else if (o == null) {
            throw new NullPointerException();
        } else {
            throw new ClassCastException("Cannot compare ImitatedAsset to " + o.getClass().getName());
        }
    }
}

public class Main {
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

//    private static ImitatedAsset generateByHistogramRecursive(int startingStep, int steps) {
//
//    }

    public static ImitatedAsset generateAssetByHistogram(int width, int branch, int steps, int sectors, double initialPrice){
        int expSteps = (int) Math.floor(Math.log(width) / Math.log(branch));
        ImitatedAsset[] nodes = new ImitatedAsset[width];
        ImitatedAsset ans = generateTreeAssetsToModeling(branch, expSteps, initialPrice, nodes);
        int len = 0;
        for (ImitatedAsset ia: nodes){
            if (ia == null){
                break;
            }
            len++;
        }
        // generate lacking children to get n children in total
//        class AssetComparator implements Comparator<ImitatedAsset> {
//            public int compare (ImitatedAsset a, ImitatedAsset b){
//                return (a.price - b.price) > 0 ? 1 : -1;
//            }
//
//            @Override
//            public boolean equals(Object obj) {
//                return false;
//            }
//        }
        for (int step = expSteps, i = expSteps; step < steps; step++) {
            List<ImitatedAsset> aNodes = Arrays.asList(nodes);
            Collections.sort(aNodes);
            double min = Collections.min(aNodes).price;
            double max = Collections.max(aNodes).price;
            double sector = (max - min) / sectors;
            // split [min(nodes); max(nodes)] in {{sectors}} parts and link nodes from each part to their average node
            double sum = 0;
            int k = 0;
            int amount = 0;
            for (int j = 0; j < len; j++){
                if (nodes[j].price > (k+1) * sector) {
                    ImitatedAsset asset = new ImitatedAsset(sum / amount, (int)(((double)amount / len) * width));
                    for (int l = 0; l < amount; l++ ){ // assign average node as a child to the previous generation
                        nodes[j-l-1].children[0] = asset;
                    }
                    // here I should generate new nodes from average
                    k++;
                    amount = 0;
                    sum = 0.;
                }
                sum += nodes[j].price;
                amount++;
            }
            // OR take all the averages and produce new {{nodes}} from them here
        }
        return ans;
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
            ans = new ImitatedAsset(initialPrice, 1, true);
            for (int i = 0; i < lastRow.length; i++){
                if (lastRow[i] == null) {
                    lastRow[i] = ans;
                    return ans;
                }
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        int branches = 3;
        int steps = 5;
        double initialPrice = 100.;
        ImitatedAsset ia = generateTreeAssets(branches, steps, initialPrice);
        System.out.println(ia);
        System.out.println(BroadieGlassermanEstimation.upperEstimate(ia, 110));
        System.out.println(BroadieGlassermanEstimation.lowerEstimate(ia, 110));
    }
}
