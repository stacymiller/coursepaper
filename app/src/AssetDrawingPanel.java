import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
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
//        mxGraph graph = getGraph();
        Object parent = graph.getDefaultParent();
//        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
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
        Object root = graph.insertVertex(graph.getDefaultParent(), null, assetToDraw, 0,0, 40, 20);
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

//    private void paintAsset(Graphics2D g, ImitatedAsset a, int x, int yTop, int yBottom) {
//        String s = String.format(" %.2f ", a.price);
//        g.drawString(s, x, yTop + (yBottom - yTop) / 2);
//        FontMetrics metrics = g.getFontMetrics();
//        int hgt = metrics.getHeight();
//        int wdt = metrics.stringWidth(s);
//        int space = metrics.stringWidth(String.format("%.2f", 100.));
//        if (!a.lastChild) {
//            int len = a.children_length();
//            double h = ((double)(yBottom - yTop)) / len;
//            for (int i=0, j=0; i < a.children.length; i++) {
//                if (a.children[i] != null) {
//                    g.drawLine(x + wdt, yTop + (yBottom - yTop - hgt) / 2, x + wdt + space, yTop + (int)((j + 0.5) * h - hgt / 2));
//                    paintAsset(g, a.children[j], x + wdt + space, yTop + (int)(j * h), yTop + (int)((j + 1) * h));
//                    j++;
//                }
//            }
//        }
//    }

//    @Override
//    protected void paintComponent(Graphics g) {
//        Graphics2D g2 = (Graphics2D) g;
//        super.paintComponent(g);
//        if (asset == null) {
//            g2.setColor(Color.GREEN);
//        } else {
//            actY = 0;
////            paintAsset(g2, asset, 0, 0, getSize().height);
//        }
//    }
}
