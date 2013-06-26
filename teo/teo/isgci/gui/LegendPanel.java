package teo.isgci.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class LegendPanel extends JPanel {

	private JLabel linearColor = new JLabel(
			"<html><div style=\"background-color:green;width:10px;height:10px;border:1px solid #000;\"></div></html>");
	private JLabel polynomialColor = new JLabel(
			"<html><div style=\"background-color:#006400;width:10px;height:10px;border:1px solid #000;\"></div></html>");
	private JLabel npCompleteColor = new JLabel(
			"<html><div style=\"background-color:red;width:10px;height:10px;border:1px solid #000;\"></div></html>");
	private JLabel intermediateColor = new JLabel(
			"<html><div style=\"background-color:#FF4500;width:10px;height:10px;border:1px solid #000;\"></div></html>");
	private JLabel unknownColor = new JLabel(
			"<html><div style=\"background-color:white;width:10px;height:10px;border:1px solid #000;\"></div></html>");

	private JLabel linear = new JLabel("Linear");
	private JLabel polynomial = new JLabel("Polynomial");
	private JLabel npComplete = new JLabel("NP-complete");
	private JLabel intermediate = new JLabel("Intermediate");
	private JLabel unknown = new JLabel("Unknown");

	public LegendPanel() {
//		setLayout(new GridLayout(5, 2));
		setLayout(new GridBagLayout());
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createTitledBorder("Legend"),
				BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		GridBagConstraints c = new GridBagConstraints();
		Insets colorInsets = new Insets(3, 0, 3, 3);
		Insets labelInsets = new Insets(0, 3, 3, 0);
		c.gridx = 0;
		c.gridy = 0;
		c.insets = colorInsets;
		c.anchor = GridBagConstraints.LINE_END;
		add(linearColor, c);
		c.gridx = 1;
		c.gridy = 0;
		c.insets = labelInsets;
		add(linear, c);
		c.gridx = 0;
		c.gridy = 1;
		c.insets = colorInsets;
		add(polynomialColor, c);
		c.gridx = 1;
		c.gridy = 1;
		c.insets = labelInsets;
		add(polynomial, c);
		c.gridx = 0;
		c.gridy = 2;
		c.insets = colorInsets;
		add(npCompleteColor, c);
		c.gridx = 1;
		c.gridy = 2;
		c.insets = labelInsets;
		add(npComplete, c);
		c.gridx = 0;
		c.gridy = 3;
		c.insets = colorInsets;
		add(intermediateColor, c);
		c.gridx = 1;
		c.gridy = 3;
		c.insets = labelInsets;
		add(intermediate, c);
		c.gridx = 0;
		c.gridy = 4;
		c.insets = colorInsets;
		add(unknownColor, c);
		c.gridx = 1;
		c.gridy = 4;
		c.insets = labelInsets;
		add(unknown, c);
		setOpaque(false);
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
