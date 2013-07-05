package teo.isgci.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Arrays;
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
import teo.isgci.util.JGraphTXAdapter;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.Utility;

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

public class JGraphXCanvas implements MouseListener, MouseWheelListener,
		MouseMotionListener {

	/** JGraphX Components */
	private mxGraphComponent component = new mxGraphComponent(new mxGraph());
	private JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;
	private mxHierarchicalLayout layout;
	public mxUndoManager undoManager;

	/** ISGCI Components */
	ISGCIMainFrame parent;
	private Algo.NamePref namingPref = Algo.NamePref.BASIC;
	private Problem problem;
	private Collection<GraphClass> classes;
	private List<String> vertexNames;
	private boolean drawUnpropper;

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

	/*
	 * Constructor
	 */
	public JGraphXCanvas(ISGCIMainFrame parent) {
		this.parent = parent;
		drawUnpropper = parent.miDrawUnproper.isSelected();

		component.addMouseListener(this);
		component.addMouseWheelListener(this);
		component.addMouseMotionListener(this);
		component.setPreferredSize(new Dimension(800, 600));
		component.setSize(component.getPreferredSize());
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
		SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g = Algo
				.createHierarchySubgraph(classes);
		this.classes = classes;
		setGraph(g);
		if (problem != null)
			setComplexityColors();
		setUnpropperEdges();
	}

	/**
	 * Sets alls attributes for the given graph and adds it to the canvas
	 * 
	 * @param graph
	 *            to be drawn
	 * @param edgeStyle
	 *            Style for vertices
	 * @param vertexStyle
	 *            Style for edges
	 */
	private void setGraph(Graph<Set<GraphClass>, DefaultEdge> graph,
			String edgeStyle, String vertexStyle) {
		adapter = new JGraphTXAdapter<Set<GraphClass>, DefaultEdge>(graph,
				edgeStyle + ";noLabel=1", vertexStyle) {
			
			/* Used for changing ToolTips of vertexes
			 * (non-Javadoc)
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
		
		adapter.setCellsMovable(false);
		adapter.setCellsDeletable(false);
		adapter.setCellsResizable(false);
		adapter.setAutoSizeCells(true);
		adapter.setCellsDisconnectable(false);
		adapter.setCellsSelectable(true);
		adapter.setConnectableEdges(false);
		adapter.setAllowDanglingEdges(false);
		adapter.setCellsEditable(false);
		adapter.setHtmlLabels(true);
		adapter.setCellsBendable(false);
		component.setConnectable(false);

		setNamingPref(namingPref);
		component.setGraph(adapter);
		
		undoManager = new mxUndoManager();
		// Adds the command history to the model and view
		adapter.getModel().addListener(mxEvent.UNDO, undoHandler);
		adapter.getView().addListener(mxEvent.UNDO, undoHandler);
		// Keeps the selection in sync with the command history
		mxIEventListener undoHandler = new mxIEventListener() {
			@Override
			public void invoke(Object source, mxEventObject evt) {
				List<mxUndoableChange> changes = ((mxUndoableEdit) evt
						.getProperty("edit")).getChanges();
				adapter.setSelectionCells(adapter
						.getSelectionCellsForChanges(changes));
				if (evt.getName().equals(mxEvent.UNDO)) {
					System.out.println("UNDO");
					Set<GraphClass> tmp = parent.classesHandler
							.getDeactivated();
					System.out.println("Deactivated: " + tmp);
					List<Set<GraphClass>> redo = new LinkedList<Set<GraphClass>>();
					System.out.println(Arrays.toString(adapter.getSelectionCells()));
					for (Object o : adapter.getSelectionCells()) {
						mxCell cell = (mxCell) o;
						for (GraphClass gc : tmp) {
							if (cell.isVertex()
									&& getNodeName(gc.toString())
											.equals((String) cell.getValue())) {
								Set<GraphClass> gcs = DataSet
										.getEquivalentClasses(gc);
								redo.add(gcs);
								System.out.println("Removing Graphclasses: " + gcs);
								for (GraphClass gcr : gcs) {
									System.out.println("Removing Graphclass: " + gcr + " Success: " + parent.classesHandler.getDeactivated().remove(
															gcr));
								}
								break;
							}
						}
					}
					parent.classesList.revalidate();
					parent.classesList.repaint();
				}
				if (evt.getName().equals(mxEvent.REDO)) {
					System.out.println("REDO");
					System.out.println(Arrays.toString(adapter.getSelectionCellsForChanges(changes)));
					for (Object o : adapter.getSelectionCellsForChanges(changes)) {
						mxCell cell = (mxCell) o;
						if (cell.isVertex()){
							Set<GraphClass> gcs = adapter.getCellToVertex(cell);
							parent.classesHandler.addDeactivated(gcs);
						}
					}
					parent.classesList.revalidate();
					parent.classesList.repaint();
				}
			}
		};
		undoManager.addListener(mxEvent.UNDO, undoHandler);
		undoManager.addListener(mxEvent.REDO, undoHandler);
		component.refresh();
		vertexNames = safeNames(graph.vertexSet());
	}

	/**
	 * Sets alls attributes for the given graph and adds it to the canvas
	 */
	private void setGraph(Graph<Set<GraphClass>, DefaultEdge> graph) {
		setGraph(graph, null, null);
	}

	/**
	 * Set all nodes to their prefered names and writes the Latex-Label
	 */
	public void setNamingPref(NamePref pref) {
		namingPref = pref;
		adapter.getModel().beginUpdate();
		try {
			for (Object o : adapter.getChildCells(adapter.getDefaultParent(),
					true, false)) {
				if (o instanceof mxCell) {
					mxCell cell = (mxCell) o;
					// cell.setValue(parent.latex.newLabel(getNodeName(Algo.getName(adapter.getCellToVertex(cell),
					// namingPref))));
					cell.setValue(getNodeName(Algo.getName(
							adapter.getCellToVertex(cell), namingPref)));
					adapter.updateCellSize(cell, true);
				}
			}
		} finally {
			adapter.getModel().endUpdate();
		}

		if (layout == null)
			layout = new mxHierarchicalLayout(adapter);
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
		for (Object o : adapter.getChildCells(adapter.getDefaultParent(), true,
				false)) {
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
	 */
	public void setProblem(Problem p) {
		if (problem != p) {
			problem = p;
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

		for (Object o : adapter.getChildCells(adapter.getDefaultParent(), true,
				false)) {
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
				mxHtmlColor.getHexColorString(COLOR_UNKNOWN), unknown.toArray());
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
	 * Set unpropperInclusions and refresh
	 */
	public void setUnpropper(boolean drawUnpropper) {
		if (this.drawUnpropper != drawUnpropper) {
			this.drawUnpropper = drawUnpropper;
			if (adapter != null) {
				setUnpropperEdges();
				adapter.refresh();
			}
		}
	}

	/*
	 * Draws the appropriate ending of the edge
	 */
	private void setUnpropperEdges() {
		if (!drawUnpropper) {
			adapter.setCellStyles(mxConstants.STYLE_STARTARROW,
					mxConstants.NONE, adapter.getChildCells(
							adapter.getDefaultParent(), false, true));
			return;
		}
		List<Object> unpropperEdges = new LinkedList<Object>();
		for (Object o : adapter.getChildCells(adapter.getDefaultParent(),
				false, true)) {
			if (o instanceof mxCell) {
				mxCell e = (mxCell) o;
				if (!getProperness(e)) {
					unpropperEdges.add(e);
				}
			}
		}
		adapter.setCellStyles(mxConstants.STYLE_STARTARROW,
				mxConstants.ARROW_DIAMOND, unpropperEdges.toArray());
	}

	/*
	 * Checks the appropriate properness of the given edge.
	 */
	private boolean getProperness(mxCell edge) {
		Set<GraphClass> src = adapter
				.getCellToVertex((mxCell) edge.getSource());
		Set<GraphClass> dst = adapter
				.getCellToVertex((mxCell) edge.getTarget());

		List<Inclusion> path = GAlg.getPath(DataSet.inclGraph, src.iterator()
				.next(), dst.iterator().next());
		return (Algo.isPathProper(path) || Algo.isPathProper(Algo
				.makePathProper(path)));
	}

	/* Hide Sub and Superclasses */

	/**
	 * Executes the hirarchical Layout on the actual Graph
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
		}
	}

	/**
	 * Restores the last drawn graph
	 */
	public void restoreGraph() {
		if (classes != null)
			drawGraph(classes);
		parent.classesHandler.getDeactivated().clear();
		parent.informationPanel.revalidate();
		parent.informationPanel.repaint();
		mxCell vertex = findNode(parent.classesList.getSelectedNode());
		if (vertex != null) {
			adapter.setSelectionCell(vertex);
			component.zoomActual();
			component.scrollCellToVisible(vertex, true);
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
			for(mxCell node : nodes){
				if(node != null)
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
			for(mxCell node : nodes){
				if(node != null)
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
		Set<GraphClass> gcl = new HashSet<GraphClass>();
		for (GraphClass gc : classes) {
			gcl.addAll(DataSet.getEquivalentClasses(gc));
		}
		return new ArrayList<GraphClass>(gcl);
	}

	public List<String> getNames() {
		return vertexNames;
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
	}
	
	/* Used to select a vertex or open the context-menu
	 * (non-Javadoc)
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			Object o = component.getCellAt(e.getX(), e.getY());
			if (o != null) {
				if (o instanceof mxCell) {
					mxCell cell = (mxCell) o;
					if (cell.isVertex()) {
						nodePopup.setNode(adapter.getCellToVertex(cell),
								(String) cell.getValue());
						nodePopup.show(component,
								e.getX()
										- component.getHorizontalScrollBar()
												.getValue(), e.getY()
										- component.getVerticalScrollBar()
												.getValue());
					} else if (cell.isEdge()) {
						edgePopup.setEdgeNodes(adapter
								.getCellToVertex((mxCell) cell.getSource()),
								(String) cell.getSource().getValue(), adapter
										.getCellToVertex((mxCell) cell
												.getTarget()), (String) cell
										.getTarget().getValue());
						edgePopup.show(component,
								e.getX()
										- component.getHorizontalScrollBar()
												.getValue(), e.getY()
										- component.getVerticalScrollBar()
												.getValue());
						System.out.println(component.getGraph().getView()
								.getState(cell).getAbsolutePoints().size());
					}
				}
			}
		} else if (e.getButton() == MouseEvent.BUTTON1) {
			Object o = component.getCellAt(e.getX(), e.getY());
			if (o != null) {
				if (o instanceof mxCell) {
					mxCell cell = (mxCell) o;
					if (cell.isVertex()) {
						for (GraphClass gc : adapter.getCellToVertex(cell)) {
							if (getNodeName(gc.toString()).equals(
									(String) cell.getValue())) {
								parent.classesList.setSelectedValue(gc, true);
								break;
							}
						}
					}
				}
			}
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

	protected mxIEventListener undoHandler = new mxIEventListener() {
		public void invoke(Object source, mxEventObject evt) {
			undoManager.undoableEditHappened((mxUndoableEdit) evt
					.getProperty("edit"));
			if (evt.getName().equals(mxEvent.UNDO)) {
				System.out.println("DO");
			}
		}
	};
	

	/**
	 * creates label for mxCells of JgraphX
	 * @param label String generated from Latex2JHtml
	 * @return label as String
	 */
	static String createLabel(String label) {

		String temp = "";
		boolean co = false;
		int count = 0;

		for (int i = 0; i < label.length(); i++) {
			// temp += label.charAt(i);
			// temp += "_";

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

					// temp += "<sub>";
					// temp += "<span style=\"text-decoration:underlined\">";

					/*
					 * if(count > 12){ temp +=
					 * "&#160&#160&#160&#160&#160<sub>&#160</sub>"; count -= 5;
					 * } else if(count > 7){ temp += "&#160&#160&#160"; count -=
					 * 3; }
					 */

					co = true;
				}
				/*
				 * else { temp = temp.substring(0, temp.length()-1); temp +=
				 * "l"; }
				 */
			}

			if (co && label.charAt(i) == ')') {
				// if(co){
				// temp = temp.substring(0,temp.length()-1);
				// temp += "</span>";
				temp = temp.substring(0, temp.length() - 5);
				co = false;
				// }
				/*
				 * else{ temp = temp.substring(0, temp.length()-1); temp += "l";
				 * }
				 */
			}

		}

		/*
		 * if(co){ temp += "</span>"; co = false; }
		 */

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

	public String getNodeName(String gcn) {
		return createLabel(converter.html(Utility.getShortName(gcn)));
	}

	public JGraphTXAdapter<Set<GraphClass>, DefaultEdge> getAdapter() {
		return adapter;
	}
}
