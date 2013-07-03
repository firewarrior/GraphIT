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
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class SVGExport {
	
	private JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;
	private mxGraph graph;
	private LatexGraphics latexgraphics;
	private String svg;
	private SVGGraphics svggraphics;
	
	public SVGExport(JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter){
		this.adapter = adapter;
		svggraphics = new SVGGraphics();
	}
	
	public String createExportString(){
		latexgraphics = new LatexGraphics();
		svg = "";
		Graphics graphics = svggraphics.create();
		
		graph = new mxGraph(adapter.getModel());
		/*mxHierarchicalLayout layout = new mxHierarchicalLayout(graph);
    	layout.setInterRankCellSpacing(150);
	    layout.execute(adapter.getDefaultParent());
    	graph.refresh();*/
    	
    	int width = (int) graph.getGraphBounds().getWidth();
    	int height = (int) graph.getGraphBounds().getHeight();
    	
    	graphics.setClip(new Rectangle(width,height));
    	graphics.setFont(latexgraphics.getFont());
		
		for(Object o : graph.getChildCells(graph.getDefaultParent())){
			if(o instanceof mxCell){
				mxCell cell = (mxCell) o;
				if(cell.isVertex()){
					updateNodeLabel(cell);
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

	private String merge() {
		String temp = svggraphics.getContent();
		temp = temp.substring(0, temp.length()-6);
		temp += svg + "</svg>";
		return temp;
	}

	private void addNode(mxCell cell) {
		mxGeometry geo = cell.getGeometry();
		svg += "<rect x=\""+ geo.getX() + "\" y=\""+ geo.getY()+ "\" width=\""+ geo.getWidth()+ "\" height=\""+ geo.getHeight() +"\"  fill=\"none\" stroke=\"black\"/>\n";		
	}

	private void drawLabel(mxCell cell, Graphics g) {
		//FontMetrics m = g.getFontMetrics();
		mxGeometry geo = cell.getGeometry();

        /*Rectangle r = new Rectangle((int) geo.getWidth(), (int) geo.getHeight());
        if (r.intersects(g.getClipBounds())) {
            if (g instanceof SmartGraphics) {
            		g.setColor(Color.blue);
            		((SmartGraphics) g).drawNode(r.x, r.y, r.width, r.height);
            		g.setColor(Color.black);*/
                
                int w = latexgraphics.getLatexWidth(g, (String) cell.getValue());
                latexgraphics.drawLatexString(g, (String) cell.getValue(), (int) geo.getX(),
                        (int) geo.getY());
           // } else {
                /*Color c = g.getColor();
                g.fillOval(r.x, r.y, r.width, r.height);
                g.setColor(color);
                if (marked) 
                    g.fillOval(r.x+3, r.y+3, r.width-6, r.height-6);
                else
                    g.fillOval(r.x+1, r.y+1, r.width-2, r.height-2);
                g.setColor(c);
                getLatexGraphics().drawLatexString(g, label, r.x+HORMARGIN,
                        r.y+r.height/2+ (m.getAscent()-m.getDescent())/2);*/
           // }
       // }
		
	}

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
	
	/*public static String createExportString(mxGraph graph, JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter){
		String svg = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\" ?>" +
		"<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 20010904//EN\" "+
		  "\"http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd\">" +
		"<svg width=\"" + graph.getGraphBounds().getWidth() + "\" height=\""+ graph.getGraphBounds().getHeight() + "\" xmlns=\"http://www.w3.org/2000/svg\" " +
		  "xmlns:xlink=\"http://www.w3.org/1999/xlink\">";

		svg += "<defs>" +
				"<marker id=\"pfeil\" " +
			      "viewBox=\"0 0 10 10\" refX=\"10\" refY=\"5\" " +
			      "markerUnits=\"strokeWidth\" " +
			      "markerWidth=\"15\" markerHeight=\"15\" " +
			      "orient=\"auto\"> " +
			      "<path d=\"M 0,0 l 10,5 l -10,5 z\" /> " +
			    "</marker>" +
			    "</defs>";

		Latex2JHtml converter = new Latex2JHtml();
		
		for(Object o : graph.getChildCells(graph.getDefaultParent())){
			if(o instanceof mxCell){
				mxCell cell = (mxCell) o;
				mxGeometry geo = cell.getGeometry();
				String temp = "";
				
				if(cell.isVertex()){
					String old = (String) cell.getValue();
					for(GraphClass gc : adapter.getCellToVertex(cell)){
						if(JGraphXCanvas.createLabel(Utility.getShortName(converter.html(gc.toString()))).equals((String)cell.getValue())){
							temp = converter.html(gc.toString());
							temp = createSVGLabel(temp);
							cell.setValue(temp);
							break;
						}
					}
					adapter.updateCellSize(cell, true);
					svg += "<rect x=\""+ geo.getX() + "\" y=\""+ geo.getY()+ "\" width=\""+ geo.getWidth()+ "\" height=\""+ geo.getHeight() +"\"  fill=\"none\" stroke=\"black\"/>\n";
					svg += "<foreignObject requiredExtensions=\"http://www.w3.org/1999/xhtml\" " +
							 "x=\""+ geo.getX() + "\" y=\""+ (geo.getY()) +"\" width=\""+(geo.getWidth())+"\" height=\""+(geo.getHeight())+"\">" +
					 "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
					   "<body>" +
					   temp +
					   "</body>" +
					 "</html>" +
					"</foreignObject>";
					cell.setValue(old);
					adapter.updateCellSize(cell, true);
					System.out.println(temp);
				}
				else{
					if(cell.isEdge()){
						
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
						svg += "\" fill=\"none\" stroke=\"black\" marker-end=\"url(#pfeil)\" /> \n";
					}
				}
			}
		}
		
		return svg + "</svg>";
		
	}
	
	private static String createSVGLabel(String label){
		String temp = "";
		boolean co = false;
		boolean end = false;
		
		for(int i=0; i<label.length(); i++){
			
			temp += label.charAt(i);
			
			if(temp.endsWith("<sub>")){
				temp = temp.substring(0,temp.length()-5);
				temp += "<tspan dy=\"-10\">";
			}
			else if(temp.endsWith("<sup>")){
				temp = temp.substring(0,temp.length()-5);
				temp += "<tspan dy=\"10\">";
			}
			else if(temp.endsWith("</sup>")){
				temp = temp.substring(0,temp.length()-6);
				temp += "</tspan><tspan dy=\"-10\">";
				end = true;
			}
			else if(temp.endsWith("</sub>")){
				temp = temp.substring(0,temp.length()-6);
				temp += "</tspan><tspan dy=\"10\">";
				end = true;
			}
			else if(temp.endsWith("co-(")){
					temp = temp.substring(0,temp.length()-4);
			
					temp += "<span style=\"text-decoration:overline\">";
					co = true;
			}
			else if(co && label.charAt(i) == ')'){			
				    temp = temp.substring(0,temp.length()-1);
				    temp += "</span>";
					co = false;
			}
		}
		
		if(co){
			temp += "</tspan>";
		}
		
		if(end){
			return temp+"</tspan>";
		}
		
		return temp;
	}*/

}
