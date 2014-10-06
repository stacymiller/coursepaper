import com.sun.istack.internal.NotNull;

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

    @Override
    public String toString(){
        String ans = "";
        if (!lastChild) {
            for (ImitatedAsset child: children){
                String temp;
                if (child == null) {
                    temp = "null";
                } else {
                    temp = child.toString();
                }
                ans = ans + temp + "\n";
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
            return -1;
        } else {
            throw new ClassCastException("Cannot compare ImitatedAsset to " + o.getClass().getName());
        }
    }
}

public class Main {

    public static void main(String[] args) {
        int branches = 3;
        int steps = 5;
        double initialPrice = 100.;
        ImitatedAsset ia = AssetGenerator.generateAssetByHistogram(15, branches, steps, 5, initialPrice);
//        System.out.println(ia);
        System.out.println(BroadieGlassermanEstimation.upperEstimate(ia, 110));
        System.out.println(BroadieGlassermanEstimation.lowerEstimate(ia, 110));
    }
}
