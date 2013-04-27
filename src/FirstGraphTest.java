import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;


public class FirstGraphTest {

	public static void main(String[] args) {
		DirectedGraph<String, org.jgrapht.graph.DefaultEdge> g = new SimpleDirectedGraph<>(DefaultEdge.class);
		String v1 = "HEAD";
		String v2 = "master";
		String v3 = "testing";
		String v4 = "ds3a";		
		
		g.addVertex(v1);
		g.addVertex(v2);
		g.addVertex(v3);
		g.addVertex(v4);
		
		g.addEdge(v1, v2);
		g.addEdge(v2, v4);
		g.addEdge(v3, v4);
		
		System.out.println(g);
	}
}
