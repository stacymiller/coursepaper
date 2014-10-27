import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;

/**
 * Created by derketzer on 18.10.14.
 */
public class AssetDrawingPanel extends mxGraphComponent{
    private ImitatedAsset asset;
    private int prefX=500, prefY=250;

    public AssetDrawingPanel(mxGraph g, ImitatedAsset assetToDraw) {
        super(g);
        asset = assetToDraw;
    }

    public AssetDrawingPanel(mxGraph g) {
        super(g);
        asset = null;
    }

    public AssetDrawingPanel() {
        super(new mxGraph());
        asset = null;
    }

    public void drawAsset(ImitatedAsset assetToDraw) {
        asset = assetToDraw;
        mxGraph graph = getGraph();
        Object parent = graph.getDefaultParent();

        graph.getModel().beginUpdate();
        try
        {
            Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80,
                    30);
            Object v2 = graph.insertVertex(parent, null, "World!", 240, 150,
                    80, 30);
            graph.insertEdge(parent, null, "Edge", v1, v2);
        }
        finally
        {
            graph.getModel().endUpdate();
        }
//        repaint();
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

    private void paintAsset(Graphics2D g, ImitatedAsset a, int x, int yTop, int yBottom) {
        String s = String.format(" %.2f ", a.price);
        g.drawString(s, x, yTop + (yBottom - yTop) / 2);
        FontMetrics metrics = g.getFontMetrics();
        int hgt = metrics.getHeight();
        int wdt = metrics.stringWidth(s);
        int space = metrics.stringWidth(String.format("%.2f", 100.));
        if (!a.lastChild) {
            int len = a.children_length();
            double h = ((double)(yBottom - yTop)) / len;
            for (int i=0, j=0; i < a.children.length; i++) {
                if (a.children[i] != null) {
                    g.drawLine(x + wdt, yTop + (yBottom - yTop - hgt) / 2, x + wdt + space, yTop + (int)((j + 0.5) * h - hgt / 2));
                    paintAsset(g, a.children[j], x + wdt + space, yTop + (int)(j * h), yTop + (int)((j + 1) * h));
                    j++;
                }
            }
        }
    }

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
