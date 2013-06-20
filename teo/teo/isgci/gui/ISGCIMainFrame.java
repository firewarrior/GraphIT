/*
 * The main window of ISGCI. Also the class to start the program.
 *
 * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ISGCIMainFrame.java,v 2.4 2013/04/07 10:51:04 ux Exp $
 *
 * This file is part of the Information System on Graph Classes and their
 * Inclusions (ISGCI) at http://www.graphclasses.org.
 * Email: isgci@graphclasses.org
 */

package teo.isgci.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import teo.isgci.db.DataSet;
import teo.isgci.problem.*;
import teo.isgci.gc.ForbiddenClass;
import teo.isgci.gc.GraphClass;

import java.awt.Color;
import org.jgrapht.*;
import org.jgrapht.graph.SimpleDirectedGraph;
import teo.isgci.grapht.*;
import teo.isgci.util.LessLatex;
import teo.isgci.xml.GraphMLWriter;

/*import teo.isgci.gc.GraphClass;
import java.util.ArrayList;*/


/** The main frame of the application.
 */
public class ISGCIMainFrame extends JFrame
        implements WindowListener, ActionListener, ItemListener, KeyListener {

    public static final String APPLICATIONNAME = "ISGCI";

    public static ISGCIMainFrame tracker; // Needed for MediaTracker (hack)
    public static LatexGraphics latex;
    public static Font font;

    protected teo.Loader loader;

    // The menu
    protected JMenuItem miNew, miExport, miExit;
    protected JMenuItem miNaming, miDrawUnproper;
    protected JCheckBoxMenuItem miInformationBar;
    protected JMenuItem miCheckInclusion,miSelectGraphClasses;
    protected JMenuItem miGraphClassInformation,miOpenProblem;
    protected JMenuItem miSmallgraphs, miHelp, miAbout;
    protected JCheckBox superCheck,subCheck;
    protected WebSearch search;
    
    // Statusleiste
    protected JPanel problem;
    private boolean showStatus = false,checkStatus = true;
    protected JButton OpenBoundaryButton,addTab;
    protected JButton zoomIn,zoomOut,zoom;
    private String[] problems = {"None","Recognition","Treewidth","Cliquewidth","Cliquewidth expression",
			 "Weighted independent set","Independent set","Weighted clique","Clique",
			 "Domination","Colourability","Clique cover","3-Colourability","Cutwidth",
			 "Hamiltonian cycle","Hamiltonian path","Weighted feedback vertex set",
			 "Feedback vertex set"};
    protected JComboBox<String> chooseProblem;
    
    //New added for Graph Browser
	protected NodeList classesList;

    // This is where the drawing goes.
    protected JScrollPane drawingPane;
    public ISGCIGraphCanvas graphCanvas;


    /** Creates the frame.
     * @param locationURL The path/URL to the applet/application.
     * @param isApplet true iff the program runs as an applet.
     */
    public ISGCIMainFrame(teo.Loader loader) {
        super(APPLICATIONNAME);

        loader.register();
        this.loader = loader;
        tracker = this;

        DataSet.init(loader, "data/isgci.xml");
        ForbiddenClass.initRules(loader, "data/smallgraphs.xml");
        PSGraphics.init(loader);
        if (latex == null) {
            latex = new LatexGraphics();
            latex.init(loader);
        }

        boolean createMaps = false;
        try {
            createMaps = System.getProperty("org.isgci.mappath") != null;
        } catch (Exception e) {}

        if (createMaps) {       // Create maps and terminate
            createCanvasPanel();
            new teo.isgci.util.LandMark(this).createMaps();
            closeWindow();
        }
        
        //SetSize for the GUI
        setSize(800, 600);
        //setMinimumSize(getSize());
        
        //Create JMenu
        setJMenuBar(createMenus());
        
        //Set Layout for main panel
        setLayout(new BorderLayout());
        
        //Add canvas panel to main panel
        JPanel canvas = new JPanel(new BorderLayout());
        canvas.add(createCanvasPanel(),BorderLayout.CENTER);
        
        //Add tabbed menu to canvas
        canvas.add(createStatus(),BorderLayout.EAST); //<-----
        
        //Adding canvas to main panel
        getContentPane().add(canvas);
     
        registerListeners();
        setLocation(20, 20);
        pack();
        setVisible(true);
    }


    /**
     * Write the entire database in GraphML to isgcifull.graphml.
     */
    private void writeGraphML() {
        OutputStreamWriter out = null;

        SimpleDirectedGraph<GraphClass, Inclusion> g =
            new SimpleDirectedGraph<GraphClass, Inclusion>(Inclusion.class);
        Graphs.addGraph(g, DataSet.inclGraph);
        GAlg.transitiveReductionBruteForce(g);

        try {
            out = new OutputStreamWriter(
                    new FileOutputStream("isgcifull.graphml"), "UTF-8");
            GraphMLWriter w = new GraphMLWriter(out,
                        GraphMLWriter.MODE_PLAIN,
                        true,
                        false);
            w.startDocument();
            for (GraphClass gc : g.vertexSet()) {
                w.writeNode(gc.getID(), gc.toString(), Color.WHITE);
            }
            for (Inclusion e : g.edgeSet()) {
                w.writeEdge(e.getSuper().getID(), e.getSub().getID(),
                        e.isProper());
            }
            w.endDocument();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Creates and attaches the necessary eventlisteners.
     */
    protected void registerListeners() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        miNew.addActionListener(this);
        miExport.addActionListener(this);
        miExit.addActionListener(this);
        miNaming.addActionListener(this);
        miDrawUnproper.addItemListener(this);
        miCheckInclusion.addActionListener(this);
        //miDelete.addActionListener(this);
        //miSelectAll.addActionListener(this);
        //miOpenProblem.addActionListener(this);
        miSmallgraphs.addActionListener(this);
        miHelp.addActionListener(this);
        miAbout.addActionListener(this);
        miInformationBar.addItemListener(this);
        miSelectGraphClasses.addActionListener(this);
        miGraphClassInformation.addActionListener(this);
        miOpenProblem.addActionListener(this);
        
        search.addKeyListener(this);
        addTab.addActionListener(this);
        chooseProblem.addActionListener(this);
     
        
    }

    

    /**
     * Creates the problem menu.
     * @return The created problem panel
     * @see JTabbedPanel
     */
    protected JTabbedPane createStatus() {
    	
    	//Insert InformationPanel
    	problem = createInformationPanel();
    	
    	//At beginning Settings everytime checked
        problem.setPreferredSize(new Dimension(0,0));
    	
    	
        //Tabs
        JTabbedPane tabs = new JTabbedPane();
        
        tabs.addTab("We are Legend =)", problem);
        JPanel panel = new JPanel();
        addTab = new JButton("+");
        addTab.setOpaque(false); //
        addTab.setBorder(null);
        addTab.setContentAreaFilled(false);
        addTab.setFocusPainted(false);

        addTab.setFocusable(false);
        panel.add(addTab);
        
        tabs.setTabComponentAt(0,panel );
        tabs.setTabPlacement(JTabbedPane.LEFT);
        
		return tabs;
    }
    

    /**
     * Creates the menu system.
     * @return The created JMenuBar
     * @see JMenuBar
     */
    protected JMenuBar createMenus() {
        JMenuBar mainMenuBar = new JMenuBar();
        JMenu fileMenu, setMenu, graphClassesMenu, helpMenu;

        fileMenu = new JMenu("File");
        fileMenu.add(miNew = new JMenuItem("New window"));
        fileMenu.add(miExport = new JMenuItem("Export drawing..."));
        fileMenu.add(miExit = new JMenuItem("Exit"));
        mainMenuBar.add(fileMenu);
        
        graphClassesMenu = new JMenu("Graph classes");
        graphClassesMenu.add( miSelectGraphClasses = new JMenuItem("Draw..."));
        graphClassesMenu.add( miGraphClassInformation = new JMenuItem("Browse Database"));
        miCheckInclusion = new JMenuItem("Find Relation");
        graphClassesMenu.add(miCheckInclusion);
        graphClassesMenu.add( miOpenProblem = new JMenuItem("Boundary/Open classes"));
        mainMenuBar.add(graphClassesMenu);

        setMenu = new JMenu("Settings");
        setMenu.add(miNaming = new JMenuItem("Naming preference..."));
        setMenu.add(miDrawUnproper =
                new JCheckBoxMenuItem("Mark unproper inclusions", true));
        setMenu.add(miInformationBar =
                new JCheckBoxMenuItem("Hide information bar", true));
        mainMenuBar.add(setMenu);



        helpMenu = new JMenu("Help");
        miSmallgraphs = new JMenuItem("Small graphs");
        helpMenu.add(miSmallgraphs);
        miHelp = new JMenuItem("Help");
        helpMenu.add(miHelp);
        miAbout = new JMenuItem("About");
        helpMenu.add(miAbout);
        mainMenuBar.add(helpMenu);

        return mainMenuBar;
    }
    
    
    protected JPanel createInformationPanel(){
    	JPanel mainPanel = new JPanel(new BorderLayout());
    	
    	//Search Panel
    	JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));   	
    	search = new WebSearch();
    	search.setPreferredSize(new Dimension(280, 20));
    	search.setText("Search...");
    	searchPanel.add(search);
        
    	//Bottom Panel
    	JPanel bottomPanel = new JPanel(new BorderLayout(30,30));
    	
    	//Problem
    	JPanel problemPanel = new JPanel(new GridLayout(2, 1));
    	JPanel flowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    	JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    	JLabel l_prob = new JLabel("Problem:");
    	JLabel l_color = new JLabel("Color for");
    	chooseProblem = new JComboBox<String>(problems);
    	
    	flowPanel.add(l_prob);
    	checkPanel.add(l_color);
    	checkPanel.add(chooseProblem);
    	problemPanel.add(flowPanel);
    	problemPanel.add(checkPanel);
    	
    	
    	//Zoom
    	JPanel zoomPanel = new JPanel(new GridLayout(2, 1));
    	JPanel flowZoomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    	JPanel zoomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
    	JLabel l_zoom = new JLabel("Zoom:");
    	zoomIn = new JButton("+");
    	zoomOut = new JButton("-");
    	zoom = new JButton("out");
    	
    	flowZoomPanel.add(l_zoom);
    	zoomPanel.add(flowZoomPanel);
    	zoomButtonPanel.add(zoomIn);
    	zoomButtonPanel.add(zoomOut);
    	zoomButtonPanel.add(zoom);
    	zoomPanel.add(zoomButtonPanel);
    	
    	//Set on bottom Panel
    	bottomPanel.add(problemPanel,BorderLayout.NORTH);
    	bottomPanel.add(zoomPanel,BorderLayout.CENTER);
    	JPanel space = new JPanel();
    	space.setPreferredSize(new Dimension(280,10));
    	bottomPanel.add(space,BorderLayout.SOUTH);
    	
        //Set on Main Panel
        mainPanel.add(searchPanel,BorderLayout.PAGE_START);
        mainPanel.add(createSearchBrowser(280,380), BorderLayout.CENTER);
        mainPanel.add(bottomPanel,BorderLayout.SOUTH);
        return mainPanel;
    }
   
    
    /**
     * Creates the search browser with scrollbars at the bottom and at the
     * right.
     * @return the panel
     */
    protected JPanel createSearchBrowser(int width,int height){
    	JPanel searchBrowser = new JPanel();
  
        classesList = new NodeList(latex);
        classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroller = new JScrollPane(classesList);
        scroller.setPreferredSize(new Dimension(width, height));
        searchBrowser.add(scroller);
        //Mouselistener for double click
        classesList.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if ( e.getClickCount() == 2 ) {
					  NodeView view = graphCanvas.findNode(
	                            classesList.getSelectedNode());
					  graphCanvas.markOnly(view);
					  graphCanvas.centerNode(view);
				}
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
		});
        
		return searchBrowser;
    }


    /**
     * Creates the drawing canvas with scrollbars at the bottom and at the
     * right.
     * @return the panel
     */
    protected JComponent createCanvasPanel() {
        graphCanvas = new ISGCIGraphCanvas(this);
        drawingPane = new JScrollPane(graphCanvas,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        drawingPane.getHorizontalScrollBar().setUnitIncrement(100);
        drawingPane.getVerticalScrollBar().setUnitIncrement(100);
        
        return drawingPane;
    }


    /**
     * Center the canvas on the given point.
     */
    public void centerCanvas(Point p) {
        JViewport viewport = drawingPane.getViewport();
        Dimension port = viewport.getExtentSize();
        Dimension view = viewport.getViewSize();

        p.x -= port.width/2;
        if (p.x + port.width > view.width)
            p.x = view.width - port.width;
        if (p.x < 0)
            p.x = 0;
        p.y -= port.height/2;
        if (p.y + port.height > view.height)
            p.y = view.height - port.height;
        if (p.y < 0)
            p.y = 0;
        viewport.setViewPosition(p);
    }

    public void printPort() {
        Rectangle view = getViewport();
        System.err.println("port: "+view);
    }

    public Rectangle getViewport() {
        return drawingPane.getViewport().getViewRect();
    }

    
    /** Closes the window and possibly terminates the program. */
    public void closeWindow() {
        setVisible(false);
        dispose();
        loader.unregister();
    }

    /**
     * Eventhandler for window events
     */
    public void windowClosing(WindowEvent e) {
        closeWindow();
    }

    /**
     * Required to overload (abstract)
     */
    public void windowOpened(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowActivated(WindowEvent e) {}

    /**
     * Eventhandler for menu selections
     */
    public void actionPerformed(ActionEvent event) {
        Object object = event.getSource();

        if (object == miExit) {
            closeWindow();
        } else if (object == miNew) {
            new ISGCIMainFrame(loader);
        } else if (object == miExport) {
            JDialog export = new ExportDialog(this);
            export.setLocation(50, 50);
            export.pack();
            export.setVisible(true);
        } else if (object == miNaming) {
            JDialog d = new NamingDialog(this);
            d.setLocation(50,50);
            d.pack();
            d.setVisible(true);
        } else if (object == miGraphClassInformation) {
            JDialog info = new GraphClassInformationDialog(this);
            info.setLocation(50, 50);
            info.pack();
            info.setSize(800, 600);
            info.setVisible(true);
        } else if (object == miCheckInclusion) {
            JDialog check = new CheckInclusionDialog(this);
            check.setLocation(50, 50);
            check.pack();
            check.setSize(700, 400);
            check.setVisible(true);
        }else if (object == miSelectGraphClasses) {
                JDialog select = new GraphClassSelectionDialog(this);
                select.setLocation(50, 50);
                select.pack();
                select.setSize(500, 400);
                select.setVisible(true);
        } else if (object == miAbout) {
            JDialog select = new AboutDialog(this);
            select.setLocation(50, 50);
            select.setVisible(true);
       
//        } else if (object == draw) {
//        	//////////////////////////////////////////////////////////////////////////
//        	// Drawing Graph
//        	//////////////////////////////////////////////////////////////////////////
//        	Cursor oldcursor = this.getCursor();
//            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//            this.graphCanvas.drawHierarchy(getNodes());
//            
//            for (Object o : classesList.getSelectedValues()) {
//                GraphClass gc = (GraphClass) o;
//                NodeView v = this.graphCanvas.findNode(gc);
//                if (v != null)
//                    v.setNameAndLabel(gc.toString());
//            }
//
//            System.out.println(this.graphCanvas.getSize());
//            this.graphCanvas.updateBounds();
//            
//            setCursor(oldcursor);
//        	 
        } else if (object == miHelp) {
            loader.showDocument("help.html");
        } else if (object == miSmallgraphs) {
            loader.showDocument("smallgraphs.html");
        } else if (object == miOpenProblem) {
            JDialog open=new OpenProblemDialog(this);
            open.setLocation(50, 50);
            open.setVisible(true);
        } else if(object == chooseProblem){
        	//////////////////////////////////////////////////////////////////////////
        	// Choose Problem and color graph
        	//////////////////////////////////////////////////////////////////////////
            this.graphCanvas.setProblem(DataSet.getProblem((String)chooseProblem.getSelectedItem()));
        } else if(object == addTab){
        	if(checkStatus == true){
				//Come Up
				if(showStatus == false){
					showStatus = true;
					problem.setPreferredSize(new Dimension(300,200));
			        problem.revalidate();
				
			    //Come down
				} else{
					showStatus = false;
					problem.setPreferredSize(new Dimension(0,0));
					problem.revalidate();
				}
			}      	
        }
    }

    public void itemStateChanged(ItemEvent event) {
        Object object = event.getSource();

        if (object == miDrawUnproper) {
            graphCanvas.setDrawUnproper(
                    ((JCheckBoxMenuItem) object).getState());
        }else if (object == miInformationBar){
        	//Hide Information bar
        	if(miInformationBar.getState()){
				problem.setPreferredSize(new Dimension(0,0));
				problem.revalidate();
        		checkStatus = true;
        	}else{
				problem.setPreferredSize(new Dimension(300,200));
				problem.revalidate();
        		checkStatus = false;
        	}
        }
    }

    //Needed for interactive Search
	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		search.setListDataSearch(this, classesList);
	}    
	
}

/* EOF */
