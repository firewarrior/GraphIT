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
    
    /** Create and display the dialog
     * @param parent the parent of the dialog
     */
    public CheckInclusionDialog(ISGCIMainFrame parent) {
        super(parent, "Find Relation", false);
        this.parent = parent;

        JPanel mainPanel = new JPanel(new BorderLayout(20,20));
        JPanel gridTop = new JPanel(new GridLayout(1,2,20,0));
        JPanel gridMid = new JPanel(new GridLayout(1,2,20,0));
        JPanel flowBottom = new JPanel(new FlowLayout(FlowLayout.CENTER,250,10));
        
        //GridFirst
        JPanel gridFirst = new JPanel(new GridLayout(2, 1));
        gridFirst.setPreferredSize(new Dimension(0, 60));
        
        JLabel l_first = new JLabel("First Class:");
        firstSearch = new WebSearch();
        
        gridFirst.add(l_first);
        gridFirst.add(firstSearch);
        
        //First List
        firstList = new NodeList(parent.latex);
        firstList.setListData(DataSet.getClasses());
        firstList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(firstList);
        
        //GridSecond
        JPanel gridSecond = new JPanel(new GridLayout(2, 1));
        gridSecond.setPreferredSize(new Dimension(0, 60));
        
        JLabel l_second = new JLabel("Second Class:");
        secondSearch = new WebSearch();
        
        gridSecond.add(l_second);
        gridSecond.add(secondSearch);
        
        //Second List
        secondList = new NodeList(parent.latex);
        secondList.setListData(DataSet.getClasses());
        secondList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller2 = new JScrollPane(secondList);
        
        //Bottom
        inclusionCheckButton = new JButton("Find Relation");
        cancelButton = new JButton("Close");
        flowBottom.add(inclusionCheckButton);
        flowBottom.add(cancelButton);
        
        //Set Panels on Main Panel
        gridTop.add(gridFirst);
        gridTop.add(gridSecond);
        
        gridMid.add(scroller);
        gridMid.add(scroller2);
             
        
        mainPanel.add(gridTop,BorderLayout.NORTH);
        mainPanel.add(gridMid,BorderLayout.CENTER);
        mainPanel.add(flowBottom,BorderLayout.SOUTH);
        
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
