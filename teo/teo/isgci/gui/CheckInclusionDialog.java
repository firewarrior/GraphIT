/*
 * Allows the user to select two graphclasses and check their relation.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/CheckInclusionDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import teo.isgci.db.DataSet;
import teo.isgci.db.Algo;
import java.io.IOException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

/**
 * The dialog the checks for an inclusion between two graphclasses.
 * It contains two lists in single selection mode.
 */
public class CheckInclusionDialog extends JDialog
        implements ActionListener, ListSelectionListener,KeyListener {
    
    protected ISGCIMainFrame parent;
    protected NodeList firstList, secondList;
    protected JButton cancelButton;
    protected JButton inclusionCheckButton;
    protected WebSearch firstSearch, secondSearch;
    
    protected Dimension minSize = new Dimension(511,288);
    
    /** Create and display the dialog
     * @param parent the parent of the dialog
     */
    public CheckInclusionDialog(ISGCIMainFrame parent) {
        super(parent, "Find Relation", false);
        this.parent = parent;
        this.setMinimumSize(minSize);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        //Global Margin
        c.insets = new Insets(5, 10, 5, 10);
        
        //First Label
        JLabel l_first = new JLabel("First Class:");
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(l_first,c);
       
        //First Search
        firstSearch = new WebSearch();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(firstSearch,c);
        
        //First List
        firstList = new NodeList(parent.latex);
        firstList.setListData(DataSet.getClasses());
        firstList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(firstList);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(scroller,c);
        
        //Set to Null
        c.weighty = 0;
        c.weightx = 0;
        
        //Second Label
        JLabel l_second = new JLabel("Second Class:");
        c.gridx = 1;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(l_second,c);
        
        //Second Search
        secondSearch = new WebSearch();
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(secondSearch,c);
        
        //Second List
        secondList = new NodeList(parent.latex);
        secondList.setListData(DataSet.getClasses());
        secondList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller2 = new JScrollPane(secondList);
        c.gridx = 1;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(scroller2,c);
        
        //Set to Null
        c.weighty = 0;
        c.weightx = 0;
        
        //Button Globale Settings
        c.insets = new Insets(5, 80, 5, 80);
        c.fill = 0;
        
        //Find Relation Button
        inclusionCheckButton = new JButton("Find Relation");
        c.gridx = 0;
        c.gridy = 3;
        mainPanel.add(inclusionCheckButton,c);
        
        //Cancel Button
        cancelButton = new JButton("Close");
        c.gridx = 1;
        c.gridy = 3;
        mainPanel.add(cancelButton,c);
  
        this.add(mainPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addListeners();
        handleButtons();

    }

    /** Sets the event listeners for this dialog */
    protected void addListeners() {
        cancelButton.addActionListener(this);
        inclusionCheckButton.addActionListener(this);
        firstSearch.addKeyListener(this);
        secondSearch.addKeyListener(this);
        firstList.addListSelectionListener(this);
        secondList.addListSelectionListener(this);
    }


    /** Enables/disables the buttons depending on whether any items are
     * selected
     */
    public void handleButtons() {
        if (firstList.getSelectedValue() == null ||
                secondList.getSelectedValue() == null) {
            inclusionCheckButton.setEnabled(false);
        } else {
            inclusionCheckButton.setEnabled(true);
        }
    }

    
    /** Eventhandlers for the buttonclicks.
     */
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        if (e.getSource() instanceof NodeList)
            handleButtons();
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == inclusionCheckButton) {
            inclusionCheck();
        } else if (source == cancelButton) {
            closeDialog();
        } 
    }


    /** Close the dialog and release resources */
    public void closeDialog() {
        setVisible(false);
        dispose();
    }

    
    /** Checks whether the selected classes are related and displays the
     * result of this check in a new dialog.
     */
    public void inclusionCheck() {
        InclusionResultDialog.newInstance(parent,
                firstList.getSelectedNode(), secondList.getSelectedNode()
        ).setVisible(true);
    }

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {

	}

	@Override
	public void keyReleased(KeyEvent e) {
			firstSearch.setListData(parent, firstList);
			secondSearch.setListData(parent, secondList);
	}

}
/* EOF */
