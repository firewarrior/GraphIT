import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

public class FirstGraphTest {

    public static void main(String[] args) {
        ListenableGraph<String, DefaultEdge> g = new ListenableDirectedGraph<String, DefaultEdge>(
                DefaultEdge.class);
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

        // JGraphXModelAdapter<String, DefaultEdge> graph = new
        // JGraphXModelAdapter<>(g);
        // graph.setAutoSizeCells(true);
        // graph.setCellsResizable(true);
        // for (Object o : graph.getChildCells(graph.getDefaultParent(), true,
        // false)) {
        // graph.updateCellSize(o, true);
        // }

        // mxHierarchicalLayout m = new mxHierarchicalLayout(graph);
        // m.execute(graph.getDefaultParent());
        // mxGraphComponent graphComponent = new mxGraphComponent(graph);
        // JFrame frame = new JFrame("First test");
        // frame.getContentPane().add(graphComponent);
        // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // frame.pack();
        // frame.setVisible(true);
    }
}
