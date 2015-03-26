import com.sun.istack.internal.NotNull;

import java.util.HashSet;

/**
 * Created by stacymiller on 06/10/14.
 */
class ImitatedAsset implements Comparable{
    double price;
    HashSet<ImitatedAsset> children;
    ImitatedAsset parent;
    boolean isLastChild;
    private int lastChild;
    private int p_children_length = -1;
    private Object vertex = null;

    ImitatedAsset(double newPrice, int branches){
        price = newPrice;
        children = new HashSet<ImitatedAsset>();
        isLastChild = true;
    }

    ImitatedAsset(double newPrice, int branches, boolean isLastChild){
        if (Double.isNaN(newPrice)){
            throw new AssertionError("Asset price is NaN");
        }
        price = newPrice;
        children = new HashSet<ImitatedAsset>();
        this.isLastChild = isLastChild;
    }

    public void addChild(ImitatedAsset child){
        if (this.isLastChild){
            throw new IllegalArgumentException("Last child cannot have children");
        }
        this.children.add(child);
    }

    @Override
    public String toString(){
//        String ans = "";
//        if (!isLastChild) {
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
            return (price < ((ImitatedAsset)o).price) ? -1 : 1;
        } else if (o == null) {
            return -1;
        } else {
            throw new ClassCastException("Cannot compare ImitatedAsset to " + o.getClass().getName());
        }
    }

    public boolean hasAssociatedVertex(){
        return vertex != null;
    }

    public void setAssociatedVertex(Object v){
        vertex = v;
    }

    public Object getAssociatedVertex(){
        return vertex;
    }
}
