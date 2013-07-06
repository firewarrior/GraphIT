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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import teo.isgci.db.DataSet;
import teo.isgci.gc.GraphClass;
import teo.isgci.util.Latex2Html;

/**
 * Creates a context menu for nodes and offers methods for hide sub- and
 * superclasses, information and change name.
 * 
 * @author ISGCI, Fabian Brosda, Thorsten Breitkreutz, Cristiana Grigoriu,
 *         Moritz Heine, Florian Krönert, Thorsten Sauter, Christian Stohr
 * 
 */
public class NodePopup extends JPopupMenu implements ActionListener {
    private ISGCIMainFrame parent;
    private JMenuItem deleteSub, deleteSup, infoItem;
    private JMenu nameItem;
    private Set<GraphClass> view;
    private String latexName;
    private Latex2Html converter = new Latex2Html("images/");
    private static String CHANGENAME = "Name: ";

    public NodePopup(ISGCIMainFrame parent) {
        super();
        this.parent = parent;
        add(infoItem = new JMenuItem("Information"));
        add(nameItem = new JMenu("Change name"));
        add(deleteSub = new JMenuItem("Hide subclasses"));
        add(deleteSup = new JMenuItem("Hide superclasses"));
        infoItem.addActionListener(this);
        deleteSub.addActionListener(this);
        deleteSup.addActionListener(this);
    }

    public void setNode(Set<GraphClass> set, String latexName) {
        view = set;
        this.latexName = latexName;
    }

    public void actionPerformed(ActionEvent event) {
        Object source = event.getSource();
        if (source == infoItem) {

            for (GraphClass gc : view) {
                if (parent.getxCanvas().getNodeName(gc.toString())
                        .equals(latexName)) {
                    JDialog d = new GraphClassInformationDialog(parent,
                            DataSet.getClass(gc.toString()));
                    d.setLocation(50, 50);
                    d.pack();
                    d.setSize(800, 600);
                    d.setVisible(true);
                    break;
                }
            }

        } else if (source == deleteSub) {
            parent.getxCanvas().hideSubClasses(view);
        } else if (source == deleteSup) {
            parent.getxCanvas().hideSuperClasses(view);
        } else if (event.getActionCommand().startsWith(CHANGENAME)) {
            String fullname = event.getActionCommand().substring(
                    CHANGENAME.length());
            parent.getxCanvas().renameNode(view, fullname);
        }
    }

    public void show(Component orig, int x, int y) {
        Set<GraphClass> gcs = view;
        int i = 0;

        nameItem.removeAll();
        nameItem.setEnabled(gcs.size() != 1);
        JMenuItem[] mItem = new JMenuItem[gcs.size()];
        // FIXME sort and render latex overline properly
        for (GraphClass gc : gcs) {
            nameItem.add(mItem[i] = new JMenuItem(("<html>" + converter
                    .html(gc.toString())) + "</html>"));
            mItem[i].setActionCommand(CHANGENAME + gc.toString());
            mItem[i].addActionListener(this);
            i++;
        }

        super.show(orig, x, y);
    }
}

/* EOF */
