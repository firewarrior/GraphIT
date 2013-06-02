import java.util.HashMap;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;

public class JGraphTXAdapter<V, E> extends mxGraph implements
		GraphListener<V, E>, mxIEventListener {

	private Graph<V, E> graphT;

	private String edgeStyle, vertexStyle;
	
	private Map<V, mxCell> vertexToCell = new HashMap<V, mxCell>();
	private Map<mxCell, V> cellToVertex = new HashMap<mxCell, V>();

	private Map<E, mxCell> edgeToCell = new HashMap<E, mxCell>();
	private Map<mxCell, E> cellToEdge = new HashMap<mxCell, E>();

	public JGraphTXAdapter(Graph<V, E> graphT) {
		this(graphT, null, null);
	}
	
	public JGraphTXAdapter(Graph<V, E> graphT, String edgeStyle, String vertexStyle) {
		this.graphT = graphT;
		this.edgeStyle = edgeStyle;
		this.vertexStyle = vertexStyle;
		addListener(mxEvent.ADD_CELLS, this);
		addListener(mxEvent.REMOVE_CELLS, this);
		if (graphT instanceof ListenableGraph<?, ?>) {
			((ListenableGraph<V, E>) graphT).addGraphListener(this);
		}
		insertGraph();
	}

	private void insertGraph() {
		getModel().beginUpdate();
		try {
			for (V vertex : graphT.vertexSet()) {
				addGraphTVertex(vertex);
			}
			for (E edge : graphT.edgeSet()) {
				addGraphTEdge(edge);
			}
		} finally {
			getModel().endUpdate();
		}

	}

	private void addGraphTEdge(E edge) {
		V source = graphT.getEdgeSource(edge);
		V target = graphT.getEdgeTarget(edge);

	}

	private void addGraphTVertex(V vertex) {

	}

	@Override
	public void vertexAdded(GraphVertexChangeEvent<V> e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vertexRemoved(GraphVertexChangeEvent<V> e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void edgeAdded(GraphEdgeChangeEvent<V, E> e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void edgeRemoved(GraphEdgeChangeEvent<V, E> e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void invoke(Object sender, mxEventObject evt) {
		if (evt.getName().equals(mxEvent.ADD_CELLS)) {

		}
		if (evt.getName().equals(mxEvent.REMOVE_CELLS)) {

		}

	}

}
