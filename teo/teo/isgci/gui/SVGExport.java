package teo.isgci.gui;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.util.mxPoint;
import com.mxgraph.view.mxGraph;

public class SVGExport {
	
	public static String createExportString(mxGraph graph){
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
			    "</marker></defs>";

		
		for(Object o : graph.getChildCells(graph.getDefaultParent())){
			if(o instanceof mxCell){
				mxCell cell = (mxCell) o;
				mxGeometry geo = cell.getGeometry();
				
				if(cell.isVertex()){
					svg += "<rect x=\""+ geo.getX() + "\" y=\""+ geo.getY()+ "\" width=\""+ geo.getWidth()+ "\" height=\""+ geo.getHeight() +"\"  fill=\"none\" stroke=\"black\"/>\n";
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

}
