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

    public GraphClassInformationDialog(ISGCIMainFrame parent) {
        this(parent, null);
    }

    public GraphClassInformationDialog(ISGCIMainFrame parent,
            GraphClass target) {
        super(parent, "Graph Class Information", false);
        this.parent = parent;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Container contents = getContentPane();
        Dimension listdim = new Dimension(150, 150);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel mid = new JPanel(new GridLayout(1,2,20,20));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER,70,10));
        
        //---- Create Top Panel ----
        search = new WebSearch();
        search.setText("Search...");
        search.setPreferredSize(new Dimension(180,25));
        search.addKeyListener(this);
        top.add(search);


        //---- Mid Panel ----
        // GraphList
        classesList = new NodeList(parent.latex);
        classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(classesList);
        scroller.setPreferredSize(listdim);
        scroller.setMinimumSize(listdim);
        mid.add(scroller);

        //---- Complexity ----
        JPanel p = new JPanel(new BorderLayout());
        JPanel problem = new JPanel(new BorderLayout());
        problem.setPreferredSize(new Dimension(300,292));
        problems = new JTable(new ProblemsModel());
        problem.add(problems.getTableHeader(), BorderLayout.NORTH);
        problem.add(problems, BorderLayout.CENTER);
        problems.setShowVerticalLines(false);
        problems.setBorder(
                BorderFactory.createMatteBorder(0,1,0,1,Color.black));
        
        p.add(problem,BorderLayout.NORTH);
        
        //---- Sub/super/equ classes ----
        JPanel grid = new JPanel(new GridLayout(1,3,10,0));
        
        JPanel superP = new JPanel(new BorderLayout(0,5));
        JLabel superLabel = new JLabel("Superclasses:", JLabel.LEFT);
        supClassesList = new NodeList(parent.latex);
        scroller = new JScrollPane(supClassesList);
        scroller.setPreferredSize(listdim);
        scroller.setMinimumSize(listdim);
        superP.add(superLabel,BorderLayout.NORTH);
        superP.add(scroller,BorderLayout.CENTER);
        
        JPanel equP = new JPanel(new BorderLayout(0,5));
        JLabel equLabel = new JLabel("Equivalent Classes:", JLabel.LEFT);
        equClassesList = new NodeList(parent.latex);
        scroller = new JScrollPane(equClassesList);
        scroller.setPreferredSize(listdim);
        scroller.setMinimumSize(listdim);
        equP.add(equLabel,BorderLayout.NORTH);
        equP.add(scroller,BorderLayout.CENTER);

        JPanel subP = new JPanel(new BorderLayout(0,5));
        JLabel subLabel = new JLabel("Subclasses:", JLabel.LEFT);
        subClassesList = new NodeList(parent.latex);
        scroller = new JScrollPane(subClassesList);
        scroller.setPreferredSize(listdim);
        scroller.setMinimumSize(listdim);
        subP.add(subLabel,BorderLayout.NORTH);
        subP.add(scroller,BorderLayout.CENTER);

        //Set to GridLayout
        grid.add(superP);
        grid.add(equP);
        grid.add(subP);
        
        //Add grid to p Panel
        p.add(grid, BorderLayout.SOUTH);
        mid.add(p);
        
        //---- Bottom Panel ----
        classButton = new JButton("Class details");
        bottom.add(classButton);
        inclButton = new JButton("Inclusion info");
        bottom.add(inclButton);
        drawButton = new JButton ("Draw");
        bottom.add(drawButton);
        okButton = new JButton("Close");
        bottom.add(okButton);
        
        //---- Main Panel ----
        mainPanel.add(top,BorderLayout.NORTH);
        mainPanel.add(mid,BorderLayout.CENTER);
        mainPanel.add(bottom,BorderLayout.SOUTH);
        
        contents.add(mainPanel);

        lists = new ListGroup(3);
        lists.add(subClassesList);
        lists.add(supClassesList);
        lists.add(equClassesList);


        classesList.setListData(DataSet.getClasses());

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
        supClassesList.addMouseListener(mouseAdapter);
        equClassesList.addMouseListener(mouseAdapter);
        subClassesList.addMouseListener(mouseAdapter);

        if (target != null)
            showNode(target);
        else
            showNode();
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
