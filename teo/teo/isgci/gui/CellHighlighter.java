package teo.isgci.gui;

import java.awt.Color;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.handler.mxCellMarker;
import com.mxgraph.view.mxCellState;

/**
 * Custom mxCellMarker to change highlighting of vertices and edges.
 * 
 * @author Fabian Brosda, Thorsten Breitkreutz, Cristiana Grigoriu, Moritz
 *         Heine, Florian Krönert, Thorsten Sauter, Christian Stohr
 * 
 */
public class CellHighlighter extends mxCellMarker {

    private Color color;
    private mxGraphComponent parent;
    private mxCell currCell;

    /**
     * Constructor
     * 
     * @param component
     *            the graphComponent
     * @param color
     *            highlighting color
     */
    public CellHighlighter(mxGraphComponent component, Color color) {
        super(component);
        this.parent = component;
        this.color = color;
    }

    /**
     * Highlights the given cell.
     * 
     * @param cell
     *            to be highlighted
     */
    public void highlight(mxCell cell) {
        mxCellState state = parent.getGraph().getView().getState(cell);
        highlight(state, color);
        currCell = cell;
    }

    /**
     * Refreshes highlighting of the current cell.
     */
    public void refresh() {
        reset();
        highlight(currCell);
    }
}
