import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by stacymiller on 07/10/14.
 */
public class MainWindow {
    private JTextField branchesFormattedTextField;
    private JTextField stepsFormattedTextField;
    private JTextField widthFormattedTextField;
    private JTextField columnsFormattedTextField;
    private JTextField initialPriceFormattedTextField;
    private JPanel mainPanel;
    private JButton generateAssetButton;
    private AssetDrawingPanel assetDrawingPanel;

    public MainWindow() {
        ActionListener generateAssetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int branches = 0, steps = 0, width = 0, columns = 0;
                double initialPrice;

                ImitatedAsset ia;
                String s = branchesFormattedTextField.getText();
                if (!s.equals("")) {
                    branches = Integer.parseInt(branchesFormattedTextField.getText());
                } else {
                    return;
                }

                if (!stepsFormattedTextField.getText().equals("")) {
                    steps = Integer.parseInt(stepsFormattedTextField.getText());
                } else {
                    return;
                }

                if (!initialPriceFormattedTextField.getText().equals("")) {
                    initialPrice = Double.parseDouble(initialPriceFormattedTextField.getText());
                } else {
                    return;
                }

//                if (!widthFormattedTextField.getText().equals("")) {
//                    width = Integer.parseInt(widthFormattedTextField.getText());
//                } else {
//                    ia = AssetGenerator.generateTreeAssets(branches, steps, initialPrice);
//                    System.out.println(ia);
//                    return;
//                }
//
//                if (!columnsFormattedTextField.getText().equals("")) {
//                    columns = Integer.parseInt(columnsFormattedTextField.getText());
//                } else {
//                    ia = AssetGenerator.generateTreeAssets(branches, steps, initialPrice);
//                    System.out.println(ia);
//                    return;
//                }

                ia = AssetGenerator.generateTreeAssets(branches, steps, initialPrice);
                assetDrawingPanel.drawAsset(ia);
            }
        };
        generateAssetButton.addActionListener(generateAssetListener);

//        mxGraph graph = new mxGraph();
//
//        assetDrawingPanel = new AssetDrawingPanel(graph);
//        mainPanel.add(assetDrawingPanel);
    }



    public static void main(String[] args) {
        JFrame frame = new JFrame("MainWindow");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
//        mxGraph graph = new mxGraph();
//
//        assetDrawingPanel = new AssetDrawingPanel(graph);
    }
}
