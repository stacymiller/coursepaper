import java.util.Random;

class ImitatedAsset{
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
}

public class Main {
    static Random rnd = new Random();
    static double lambda = 10.;

    public static ImitatedAsset generateAssets(int branches, int steps, double initialPrice){
        ImitatedAsset ans = new ImitatedAsset(initialPrice, branches, steps == 0);
        if (steps > 0) {
            for (int branch = 0; branch < branches; branch++) {
                double price = initialPrice + rnd.nextGaussian() * lambda;
                ans.children[branch] = generateAssets(branches, steps-1, price);
            }
        }
        return ans;
    }

    public static void main(String[] args) {
        int branches = 3;
        int steps = 5;
        double initialPrice = 100.;
        ImitatedAsset ia = generateAssets(branches, steps, initialPrice);
        System.out.println(ia);
        System.out.println(BroadieGlassermanEstimation.upperEstimate(ia, 110));
        System.out.println(BroadieGlassermanEstimation.lowerEstimate(ia, 110));
    }
}
