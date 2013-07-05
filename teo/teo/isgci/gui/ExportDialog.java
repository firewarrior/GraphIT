/*
 * Export dialog.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ExportDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.io.*;
import java.util.Set;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Component;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Container;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import org.jgrapht.graph.DefaultEdge;

import teo.isgci.gc.GraphClass;
import teo.isgci.grapht.*;
import teo.isgci.util.JGraphTXAdapter;
import teo.isgci.xml.GraphMLWriter;

public class ExportDialog extends JDialog implements ActionListener {

    /** The card titles (and ids) */
    protected static final String DEST_FILE = "Choose destination file";
    protected static final String SVG_EXPL="An SVG file is suitable for editing the diagram, e.g. with\n"+
            "inkscape (http://www.inkscape.org), but cannot be included\n"+
            "directly in LaTeX.";
    protected String current;

    /* Global items */
    protected ISGCIMainFrame parent;
    protected JTextArea title;
    protected JTextArea svg_expl;
    protected JPanel cardPanel;
    protected CardLayout cardLayout;
    
    /* SVG items */
    protected JCheckBox m_shortLabels, m_relayout;

    /* Save location items */
    protected JFileChooser file;
    
    /* Minimum Size */
    protected Dimension minSize = new Dimension(534,425);
    
    /*JGraphT 2 JGraphX Adapter*/
    JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter;

    public ExportDialog(ISGCIMainFrame parent, JGraphTXAdapter<Set<GraphClass>, DefaultEdge> adapter) {
        super(parent, "Export drawing to SVG", true);
        this.parent = parent;
        this.setMinimumSize(minSize);
        this.adapter = adapter;
        
        Container content = getContentPane();
        JPanel buttonPanel = new JPanel();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        cardPanel = new JPanel();
        cardPanel.setBorder(new EmptyBorder(new Insets(5, 10, 5, 10))); 

        cardLayout = new CardLayout(); 
        cardPanel.setLayout(cardLayout);

        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(new JSeparator(), BorderLayout.NORTH);

        buttonBox.setBorder(new EmptyBorder(new Insets(5, 50, 10, 10))); 
        m_shortLabels = new JCheckBox("Short node labels", true);
        m_shortLabels.setToolTipText("Use shortened labels for nodes. Long labels may case clipping issues.");
        m_shortLabels.addActionListener(this);
        m_relayout = new JCheckBox("Relayout",false);
        m_relayout.setToolTipText("Relayout will fix clipping issues caused by long node labels, but has side effects i.e. move single nodes to the top.");
        m_relayout.setEnabled(false);
        buttonBox.add(m_shortLabels);
        buttonBox.add(m_relayout);
        buttonPanel.add(buttonBox, java.awt.BorderLayout.WEST);
        buttonPanel.add(new JSeparator(), BorderLayout.SOUTH);

        title = new JTextArea(DEST_FILE);
        Font f = title.getFont();
        title.setFont(f.deriveFont((float) (f.getSize() * 1.5)));
        title.setOpaque(true);
        title.setBackground(Color.darkGray);
        title.setForeground(Color.white);
        title.setBorder(new EmptyBorder(new Insets(10,50,0,10)));
        
        svg_expl = new JTextArea(SVG_EXPL);
        f = svg_expl.getFont();
        svg_expl.setFont(f.deriveFont((float) (f.getSize())));
        svg_expl.setOpaque(true);
        svg_expl.setBackground(Color.darkGray);
        svg_expl.setForeground(Color.white);
        svg_expl.setBorder(new EmptyBorder(new Insets(5,50,10,10)));

        Box titleBox = new Box(BoxLayout.Y_AXIS);
        titleBox.setBorder(new EmptyBorder(new Insets(0, 0, 0, 0))); 
        titleBox.add(title, BorderLayout.NORTH);
        titleBox.add(svg_expl, BorderLayout.SOUTH);
        
        cardPanel.setBorder(new EmptyBorder(new Insets(5,40,5,40)));
        content.add(titleBox, BorderLayout.NORTH);
        content.add(buttonPanel, BorderLayout.CENTER);
        content.add(cardPanel, BorderLayout.SOUTH);

        cardPanel.add(cardFile(), DEST_FILE);

        showCard(DEST_FILE);
    }


    /**
     * Show the given card and adjust button settings etc. for it.
     */
    protected void showCard(String card) {
        current = card;
        cardLayout.show(cardPanel,card);
    }

    /**
     * Return the card where the user can select the destination file.
     */
    private Component cardFile() {
        file = new JFileChooser();
        file.setApproveButtonText("Export");
        file.addActionListener(this);
        //file.setControlButtonsAreShown(false); doesn't work - see showCard()
        return file;
    }

    public void closeDialog() {
        setVisible(false);
        dispose();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == JFileChooser.APPROVE_SELECTION) {
            if (export())
                closeDialog();
        } else if (e.getActionCommand()== JFileChooser.CANCEL_SELECTION)
            closeDialog();
        m_relayout.setEnabled(!m_shortLabels.isSelected());
    }

    /**
     * Export using the entered settings. Return true iff no error occured.
     */
    protected boolean export() {
        boolean res = true;
        FileOutputStream f;
        try {
            f = new FileOutputStream(file.getSelectedFile());
        } catch (Exception e) {
            e.printStackTrace();
            MessageDialog.error(parent, "Cannot open file for writing:\n"+
                file.getSelectedFile().getPath());
            return false;
        }

        try {
            exportSVG(f);
        } catch (Exception e) {
            res = false;
            e.printStackTrace();
            MessageDialog.error(parent, "Error while exporting:\n"+
                e.toString());
        }
        return res;
    }

    /**
     * Export to SVG.
     */
    protected void exportSVG(FileOutputStream f) throws Exception {
        Exception res = null;
        String outstr;
        Writer out = null;
        
        try {
            out = new OutputStreamWriter(f, "UTF-8");
			//SVGGraphics g = new SVGGraphics();
           // parent.graphCanvas.forcePaint(g);
            SVGExport export = new SVGExport(adapter, m_shortLabels.isSelected(), m_relayout.isSelected());
            outstr = export.createExportString();
            //outstr = SVGExport.createExportString(parent.getxCanvas().getComponent().getGraph(), adapter);
           // g.dispose();
            out.write(outstr, 0, outstr.length());
        } catch (IOException ex)  {
            res = ex;
        } finally {
            out.close();
        }
        
        if (res != null)
            throw res;
    }

}

/* EOF */
