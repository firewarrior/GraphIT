/*
 * Displays information about a given graphclass.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphClassInformationDialog.java,v 2.1 2011/09/29 08:38:57 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.io.IOException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Container;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.AbstractTableModel;

import org.jgrapht.generate.GridGraphGenerator;

import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;
import teo.isgci.problem.*;
import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.LessLatex;


/**
 * Display a list of graphclasses and for the selected class also its super-,
 * sub- and equivalent classes.
 */
public class GraphClassInformationDialog extends JDialog
        implements ActionListener, ListSelectionListener,KeyListener {

    protected ISGCIMainFrame parent;
    protected NodeList classesList;
    protected NodeList subClassesList, supClassesList, equClassesList;
    protected ListGroup lists;
    protected JLabel complexity;
    protected JTable problems;
    protected JButton okButton, classButton, inclButton, drawButton;
    protected WebSearch search;
    protected MouseAdapter mouseAdapter;
    protected Dimension minSize = new Dimension(611,394);

    public GraphClassInformationDialog(ISGCIMainFrame parent) {
        this(parent, null);
    }

    public GraphClassInformationDialog(ISGCIMainFrame parent,
            GraphClass target) {
        super(parent, "Graph Class Information", false);
        this.parent = parent;
        this.setMinimumSize(minSize);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Container contents = getContentPane();
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        //Global Settings
        c.insets = new Insets(5, 5, 5, 5);
        
        //---- Search ----
        search = new WebSearch();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        mainPanel.add(search,c);

        //Set to Null
        c.anchor = GridBagConstraints.CENTER;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        
        // GraphList
        classesList = new NodeList(parent.latex);
        classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(classesList);
        c.gridx = 0;
        c.gridy = 1;
        c.gridheight = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(scroller,c);
        
        //Set to Null
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;
        
        //---- Complexity ----
        JPanel problem = new JPanel(new GridBagLayout());
        GridBagConstraints cc = new GridBagConstraints();
        
        //Global Setting
        cc.weightx = 1;
        cc.weighty = 1;
        cc.insets = new Insets(0, 5, 0, 5);
        
        //Complexity List
        problems = new JTable(new ProblemsModel());
        JScrollPane scroller2 = new JScrollPane(problems);
        
        problems.setShowVerticalLines(false);
        problems.setBorder(
                BorderFactory.createMatteBorder(0,1,0,1,Color.black));
        
        //Titel
        cc.gridx = 0;
        cc.gridy = 0;
        cc.anchor = GridBagConstraints.PAGE_START;
        
        problem.add(problems.getTableHeader(), cc);
        
        //Set to Null
        cc.anchor = GridBagConstraints.CENTER;
      
        //List
        cc.gridx = 0;
        cc.gridy = 1;
        cc.fill = GridBagConstraints.BOTH;
        problem.add(scroller2,cc);
        
        //Add to Main Panel
        c.gridx = 1;
        c.gridy = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(problem,c);
        
        //Set to Null
        c.fill = GridBagConstraints.NONE;
        c.weighty = 0;
        
        //---- Sub/super/equ classes ----
        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints cg = new GridBagConstraints();
        
        //Global Settings
        cg.anchor = GridBagConstraints.WEST;
        cg.insets = new Insets(0, 5, 0, 5);
        
        JLabel superLabel = new JLabel("Superclasses:");
        cg.gridx = 0;
        cg.gridy = 0;
        grid.add(superLabel,cg);
        
        supClassesList = new NodeList(parent.latex);
        scroller = new JScrollPane(supClassesList);
        cg.gridx = 0;
        cg.gridy = 1;
        cg.weighty = 1;
        cg.weightx = 1;
        cg.fill = GridBagConstraints.BOTH;
        grid.add(scroller,cg);
        
        //Set to Null
        cg.fill = GridBagConstraints.NONE;
        cg.weighty = 0;
        cg.weightx = 0;
        
        JLabel equLabel = new JLabel("Equivalent Classes:");
        cg.gridx = 1;
        cg.gridy = 0;
        grid.add(equLabel,cg);
        
        equClassesList = new NodeList(parent.latex);
        scroller = new JScrollPane(equClassesList);
        cg.gridx = 1;
        cg.gridy = 1;
        cg.weighty = 1;
        cg.weightx = 1;
        cg.fill = GridBagConstraints.BOTH;
        grid.add(scroller,cg);
        
        //Set to Null
        cg.fill = GridBagConstraints.NONE;
        cg.weighty = 0;
        cg.weightx = 0;
        
        JLabel subLabel = new JLabel("Subclasses:");
        cg.gridx = 2;
        cg.gridy = 0;
        grid.add(subLabel,cg);
        
        subClassesList = new NodeList(parent.latex);
        scroller = new JScrollPane(subClassesList);
        cg.gridx = 2;
        cg.gridy = 1;
        cg.weighty = 1;
        cg.weightx = 1;
        cg.fill = GridBagConstraints.BOTH;
        grid.add(scroller,cg);
        
        //Add grid to MainPanel
        c.gridx = 1;
        c.gridy = 2;
        c.weighty = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(grid,c);
        
        //Set to Null
        c.fill = GridBagConstraints.NONE;
        c.weighty = 0;
        c.weightx = 0; 
        
        //---- Bottom Panel ----
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER,60,0));
        
        classButton = new JButton("Class details");
        bottom.add(classButton);
        inclButton = new JButton("Inclusion info");
        bottom.add(inclButton);
        drawButton = new JButton ("Draw");
        bottom.add(drawButton);
        okButton = new JButton("Close");
        bottom.add(okButton);
        
        //Set Bottom Panel on Main Panel
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        c.weightx = 1;
        mainPanel.add(bottom,c);
        
        
        contents.add(mainPanel);

        lists = new ListGroup(3);
        lists.add(subClassesList);
        lists.add(supClassesList);
        lists.add(equClassesList);


        classesList.setListData(DataSet.getClasses());
        
        //Register the whole Listener
        registerListener();

        if (target != null)
            showNode(target);
        else
            showNode();
    }


    private void registerListener() {
    	
      	 mouseAdapter = new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2) {
                     NodeList list = (NodeList) e.getSource();
                     showNode(list.getSelectedNode());
                 }
             }
         };
         
        classButton.addActionListener(this);
        inclButton.addActionListener(this);
        okButton.addActionListener(this);
        drawButton.addActionListener(this);
        classesList.addListSelectionListener(this);
        search.addKeyListener(this);
        supClassesList.addMouseListener(mouseAdapter);
        equClassesList.addMouseListener(mouseAdapter);
        subClassesList.addMouseListener(mouseAdapter);
        

		
	}

	/**
     * Show the information about the given class.
     */
    private void showNode(GraphClass target) {
        classesList.setSelectedValue(target, true);
        updateLists(target);
        updateComplexity(target);
    }

    /**
     * Show the information abot the first class in the list.
     */
    private void showNode() {
        GraphClass node = null;
        if (classesList.getElementCount() > 0)
            node = (GraphClass) classesList.getModel().getElementAt(0);
        showNode(node);
        repaint();
    }


    /**
     * Update the information in the complexity label.
     */
    private void updateComplexity(GraphClass target) {
        ((ProblemsModel) problems.getModel()).setNode(target);
    }


    /**
     * Update the information displayed in the super/sub/equiv lists.
     * @param name graph classe of which to display the information
     */
    private synchronized void updateLists(GraphClass target) {
        if (target == null) {
            Vector empty = new Vector();
            subClassesList.setListData(empty);
            supClassesList.setListData(empty);
            equClassesList.setListData(empty);
            return;
        }

        Iterator iter;
        int i;
        ArrayList<GraphClass> sup = Algo.superNodes(target);
        ArrayList<GraphClass> sub = Algo.subNodes(target);
        ArrayList<GraphClass> equ = Algo.equNodes(target);

        sup.removeAll(equ);
        sub.removeAll(equ);

        supClassesList.setListData(sup);
        subClassesList.setListData(sub);

        Collections.sort(equ, new LessLatex());
        equClassesList.setListData(equ);
    }


    protected void closeDialog() {
        setVisible(false);
        dispose();
    }


    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting())
            return;
        if (e.getSource() == classesList);
            showNode(classesList.getSelectedNode());
    }


    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == okButton) {
            closeDialog();
        } else if (source == classButton) {
            parent.loader.showDocument("classes/"+
                classesList.getSelectedNode().getID() +".html");
        } else if (source == inclButton) {
            GraphClass c1 = classesList.getSelectedNode();
            GraphClass c2 = lists.getSelectedNode();
            if (c1 != null && c2 != null) {
                JDialog dia = InclusionResultDialog.newInstance(parent, c1,c2);
                dia.setVisible(true);
            }
        } else if (source == drawButton) {
            GraphClassSelectionDialog draw = 
                new GraphClassSelectionDialog(parent);
            draw.select(classesList.getSelectedNode());
            draw.setVisible(true);
        } 
    }

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		search.setListData(parent, classesList);
		
	}

}

/**
 * The model for the problem - complexity table in the dialogue.
 */
class ProblemsModel extends AbstractTableModel {
    private static String[] colNames = {"Problem", "Complexity"};
    private GraphClass gc;

    public ProblemsModel() {
        super();
        gc = null;
    }


    public int getColumnCount() {
        return 2;
    }

    public int getRowCount() {
        return DataSet.problems.size();
    }

    public String getColumnName(int col) {
        return colNames[col];
    }

    public Object getValueAt(int row, int col) {
        if (row < 0  ||  row >= DataSet.problems.size()  ||
                col < 0  ||  col > 1 ||
                gc == null)
            return "???";

        Problem p = DataSet.problems.elementAt(row);
        if (col == 0)
            return p.getName();
        else if (col == 1)
            return p.getComplexityString(p.getComplexity(gc));

        return "???";
    }

    void setNode(GraphClass n) {
        gc = n;
        fireTableDataChanged();
    }
}

/* EOF */
