package teo.isgci.gui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import teo.isgci.gc.GraphClass;
import teo.isgci.util.JGraphTXAdapter;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.Utility;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxHtmlColor;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class SVGExport {
	
	private JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;
	private mxGraph graph;
	private LatexGraphics latexgraphics;
	private String svg;
	private SVGGraphics svggraphics;
	
	
	
	/**
	 * Constructor
	 * @param adapter for JGraphT to JGraphX
	 */
	public SVGExport(JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter){
		this.adapter = adapter;
		svggraphics = new SVGGraphics();
	}
	
	
		
	/**
	 * Creates an string using xml-code to define a svg-file
	 * @return svg-string
	 */
	public String createExportString(){
		copyGraph();
		latexgraphics = new LatexGraphics();
		svg = "";
		Graphics graphics = svggraphics.create();
		
    	int width = (int) graph.getGraphBounds().getWidth();
    	int height = (int) graph.getGraphBounds().getHeight();
    	
    	graphics.setClip(new Rectangle(width,height));
    	graphics.setFont(latexgraphics.getFont());
    	
		
		for(Object o : graph.getChildCells(graph.getDefaultParent())){
			if(o instanceof mxCell){
				mxCell cell = (mxCell) o;
				if(cell.isVertex()){
					drawLabel(cell, graphics);
					addNode(cell);
					
				}
				else{
					addEdge(cell);
				}
			}
		}
		
		return merge();
	}
	
	
	/*creates a copy of the graph*/

	private void copyGraph() {
		graph = new mxGraph();
		graph.addCells(adapter.getChildCells(adapter.getDefaultParent()));
		mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
 
	    
		graph.getModel().beginUpdate();
		
		for(Object o : graph.getChildCells(graph.getDefaultParent())){
			if(o instanceof mxCell){
				mxCell cell = (mxCell) o;
				if(cell.isVertex()){
					updateNodeLabel(cell);
					graph.updateCellSize(cell, true);
				}
			}
		}
		
		graph.getModel().endUpdate();
		
		layout.setInterRankCellSpacing(150);
	    layout.execute(graph.getDefaultParent());
	    graph.refresh();	
	}
	
	
	/*merges the labels and the graph to one string*/

	private String merge() {
		String temp = svggraphics.getContent();
		String test = "";
		for(int i=0; i<temp.length(); i++){
			test += temp.charAt(i);
			if(test.endsWith("</desc>")){
				temp = test + svg + temp.substring(i,temp.length());
				break;
			}
		}
		return temp;
	}
	

	/*adds node to SVG-String including color*/
	
	private void addNode(mxCell cell) {
		mxGeometry geo = cell.getGeometry();
		graph.getCellStyle(cell);
		String color = (String) graph.getCellStyle(cell).get(mxConstants.STYLE_FILLCOLOR);
		System.out.println("-"+color);
		if(!color.startsWith("#")){
			color = "#" + color.substring(2, color.length());
		}
		System.out.println(color);
		svg += "<rect x=\""+ geo.getX() + "\" y=\""+ geo.getY()+ "\" width=\""+ geo.getWidth()+ "\" height=\""+ geo.getHeight() +"\"  fill=\""+color+"\" stroke=\"black\"/>\n";		
	}
	
	
	/*draws the label for the svg-file*/

	private void drawLabel(mxCell cell, Graphics g) {
		mxGeometry geo = cell.getGeometry();
                
		int w = latexgraphics.getLatexWidth(g, (String) cell.getValue());
        latexgraphics.drawLatexString(g, (String) cell.getValue(), (int) geo.getX(),
        	(int) (geo.getY()+geo.getHeight()));
	}
	
	
	/*adds edges to SVG-String*/

	private void addEdge(mxCell cell) {
		boolean first = true;
		svg+= "<path d=\"";
		for(mxPoint point : graph.getView().getState(cell).getAbsolutePoints()){
			if(first){
				svg += "M "+point.getX() +","+ point.getY();
				first = false;
			}
			else{
				svg += " L "+point.getX() +","+ point.getY();
			}
			
		}
		svg += "\" fill=\"none\" stroke=\"black\" marker-end=\"url(#arrow)\" /> \n";
		
	}
	
	
	/*updates node-labels to get rid of shortened labels*/

	private void updateNodeLabel(mxCell cell) {
		String temp = "";
		Latex2JHtml converter = new Latex2JHtml();
		
		for(GraphClass gc : adapter.getCellToVertex(cell)){
			if(JGraphXCanvas.createLabel(converter.html(Utility.getShortName(gc.toString()))).equals((String)cell.getValue())){
				cell.setValue(gc.toString());
				break;
			}
			graph.updateCellSize(cell, true);
		}
	}
	
}
