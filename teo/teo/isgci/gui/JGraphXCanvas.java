package teo.isgci.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import teo.isgci.db.Algo;
import teo.isgci.db.Algo.NamePref;
import teo.isgci.gc.GraphClass;
import teo.isgci.problem.Problem;
import teo.isgci.util.JGraphTXAdapter;
import teo.isgci.util.Latex2JHtml;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class JGraphXCanvas implements MouseListener, MouseWheelListener {
    
    private mxGraphComponent component = new mxGraphComponent(new mxGraph());
    private JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;
    private Algo.NamePref namingPref = Algo.NamePref.BASIC;
    private Problem problem;
    private Set<Set<GraphClass>> vertexes;

	public JGraphXCanvas() {
		component.addMouseListener(this);
		component.addMouseWheelListener(this);
	}
	
	public void setProblem(Problem problem)  {
	    this.problem = problem;
	}
	
	/**
     * Set all nodes to their prefered names and writes the Latex-Label
     */
    public void setNamingPref(NamePref pref) {
    	Latex2JHtml converter = new Latex2JHtml();
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
	    adapter.setHtmlLabels(true);
	    component.setConnectable(false);
	    setNamingPref(namingPref);
	    component.setGraph(adapter);
	    vertexes = graph.vertexSet();
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
	
	public List<String> getNames() {
        List<String> result = new ArrayList<String>();
        for (Set<GraphClass> ver : vertexes) {
            for (GraphClass gc : ver)
                    result.add(gc.toString());
        }
        return result;
    }

}
