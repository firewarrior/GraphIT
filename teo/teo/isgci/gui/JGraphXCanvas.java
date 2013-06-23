package teo.isgci.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.db.Algo;
import teo.isgci.db.Algo.NamePref;
import teo.isgci.gc.GraphClass;
import teo.isgci.problem.Problem;
import teo.isgci.util.JGraphTXAdapter;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class JGraphXCanvas implements MouseListener, MouseWheelListener {
    
    private mxGraphComponent component = new mxGraphComponent(new mxGraph());
    private JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;
    private Algo.NamePref namingPref = Algo.NamePref.BASIC;
    private Problem problem;

	public JGraphXCanvas() {
		component.addMouseListener(this);
		component.addMouseWheelListener(this);
	}
	
	public void setProblem(Problem problem)  {
	    this.problem = problem;
	}
	
	/**
     * Set all nodes to their prefered names.
     */
    public void setNamingPref(NamePref pref) {
    	namingPref = pref;
    	adapter.getModel().beginUpdate();
    	try {
    		for (Object o : adapter.getChildCells(adapter.getDefaultParent(), true, false)) {
    			if (o instanceof mxCell) {
    				mxCell cell = (mxCell) o;
    				cell.setValue(Algo.getName(adapter.getCellToVertex(cell), namingPref));
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
	
	public mxGraphComponent getComponent() {
        return component;
    }
	
	public Algo.NamePref getNamingPref() {
		return namingPref;
	}
    
    public void drawGraph(Collection<GraphClass> classes) {
        SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> g = Algo.createHierarchySubgraph(classes);
        setGraph(g);
    }
	
    public void setGraph(Graph<Set<GraphClass>, DefaultEdge> graph) {
    	setGraph(graph, null, null);
    }

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
	    component.setConnectable(false);
	    setNamingPref(namingPref);
	    component.setGraph(adapter);
	}

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
		// TODO Auto-generated method stub
		
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
