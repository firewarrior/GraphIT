package teo.isgci.gui;

import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import teo.isgci.db.Algo;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.GAlg;
import teo.isgci.grapht.Inclusion;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.Utility;

import com.mxGraph.adapter.mxJGraphTAdapter;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

/**
 * Realizes export functionality of the currently displayed mxGraph into a
 * scalable vector graphic file.
 * 
 * @author Fabian Brosda, Thorsten Breitkreutz, Cristiana Grigoriu, Moritz
 *         Heine, Florian Krönert, Thorsten Sauter, Christian Stohr
 * 
 */
public class SVGExport {

    private mxJGraphTAdapter<Set<GraphClass>, DefaultEdge> adapter;
    private mxGraph graph;
    private LatexGraphics latexgraphics;
    private String svg;
    private SVGGraphics svggraphics;
    private boolean shortLabels, relayout;

    /**
     * Constructor
     * 
     * @param adapter
     *            for JGraphT to JGraphX
     * @param c
     * @param shortLa
     */
    public SVGExport(mxJGraphTAdapter<Set<GraphClass>, DefaultEdge> adapter,
            boolean shortLabels, boolean relayout) {
        this.adapter = adapter;
        this.shortLabels = shortLabels;
        this.relayout = relayout;
        svggraphics = new SVGGraphics();
    }

    /**
     * Creates an string using xml-code to define a svg-file
     * 
     * @return svg-string
     */
    public String createExportString() {
        if (adapter == null) {
            return "";
        }

        latexgraphics = new LatexGraphics();
        svg = "";
        Graphics graphics = svggraphics.create();

        copyGraph();

        int width = (int) graph.getGraphBounds().getWidth();
        int height = (int) graph.getGraphBounds().getHeight();

        graphics.setClip(new Rectangle(width, height));
        graphics.setFont(latexgraphics.getFont());

        for (Object o : graph.getChildCells(graph.getDefaultParent())) {
            if (o instanceof mxCell) {
                mxCell cell = (mxCell) o;
                if (cell.isVertex()) {
                    drawLabel(cell, graphics);
                    addNode(cell, width);

                } else {
                    addEdge(cell);
                }
            }
        }

        return merge();
    }

    /* creates a copy of the graph including full labels */

    private void copyGraph() {
        graph = new mxGraph();
        graph.addCells(adapter.cloneCells(adapter.getChildCells(adapter
                .getDefaultParent())));
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);

        graph.getModel().beginUpdate();

        for (Object o : graph.getChildCells(graph.getDefaultParent())) {
            if (o instanceof mxCell) {
                mxCell cell = (mxCell) o;
                if (cell.isVertex()) {
                    updateNodeLabel(cell);

                    /* for updating cells width */

                    Graphics g = svggraphics.create();

                    g.setFont(latexgraphics.getFont());

                    int width = latexgraphics.getLatexWidth(g,
                            (String) cell.getValue());

                    cell.getGeometry().setWidth(width);
                }
            }
        }

        graph.getModel().endUpdate();

        if (relayout) {
            layout.setInterRankCellSpacing(150);
            layout.execute(graph.getDefaultParent());
            graph.refresh();
        }

    }

    /* merges the labels and the graph to one string */

    private String merge() {
        String temp = svggraphics.getContent();
        String test = "";
        for (int i = 0; i < temp.length(); i++) {
            test += temp.charAt(i);
            if (test.endsWith("</desc>")) {
                temp = test + svg + temp.substring(i, temp.length());
                break;
            }
        }
        return temp;
    }

    /* adds node to SVG-String including color */

    private void addNode(mxCell cell, int width) {
        mxGeometry geo = cell.getGeometry();
        graph.getCellStyle(cell);
        String color = (String) graph.getCellStyle(cell).get(
                mxConstants.STYLE_FILLCOLOR);
        if (!color.startsWith("#")) {
            color = "#" + color.substring(2, color.length());
        }

        svg += "<rect x=\"" + geo.getX() + "\" y=\"" + geo.getY()
                + "\" width=\"" + (geo.getWidth() + 10) + "\" height=\""
                + (geo.getHeight() + 5) + "\"  fill=\"" + color
                + "\" stroke=\"black\"/>\n";
    }

    /* draws the label for the svg-file */

    private void drawLabel(mxCell cell, Graphics g) {
        mxGeometry geo = cell.getGeometry();

        latexgraphics.drawLatexString(g, (String) cell.getValue(),
                (int) geo.getX() + 5, (int) (geo.getY() + geo.getHeight() * 1
                        / 2 + 8));
    }

    /* adds edges to SVG-String */

    private void addEdge(mxCell cell) {

        mxCell cell2 = null;
        for (Object o : adapter.getChildCells(adapter.getDefaultParent())) {
            if (o instanceof mxCell) {
                cell2 = (mxCell) o;
                if (cell.isEdge()) {
                    if (cell.getValue().equals(cell2.getValue())) {
                        break;
                    }
                }
            }
        }

        boolean first = true;
        svg += "<path d=\"";
        for (mxPoint point : graph.getView().getState(cell)
                .getAbsolutePoints()) {
            if (first) {
                svg += "M " + point.getX() + "," + (point.getY() + 5);
                first = false;
            } else {
                svg += " L " + point.getX() + "," + point.getY();
            }

        }
        if (!getProperness(cell2)) {
            svg += "\" fill=\"none\" stroke=\"black\" marker-end=\"url(#arrow)\" marker-start=\"url(#unproper)\" /> \n";
            return;
        }

        svg += "\" fill=\"none\" stroke=\"black\" marker-end=\"url(#arrow)\" /> \n";

    }

    /* updates node-labels to get rid of shortened labels */

    private void updateNodeLabel(mxCell cell) {
        Latex2JHtml converter = new Latex2JHtml();
        mxCell cell2 = null;
        for (Object o : adapter.getChildCells(adapter.getDefaultParent())) {
            if (o instanceof mxCell) {
                cell2 = (mxCell) o;
                if (cell.isVertex()) {
                    if (cell.getValue().equals(cell2.getValue())) {
                        break;
                    }
                }
            }
        }

        for (GraphClass gc : adapter.getCellToVertex(cell2)) {
            if (JGraphXCanvas.createLabel(
                    converter.html(Utility.getShortName(gc.toString())))
                    .equals((String) cell.getValue())) {
                if (shortLabels) {
                    cell.setValue(Utility.getShortName(gc.toString()));
                } else {
                    cell.setValue(gc.toString());
                }
                break;
            }
        }
        graph.updateCellSize(cell, true);
    }

    /*
     * Checks the appropriate properness of the given edge.
     */
    private boolean getProperness(mxCell edge) {
        Set<GraphClass> src = adapter.getCellToVertex((mxCell) edge
                .getSource());
        Set<GraphClass> dst = adapter.getCellToVertex((mxCell) edge
                .getTarget());

        List<Inclusion> path = GAlg.getPath(DataSet.inclGraph, src.iterator()
                .next(), dst.iterator().next());
        return (Algo.isPathProper(path) || Algo.isPathProper(Algo
                .makePathProper(path)));
    }

}
