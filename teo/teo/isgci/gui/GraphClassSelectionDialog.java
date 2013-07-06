/*
 * Allows the user to select graphclasses for drawing.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphClassSelectionDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import teo.isgci.gc.GraphClass;
import teo.isgci.db.DataSet;
import teo.isgci.grapht.*;
import teo.isgci.util.LessLatex;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;

/**
 * Display a list of graphclasses and change the drawing according to the
 * selection.
 */
public class GraphClassSelectionDialog extends JDialog implements
		ActionListener, KeyListener {

	protected ISGCIMainFrame parent;
	protected NodeList classesList;
	protected JCheckBox superCheck, subCheck;
	protected JButton newButton, cancelButton;
	protected WebSearch search;
	protected Dimension minSize = new Dimension(328, 292);

	public GraphClassSelectionDialog(ISGCIMainFrame parent) {
		super(parent, "Select Graph Classes", false);
		this.parent = parent;
		this.setMinimumSize(minSize);

		Container contents = getContentPane();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		contents.setLayout(gridbag);

		c.insets = new Insets(5, 5, 0, 0);
		c.weightx = 0.0;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.BOTH;
		JLabel label = new JLabel("Draw:      ", JLabel.LEFT);
		gridbag.setConstraints(label, c);
		contents.add(label);

		label = new JLabel("     Filter:", JLabel.RIGHT);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		gridbag.setConstraints(label, c);
		contents.add(label);

		search = new WebSearch("Search...");
		search.addKeyListener(this);
		c.weightx = 1.0;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 5, 0, 5);
		gridbag.setConstraints(search, c);
		contents.add(search);

		c.insets = new Insets(5, 5, 5, 5);
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		classesList = new NodeList(ISGCIMainFrame.latex);
		JScrollPane scroller = new JScrollPane(classesList);
		gridbag.setConstraints(scroller, c);
		contents.add(scroller);

		c.insets = new Insets(0, 5, 0, 0);
		c.weighty = 0.0;
		label = new JLabel("and their", JLabel.LEFT);
		gridbag.setConstraints(label, c);
		contents.add(label);

		c.insets = new Insets(0, 5, 0, 0);
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		superCheck = new JCheckBox("superclasses");
		gridbag.setConstraints(superCheck, c);
		contents.add(superCheck);

		subCheck = new JCheckBox("subclasses");
		gridbag.setConstraints(subCheck, c);
		contents.add(subCheck);

		JPanel buttonPanel = new JPanel(
				new FlowLayout(FlowLayout.CENTER, 10, 0));
		newButton = new JButton("New drawing");
		cancelButton = new JButton("Close");
		buttonPanel.add(newButton);
		buttonPanel.add(cancelButton);
		c.insets = new Insets(5, 0, 5, 0);
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(buttonPanel, c);
		contents.add(buttonPanel);
		addListeners();
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

		classesList.setListData(DataSet.getClasses());
		pack();
		setSize(500, 400);
	}

	protected void addListeners() {
		newButton.addActionListener(this);
		cancelButton.addActionListener(this);
	}

	protected void closeDialog() {
		setVisible(false);
		dispose();
	}

	/**
	 * Select the given node.
	 */
	public void select(GraphClass node) {
		classesList.setSelectedValue(node, true);
	}

	public void actionPerformed(ActionEvent event) {
		Object source = event.getSource();
		if (source == cancelButton) {
			closeDialog();
		} else if (source == newButton) {
			Cursor oldcursor = parent.getCursor();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			Collection<GraphClass> gcl = getNodes();
			if(!gcl.isEmpty()){
				parent.getxCanvas().drawGraph(getNodes());
			}
			setCursor(oldcursor);
			closeDialog();

			// Load information into informationbar
			List<GraphClass> names = parent.getxCanvas().getGraphClassList();
			if (!names.isEmpty()) {
				Collections.sort(names, new LessLatex());
				parent.classesList.setListData(names);
			}
			parent.informationPanel.revalidate();

		}

	}

	/**
	 * Returns a Collection with the classes (in DataSet.inclGraph) that are
	 * selected by the current settings.
	 */
	protected Collection<GraphClass> getNodes() {
		final HashSet<GraphClass> result = new HashSet<GraphClass>();
		boolean doSuper = superCheck.isSelected(), doSub = subCheck
				.isSelected();

		for (Object o : classesList.getSelectedValuesList()) {
			GraphClass gc = (GraphClass) o;
			result.add(gc);
			if (doSuper) {
				new RevBFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, gc,
						null, GraphWalker.InitCode.DYNAMIC) {
					public void visit(GraphClass v) {
						result.add(v);
						super.visit(v);
					}
				}.run();
			}
			if (doSub) {
				new BFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, gc,
						null, GraphWalker.InitCode.DYNAMIC) {
					public void visit(GraphClass v) {
						result.add(v);
						super.visit(v);
					}
				}.run();
			}
		}

		return result;
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		search.setCustomTextSet(true);
		search.setListData(parent, classesList);
	}
}

/* EOF */

