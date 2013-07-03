package teo.isgci.gui;

import java.util.Set;

import org.jgrapht.graph.DefaultEdge;

import teo.isgci.gc.GraphClass;
import teo.isgci.util.JGraphTXAdapter;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.Utility;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class SVGExport {
	
	public static String createExportString(mxGraph graph, JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter){
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
				
				if(cell.isVertex()){
					String old = (String) cell.getValue();
					for(GraphClass gc : adapter.getCellToVertex(cell)){
						if(JGraphXCanvas.createLabel(Utility.getShortName(converter.html(gc.toString()))).equals((String)cell.getValue())){
							String temp = converter.html(gc.toString());
							cell.setValue(createSVGLabel(temp));
							break;
						}
					}
					adapter.updateCellSize(cell, true);
					svg += "<rect x=\""+ geo.getX() + "\" y=\""+ geo.getY()+ "\" width=\""+ geo.getWidth()+ "\" height=\""+ geo.getHeight() +"\"  fill=\"none\" stroke=\"black\"/>\n";
					svg += "<foreignObject requiredExtensions=\"http://www.w3.org/1999/xhtml\"" +
							 "x=\""+ geo.getX() + "\" y=\""+ (geo.getY()+geo.getHeight()) +"\" width=\""+(geo.getWidth()-2)+"\" height=\""+(geo.getHeight()-2)+"\">" +
					 "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
					   "<body>" +
					     createSVGLabel((String)cell.getValue()) +
					   "</body>" +
					 "</html>" +
					"</foreignObject>";
					cell.setValue(old);
					adapter.updateCellSize(cell, true);
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
			
			/*if(temp.endsWith("<sub>")){
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
			else*/ if(temp.endsWith("co-(")){
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
		
		/*if(co){
			temp += "</tspan>";
		}
		
		if(end){
			return temp+"</tspan>";
		}*/
		
		return temp;
	}

}
