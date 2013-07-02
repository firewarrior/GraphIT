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

		
		for(Object o : graph.getChildCells(graph.getDefaultParent())){
			if(o instanceof mxCell){
				mxCell cell = (mxCell) o;
				mxGeometry geo = cell.getGeometry();
				
				if(cell.isVertex()){
					svg += "<rect x=\""+ geo.getX() + "\" y=\""+ geo.getY()+ "\" width=\""+ geo.getWidth()+ "\" height=\""+ geo.getHeight() +"\"  fill=\"none\" stroke=\"black\"/>\n";
				}
				else{
					if(cell.isEdge()){
						svg+= "<path d=\"M "+geo.getSourcePoint().getX() + ","+ geo.getSourcePoint().getY();
						for(mxPoint point : geo.getPoints()){
							svg += " L "+point.getX() +","+ point.getY();
						}
						svg += " L "+geo.getTargetPoint().getX() + ","+ geo.getTargetPoint().getY() + " /> \n";
					}
				}
			}
		}
		
		return svg + "</svg>";
		
	}

}
