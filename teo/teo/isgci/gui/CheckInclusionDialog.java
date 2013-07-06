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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import teo.isgci.db.DataSet;

/**
 * The dialog the checks for an inclusion between two graphclasses. It contains
 * two lists in single selection mode.
 */
public class CheckInclusionDialog extends JDialog implements ActionListener,
        ListSelectionListener, KeyListener {

    protected ISGCIMainFrame parent;
    protected NodeList firstList, secondList;
    protected JButton cancelButton;
    protected JButton inclusionCheckButton;
    protected WebSearch firstSearch, secondSearch;

    protected Dimension minSize = new Dimension(500, 300);

    /**
     * Create and display the dialog
     * 
     * @param parent
     *            the parent of the dialog
     */
    public CheckInclusionDialog(ISGCIMainFrame parent) {
        super(parent, "Find Relation", false);
        this.parent = parent;
        this.setMinimumSize(minSize);

        setLayout(new GridBagLayout());
        Container mainPanel = getContentPane();
        JScrollPane scroller;
        GridBagConstraints c = new GridBagConstraints();
        Dimension scrollerPreferredSize = new Dimension(250, 500);
        Dimension scrollerMinimumSize = new Dimension(150, 300);

        // Global Margin
        c.insets = new Insets(5, 10, 5, 10);

        // First Label
        JLabel l_first = new JLabel("First Class:");
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        mainPanel.add(l_first, c);

        // Second Label
        JLabel l_second = new JLabel("Second Class:");
        c.gridx = 1;
        c.gridy = 0;
        mainPanel.add(l_second, c);

        // First Search
        firstSearch = new WebSearch("Search...");
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(firstSearch, c);

        // Second Search
        secondSearch = new WebSearch("Search...");
        c.gridx = 1;
        c.gridy = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(secondSearch, c);

        // First List
        firstList = new NodeList(parent.latex);
        firstList.setListData(DataSet.getClasses());
        firstList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroller = new JScrollPane(firstList);
        scroller.setPreferredSize(scrollerPreferredSize);
        scroller.setMinimumSize(scrollerMinimumSize);
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        mainPanel.add(scroller, c);

        // Second List
        secondList = new NodeList(parent.latex);
        secondList.setListData(DataSet.getClasses());
        secondList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scroller = new JScrollPane(secondList);
        scroller.setPreferredSize(scrollerPreferredSize);
        scroller.setMinimumSize(scrollerMinimumSize);
        c.gridx = 1;
        c.gridy = 2;
        mainPanel.add(scroller, c);

        // Set to Null
        c.weighty = 0;
        c.weightx = 0;

        // ButtonPanel
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 5));
        inclusionCheckButton = new JButton("Find Relation");
        cancelButton = new JButton("Close");
        buttons.add(inclusionCheckButton);
        buttons.add(cancelButton);
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.insets = new Insets(5, 10, 5, 0);
        mainPanel.add(buttons, c);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addListeners();
        handleButtons();
        pack();

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

    /**
     * Enables/disables the buttons depending on whether any items are selected
     */
    public void handleButtons() {
        if (firstList.getSelectedValue() == null
                || secondList.getSelectedValue() == null) {
            inclusionCheckButton.setEnabled(false);
        } else {
            inclusionCheckButton.setEnabled(true);
        }
    }

    /**
     * Eventhandlers for the buttonclicks.
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

    /**
     * Checks whether the selected classes are related and displays the result
     * of this check in a new dialog.
     */
    public void inclusionCheck() {
        InclusionResultDialog.newInstance(parent, firstList.getSelectedNode(),
                secondList.getSelectedNode()).setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getSource() == firstSearch) {
            firstSearch.setCustomTextSet(true);
            firstSearch.setListData(parent, firstList);
        } else if (e.getSource() == secondSearch) {
            secondSearch.setCustomTextSet(true);
            secondSearch.setListData(parent, secondList);
        }
    }

}
/* EOF */
