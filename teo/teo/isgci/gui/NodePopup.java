/*
 * Popupmenu for NodeViews
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/NodePopup.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.Component;
import java.awt.event.*;
import java.util.Set;
import javax.swing.*;
import org.jgrapht.graph.DefaultEdge;
import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.Latex2JHtml;
import teo.isgci.util.Utility;


public class NodePopup extends JPopupMenu implements ActionListener {
	private ISGCIMainFrame parent;
	private JMenuItem deleteItem, infoItem;
	private JMenu nameItem;
    private Set<GraphClass> view;
	private String latexName;
    private static String CHANGENAME = "Name: ";

    public NodePopup(ISGCIMainFrame parent) {
        super();
        this.parent = parent;
        //deleteItem = new JMenuItem("Delete");
        add(infoItem = new JMenuItem("Information"));
        add(nameItem = new JMenu("Change name"));
        infoItem.addActionListener(this);
    }

    public void setNode(Set<GraphClass> set, String latexName) {
        view = set;
        this.latexName = latexName;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {
        	Latex2JHtml converter = new Latex2JHtml();
        	
        	for(GraphClass gc : view){
        		if(converter.html(gc.toString()).equals(latexName)){
        			JDialog d = new GraphClassInformationDialog(
                    parent, DataSet.getClass(gc.toString()));
        			d.setLocation(50, 50);
        			d.pack();
        			d.setSize(800, 600);
        			d.setVisible(true);
        			break;
        		}
        	}
        	
            
        } else if (event.getActionCommand().startsWith(CHANGENAME)) {
            String fullname = event.getActionCommand().substring(
                    CHANGENAME.length());
            
            //for(GraphClass gc : view){
            	//if(gc.toString().equals(fullname)){
            		parent.getxCanvas().renameNode(view, fullname);
            		//break;
            //	}
            //}
        }
    }
    
    public void show(Component orig, int x, int y) {
    	Set<GraphClass> gcs = view;
        int i = 0;

        nameItem.removeAll();
        nameItem.setEnabled(gcs.size() != 1);
        JMenuItem[] mItem = new JMenuItem[gcs.size()];
        //FIXME sort and render latex properly
        for (GraphClass gc : gcs) {
            nameItem.add(mItem[i] = new JMenuItem(
                    Utility.getShortName(gc.toString())));
            mItem[i].setActionCommand(CHANGENAME + gc.toString());
            mItem[i].addActionListener(this);
            i++;
        }
        
        super.show(orig, x, y);
    }
}

/* EOF */
