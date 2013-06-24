import static org.junit.Assert.assertEquals;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;
import org.junit.Test;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class GraphUnitTest {
	
	Comparator<DefaultEdge> comparator = new compDefaultEdge();

	public ListenableGraph<String, DefaultEdge> initTestGraph() {

		ListenableGraph<String, DefaultEdge> graph = new ListenableDirectedGraph<String, DefaultEdge>(
				DefaultEdge.class);
		String s1 = "0,2 Colorable";
		String s2 = "PURE-2-DIR";
		String s3 = "cubical";
		String s4 = "modular";
		String s5 = "tree convex";
		String s6 = "star convex";
		String s7 = "triad convex";

		graph.addVertex(s1);
		graph.addVertex(s2);
		graph.addVertex(s3);
		graph.addVertex(s4);
		graph.addVertex(s5);
		graph.addVertex(s6);
		graph.addVertex(s7);

		graph.addEdge(s1, s2);
		graph.addEdge(s1, s3);
		graph.addEdge(s1, s4);
		graph.addEdge(s1, s5);
		graph.addEdge(s5, s6);
		graph.addEdge(s5, s7);

		return graph;

	}

	/**
	 * Test whether the translation from JGraphT to JGraphX works without loss
	 * of information
	 */
	@Test
	public void translationTest() {
		ListenableGraph<String, DefaultEdge> graphT = initTestGraph();
		JGraphTXAdapter<String, DefaultEdge> graphX = new JGraphTXAdapter<String, DefaultEdge>(
				graphT, "startArrow=none;endArrow=none",
				"shape=triangle;perimeter=trianglePerimeter");
		assertEquals(vertexSet(graphX).toString(),
				new TreeSet<String>(graphT.vertexSet()).toString());
		Set<DefaultEdge> edgesT = new TreeSet<DefaultEdge>(comparator);
		edgesT.addAll(graphT.edgeSet());
		assertEquals(edgesT.toString(), edgeSet(graphX).toString());
	}

	/**
	 * Test whether
	 */
	@Test
	public void graphXVertexDeletionTest() {
		ListenableGraph<String, DefaultEdge> graphT = initTestGraph();
		JGraphTXAdapter<String, DefaultEdge> graphX = new JGraphTXAdapter<String, DefaultEdge>(
				graphT, "noLabel=1",
				"shape=triangle;perimeter=trianglePerimeter");
		graphX.getModel().beginUpdate();
		try {
			graphX.getModel().remove("cubical"); // Test if deletion in JGraphX
													// also occurs in JGraphT
		} finally {
			graphX.getModel().endUpdate();
		}
		assertEquals(vertexSet(graphX).toString(),
				new TreeSet<String>(graphT.vertexSet()).toString());
		Set<DefaultEdge> edgesT = new TreeSet<DefaultEdge>(comparator);
		edgesT.addAll(graphT.edgeSet());
		assertEquals(edgesT.toString(), edgeSet(graphX).toString());
	}
	
	@Test
	public void graphTVertexDeletionTest(){
		ListenableGraph<String, DefaultEdge> graphT = initTestGraph();
		JGraphTXAdapter<String, DefaultEdge> graphX = new JGraphTXAdapter<String, DefaultEdge>(
				graphT, "noLabel=1",
				"shape=triangle;perimeter=trianglePerimeter");
		graphT.removeVertex("modular"); // Test if deletion in JGraphT also
										// occurs in JGraphX
		assertEquals(vertexSet(graphX).toString(),
				new TreeSet<String>(graphT.vertexSet()).toString());
		Set<DefaultEdge> edgesT = new TreeSet<DefaultEdge>(comparator);
		edgesT.addAll(graphT.edgeSet());
		assertEquals(edgesT.toString(), edgeSet(graphX).toString());
	}

	@Test
	public void graphXVertexAdditionTest() {
		ListenableGraph<String, DefaultEdge> graphT = initTestGraph();
		JGraphTXAdapter<String, DefaultEdge> graphX = new JGraphTXAdapter<String, DefaultEdge>(
				graphT, "noLabel=1",
				"shape=triangle;perimeter=trianglePerimeter");
		graphX.getModel().beginUpdate();
		try {
			graphX.insertVertex(graphX.getDefaultParent(), null, "fooBar", 10, 10,
					10, 0); // Test if addition in JGraphX also occurs in
							// JgraphT
		} finally {
			graphX.getModel().endUpdate();
		}
		assertEquals(vertexSet(graphX).toString(),
				new TreeSet<String>(graphT.vertexSet()).toString());
		Set<DefaultEdge> edgesT = new TreeSet<DefaultEdge>(comparator);
		edgesT.addAll(graphT.edgeSet());
		assertEquals(edgesT.toString(), edgeSet(graphX).toString());
	}
	
	@Test
	public void graphTVertexAdditionTest(){
		ListenableGraph<String, DefaultEdge> graphT = initTestGraph();
		JGraphTXAdapter<String, DefaultEdge> graphX = new JGraphTXAdapter<String, DefaultEdge>(
				graphT, "noLabel=1",
				"shape=triangle;perimeter=trianglePerimeter");
		graphT.addVertex("foo"); // Test if addition in JGraphT also occurs in
		// JGraphX
		assertEquals(vertexSet(graphX).toString(),
				new TreeSet<String>(graphT.vertexSet()).toString());
		Set<DefaultEdge> edgesT = new TreeSet<DefaultEdge>(comparator);
		edgesT.addAll(graphT.edgeSet());
		assertEquals(edgesT.toString(), edgeSet(graphX).toString());
	}
	
	@Test
	public void graphXEdgeAdditionTest(){
		ListenableGraph<String, DefaultEdge> graphT = initTestGraph();
		JGraphTXAdapter<String, DefaultEdge> graphX = new JGraphTXAdapter<String, DefaultEdge>(
				graphT, "noLabel=1",
				"shape=triangle;perimeter=trianglePerimeter");
		graphX.getModel().beginUpdate();
		try {
			mxCell modular = graphX.getVertexToCell("modular");
			mxCell cubical = graphX.getVertexToCell("cubical");
			graphX.insertEdge(graphX.getDefaultParent(), null, "", modular, cubical);
		} finally {
			graphX.getModel().endUpdate();
		}
		assertEquals(vertexSet(graphX).toString(),
				new TreeSet<String>(graphT.vertexSet()).toString());
		Set<DefaultEdge> edgesT = new TreeSet<DefaultEdge>(comparator);
		edgesT.addAll(graphT.edgeSet());
		assertEquals(edgesT.toString(), edgeSet(graphX).toString());
	}
	
	@Test
	public void graphTEdgeAdditionTest(){
		ListenableGraph<String, DefaultEdge> graphT = initTestGraph();
		JGraphTXAdapter<String, DefaultEdge> graphX = new JGraphTXAdapter<String, DefaultEdge>(
				graphT, "noLabel=1",
				"shape=triangle;perimeter=trianglePerimeter");
		graphT.addEdge("modular", "cubical");
		assertEquals(vertexSet(graphX).toString(),
				new TreeSet<String>(graphT.vertexSet()).toString());
		Set<DefaultEdge> edgesT = new TreeSet<DefaultEdge>(comparator);
		edgesT.addAll(graphT.edgeSet());
		assertEquals(edgesT.toString(), edgeSet(graphX).toString());
	}
	

	/**
	 * return all vertices
	 */
	private Set<Object> vertexSet(mxGraph g) {
		Set<Object> vertices = new TreeSet<Object>();
		for (Object vertex : g.getChildCells(g.getDefaultParent(), true, false)) {
			vertices.add(((mxCell) vertex).getValue());
		}
		return vertices;
	}

	/**
	 * return all edges
	 */
	private Set<DefaultEdge> edgeSet(mxGraph g) {
		Set<DefaultEdge> edges = new TreeSet<DefaultEdge>(comparator);
		for (Object edge : g.getChildCells(g.getDefaultParent(), false, true)) {
			edges.add((DefaultEdge) (((mxCell) edge).getValue()));
		}
		return edges;
	}
	
	private class compDefaultEdge implements Comparator<DefaultEdge> {

		@Override
		public int compare(DefaultEdge o1, DefaultEdge o2) {
			return o1.toString().compareTo(o2.toString());
		}

			
	}

}
