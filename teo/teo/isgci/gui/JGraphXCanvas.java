package teo.isgci.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JLabel;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class JGraphXCanvas extends mxGraphComponent implements MouseListener, MouseWheelListener {

	public JGraphXCanvas(mxGraph graph) {
		super(graph);
		addMouseListener(this);
		addMouseWheelListener(this);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (notches < 0) {
			for (; notches < 0; notches++) {
				zoomIn();
			}
		} else {
			for (; notches > 0; notches--) {
				zoomOut();
			}
		}
		System.out.println("Viewport: " + getViewport().getSize());
		System.out.println("GraphControl: " + getGraphControl().getSize());
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
