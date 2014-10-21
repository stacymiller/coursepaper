import javax.swing.*;
import java.awt.*;

/**
 * Created by derketzer on 18.10.14.
 */
public class AssetDrawingPanel extends JPanel{
    private ImitatedAsset asset;
    private int prefX=500, prefY=250;
    private int actY = 0;
    private int actX = 0;

    public AssetDrawingPanel(ImitatedAsset assetToDraw) {
        super();
        asset = assetToDraw;
    }

    public AssetDrawingPanel() {
        super();
        asset = null;
    }

    public void drawAsset(ImitatedAsset assetToDraw) {
        asset = assetToDraw;
        repaint();
    }

    public Dimension getMinimumSize() {
        return new Dimension(50, 50);
    }

    public Dimension getPreferredSize() {
        return new Dimension(prefX, prefY);
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

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g);
        if (asset == null) {
            g2.setColor(Color.GREEN);
        } else {
            actY = 0;
            paintAsset(g2, asset, 0, 0, prefY);
        }
    }
}
