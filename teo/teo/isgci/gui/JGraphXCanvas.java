package teo.isgci.gui;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.db.Algo;
import teo.isgci.db.Algo.NamePref;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.BFSWalker;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.Inclusion;
import teo.isgci.grapht.RevBFSWalker;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.Utility;

import com.mxGraph.adapter.mxJGraphTAdapter;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.util.mxUndoManager;
import com.mxgraph.util.mxUndoableEdit;
import com.mxgraph.util.mxUndoableEdit.mxUndoableChange;
import com.mxgraph.view.mxGraph;

/**
 * Handles the drawn mxGraph. This includes: - drawing a new mxGraph on top of a
 * Graph - layouting the mxGraph - colors the mxGraph for a given Problem -
 * hiding sub- and superclasses - marking unproper inclusions - restoring the
 * original mxGraph - zoom functionality - undo/redo - highlighted selection of
 * edges and vertices - panning - context menu on edges and vertices - tooltips
 * on vertices
 * 
 * @author Fabian Brosda, Thorsten Breitkreutz, Cristiana Grigoriu, Moritz
 *         Heine, Florian Krönert, Thorsten Sauter, Christian Stohr
 * 
 */
public class JGraphXCanvas implements MouseListener, MouseWheelListener,
        MouseMotionListener {

    /** JGraphX Components */
    private mxGraphComponent component = new mxGraphComponent(new mxGraph());
    private mxJGraphTAdapter<Set<GraphClass>, DefaultEdge> adapter;
    private mxHierarchicalLayout layout;
    public CellHighlighter highliter = new CellHighlighter(component,
            Color.orange);
    public mxUndoManager undoManager;
    protected mxIEventListener undoHandler;

    /** ISGCI Components */
    ISGCIMainFrame parent;
    private Algo.NamePref namingPref = Algo.NamePref.BASIC;
    private Problem problem;
    private Collection<GraphClass> classes;
    private List<String> vertexNames;
    private boolean drawUnproper;

    private Latex2JHtml converter = new Latex2JHtml();

    /** Colours for different complexities */
    public static final Color COLOR_LIN = Color.green;
    public static final Color COLOR_P = Color.green.darker();
    public static final Color COLOR_NPC = Color.red;
    public static final Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    public static final Color COLOR_UNKNOWN = Color.white;

    private NodePopup nodePopup;
    private EdgePopup edgePopup;

    private Point startPoint;

    /**
     * Constructor
     * 
     * @param parent
     *            the parent JFrame
     */
    public JGraphXCanvas(ISGCIMainFrame parent) {
        this.parent = parent;
        drawUnproper = parent.miDrawUnproper.isSelected();

        component.addMouseListener(this);
        component.addMouseWheelListener(this);
        component.addMouseMotionListener(this);
        component.setBorder(BorderFactory.createEmptyBorder());
        component.setToolTips(true);
        component.setAutoScroll(false);
        component.getGraphControl().addMouseListener(this);
        component.getGraphControl().addMouseMotionListener(this);

        nodePopup = new NodePopup(parent);
        edgePopup = new EdgePopup(parent);
        component.add(nodePopup);
        component.add(edgePopup);
    }

    /**
     * Draws the Graph with the given classes as vertices
     * 
     * @param classes
     */
    public void drawGraph(Collection<GraphClass> classes) {
        highliter.reset();
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g = Algo
                .createHierarchySubgraph(classes);
        this.classes = classes;
        setGraph(g);
        if (problem != null)
            setComplexityColors();
        setUnproperEdges();
    }

    /**
     * Sets alls attributes for the given graph and adds it to the canvas
     * 
     * @param graph
     *            to be drawn
     */
    private void setGraph(Graph<Set<GraphClass>, DefaultEdge> graph) {
        adapter = new mxJGraphTAdapter<Set<GraphClass>, DefaultEdge>(graph,
                "noLabel=1", null) {

            /*
             * Used for changing ToolTips of vertexes (non-Javadoc)
             * 
             * @see com.mxgraph.view.mxGraph#getToolTipForCell(java.lang.Object)
             */

            @Override
            public String getToolTipForCell(Object arg0) {
                if (arg0 instanceof mxCell) {
                    Latex2JHtml converter = new Latex2JHtml();
                    mxCell cell = (mxCell) arg0;

                    if (cell.isVertex()) {
                        Set<GraphClass> gcs = this.getCellToVertex(cell);
                        for (GraphClass gc : gcs) {
                            if (getNodeName(gc.toString()).equals(
                                    (String) cell.getValue())) {
                                return "<html>"
                                        + JGraphXCanvas.createLabel(converter
                                                .html(gc.toString()))
                                        + "</html>";
                            }

                        }
                    }
                }
                return null;
            }
        };

        adapter.setHtmlLabels(true);
        adapter.setAutoSizeCells(true);
        adapter.setAllowDanglingEdges(false);
        component.setEnabled(false);
        component.setAntiAlias(true);

        setNamingPref(namingPref);
        component.setGraph(adapter);

        createUndoManager();
        component.refresh();
        vertexNames = safeNames(graph.vertexSet());
    }

    private void createUndoManager() {
        undoManager = new mxUndoManager();
        undoHandler = new mxIEventListener() {
            public void invoke(Object source, mxEventObject evt) {
                undoManager.undoableEditHappened((mxUndoableEdit) evt
                        .getProperty("edit"));
            }
        };

        // Adds the command history to the model and view
        adapter.getModel().addListener(mxEvent.UNDO, undoHandler);
        adapter.getView().addListener(mxEvent.UNDO, undoHandler);

        // Keeps the selection in sync with the command history
        mxIEventListener listHandler = new mxIEventListener() {
            @Override
            public void invoke(Object source, mxEventObject evt) {
                List<mxUndoableChange> changes = ((mxUndoableEdit) evt
                        .getProperty("edit")).getChanges();
                if (evt.getName().equals(mxEvent.UNDO)) {
                    Set<GraphClass> tmp = parent.classesHandler
                            .getDeactivated();
                    List<Set<GraphClass>> redo = new LinkedList<Set<GraphClass>>();
                    for (Object o : adapter
                            .getSelectionCellsForChanges(changes)) {
                        mxCell cell = (mxCell) o;
                        for (GraphClass gc : tmp) {
                            if (cell.isVertex()
                                    && getNodeName(gc.toString()).equals(
                                            (String) cell.getValue())) {
                                Set<GraphClass> gcs = DataSet
                                        .getEquivalentClasses(gc);
                                redo.add(gcs);
                                for (GraphClass gcr : gcs) {
                                    parent.classesHandler.getDeactivated()
                                            .remove(gcr);
                                }
                                break;
                            }
                        }
                    }
                    parent.classesList.revalidate();
                    parent.classesList.repaint();
                }
                if (evt.getName().equals(mxEvent.REDO)) {
                    for (Object o : adapter.getRemovedCellsForChanges(changes)) {
                        mxCell cell = (mxCell) o;
                        if (cell.isVertex()) {
                            Set<GraphClass> gcs = adapter
                                    .getCellToVertex(cell);
                            parent.classesHandler.addDeactivated(gcs);
                        }
                    }
                    parent.classesList.revalidate();
                    parent.classesList.repaint();
                    component.refresh();
                }
            }
        };
        undoManager.addListener(mxEvent.UNDO, listHandler);
        undoManager.addListener(mxEvent.REDO, listHandler);
    }

    /**
     * Set all nodes to their preferred names and sets the LaTeX-label.
     * 
     * @param pref
     *            Naming preference for the labels
     */
    public void setNamingPref(NamePref pref) {
        namingPref = pref;
        adapter.getModel().beginUpdate();
        try {
            for (Object o : adapter.getChildCells(adapter.getDefaultParent(),
                    true, false)) {
                if (o instanceof mxCell) {
                    mxCell cell = (mxCell) o;
                    cell.setValue(getNodeName(Algo.getName(
                            adapter.getCellToVertex(cell), namingPref)));
                    adapter.updateCellSize(cell, true);
                }
            }
        } finally {
            adapter.getModel().endUpdate();
        }
        if (layout == null) {
            layout = new mxHierarchicalLayout(adapter);
        }
        layout.setInterRankCellSpacing(200);
        layout.execute(adapter.getDefaultParent());
        adapter.refresh();
    }

    /*
     * Saves the vertex-names of the drawn graph
     */
    private List<String> safeNames(Set<Set<GraphClass>> vertexSet) {
        List<String> result = new ArrayList<String>();
        for (Set<GraphClass> ver : vertexSet) {
            for (GraphClass gc : ver)
                result.add(gc.toString());
        }

        return result;
    }

    /**
     * Renames the vertex with the given name
     * 
     * @param view
     *            vertex to be changed
     * @param fullname
     *            current full name of the label
     */
    public void renameNode(Set<GraphClass> view, String fullname) {
        adapter.getVertexToCell(view).setValue(getNodeName(fullname));
        adapter.updateCellSize(adapter.getVertexToCell(view), true);
        adapter.refresh();
    }

    /**
     * Searches for the given GraphClass in the actual graph
     * 
     * @param selectedNode
     * @return the cell of the node corresponding to the given GraphClass,
     *         otherwise null
     */
    public mxCell findNode(GraphClass selectedNode) {
        if (adapter == null)
            return null;
        for (Object o : adapter.getChildCells(adapter.getDefaultParent(),
                true, false)) {
            if (o instanceof mxCell) {
                mxCell v = (mxCell) o;
                if (adapter.getCellToVertex(v).contains(selectedNode)) {
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Set coloring for problem p and repaint.
     * 
     * @param problem
     *            to be set to
     */
    public void setProblem(Problem problem) {
        if (this.problem != problem) {
            this.problem = problem;
            if (adapter != null) {
                setComplexityColors();
                adapter.refresh();
            }
        }
    }

    /*
     * Sets the Color given by the problem for each node
     */
    private void setComplexityColors() {
        if (problem == null) {
            adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, null, adapter
                    .getChildCells(adapter.getDefaultParent(), true, false));
            return;
        }

        List<Object> unknown = new LinkedList<Object>();
        List<Object> linear = new LinkedList<Object>();
        List<Object> polynomial = new LinkedList<Object>();
        List<Object> intermediate = new LinkedList<Object>();
        List<Object> npcomplete = new LinkedList<Object>();

        Complexity tmp = Complexity.UNKNOWN;

        for (Object o : adapter.getChildCells(adapter.getDefaultParent(),
                true, false)) {
            if (o instanceof mxCell) {
                mxCell v = (mxCell) o;
                tmp = problem.getComplexity(adapter.getCellToVertex(v)
                        .iterator().next());
                if (tmp.isUnknown())
                    unknown.add(v);
                else if (tmp.betterOrEqual(Complexity.LINEAR))
                    linear.add(v);
                else if (tmp.betterOrEqual(Complexity.P))
                    polynomial.add(v);
                else if (tmp.equals(Complexity.GIC))
                    intermediate.add(v);
                else if (tmp.likelyNotP())
                    npcomplete.add(v);
            }
        }
        adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                mxHtmlColor.getHexColorString(COLOR_UNKNOWN),
                unknown.toArray());
        adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                mxHtmlColor.getHexColorString(COLOR_LIN), linear.toArray());
        adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                mxHtmlColor.getHexColorString(COLOR_P), polynomial.toArray());
        adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                mxHtmlColor.getHexColorString(COLOR_INTERMEDIATE),
                intermediate.toArray());
        adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR,
                mxHtmlColor.getHexColorString(COLOR_NPC), npcomplete.toArray());
    }

    /* Mark unpropper inclusions */

    /**
     * Set unpropperInclusions and refresh drawing.
     * 
     * @param drawUnproper
     *            if unproper inclusions should be drawn
     */
    public void setUnproper(boolean drawUnproper) {
        if (this.drawUnproper != drawUnproper) {
            this.drawUnproper = drawUnproper;
            if (adapter != null) {
                setUnproperEdges();
                adapter.refresh();
            }
        }
    }

    /*
     * Draws the appropriate ending of the edge
     */
    private void setUnproperEdges() {
        if (!drawUnproper) {
            adapter.setCellStyles(mxConstants.STYLE_STARTARROW,
                    mxConstants.NONE, adapter.getChildCells(
                            adapter.getDefaultParent(), false, true));
            return;
        }
        List<Object> unproperEdges = new LinkedList<Object>();
        for (Object o : adapter.getChildCells(adapter.getDefaultParent(),
                false, true)) {
            if (o instanceof mxCell) {
                mxCell e = (mxCell) o;
                if (!getProperness(e)) {
                    unproperEdges.add(e);
                }
            }
        }
        adapter.setCellStyles(mxConstants.STYLE_STARTARROW,
                mxConstants.ARROW_DIAMOND, unproperEdges.toArray());
    }

    /*
     * Checks the appropriate properness of the given edge.
     */
    private boolean getProperness(mxCell edge) {
        Set<GraphClass> src = adapter.getCellToVertex((mxCell) edge
                .getSource());
        Set<GraphClass> dst = adapter.getCellToVertex((mxCell) edge
                .getTarget());

        List<Inclusion> path = GAlg.getPath(DataSet.inclGraph, src.iterator()
                .next(), dst.iterator().next());
        return (Algo.isPathProper(path) || Algo.isPathProper(Algo
                .makePathProper(path)));
    }

    /* Hide Sub and Superclasses */

    /**
     * Executes the hierarchical layout on the mxGraph
     */
    public void executeLayout() {
        if (adapter == null) {
            return;
        }
        adapter.getModel().beginUpdate();
        try {
            if (layout != null)
                layout.execute(adapter.getDefaultParent());
            component.refresh();
        } finally {
            adapter.getModel().endUpdate();
            highliter.refresh();
        }
    }

    /**
     * Restores the last drawn mxGraph
     */
    public void restoreGraph() {
        if (classes != null)
            drawGraph(classes);
        parent.classesHandler.getDeactivated().clear();
        parent.informationPanel.revalidate();
        parent.informationPanel.repaint();
        mxCell vertex = findNode(parent.classesList.getSelectedNode());
        if (vertex != null) {
            component.zoomActual();
            component.scrollCellToVisible(vertex, true);
            highliter.highlight(vertex);
        }
    }

    /**
     * Hides all subclasses of the given node
     * 
     * @param currNode
     */
    public void hideSubClasses(Set<GraphClass> currNode) {
        final Set<GraphClass> result = new HashSet<GraphClass>();
        GraphClass tmp = currNode.iterator().next();

        new BFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, tmp, null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                result.add(v);
                super.visit(v);
            }
        }.run();

        final Set<mxCell> nodes = new HashSet<mxCell>();
        List<GraphClass> names = new LinkedList<GraphClass>();
        for (GraphClass gc : result) {
            if (currNode.contains(gc))
                continue;
            nodes.add(findNode(gc));
            names.add(gc);
        }

        adapter.getModel().beginUpdate();
        try {
            for (mxCell node : nodes) {
                if (node != null)
                    adapter.getModel().setVisible(node, false);
            }
        } finally {
            adapter.getModel().endUpdate();
        }
        component.refresh();

        // Load information into informationbar
        if (!names.isEmpty()) {
            parent.classesHandler.addDeactivated(names);
        }
        parent.informationPanel.revalidate();
        parent.informationPanel.repaint();
    }

    /**
     * hides all superclasses of the given node
     * 
     * @param currNode
     */
    public void hideSuperClasses(Set<GraphClass> currNode) {
        final Set<GraphClass> result = new HashSet<GraphClass>();
        GraphClass tmp = currNode.iterator().next();

        new RevBFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, tmp, null,
                GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                result.add(v);
                super.visit(v);
            }
        }.run();

        final Set<mxCell> nodes = new HashSet<mxCell>();
        List<GraphClass> names = new LinkedList<GraphClass>();
        for (GraphClass gc : result) {
            if (currNode.contains(gc))
                continue;
            nodes.add(findNode(gc));
            names.add(gc);
        }

        adapter.getModel().beginUpdate();
        try {
            for (mxCell node : nodes) {
                if (node != null)
                    adapter.getModel().setVisible(node, false);
            }
        } finally {
            adapter.getModel().endUpdate();
        }
        component.refresh();

        // Load information into informationbar
        if (!names.isEmpty()) {
            parent.classesHandler.addDeactivated(names);
        }
        parent.informationPanel.revalidate();
        parent.informationPanel.repaint();
    }

    /* Getter Methodes */

    public mxGraphComponent getComponent() {
        return component;
    }

    public Algo.NamePref getNamingPref() {
        return namingPref;
    }

    public List<GraphClass> getGraphClassList() {
        if (classes == null) {
            return new ArrayList<GraphClass>();
        }
        Set<GraphClass> gcl = new HashSet<GraphClass>();
        for (GraphClass gc : classes) {
            gcl.addAll(DataSet.getEquivalentClasses(gc));
        }
        return new ArrayList<GraphClass>(gcl);
    }

    public List<String> getNames() {
        return vertexNames;
    }

    public String getNodeName(String gcn) {
        return createLabel(converter.html(Utility.getShortName(gcn)));
    }

    public mxJGraphTAdapter<Set<GraphClass>, DefaultEdge> getAdapter() {
        return adapter;
    }

    /* Listener */

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            for (; notches < 0; notches++) {
                component.zoomIn();
            }
        } else {
            for (; notches > 0; notches--) {
                component.zoomOut();
            }
        }
        highliter.refresh();
    }

    /*
     * Used to select a vertex or open the context-menu (non-Javadoc)
     * 
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */

    @Override
    public void mouseClicked(MouseEvent e) {
        Object o = component.getCellAt(e.getX(), e.getY());
        highliter.reset();
        if (o == null) {
            return;
        }
        if (o instanceof mxCell) {
            mxCell cell = (mxCell) o;
            if (cell.isVertex()) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    for (GraphClass gc : adapter.getCellToVertex(cell)) {
                        if (getNodeName(gc.toString()).equals(
                                (String) cell.getValue())) {
                            parent.classesList.setSelectedValue(gc, true);
                            break;
                        }
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    nodePopup.setNode(adapter.getCellToVertex(cell),
                            (String) cell.getValue());
                    nodePopup.show(component, e.getX()
                            - component.getHorizontalScrollBar().getValue(),
                            e.getY()
                                    - component.getVerticalScrollBar()
                                            .getValue());
                }
            } else if (cell.isEdge()) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    edgePopup
                            .setEdgeNodes(
                                    adapter.getCellToVertex((mxCell) cell
                                            .getSource()), (String) cell
                                            .getSource().getValue(), adapter
                                            .getCellToVertex((mxCell) cell
                                                    .getTarget()),
                                    (String) cell.getTarget().getValue());
                    edgePopup.show(component, e.getX()
                            - component.getHorizontalScrollBar().getValue(),
                            e.getY()
                                    - component.getVerticalScrollBar()
                                            .getValue());
                }
            }
            highliter.highlight(cell);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startPoint = e.getLocationOnScreen();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        startPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point endPoint = e.getLocationOnScreen();
        Rectangle current = component.getGraphControl().getVisibleRect();
        int xNew = endPoint.x - startPoint.x;
        int yNew = endPoint.y - startPoint.y;
        int xPan = 0;
        int yPan = 0;
        if (current.x - xNew > 0) {
            xPan = current.x - xNew;
        }
        if (current.y - yNew > 0) {
            yPan = current.y - yNew;
        }
        current.setLocation(xPan, yPan);
        component.getGraphControl().scrollRectToVisible(current);
        startPoint = e.getLocationOnScreen();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * creates label for mxCells of JgraphX
     * 
     * @param label
     *            String generated from Latex2JHtml
     * @return label as String
     */
    public static String createLabel(String label) {
        String temp = "";
        boolean co = false;
        int count = 0;

        for (int i = 0; i < label.length(); i++) {
            if (co) {
                temp += "&#095";
            } else {
                temp += "&#160";
                count++;
            }

            if (i > 3
                    && (label.substring(i - 4, i + 1).equals("<sub>") || label
                            .substring(i - 4, i + 1).equals("<sup>"))) {
                temp = temp.substring(0, temp.length() - 5 * 5);
                if (count < 5) {
                    count = 0;
                } else {
                    count -= 5;
                }
            } else if (i > 4
                    && (label.substring(i - 5, i + 1).equals("</sub>") || label
                            .substring(i - 5, i + 1).equals("</sup>"))) {
                temp = temp.substring(0, temp.length() - 6 * 5);
                if (count < 6) {
                    count = 0;
                } else {
                    count -= 6;
                }
            }

            if (label.charAt(i) == '(' && i > 2) {
                if (label.charAt(i - 1) == '-' && label.charAt(i - 2) == 'o'
                        && label.charAt(i - 3) == 'c') {
                    temp = temp.substring(0, temp.length() - 4 * 5);
                    co = true;
                }
            }
            if (co && label.charAt(i) == ')') {
                temp = temp.substring(0, temp.length() - 5);
                co = false;
            }
        }
        co = false;

        for (int i = 0; i < label.length(); i++) {
            if (label.charAt(i) == '(' && i > 2) {
                if (label.charAt(i - 1) == '-' && label.charAt(i - 2) == 'o'
                        && label.charAt(i - 3) == 'c') {
                    label = label.substring(0, i - 3)
                            + label.substring(i + 1, label.length());
                    co = true;
                    i -= 4;
                }
            } else if (co && label.charAt(i) == ')') {
                label = label.substring(0, i)
                        + label.substring(i + 1, label.length());
                co = false;
                i -= 1;
            }
        }

        if (temp.contains("&#095")) {
            return "<font face=\"Lucida Console\" align=\"left\" valign=\"top\">"
                    + temp
                    + "</font> <br>"
                    + "<font face=\"Lucida Console\" align=\"left\">"
                    + label
                    + "</font>";
        }

        return "<font face=\"Lucida Console\" align=\"left\">" + label
                + "</font>";
    }
}
