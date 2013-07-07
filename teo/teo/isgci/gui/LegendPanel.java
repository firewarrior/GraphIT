package teo.isgci.gui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/**
 * Display the legend for the matching of complexity classes and their colors.
 * 
 * @author Fabian Brosda, Thorsten Breitkreutz, Cristiana Grigoriu, Moritz
 *         Heine, Florian Kroenert, Thorsten Sauter, Christian Stohr
 * 
 */
public class LegendPanel extends JPanel {

    private JLabel linearColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_LIN)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");
    private JLabel polynomialColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_P)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");
    private JLabel giCompleteColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_INTERMEDIATE)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");
    private JLabel npCompleteColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_NPC)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");
    private JLabel npHardColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_NPC)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");
    private JLabel coNpCompleteColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_NPC)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");
    private JLabel openColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_UNKNOWN)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");
    private JLabel unknownColor = new JLabel(
            "<html><div style=\"background-color:"
                    + colorToHexCode(JGraphXCanvas.COLOR_UNKNOWN)
                    + ";width:10px;height:10px;border:1px solid #000;\"></div></html>");

    private JLabel linear = new JLabel("Linear");
    private JLabel polynomial = new JLabel("Polynomial");
    private JLabel giComplete = new JLabel("GI-complete");
    private JLabel npComplete = new JLabel("NP-complete");
    private JLabel npHard = new JLabel("NP-hard");
    private JLabel coNpComplete = new JLabel("coNP-complete");
    private JLabel open = new JLabel("Open");
    private JLabel unknown = new JLabel("Unknown");

    /**
     * Creates the panel containing the colored squares and their labels.
     */
    public LegendPanel() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Legend"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        GridBagConstraints c = new GridBagConstraints();
        Insets colorInsets = new Insets(3, 0, 3, 3);
        Insets labelInsets = new Insets(0, 3, 3, 0);
        c.gridx = 0;
        c.gridy = 7;
        c.insets = colorInsets;
        c.anchor = GridBagConstraints.LINE_END;
        add(linearColor, c);
        c.gridx = 1;
        c.gridy = 7;
        c.insets = labelInsets;
        add(linear, c);
        c.gridx = 0;
        c.gridy = 6;
        c.insets = colorInsets;
        add(polynomialColor, c);
        c.gridx = 1;
        c.gridy = 6;
        c.insets = labelInsets;
        add(polynomial, c);
        c.gridx = 0;
        c.gridy = 5;
        c.insets = colorInsets;
        add(giCompleteColor, c);
        c.gridx = 1;
        c.gridy = 5;
        c.insets = labelInsets;
        add(giComplete, c);
        c.gridx = 0;
        c.gridy = 4;
        c.insets = colorInsets;
        add(npCompleteColor, c);
        c.gridx = 1;
        c.gridy = 4;
        c.insets = labelInsets;
        add(npComplete, c);
        c.gridx = 0;
        c.gridy = 3;
        c.insets = colorInsets;
        add(npHardColor, c);
        c.gridx = 1;
        c.gridy = 3;
        c.insets = labelInsets;
        add(npHard, c);
        c.gridx = 0;
        c.gridy = 2;
        c.insets = colorInsets;
        add(coNpCompleteColor, c);
        c.gridx = 1;
        c.gridy = 2;
        c.insets = labelInsets;
        add(coNpComplete, c);
        c.gridx = 0;
        c.gridy = 1;
        c.insets = colorInsets;
        add(openColor, c);
        c.gridx = 1;
        c.gridy = 1;
        c.insets = labelInsets;
        add(open, c);
        c.gridx = 0;
        c.gridy = 0;
        c.insets = colorInsets;
        add(unknownColor, c);
        c.gridx = 1;
        c.gridy = 0;
        c.insets = labelInsets;
        add(unknown, c);
        setOpaque(false);
    }

    private static String colorToHexCode(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(),
                c.getBlue());
    }

    // Testing
    public static void main(String[] args) {
        JFrame frame = new JFrame("Legend");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new LegendPanel());
        frame.pack();
        frame.setVisible(true);
    }

}
