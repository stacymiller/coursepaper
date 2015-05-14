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
    private JTextField statesTextField;

    public MainWindow(){
        ActionListener generateAssetListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int branches = 0, steps = 0, width = 0, columns = 0, states = 0;
                double initialPrice;

                ImitatedAsset ia = null;
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

                if (!widthFormattedTextField.getText().equals("")) {
                    width = Integer.parseInt(widthFormattedTextField.getText());
                }

                if (!columnsFormattedTextField.getText().equals("")) {
                    columns = Integer.parseInt(columnsFormattedTextField.getText());
                }

                if (!statesTextField.getText().equals("")) {
                    states = Integer.parseInt(statesTextField.getText());
                }

                System.out.println(String.format("branches=%d, steps=%d, columns=%d, width=%d, initial price=%f", branches, steps, columns, width, initialPrice));
                if (states != 0) {
                    ia = FiniteStateAssetGenerator.generateAssetTree(states, branches, steps, initialPrice);
                } else if (!(width == 0 || columns == 0)) {
                    ia = HistogramAssetGenerator.generateAssetTreeByHistogram(width, branches, steps, columns, initialPrice);
                } else {
                    ia = AssetGenerator.generateAssetTree(branches, steps, initialPrice);

                }
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
