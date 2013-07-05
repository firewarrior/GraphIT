package teo.isgci.gui;

import java.awt.Color;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.view.mxCellState;

public class CellHighlighter extends mxCellMarker {
	
	private Color color;
	private mxGraphComponent parent;
	private mxCell currCell;
	

	public CellHighlighter(mxGraphComponent component, Color color) {
		super(component);
		this.parent = component;
		this.color = color;
	}
	
	public void highlight(mxCell cell){
		mxCellState state = parent.getGraph().getView().getState(cell);
		highlight(state, color);
		currCell = cell;
	}
	
	public void refresh() {
		reset();
		highlight(currCell);
	}
}
