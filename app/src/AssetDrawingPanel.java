import javax.swing.*;
import java.awt.*;

/**
 * Created by derketzer on 18.10.14.
 */
public class AssetDrawingPanel extends JPanel{
    private ImitatedAsset asset;

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
        return new Dimension(250, 250);
    }

    @Override
    protected void paintComponent(Graphics g) {
        System.out.println("Now painting");
        Graphics2D g2 = (Graphics2D) g;
        super.paintComponent(g2);
        if (asset == null) {
            g2.setColor(Color.GREEN);
        } else {
            g2.setColor(Color.RED);
        }
        g2.fillRoundRect(50, 50, 50, 50, 5, 5);
    }
}
