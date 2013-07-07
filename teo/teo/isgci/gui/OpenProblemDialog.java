/*
 * Displays a list of graph classes for which the given problem is still open.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/OpenProblemDialog.java,v 2.2 2013/03/12 18:43:39 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.BFSWalker;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.Inclusion;
import teo.isgci.grapht.RevBFSWalker;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;
import teo.isgci.util.LessLatex;

/**
 * Displays three lists of graph classes: Minimal classes for which the given
 * problem is NP-complete, maximal classes for which the problem is polynomial,
 * and classes for which the problem is still open.
 */
public class OpenProblemDialog extends JDialog implements ItemListener,
        ActionListener, ListSelectionListener {
    protected ISGCIMainFrame parent;
    protected JCheckBox fullBoundary;
    protected NodeList npList, openList, pList;
    protected ListGroup lists;
    protected Problem problem = DataSet.getProblem("Recognition");
    protected JButton closeButton, showButton, drawButton;
    private String[] problems = { "Recognition", "Treewidth", "Cliquewidth",
            "Cliquewidth expression", "Weighted independent set",
            "Independent set", "Weighted clique", "Clique", "Domination",
            "Colourability", "Clique cover", "3-Colourability", "Cutwidth",
            "Hamiltonian cycle", "Hamiltonian path",
            "Weighted feedback vertex set", "Feedback vertex set" };
    protected JComboBox chooseProblem;
    protected Dimension minSize = new Dimension(550, 270);

    public OpenProblemDialog(ISGCIMainFrame parent) {
        super(parent, "Boundary/Open classes", false);
        this.parent = parent;
        this.setMinimumSize(minSize);

        lists = new ListGroup(3);
        JScrollPane scroller;

        npList = new NodeList(ISGCIMainFrame.latex);
        openList = new NodeList(ISGCIMainFrame.latex);
        pList = new NodeList(ISGCIMainFrame.latex);
        initListOpen();
        initListsBoundary();
        initListsMinMax();

        // MainPanel for the Dialog
        Container contents = getContentPane();
        GridBagConstraints c = new GridBagConstraints();
        contents.setLayout(new GridBagLayout());
        Dimension scrollPreferredDimension = new Dimension(310, 180);
        Dimension scrollMinimalDimension = new Dimension(200, 100);
        c.insets = new Insets(5, 10, 5, 10);

        // Select Label
        JLabel label = new JLabel("Select:", JLabel.LEFT);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        contents.add(label, c);

        // CheckBox with the different Problems
        chooseProblem = new JComboBox(problems);
        c.gridx = 0;
        c.gridy = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        contents.add(chooseProblem, c);

        // CheckBox
        fullBoundary = new JCheckBox("List all boundary classes");
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 2;
        contents.add(fullBoundary, c);

        // Remove anchor
        c.anchor = GridBagConstraints.CENTER;

        // Set to Null
        c.gridwidth = 1;

        // ---- NPC/open/P labels ----
        label = new JLabel("Minimal (co)NP-complete:", JLabel.LEFT);
        c.gridx = 0;
        c.gridy = 2;
        contents.add(label, c);

        label = new JLabel("Open:", JLabel.LEFT);
        c.gridx = 1;
        c.gridy = 2;
        contents.add(label, c);

        label = new JLabel("Maximal P:", JLabel.LEFT);
        c.gridx = 2;
        c.gridy = 2;
        contents.add(label, c);

        // ---- NPC/open/P classes ----
        // Globale Setting
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        // The classes
        scroller = new JScrollPane(npList);
        scroller.setPreferredSize(scrollPreferredDimension);
        scroller.setMinimumSize(scrollMinimalDimension);
        c.gridx = 0;
        c.gridy = 3;
        contents.add(scroller, c);

        scroller = new JScrollPane(openList);
        scroller.setPreferredSize(scrollPreferredDimension);
        scroller.setMinimumSize(scrollMinimalDimension);
        c.gridx = 1;
        c.gridy = 3;
        contents.add(scroller, c);

        scroller = new JScrollPane(pList);
        scroller.setPreferredSize(scrollPreferredDimension);
        scroller.setMinimumSize(scrollMinimalDimension);
        c.gridx = 2;
        c.gridy = 3;
        contents.add(scroller, c);

        // Set to Null
        c.weightx = 0;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;

        // Button Panel with FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING,
                10, 5));
        drawButton = new JButton("Draw");
        showButton = new JButton("Class info");
        closeButton = new JButton("Close");
        buttonPanel.add(drawButton);
        buttonPanel.add(showButton);
        buttonPanel.add(closeButton);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.LAST_LINE_END;
        c.insets = new Insets(5, 10, 5, 0);
        contents.add(buttonPanel, c);

        lists.add(npList);
        lists.add(openList);
        lists.add(pList);

        handleButtons();
        pack();
        // setSize(700, 300);
        registerListener();
    }

    private void registerListener() {
        chooseProblem.addActionListener(this);
        fullBoundary.addItemListener(this);
        npList.addListSelectionListener(this);
        openList.addListSelectionListener(this);
        pList.addListSelectionListener(this);
        drawButton.addActionListener(this);
        showButton.addActionListener(this);
        closeButton.addActionListener(this);

    }

    protected void closeDialog() {
        setVisible(false);
        dispose();
    }

    /**
     * Set the contents of the open list.
     */
    private void initListOpen() {
        Vector v = new Vector();
        for (GraphClass gc : DataSet.getClasses()) {
            Complexity c = problem.getComplexity(gc);
            if (c.isUnknown())
                v.add(gc);
        }

        openList.setListData(v.iterator());
    }

    /**
     * Set the contents of P/NP lists as all boundary classes.
     */
    private void initListsBoundary() {
        TreeSet<GraphClass> npc = new TreeSet<GraphClass>(new LessLatex());
        TreeSet<GraphClass> p = new TreeSet<GraphClass>(new LessLatex());

        for (Inclusion e : DataSet.inclGraph.edgeSet()) {
            Complexity cfrom = problem.getComplexity(e.getSuper());
            Complexity cto = problem.getComplexity(e.getSub());
            if (cfrom.likelyNotP() && !cto.equals(cfrom)) {
                npc.addAll(DataSet.getEquivalentClasses(e.getSuper()));
            }
            if (cto.betterOrEqual(Complexity.P)
                    && (cfrom.isUnknown() || cfrom.likelyNotP())) {
                p.addAll(DataSet.getEquivalentClasses(e.getSub()));
            }
        }

        npList.setListData(npc.iterator());
        pList.setListData(p.iterator());
    }

    /**
     * Set the contents of P/NP lists as all minimal/maximal boundary classes.
     */
    private void initListsMinMax() {
        TreeSet<GraphClass> npc = new TreeSet<GraphClass>(new LessLatex());
        TreeSet<GraphClass> p = new TreeSet<GraphClass>(new LessLatex());

        for (GraphClass gc : DataSet.getClasses()) {
            if (npc.contains(gc) || p.contains(gc))
                continue;

            Complexity c = problem.getComplexity(gc);
            Set<GraphClass> equs = DataSet.getEquivalentClasses(gc);

            notP: if (c.likelyNotP()) {
                for (GraphClass equ : equs)
                    for (GraphClass down : GAlg.outNeighboursOf(
                            DataSet.inclGraph, equ)) {
                        if (problem.getComplexity(down).likelyNotP()
                                && !equs.contains(down))
                            break notP;
                    }
                npc.addAll(equs);
            }

            inP: if (c.betterOrEqual(Complexity.P)) {
                for (GraphClass equ : equs)
                    for (GraphClass up : GAlg.inNeighboursOf(
                            DataSet.inclGraph, equ)) {
                        if (problem.getComplexity(up).betterOrEqual(
                                Complexity.P)
                                && !equs.contains(up))
                            break inP;
                    }
                p.addAll(equs);
            }
        }

        npList.setListData(npc.iterator());
        pList.setListData(p.iterator());
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == drawButton) {
            Cursor oldcursor = parent.getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            parent.getxCanvas().drawGraph(getNodes(lists.getSelectedNode()));
            setCursor(oldcursor);
            closeDialog();

            // Load information into informationbar
            List<GraphClass> names = parent.getxCanvas().getGraphClassList();
            if (!names.isEmpty()) {
                Collections.sort(names, new LessLatex());
                parent.classesList.setListData(names);
            }
            parent.informationPanel.revalidate();
        } else if (source == showButton) {
            JDialog info = new GraphClassInformationDialog(parent,
                    lists.getSelectedNode());
            info.setLocation(50, 50);
            info.pack();
            info.setSize(800, 600);
            info.setVisible(true);

        } else if (source == chooseProblem) {
            this.problem = DataSet.getProblem(chooseProblem.getSelectedItem()
                    .toString());
            if (this.problem == null)
                throw new IllegalArgumentException("Problem " + problem
                        + " not found?!");
            initListOpen();
            initListsMinMax();

        } else if (source == closeButton) {
            closeDialog();
        }
    }

    public void itemStateChanged(ItemEvent event) {
        Object source = event.getSource();
        if (source == fullBoundary) {
            if (event.getStateChange() == ItemEvent.DESELECTED)
                initListsMinMax();
            else
                initListsBoundary();
        }
    }

    public void valueChanged(ListSelectionEvent event) {
        handleButtons();
    }

    /**
     * Enables/disables the buttons depending on whether any items are selected
     */
    public void handleButtons() {
        if (lists.getSelectedItem() == null) {
            showButton.setEnabled(false);
            drawButton.setEnabled(false);
        } else {
            showButton.setEnabled(true);
            drawButton.setEnabled(true);
        }
    }

    /**
     * Returns a vector with the environment of the node with the given name.
     * The environment depends on the complexity of the given node.
     */
    protected Collection<GraphClass> getNodes(GraphClass node) {
        Complexity c = problem.getComplexity(node);
        Collection<GraphClass> result = null;
        if (c.isUnknown())
            result = getNodesOpen(node, problem);
        else if (c.betterOrEqual(Complexity.P))
            result = getNodesP(node, problem);
        else if (c.likelyNotP())
            result = getNodesNP(node, problem);
        else
            throw new RuntimeException("Bad node");
        return result;
    }

    /**
     * Return a collection with the environment of the given node. The
     * environment is found by walking over open super/subclasses until the
     * first non-open node is reached.
     */
    private Collection<GraphClass> getNodesOpen(GraphClass node,
            final Problem problem) {
        /*
         * final ArrayList<GraphClass> result = new ArrayList<GraphClass>(); new
         * UBFSWalker<GraphClass,Inclusion>( DataSet.inclGraph, node, null,
         * GraphWalker.InitCode.DYNAMIC) { public void visit(GraphClass v) {
         * result.add(v); Complexity c = problem.getComplexity(v); if
         * (c.isUnknown()) super.visit(v); else finish(v); } }.run();
         */

        ArrayList<GraphClass> result = new ArrayList<GraphClass>();
        result.addAll(getNodesNP(node, problem));
        result.addAll(getNodesP(node, problem));

        return result;
    }

    /**
     * Return a collection with the environment of the given node. The
     * environment is found by walking over open subclasses until the first
     * polynomial node is reached.
     */
    private Collection<GraphClass> getNodesNP(GraphClass node,
            final Problem problem) {
        final ArrayList<GraphClass> result = new ArrayList<GraphClass>();
        new BFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, node, null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                result.add(v);
                if (problem.getComplexity(v).betterOrEqual(Complexity.P))
                    finish(v);
                else
                    super.visit(v);
            }
        }.run();

        return result;
    }

    /**
     * Fills in a vector with the environment of the given node. The environment
     * is found by walking over open superclasses until the first non-polynomial
     * node is reached.
     */
    private Collection<GraphClass> getNodesP(GraphClass node,
            final Problem problem) {
        final ArrayList<GraphClass> result = new ArrayList<GraphClass>();
        new RevBFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, node, null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                result.add(v);
                Complexity c = problem.getComplexity(v);
                if (c.likelyNotP())
                    finish(v);
                else
                    super.visit(v);
            }
        }.run();

        return result;
    }

}

/* EOF */
