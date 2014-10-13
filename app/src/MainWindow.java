import javax.swing.*;
import javax.swing.text.Document;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by stacymiller on 07/10/14.
 */
public class MainWindow {
    private JFormattedTextField branchesFormattedTextField;
    private JFormattedTextField stepsFormattedTextField;
    private JFormattedTextField widthFormattedTextField;
    private JFormattedTextField columnsFormattedTextField;
    private JFormattedTextField initialPriceFormattedTextField;
    private JPanel mainPanel;
    private JButton generateAssetButton;

    public MainWindow() {
        generateAssetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int branches, steps, width, columns;
                double initialPrice;

                ImitatedAsset ia;
                String s = branchesFormattedTextField.getValue()
                        .toString();
                if (!s.equals("")) {
                    branches = Integer.getInteger(branchesFormattedTextField.getValue().toString());
                } else {
                    return;
                }

                if (!stepsFormattedTextField.getValue().toString().equals("")) {
                    steps = Integer.getInteger(stepsFormattedTextField.getValue().toString());
                } else {
                    return;
                }

                if (!initialPriceFormattedTextField.getValue().toString().equals("")) {
                    initialPrice = Double.parseDouble(initialPriceFormattedTextField.getValue().toString());
                } else {
                    return;
                }

                if (!widthFormattedTextField.getValue().toString().equals("")) {
                    width = Integer.getInteger(widthFormattedTextField.getValue().toString());
                } else {
                    ia = AssetGenerator.generateTreeAssets(branches, steps, initialPrice);
                    System.out.println(ia);
                    return;
                }

                if (!columnsFormattedTextField.getValue().toString().equals("")) {
                    columns = Integer.getInteger(columnsFormattedTextField.getValue().toString());
                } else {
                    ia = AssetGenerator.generateTreeAssets(branches, steps, initialPrice);
                    System.out.println(ia);
                    return;
                }

                ia = AssetGenerator.generateAssetByHistogram(width, branches, steps, columns, initialPrice);
                System.out.println(ia);
            }
        });
    }



    public static void main(String[] args) {
        JFrame frame = new JFrame("MainWindow");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
