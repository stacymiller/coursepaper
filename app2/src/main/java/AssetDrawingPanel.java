import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import java.awt.*;

/**
 * Created by derketzer on 18.10.14.
 */
public class AssetDrawingPanel extends mxGraphComponent{
    private ImitatedAsset asset;
    private mxHierarchicalLayout layout;
    private int prefX=500, prefY=250;

    private void initLayout(){
        setEnabled(false);
        layout = new mxHierarchicalLayout(graph);
        layout.setInterHierarchySpacing(5);
        layout.setInterRankCellSpacing(30);
        layout.setIntraCellSpacing(5);
    }

    public AssetDrawingPanel(mxGraph g, ImitatedAsset assetToDraw) {
        super(g);
        initLayout();
        asset = assetToDraw;
    }

    public AssetDrawingPanel(mxGraph g) {
        super(g);
        initLayout();
        asset = null;
    }

    public AssetDrawingPanel() {
        super(new mxGraph());
        initLayout();
        asset = null;
    }

    public void drawAsset(ImitatedAsset assetToDraw) {
        asset = assetToDraw;
        Object parent = graph.getDefaultParent();
        graph.getModel().beginUpdate();
        try
        {
            graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
            assetToGraph(assetToDraw, graph, parent);
        }
        finally
        {
            graph.getModel().endUpdate();
        }

        graph.getModel().beginUpdate();
        layout.execute(graph.getDefaultParent());
        graph.getModel().endUpdate();
    }

    private void assetToGraph(ImitatedAsset assetToDraw, mxGraph graph, Object parent) {
        Object root;
        if (assetToDraw.hasAssociatedVertex()) {
            root = assetToDraw.getAssociatedVertex();
            graph.insertEdge(root, null, "", parent, root);
            return;
        }
        root = graph.insertVertex(graph.getDefaultParent(), null, assetToDraw, 0,0, 40, 20);
        assetToDraw.setAssociatedVertex(root);
        graph.insertEdge(root, null, "", parent, root);
        for (ImitatedAsset child: assetToDraw.children){
            if (child != null) {
                assetToGraph(child, graph, root);
            }
        }
    }

    public Dimension getMinimumSize() {
        return new Dimension(250, 250);
    }

    public Dimension getPreferredSize() {
        return new Dimension(prefX, prefY);
    }

    public Dimension getMaximumSize() {
        return new Dimension (1024, 1024);
    }

}
