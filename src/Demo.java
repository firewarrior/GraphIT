import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.jgrapht.ListenableGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.ListenableDirectedGraph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;

public class Demo {

    public static void main(String[] args) throws InterruptedException {
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

//        System.out.println(graph.vertexSet());
//        System.out.println(graph.edgeSet());

        JGraphTXAdapter<String, DefaultEdge> g = new JGraphTXAdapter<String, DefaultEdge>(
                graph, "noLabel=1",
                "shape=ellipse;perimeter=ellipsePerimeter");

        g.setCellsResizable(true);
        g.setCellsEditable(true);
        g.setCellsMovable(true);
        g.setAutoSizeCells(true);
        g.setCellsDeletable(true);
        for (Object vertex : g
                .getChildCells(g.getDefaultParent(), true, false)) {
            g.updateCellSize(vertex, true);
        }

//        for (Object vertex : g
//                .getChildCells(g.getDefaultParent(), true, false)) {
//            System.out.println(((mxCell) vertex).getValue());
//        }
//        System.out.println("test");

//        for (Object vertex : g
//                .getChildCells(g.getDefaultParent(), false, true)) {
//            System.out.println("edges");
//            System.out.println(((mxCell) vertex).getSource() + " "
//                    + ((mxCell) vertex).getTarget());
//        }

        mxHierarchicalLayout layout = new mxHierarchicalLayout(g);
        layout.execute(g.getDefaultParent());

        mxGraphComponent component = new mxGraphComponent(g);
        // component.zoom(15);

        JFrame frame = new JFrame("JGraphX Demo");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(component);
        frame.setSize(800, 600);
        frame.setVisible(true);

        Thread.sleep(5000);

        graph.addVertex("Test");
        Thread.sleep(2000);
        
        graph.addEdge(s7, "Test");
        for (Object vertex : g.getChildCells(g.getDefaultParent(), true, false)) {
        	g.updateCellSize(vertex, true);
        }
        layout.execute(g.getDefaultParent());
        Thread.sleep(2000);
       
        g.removeCells(new Object[]{g.getVertexToCell(s3)});
        Thread.sleep(2000);
        
        g.removeCells(new Object[]{g.getEdgeToCell(graph.getEdge(s1, s2))});
        Thread.sleep(2000);
        
        g.getModel().beginUpdate();
        try{
        graph.removeVertex(s5);
        } finally{
        	g.getModel().endUpdate();
        }
        Thread.sleep(2000);
        
        graph.removeEdge(graph.getEdge(s1, s4));
        Thread.sleep(2000);
         
        layout.execute(g.getDefaultParent());
        System.out.println("Ende");
    }
}
