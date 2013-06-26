/*
 * Popupmenu for EdgeViews
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/EdgePopup.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.event.*;
import java.util.Set;
import javax.swing.*;
import org.jgrapht.graph.DefaultEdge;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.Latex2JHtml;


public class EdgePopup extends JPopupMenu implements ActionListener {
	private ISGCIMainFrame parent;
    private JMenuItem deleteItem, infoItem;
    private Set<GraphClass> source, target;
    private String srcName, trgtName;

    public EdgePopup(ISGCIMainFrame parent) {
        super();
        this.parent = parent;
        //deleteItem = new JMenuItem("Delete");
        add(infoItem = new JMenuItem("Information"));
        infoItem.addActionListener(this);
    }

    public void setEdgeNodes(Set<GraphClass> source, String sname, Set<GraphClass> target, String tname) {
        this.source = source;
        this.target = target;
        srcName = sname;
        trgtName = tname;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {
        	
        	String tempsrc = null;
        	String temptrgt = null;
        	
        	Latex2JHtml converter = new Latex2JHtml();
        	
        	for(GraphClass gc : this.source){
        		if(converter.html(gc.toString()).equals(srcName)){
        			tempsrc = gc.toString();
        			break;
        		}
        	}
        	
        	for(GraphClass gc : this.target){
        		if(converter.html(gc.toString()).equals(trgtName)){
        			temptrgt = gc.toString();
        			break;
        		}
        	}
        	
            JDialog d = InclusionResultDialog.newInstance(parent,
                DataSet.getClass(tempsrc),
                DataSet.getClass(temptrgt));
            d.setLocation(50, 50);
            d.pack();
            d.setVisible(true);
            
        } 
    }

}

/* EOF */
