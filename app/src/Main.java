public class Main {

    public static void main(String[] args) {
        int branches = 3;
        int steps = 10;
        double initialPrice = 100.;
        ImitatedAsset ia = AssetGenerator.generateAssetByHistogram(30, branches, steps, 5, initialPrice);
//        System.out.println(ia);
        System.out.println(BroadieGlassermanEstimation.upperEstimate(ia, 110));
        System.out.println(BroadieGlassermanEstimation.lowerEstimate(ia, 110));
    }
}
