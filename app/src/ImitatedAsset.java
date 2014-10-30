import com.sun.istack.internal.NotNull;

/**
 * Created by stacymiller on 06/10/14.
 */
class ImitatedAsset implements Comparable{
    double price;
    ImitatedAsset[] children;
    boolean lastChild;
    private int p_children_length = -1;

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

    public int children_length(){
        if (p_children_length >= 0) {
            return p_children_length;
        }
        int ans = 0;
        for (ImitatedAsset child: children){
            if (child != null) {
                ans++;
            }
        }
        p_children_length = ans;
        return p_children_length;
    }

    @Override
    public String toString(){
//        String ans = "";
//        if (!lastChild) {
//            for (ImitatedAsset child: children){
//                String temp;
//                if (child == null) {
//                    temp = "null";
//                } else {
//                    temp = child.toString();
//                }
//                ans = ans + temp + "\n";
//            }
//            ans = ans.trim() + "\t";
//        }
//        ans = ans + price + "\n";
        return String.format("%.2f", price);
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
