package teo.isgci.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import teo.isgci.db.DataSet;

public class LegendPanel extends JPanel {
	
	protected ISGCIMainFrame parent;
	{
		setOpaque(false);
	}
	
	public LegendPanel(final ISGCIMainFrame parent) {
		super();
		this.parent = parent;
    	parent.graphCanvas.setLayout(null);
    	
    	//Listener for the Legend
		parent.addComponentListener( new ComponentAdapter() {
		    @Override
		    public void componentResized( ComponentEvent e ) {
				setBounds(parent.drawingPane.getWidth()+parent.drawingPane.getHorizontalScrollBar().getValue()-150, parent.drawingPane.getHeight()+parent.drawingPane.getVerticalScrollBar().getValue()-160, 120, 130);
		    	revalidate();
		    }
		} );
	
		//Create Listener to have the Legend everytime at the same playce =)
		parent.drawingPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				setBounds(parent.drawingPane.getWidth()+parent.drawingPane.getHorizontalScrollBar().getValue()-150, parent.drawingPane.getHeight()+parent.drawingPane.getVerticalScrollBar().getValue()-160, 120, 130);
				
			}
		});
		parent.drawingPane.getHorizontalScrollBar().addAdjustmentListener(new AdjustmentListener() {
			
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				setBounds(parent.drawingPane.getWidth()+parent.drawingPane.getHorizontalScrollBar().getValue()-150, parent.drawingPane.getHeight()+parent.drawingPane.getVerticalScrollBar().getValue()-160, 120, 130);
				
			}
		});
			
		//Create Legend Panel
		TitledBorder t_legend = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black),"Legend:");
		setBorder(t_legend);
		
	}
	
	
	public void paintComponent(Graphics g) {
		//Create the different Colors
		g.setColor(Color.green);
		g.fillRect(10, 25, 20, 10);
		
		g.setColor(Color.green.darker());
		g.fillRect(10, 45, 20, 10);
		
		g.setColor(Color.red);
		g.fillRect(10, 65, 20, 10);
		
		g.setColor(SColor.brighter(Color.red));
		g.fillRect(10, 85, 20, 10);
		
		g.setColor(Color.white);
		g.fillRect(10, 105, 20, 10);
		
		//Add borders
		g.setColor(Color.black);
		g.drawRect(10, 25, 20, 10);
		g.drawRect(10, 45, 20, 10);
		g.drawRect(10, 65, 20, 10);
		g.drawRect(10, 85, 20, 10);
		g.drawRect(10, 105, 20, 10);
		
		//Add Text to the Colors
		g.drawString("Linear", 40, 35);
		g.drawString("Polynomial", 40, 55);
		g.drawString("NP-complete", 40, 75);
		g.drawString("Intermediate", 40, 95);
		g.drawString("Unknown", 40, 115);
		
		g.setColor(parent.graphCanvas.getBackground());
	    super.paintComponent(g);
	}
}
