package teo.isgci.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import teo.isgci.gc.GraphClass;

public class ClassesListVisabilityHandler implements
		ListSelectionListener, ListCellRenderer<GraphClass> {

	private Set<GraphClass> deactivated = new HashSet<GraphClass>();
	private LatexGraphics latex;
	
	public ClassesListVisabilityHandler(LatexGraphics latex){
		this.latex = latex;
	}

	public void addDeactivated(Collection<GraphClass> deactivated) {
		if (deactivated == null)
			return;
		this.deactivated.addAll(deactivated);
	}

	public Set<GraphClass> getDeactivated() {
		return deactivated;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		GraphClass node;
		if (!(e.getSource() instanceof NodeList))
			return;

		node = ((NodeList) e.getSource()).getSelectedNode();

		if (deactivated.contains(node)) {
			((NodeList) e.getSource()).clearSelection();
		}

	}
	
	@Override
	public Component getListCellRendererComponent(
			JList<? extends GraphClass> list, GraphClass gc, int index,
			boolean isSelected, boolean cellHasFocus) {

		LatexLabel label =
                latex.newLabel(((GraphClass) gc).toString());
		
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }
        
        if(deactivated.contains(gc)){
        	label.setForeground(Color.gray.brighter());
        } else {
        	label.setForeground(list.getForeground());
        }
        
        label.setEnabled(list.isEnabled());
        label.setFont(list.getFont());
        //setOpaque(this);
        return label;
	}

}
