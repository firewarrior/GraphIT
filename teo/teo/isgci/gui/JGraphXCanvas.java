package teo.isgci.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.db.Algo.NamePref;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.BFSWalker;
import teo.isgci.grapht.GraphWalker;
import teo.isgci.grapht.Inclusion;
import teo.isgci.grapht.RevBFSWalker;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;
import teo.isgci.util.JGraphTXAdapter;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.LessLatex;
import teo.isgci.util.Utility;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.view.mxGraph;

public class JGraphXCanvas implements MouseListener, MouseWheelListener {
    
	/** JGraphX Components*/
	private mxGraphComponent component = new mxGraphComponent(new mxGraph());
	private JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;
	private mxHierarchicalLayout layout;

	/** ISGCI Components*/
	ISGCIMainFrame parent;
    private Algo.NamePref namingPref = Algo.NamePref.BASIC;
    private Problem problem;
    private Collection<GraphClass> classes;
    private List<String> vertexNames;
    
    private Latex2JHtml converter = new Latex2JHtml();
    
    /** Colours for different complexities */
    public static final Color COLOR_LIN = Color.green;
    public static final Color COLOR_P = Color.green.darker();
    public static final Color COLOR_NPC = Color.red;
    public static final Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    public static final Color COLOR_UNKNOWN = Color.white;

    private NodePopup nodePopup;
    private EdgePopup edgePopup;
    
    /*
     * Constructor
     */
	public JGraphXCanvas(ISGCIMainFrame parent) {
		this.parent = parent;
		
		component.addMouseListener(this);
		component.addMouseWheelListener(this);
		component.setPreferredSize(new Dimension(800, 600));
		component.setSize(component.getPreferredSize());
		component.setToolTips(true);
		component.getGraphControl().addMouseListener(this);
		
		nodePopup = new NodePopup(parent);
        edgePopup = new EdgePopup(parent);
        component.add(nodePopup);
        component.add(edgePopup);
	}
	
	/**
	 * Draws the Graph with the given classes as vertices
	 * @param classes
	 */
    public void drawGraph(Collection<GraphClass> classes) {
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g = Algo.createHierarchySubgraph(classes);
        this.classes = classes;
        setGraph(g);
        if(problem != null)
        	setComplexityColors();
    }

    /**
     * Sets alls attributes for the given graph and adds it to the canvas
     * @param graph to be drawn
     * @param edgeStyle Style for vertices
     * @param vertexStyle Style for edges
     */
    private void setGraph(Graph<Set<GraphClass>, DefaultEdge> graph, String edgeStyle, String vertexStyle) {
	    adapter = new JGraphTXAdapter<Set<GraphClass>, DefaultEdge>(graph, edgeStyle + ";noLabel=1", vertexStyle){
	    	
			@Override
			public String getToolTipForCell(Object arg0){
				if(arg0 instanceof mxCell){
					Latex2JHtml converter = new Latex2JHtml();				
					mxCell cell = (mxCell) arg0;
					
					if(cell.isVertex()){
						Set<GraphClass> gcs = this.getCellToVertex(cell);
						for(GraphClass gc : gcs){
							if(Utility.getShortName(converter.html(gc.toString())).equals((String)cell.getValue())){
								return "<html>"+converter.html(gc.toString())+"</html>";
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
	    component.setConnectable(false);
	    
	    setNamingPref(namingPref);
	    
	    component.setGraph(adapter);
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
    		for (Object o : adapter.getChildCells(adapter.getDefaultParent(), true, false)) {
    			if (o instanceof mxCell) {
    				mxCell cell = (mxCell) o;
    				cell.setValue(Utility.getShortName(converter.html(Algo.getName(adapter.getCellToVertex(cell), namingPref))));
    				adapter.updateCellSize(cell, true);
    			}
    		}
    	}
    	finally {
    		adapter.getModel().endUpdate();
    	}
    	
    	if(layout == null)
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
		adapter.getVertexToCell(view).setValue(converter.html(Utility.getShortName(fullname)));
		adapter.updateCellSize(adapter.getVertexToCell(view), true);
		adapter.refresh();
	}

	/**
	 * Searches for the given GraphClass in the actual graph
	 * @param selectedNode
	 * @return the cell of the node corresponding to the given GraphClass, otherwise null
	 */
	public mxCell findNode(GraphClass selectedNode) {
		for(Object o : adapter.getChildCells(adapter.getDefaultParent(), true, false)){
			if(o instanceof mxCell){
				mxCell v = (mxCell) o;
				if(adapter.getCellToVertex(v).contains(selectedNode)){
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
    	if(adapter != null){
    		if (problem != p) {
    			problem = p;
    			setComplexityColors();
    			adapter.refresh();
    		}
    	}
    }
    
	/*
	 * Sets the Color given by the problem for each node
	 */
	private void setComplexityColors() {
		if(problem == null){
			adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, null, 
					adapter.getChildCells(adapter.getDefaultParent(), true, false));
			return;
		}
		
		List<Object> unknown = new LinkedList<>();
		List<Object> linear = new LinkedList<>();
		List<Object> polynomial = new LinkedList<>();
		List<Object> intermediate = new LinkedList<>();
		List<Object> npcomplete = new LinkedList<>();
		
		Complexity tmp = Complexity.UNKNOWN;
		
		for(Object o : adapter.getChildCells(adapter.getDefaultParent(), true, false)){
			if(o instanceof mxCell){
				mxCell v = (mxCell) o;
				tmp = problem.getComplexity(adapter.getCellToVertex(v).iterator().next());
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
		adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxHtmlColor.getHexColorString(COLOR_UNKNOWN), unknown.toArray());
		adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxHtmlColor.getHexColorString(COLOR_LIN), linear.toArray());
		adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxHtmlColor.getHexColorString(COLOR_P), polynomial.toArray());
		adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxHtmlColor.getHexColorString(COLOR_INTERMEDIATE), intermediate.toArray());
		adapter.setCellStyles(mxConstants.STYLE_FILLCOLOR, mxHtmlColor.getHexColorString(COLOR_NPC), npcomplete.toArray());
	}
	
	
	/*Hide Sub and Superclasses*/
	
	/**
	 * Executes the hirarchical Layout on the actual Graph
	 */
	public void executeLayout() {
		adapter.getModel().beginUpdate();
		try{
			if(layout != null)
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
		if(classes != null)
			drawGraph(classes);
		parent.classesHandler.getDeactivated().clear();
    	parent.informationPanel.revalidate();
      	parent.informationPanel.repaint();
	}
	
	/**
	 * Hides all subclasses of the given node
	 * @param currNode
	 */
	public void hideSubClasses(Set<GraphClass> currNode){
        final Set<GraphClass> result = new HashSet<GraphClass>();
        GraphClass tmp = currNode.iterator().next();
		
		new BFSWalker<GraphClass,Inclusion>(DataSet.inclGraph,
                tmp, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
            	result.add(v);
                super.visit(v);
            }
        }.run();
        
        final Set<mxCell> nodes = new HashSet<mxCell>();
		List<GraphClass> names = new LinkedList<>();
        for(GraphClass gc : result) {
        	if(currNode.contains(gc))
        		continue;
        	nodes.add(findNode(gc));
        	names.add(gc);
        }
        
        adapter.getModel().beginUpdate();
        try{
        	adapter.setCellsDeletable(true);
            adapter.removeCells(nodes.toArray());
        } finally {
        	adapter.setCellsDeletable(false);
        	adapter.getModel().endUpdate();
        }
        component.refresh();
        
      //Load information into informationbar
    	if (!names.isEmpty()) {
    		parent.classesHandler.addDeactivated(names);
    	}
    	parent.informationPanel.revalidate();
      	parent.informationPanel.repaint();
	}
	
	/**
	 * hides all superclasses of the given node
	 * @param currNode
	 */
	public void hideSuperClasses(Set<GraphClass> currNode){
		final Set<GraphClass> result = new HashSet<GraphClass>();
        GraphClass tmp = currNode.iterator().next();
		
        new RevBFSWalker<GraphClass,Inclusion>( DataSet.inclGraph,
                tmp, null, GraphWalker.InitCode.DYNAMIC) {
            public void visit(GraphClass v) {
                result.add(v);
                super.visit(v);
            }
        }.run();
        
        final Set<mxCell> nodes = new HashSet<mxCell>();
		List<GraphClass> names = new LinkedList<>();
        for(GraphClass gc : result) {
        	if(currNode.contains(gc))
        		continue;
        	nodes.add(findNode(gc));
           	names.add(gc);
        }
        
        adapter.getModel().beginUpdate();
        try{
        	adapter.setCellsDeletable(true);
            adapter.removeCells(nodes.toArray());
        } finally {
        	adapter.setCellsDeletable(false);
        	adapter.getModel().endUpdate();
        }
        component.refresh();
        
        //Load information into informationbar
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
    
    public List<GraphClass> getGraphClassList(){
    	Set<GraphClass> gcl = new HashSet<GraphClass>();
    	for(GraphClass gc : classes) {
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
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON3){
			Object o = component.getCellAt(e.getX(), e.getY());
			if(o != null){
				if(o instanceof mxCell){
					mxCell cell = (mxCell) o;
					if(cell.isVertex()){
						nodePopup.setNode(adapter.getCellToVertex(cell), (String) cell.getValue());
						nodePopup.show(component, e.getX() - component.getHorizontalScrollBar().getValue(), e.getY() - component.getVerticalScrollBar().getValue());
						System.out.println(cell.getValue());
					}
					else if(cell.isEdge()){
						edgePopup.setEdgeNodes(adapter.getCellToVertex((mxCell)cell.getSource()), (String) cell.getSource().getValue(), adapter.getCellToVertex((mxCell) cell.getTarget()), (String) cell.getTarget().getValue());
						edgePopup.show(component, e.getX() - component.getHorizontalScrollBar().getValue(), e.getY() - component.getVerticalScrollBar().getValue());
					}
				}
			}
		}
		else if(e.getButton() == MouseEvent.BUTTON1){
			Object o = component.getCellAt(e.getX(), e.getY());
			System.out.println(o);
			if(o != null){
				if(o instanceof mxCell){
					mxCell cell = (mxCell) o;
					if(cell.isVertex()){
						for(GraphClass gc : adapter.getCellToVertex(cell)){
							if(Utility.getShortName(converter.html(gc.toString())).equals((String)cell.getValue())){
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
