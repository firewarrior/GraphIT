package teo.isgci.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.db.Algo;
import teo.isgci.db.Algo.NamePref;
import teo.isgci.gc.GraphClass;
import teo.isgci.problem.Complexity;
import teo.isgci.problem.Problem;
import teo.isgci.util.JGraphTXAdapter;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.Utility;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.view.mxGraph;

public class JGraphXCanvas implements MouseListener, MouseWheelListener {
    
	private mxGraphComponent component = new mxGraphComponent(new mxGraph());
	private JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;
    private Algo.NamePref namingPref = Algo.NamePref.BASIC;
    private Problem problem;
    private Latex2JHtml converter = new Latex2JHtml();
    private List<String> vertexNames;
    private List<GraphClass> graphClassList;
    
    /** Colours for different complexities */
    public static final Color COLOR_LIN = Color.green;
    public static final Color COLOR_P = Color.green.darker();
    public static final Color COLOR_NPC = Color.red;
    public static final Color COLOR_INTERMEDIATE = SColor.brighter(Color.red);
    public static final Color COLOR_UNKNOWN = Color.white;

    
    private NodePopup nodePopup;
    private EdgePopup edgePopup;
    
	public JGraphXCanvas(ISGCIMainFrame parent) {
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
        graphClassList = new ArrayList<GraphClass>(classes);
        setGraph(g);
    }

    /**
     * Sets alls attributes for the given graph and adds it to the canvas
     * @param graph to be drawn
     * @param edgeStyle Style for vertices
     * @param vertexStyle Style for edges
     */
    public void setGraph(Graph<Set<GraphClass>, DefaultEdge> graph, String edgeStyle, String vertexStyle) {
	    adapter = new JGraphTXAdapter<Set<GraphClass>, DefaultEdge>(graph, edgeStyle + ";noLabel=1", vertexStyle);
	    
	    adapter.setCellsMovable(false);
	    adapter.setCellsDeletable(false);
	    adapter.setCellsResizable(true);
	    adapter.setAutoSizeCells(true);
	    adapter.setCellsDisconnectable(false);
	    adapter.setCellsSelectable(true);
	    adapter.setConnectableEdges(false);
	    adapter.setAllowDanglingEdges(false);
	    adapter.setCellsEditable(false);
	    adapter.setHtmlLabels(true);
	    component.setConnectable(false);
	    
	    vertexNames = safeNames(graph.vertexSet());
	    setNamingPref(namingPref);
	    
	    component.setGraph(adapter);
	}
    
    /**
     * Sets alls attributes for the given graph and adds it to the canvas
     */
    public void setGraph(Graph<Set<GraphClass>, DefaultEdge> graph) {
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
    				cell.setValue(converter.html(Algo.getName(adapter.getCellToVertex(cell), namingPref)));
    				adapter.updateCellSize(cell, true);
    			}
    		}
    	}
    	finally {
    		adapter.getModel().endUpdate();
    	}
    	
    	mxHierarchicalLayout layout = new mxHierarchicalLayout(adapter);
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
	

	/* Getter Methodes */

    public mxGraphComponent getComponent() {
    	return component;
    }
    
    public Algo.NamePref getNamingPref() {
    	return namingPref;
    }
    
    public List<GraphClass> getGraphClassList(){
    	return graphClassList;
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
						nodePopup.show(component, e.getX(), e.getY());
						System.out.println(cell.getValue());
					}
					else if(cell.isEdge()){
						edgePopup.setEdgeNodes(adapter.getCellToVertex((mxCell)cell.getSource()), (String) cell.getSource().getValue(), adapter.getCellToVertex((mxCell) cell.getTarget()), (String) cell.getTarget().getValue());
						edgePopup.show(component, e.getX(), e.getY());
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
	
	/*
	 * Renames the vertex with the given name
	 */
	public void renameNode(Set<GraphClass> view, String fullname) {
		adapter.getVertexToCell(view).setValue(converter.html(Utility.getShortName(fullname)));
		adapter.updateCellSize(adapter.getVertexToCell(view), true);
		adapter.refresh();
		
	}
}
