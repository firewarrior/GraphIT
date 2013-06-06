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

/**
 * Offers compatibility between a {@link org.jgrapht.Graph} and an
 * {@link com.mxgraph.view.mxGraph}. Propagates changes (adding and deleting
 * vertices and edges) from the mxGraph to the Graph. If the Graph is also an
 * instance of ListenableGraph, changes from the Graph will also be propagated
 * to the mxGraph.
 * 
 * 
 * @author GraphIT
 * 
 * @param <V>
 *            type of the vertices of the corresponding
 *            {@link org.jgrapht.Graph}
 * @param <E>
 *            type of the edges of the corresponding {@link org.jgrapht.Graph}
 * 
 * @since 1.6
 * 
 */
public class JGraphTXAdapter<V, E> extends mxGraph implements
		GraphListener<V, E>, mxIEventListener {

	/**
	 * the corresponding Graph
	 */
	private Graph<V, E> graphT;

	/**
	 * optional style information for vertices and edges in the mxGraph
	 */
	private String edgeStyle, vertexStyle;

	/**
	 * maps from the Graph vertices to the mxCells of the mxGraph and vice versa
	 */
	private Map<V, mxCell> vertexToCell = new HashMap<V, mxCell>();
	private Map<mxCell, V> cellToVertex = new HashMap<mxCell, V>();

	/**
	 * maps from the Graph edges to the mxCells of the mxGraph and vice versa
	 */
	private Map<E, mxCell> edgeToCell = new HashMap<E, mxCell>();
	private Map<mxCell, E> cellToEdge = new HashMap<mxCell, E>();

	/**
	 * simple constructor
	 * 
	 * @param graphT
	 *            the Graph to describe as an mxGraph
	 */
	public JGraphTXAdapter(Graph<V, E> graphT) {
		this(graphT, null, null);
	}

	/**
	 * constructor with optional style information for vertices and edges
	 * 
	 * @param graphT
	 *            the Graph to describe as an mxGraph
	 * @param edgeStyle
	 *            optional style information for the mxGraph edges.
	 *            <code>null</code> if no style is desired
	 * @param vertexStyle
	 *            optional style information for the mxGraph vertices.
	 *            <code>null</code> if no style is desired
	 */
	public JGraphTXAdapter(Graph<V, E> graphT, String edgeStyle,
			String vertexStyle) {
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

	/**
	 * adds all vertices and edges of the Graph to the mxGraph
	 */
	private void insertGraph() {
		model.beginUpdate();
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

	/**
	 * adds an edge from the corresponding Graph to the mxGraph
	 * 
	 * @param edge
	 *            to be added
	 */
	private void addGraphTEdge(E edge) {
		model.beginUpdate();
		try {
			V source = graphT.getEdgeSource(edge);
			V target = graphT.getEdgeTarget(edge);
			insertEdge(defaultParent, null, edge, edgeToCell.get(source), edgeToCell.get(target), edgeStyle);
		} finally {
			model.endUpdate();
		}
	}

	/**
	 * adds a vertex from the corresponding Graph to the mxGraph
	 * 
	 * @param vertex
	 *            to be added
	 */
	private void addGraphTVertex(V vertex) {
		model.beginUpdate();
		try {
			insertVertex(defaultParent, null, vertex, Math.random() * 100, Math.random() * 100, 20, 20, null, true);
		}
		finally {
			model.endUpdate();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void vertexAdded(GraphVertexChangeEvent<V> e) {
		addGraphTVertex(e.getVertex());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void vertexRemoved(GraphVertexChangeEvent<V> e) {
		mxCell cell = vertexToCell.remove(e.getVertex());
		cellToVertex.remove(cell);
		removeCells(new Object[] { cell });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void edgeAdded(GraphEdgeChangeEvent<V, E> e) {
		addGraphTEdge(e.getEdge());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void edgeRemoved(GraphEdgeChangeEvent<V, E> e) {
		mxCell cell = edgeToCell.remove(e.getEdge());
		cellToEdge.remove(cell);
		removeCells(new Object[] { cell });
	}

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void invoke(Object sender, mxEventObject evt) {
		Object[] cells = (Object[]) evt.getProperty("cells");
		if (evt.getName().equals(mxEvent.ADD_CELLS)) {
			for (Object o : cells) {
				if (o instanceof mxCell) {
					mxCell cell = (mxCell) o;
					if (cell.isEdge()) {
						edgeToCell.put((E) cell.getValue(), cell);
						cellToEdge.put(cell, (E) cell.getValue());
					} else if (cell.isVertex()) {
						vertexToCell.put((V) cell.getValue(), cell);
						cellToVertex.put(cell, (V) cell.getValue());
					}
				}
			}
		}
		if (evt.getName().equals(mxEvent.REMOVE_CELLS)) {
			for (Object o : cells) {
				if (o instanceof mxCell) {
					mxCell cell = (mxCell) o;
					if (cell.isEdge()) {
						edgeToCell.remove(cellToEdge.remove(cell));
					} else if (cell.isVertex()) {
						vertexToCell.remove(cellToVertex.remove(cell));
					}
				}
			}
		}

	}

}