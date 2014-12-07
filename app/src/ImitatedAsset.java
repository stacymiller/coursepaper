import com.sun.istack.internal.NotNull;

/**
 * Created by stacymiller on 06/10/14.
 */
class ImitatedAsset implements Comparable{
    double price;
    ImitatedAsset[] children;
    ImitatedAsset[] lastRow;
    boolean lastChild;
    private int p_children_length = -1;
    private Object vertex = null;
    public long vertexVersion = 0;

    ImitatedAsset(double newPrice, int branches){
        price = newPrice;
        children = new ImitatedAsset[branches];
        lastChild = true;
    }

    ImitatedAsset(double newPrice, int branches, boolean isLastChild){
        if (Double.isNaN(newPrice)){
            throw new AssertionError("Asset price is NaN");
        }
        price = newPrice;
        children = new ImitatedAsset[branches];
//        System.out.println(String.format("new ImitatedAsset(%f, %d, %b).children.length = %d", newPrice, branches, isLastChild, children.length));
        lastChild = isLastChild;
    }

    public void addRows(int n, int width, int sectors){
        if (lastRow == null || lastChild) {
            throw new UnsupportedOperationException("lastRow not defined; please find parent asset with defined lastRow or calculate it manually");
        }
        ImitatedAsset[] newLastRow;
        for (int i = 0; i < n; i++) {
            newLastRow = AssetGenerator.generateRow(width, i+1 == n, sectors, lastRow);
            lastRow = newLastRow;
        }
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

    private boolean _equals(ImitatedAsset asset){
        if (price != asset.price){
            return false;
        }
        if (lastChild != asset.lastChild) {
            return false;
        }
        if (!lastChild){
            if (children.length != asset.children.length){
                return false;
            }
            for (int i = 0; i < children.length; i++){
                if (!children[i]._equals(asset.children[i])){
                    return false;
                }
            }
        }
        return true;
    }

    public boolean equals(Object o) {
        return o instanceof ImitatedAsset && _equals((ImitatedAsset) o);
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
