package jmri.jmrit.display.layoutEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import jmri.NamedBeanHandle;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SignalMast;
import jmri.Path;
import jmri.jmrit.signalling.SignallingGuiTools;
import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;

/**
 * PositionablePoint is a Point defining a node in the Track that can be dragged around the
 * inside of the enclosing LayoutEditor panel using a right-drag (drag with meta key).
 * <P>
 * Three types of Positionable Point are supported:
 *		Anchor - point on track - two track connections
 *		End Bumper - end of track point - one track connection
 *      Edge Connector - This is used to link track segements between two different panels
 * <P>
 * Note that a PositionablePoint exists for specifying connectivity and drawing position
 * only.  The Track Segments connected to a PositionablePoint may belong to the same block
 * or to different blocks.  Since each Track Segment may only belong to one block, a 
 * PositionablePoint may function as a Block Boundary.
 * <P>
 * Signal names are saved here at a Block Boundary anchor point by the tool Set Signals at
 * Block Boundary. PositionablePoint does nothing with these signal head names; it only 
 * serves as a place to store them.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision$
 */

public class PositionablePoint
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

	// defined constants
	public static final int ANCHOR = 1;
	public static final int END_BUMPER = 2;
	public static final int EDGE_CONNECTOR = 3;
	
	// operational instance variables (not saved between sessions)
	private PositionablePoint instance = null;
	private LayoutEditor layoutEditor = null;
	
	// persistent instances variables (saved between sessions)
	private String ident = "";
	private int type = 0;
	private TrackSegment connect1 = null;
	private TrackSegment connect2 = null;
	private Point2D coords = new Point2D.Double(10.0,10.0);
    
	private String eastBoundSignalName = ""; // signal head for east (south) bound trains
	private String westBoundSignalName = ""; // signal head for west (north) bound trains
    
    private NamedBeanHandle<SignalMast> eastBoundSignalMastNamed = null;
    private NamedBeanHandle<SignalMast> westBoundSignalMastNamed = null;
    /* We use a namedbeanhandle for the the sensors, even though we only store the name here, 
    this is so that we can keep up with moves and changes of userNames */
    private NamedBeanHandle<Sensor> eastBoundSensorNamed = null;
    private NamedBeanHandle<Sensor> westBoundSensorNamed = null;

	
    public PositionablePoint(String id, int t, Point2D p, LayoutEditor myPanel) {
		instance = this;
		layoutEditor = myPanel;
		if ( (t==ANCHOR) || (t==END_BUMPER) || (t==EDGE_CONNECTOR)) {
			type = t;
		}
		else {
			log.error("Illegal type of PositionablePoint - "+t);
			type = ANCHOR;
		}
		ident = id;
		coords = p;
    }
    
	/**
	 * Accessor methods
	*/
	public String getID() {return ident;}
	public int getType() {return type;}
	public TrackSegment getConnect1() {return connect1;}
	public TrackSegment getConnect2() {
        if(type==EDGE_CONNECTOR && getLinkedPoint()!=null){
            return getLinkedPoint().getConnect1();
        }
        return connect2;
    }
	public Point2D getCoords() {return coords;}
	public void setCoords(Point2D p) {coords = p;}
    
    private PositionablePoint linkedPoint;
    
    public String getLinkEditorName(){
        if(getLinkedEditor()!=null){
            return getLinkedEditor().getLayoutName();
        }
        return "";
    }
    
    public PositionablePoint getLinkedPoint(){
        return linkedPoint;
    }
    
    public String getLinkedPointId(){
        if(linkedPoint!=null)
            return linkedPoint.getID();
        return "";
    }
    
    public void setLinkedPoint(PositionablePoint p){
        if(p==linkedPoint) return;
        if(linkedPoint!=null && linkedPoint!=p){
            log.info("in here");
            PositionablePoint oldLinkedPoint = linkedPoint;
            linkedPoint=null;
            if(oldLinkedPoint.getLinkedPoint()!=null){
                oldLinkedPoint.setLinkedPoint(null);
            }
            if(oldLinkedPoint.getConnect1()!=null){
                TrackSegment ts = oldLinkedPoint.getConnect1();
                oldLinkedPoint.getLayoutEditor().auxTools.setBlockConnectivityChanged();
                ts.updateBlockInfo();
                oldLinkedPoint.getLayoutEditor().repaint();
            }
            if(getConnect1()!=null){
                layoutEditor.auxTools.setBlockConnectivityChanged();
                getConnect1().updateBlockInfo();
                layoutEditor.repaint();
            }
        }
        linkedPoint = p;
    }
    
    public LayoutEditor getLinkedEditor(){
        if(getLinkedPoint()!=null)
            return getLinkedPoint().getLayoutEditor();
        return null;
    }
    
    protected LayoutEditor getLayoutEditor(){
        return layoutEditor;
    }
    
	public String getEastBoundSignal() {return eastBoundSignalName;}
	public void setEastBoundSignal(String signalName) {eastBoundSignalName = signalName;}
	public String getWestBoundSignal() {return westBoundSignalName;}
	public void setWestBoundSignal(String signalName) {westBoundSignalName = signalName;}
    
    public String getEastBoundSensorName() {
        if(eastBoundSensorNamed!=null)
            return eastBoundSensorNamed.getName();
        return "";
    }
    
    public Sensor getEastBoundSensor(){
        if(eastBoundSensorNamed!=null)
            return eastBoundSensorNamed.getBean();
        return null;
    }
    
	public void setEastBoundSensor(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            eastBoundSensorNamed=null;
            return;
        }
            
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            eastBoundSensorNamed = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            eastBoundSensorNamed=null;
        }
    }
    
	public String getWestBoundSensorName() {
        if(westBoundSensorNamed!=null)
            return westBoundSensorNamed.getName();
        return "";
    }
    
    public Sensor getWestBoundSensor(){
        if(westBoundSensorNamed!=null)
            return westBoundSensorNamed.getBean();
        return null;
    }
    
	public void setWestBoundSensor(String sensorName) {
        if(sensorName==null || sensorName.equals("")){
            westBoundSensorNamed=null;
            return;
        }
        Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(sensorName);
        if (sensor != null) {
            westBoundSensorNamed = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(sensorName, sensor);
        } else {
            westBoundSensorNamed=null;
        }
    }
    
    public String getEastBoundSignalMastName(){
        if(eastBoundSignalMastNamed!=null)
            return eastBoundSignalMastNamed.getName();
        return "";
    }
    
    public SignalMast getEastBoundSignalMast(){
        if(eastBoundSignalMastNamed!=null)
            return eastBoundSignalMastNamed.getBean();
        return null;
    }
    
	public void setEastBoundSignalMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            eastBoundSignalMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().provideSignalMast(signalMast);
        if (mast != null) {
            eastBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            eastBoundSignalMastNamed=null;
        }
    }
    
	public String getWestBoundSignalMastName() {
        if(westBoundSignalMastNamed!=null)
            return westBoundSignalMastNamed.getName();
        return "";
    }
    
    public SignalMast getWestBoundSignalMast(){
        if(westBoundSignalMastNamed!=null)
            return westBoundSignalMastNamed.getBean();
        return null;
    }
    
	public void setWestBoundSignalMast(String signalMast){
        if(signalMast==null || signalMast.equals("")){
            westBoundSignalMastNamed=null;
            return;
        }
        
        SignalMast mast = InstanceManager.signalMastManagerInstance().provideSignalMast(signalMast);
        if (mast != null) {
            westBoundSignalMastNamed = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(signalMast, mast);
        } else {
            westBoundSignalMastNamed=null;
        }
    }
    
	// initialization instance variables (used when loading a LayoutEditor)
	public String trackSegment1Name = "";
	public String trackSegment2Name = "";
	/**
	 * Initialization method
	 *   The above variables are initialized by PositionablePointXml, then the following
	 *        method is called after the entire LayoutEditor is loaded to set the specific
	 *        TrackSegment objects.
	 */
	public void setObjects(LayoutEditor p) {
        if(type==EDGE_CONNECTOR){
            connect1 = p.findTrackSegmentByName(trackSegment1Name);
            if(getConnect2()!=null && getLinkedEditor()!=null){
                //now that we have a connection we can fire off a change
                TrackSegment ts = getConnect2();
                getLinkedEditor().auxTools.setBlockConnectivityChanged();
                ts.updateBlockInfo();
            }
        } else {
            connect1 = p.findTrackSegmentByName(trackSegment1Name);
            connect2 = p.findTrackSegmentByName(trackSegment2Name);
        }
	}
		
	/**
	 * Setup and remove connections to track
	 */
	public void setTrackConnection (TrackSegment track) {
		if (track==null) {
			return;
		}
		if ( (connect1!=track) && (connect2!=track) ) {
			// not connected to this track
			if (connect1==null) {
				connect1 = track;
			}
			else if ( (type==ANCHOR) && (connect2==null) ) {
				connect2 = track;
                if(connect1.getLayoutBlock()==connect2.getLayoutBlock()){
                    westBoundSignalMastNamed=null;
                    eastBoundSignalMastNamed=null;
                    setWestBoundSensor("");
                    setEastBoundSensor("");
                }
			}
			else {
				log.error ("Attempt to assign more than allowed number of connections");
			}
		}
	}
    
	public void removeTrackConnection (TrackSegment track) {
		if (track==connect1) {
			connect1 = null;
            reCheckBlockBoundary();
            removeLinkedPoint();
		}
		else if (track==connect2) {
			connect2 = null;
            reCheckBlockBoundary();
		}
		else {
			log.error ("Attempt to remove non-existant track connection");
		}
	}
    
    public void reCheckBlockBoundary(){
        if(type==END_BUMPER)
            return;
        if(getConnect1()==null && getConnect2()==null){
            //This is no longer a block boundary, therefore will remove signal masts and sensors if present
            if(westBoundSignalMastNamed!=null)
                removeSML(getWestBoundSignalMast());
            if(eastBoundSignalMastNamed!=null)
                removeSML(getEastBoundSignalMast());
            westBoundSignalMastNamed=null;
            eastBoundSignalMastNamed=null;
            setWestBoundSensor("");
            setEastBoundSensor("");
            //May want to look at a method to remove the assigned mast from the panel and potentially any SignalMast logics generated
        }  else if(getConnect1()==null || getConnect2()==null){
            //could still be in the process of rebuilding the point details
            return;
        } else if (getConnect1().getLayoutBlock()==getConnect2().getLayoutBlock()){
            //We are no longer a block bounardy
            if(westBoundSignalMastNamed!=null)
                removeSML(getWestBoundSignalMast());
            if(eastBoundSignalMastNamed!=null)
                removeSML(getEastBoundSignalMast());
            westBoundSignalMastNamed=null;
            eastBoundSignalMastNamed=null;
            setWestBoundSensor("");
            setEastBoundSensor("");
            //May want to look at a method to remove the assigned mast from the panel and potentially any SignalMast logics generated
        }
    }
    
    void removeSML(SignalMast signalMast){
        if(signalMast==null)
            return;
        if(jmri.InstanceManager.layoutBlockManagerInstance().isAdvancedRoutingEnabled() && InstanceManager.signalMastLogicManagerInstance().isSignalMastUsed(signalMast)){
            SignallingGuiTools.removeSignalMastLogic(null, signalMast);
        }
    }

    protected int maxWidth(){
        return 5;
    }
    protected int maxHeight(){
        return 5;
    }
    // cursor location reference for this move (relative to object)
    int xClick = 0;
    int yClick = 0;

    public void mousePressed(MouseEvent e) {
        // remember where we are
        xClick = e.getX();
        yClick = e.getY();
        // if (debug) log.debug("Pressed: "+where(e));
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        // if (debug) log.debug("Release: "+where(e));
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopUp(e);
        }
    }

    JPopupMenu popup = null;
	LayoutEditorTools tools = null;
    /**
     * For editing: only provides remove
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null ) {
			popup.removeAll();
		}
		else {
            popup = new JPopupMenu();
		}
		boolean blockBoundary = false;
        boolean endBumper = false;
		switch (getType()) {
			case ANCHOR:
				popup.add(rb.getString("Anchor"));
				LayoutBlock block1 = null;
				LayoutBlock block2 = null;
				if (connect1!=null) block1 = connect1.getLayoutBlock();
				if (connect2!=null) block2 = connect2.getLayoutBlock();
				if ( (block1!=null) && (block1==block2) ) {
					popup.add(rb.getString("Block")+": "+block1.getID());
				}
				else if ( (block1!=null) && (block2!=null) && (block1!=block2) ) {
					popup.add(rb.getString("BlockDivider"));
					popup.add(" "+rb.getString("Block1ID")+": "+block1.getID());
					popup.add(" "+rb.getString("Block2ID")+": "+block2.getID());
					blockBoundary = true;
				}
				break;
			case END_BUMPER:
				popup.add(rb.getString("EndBumper"));
				LayoutBlock blockEnd = null;
				if (connect1!=null) blockEnd = connect1.getLayoutBlock();
				if (blockEnd!=null) {
					popup.add(rb.getString("BlockID")+": "+blockEnd.getID());
				}
                endBumper = true;
				break;
			case EDGE_CONNECTOR:
				popup.add(rb.getString("EdgeConnector"));
                if(getLinkedEditor()!=null)
                    popup.add(getLinkEditorName());
                else
                    popup.add(rb.getString("EdgeNotLinked"));
				block1 = null;
				block2 = null;
				if (connect1!=null) block1 = connect1.getLayoutBlock();
                if (getConnect2()!=null) block2 = getConnect2().getLayoutBlock();
				if ( (block1!=null) && (block2!=null) && (block1!=block2) ) {
					popup.add(rb.getString("BlockDivider"));
					popup.add(" "+rb.getString("Block1ID")+": "+block1.getID());
					popup.add(" "+rb.getString("Block2ID")+": "+block2.getID());
					
				}
                blockBoundary = true;
				break;
            default : break;
		}
		popup.add(new JSeparator(JSeparator.HORIZONTAL));
		popup.add(new AbstractAction(rb.getString("Remove")) {
				public void actionPerformed(ActionEvent e) {
					if (layoutEditor.removePositionablePoint(instance)) {
						// user is serious about removing this point from the panel
						remove();
						dispose();
					}
				}
			});
		if (blockBoundary) {
            if(getType()==EDGE_CONNECTOR){
                popup.add(new AbstractAction(rb.getString("EdgeEditLink")) {
                    public void actionPerformed(ActionEvent e) {
                        setLink();
                    }
                });
            } else {
                popup.add(new AbstractAction(rb.getString("SetSignals")) {
                        public void actionPerformed(ActionEvent e) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        // bring up signals at level crossing tool dialog
                        tools.setSignalsAtBlockBoundaryFromMenu(instance,
                            layoutEditor.signalIconEditor,layoutEditor.signalFrame);
                        }
                    });
                popup.add(new AbstractAction(rb.getString("SetSensors")) {
                    public void actionPerformed(ActionEvent event) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        // bring up signals at block boundary tool dialog
                        tools.setSensorsAtBlockBoundaryFromMenu(instance,
                            layoutEditor.sensorIconEditor,layoutEditor.sensorFrame);
                    }
                });
                popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                    public void actionPerformed(ActionEvent event) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        // bring up signals at block boundary tool dialog
                        tools.setSignalMastsAtBlockBoundaryFromMenu(instance);
                    }
                });
            }
		}
        if (endBumper){
            popup.add(new AbstractAction(rb.getString("SetSensors")) {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					// bring up signals at block boundary tool dialog
					tools.setSensorsAtBlockBoundaryFromMenu(instance,
                        layoutEditor.sensorIconEditor,layoutEditor.sensorFrame);
                }
            });
            popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                public void actionPerformed(ActionEvent event) {
					if (tools == null) {
						tools = new LayoutEditorTools(layoutEditor);
					}
					// bring up signals at block boundary tool dialog
					tools.setSignalMastsAtBlockBoundaryFromMenu(instance);
                }
            });
        }
        layoutEditor.setShowAlignmentMenu(popup);
		popup.show(e.getComponent(), e.getX(), e.getY());
	}

    String where(MouseEvent e) {
        return ""+e.getX()+","+e.getY();
    }

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    void dispose() {
        if (popup != null) popup.removeAll();
        popup = null;
        removeLinkedPoint();
    }
    
    void removeLinkedPoint(){
        if(type==EDGE_CONNECTOR && getLinkedPoint()!=null){
            getLinkedPoint().setLinkedPoint(null);
            getLinkedEditor().repaint();
            if(getConnect2()!=null && getLinkedEditor()!=null){
                //as we have removed the point, need to force the update on the remote end.
                TrackSegment ts = getConnect2();
                getLinkedEditor().auxTools.setBlockConnectivityChanged();
                ts.updateBlockInfo();
            }
            linkedPoint=null;
            //linkedEditor=null;
        }
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }
    
    protected int getConnect1Dir(){
        Point2D p1;
        if (getConnect1().getConnect1()==this) p1 = layoutEditor.getCoords(getConnect1().getConnect2(),getConnect1().getType2());
        else p1 = layoutEditor.getCoords(getConnect1().getConnect1(),getConnect1().getType1());
        
        double dh = getCoords().getX() - p1.getX();
		double dv = getCoords().getY() - p1.getY();
		int dir = Path.NORTH;
		double tanA;
		if (dv!=0.0) tanA = Math.abs(dh)/Math.abs(dv);
		else tanA = 10.0;
		if (tanA<0.38268) {
			// track is mostly vertical
			if (dv<0.0) dir = Path.NORTH;
			else dir = Path.SOUTH;
		}
		else if (tanA>2.4142) {
			// track is mostly horizontal
			if (dh>0.0) dir = Path.EAST;
			else dir = Path.WEST;
		}
		else {
			// track is between horizontal and vertical
			if ( (dv>0.0) && (dh>0.0) ) dir = Path.SOUTH + Path.EAST;
			else if ( (dv>0.0) && (dh<0.0) ) dir = Path.SOUTH + Path.WEST;
			else if ( (dv<0.0) && (dh<0.0) ) dir = Path.NORTH + Path.WEST;
			else dir = Path.NORTH + Path.EAST;
		}
		return dir;
    
    }
    
    JComboBox linkPointsBox;
    JComboBox editorCombo;
    
    void setLink(){
        if(getConnect1()==null || getConnect1().getLayoutBlock()==null){
            log.error("Can not set link until we have a connecting track with a block assigned");
            return;
        }
        editLink = new JDialog();
        editLink.setTitle("EDIT LINK from " + getConnect1().getLayoutBlock().getDisplayName());
        
        
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());

        JButton done = new JButton("Done");
        done.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent e){
                    updateLink();
                }
            }
        );
        container.add(getLinkPanel(), BorderLayout.NORTH);
        container.add(done, BorderLayout.SOUTH);
        container.validate();

        editLink.add(container);

        editLink.pack();
        editLink.setModal(false);
        editLink.setVisible(true);
    }
    ArrayList<PositionablePoint> pointList;
    
    public JPanel getLinkPanel(){
        editorCombo = new JComboBox();
        ArrayList<LayoutEditor> panels = jmri.jmrit.display.PanelMenu.instance().getLayoutEditorPanelList();
        editorCombo.addItem("None");
        if(panels.contains(layoutEditor))
            panels.remove(layoutEditor);
        for (LayoutEditor p:panels){
            editorCombo.addItem(p);
            if(p==getLinkedEditor())
                editorCombo.setSelectedItem(p);
        }
        
        ActionListener selectPanelListener = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePointBox();
            }
        };
        
        editorCombo.addActionListener(selectPanelListener);
        JPanel selectorPanel= new JPanel();
        selectorPanel.add(new JLabel("Select Panel"));
        selectorPanel.add(editorCombo);
        linkPointsBox = new JComboBox();
        updatePointBox();
        selectorPanel.add(new JLabel("Connecting Block"));
        selectorPanel.add(linkPointsBox);
        return selectorPanel;
    }
    
    void updatePointBox(){
        linkPointsBox.removeAllItems();
        pointList = new ArrayList<PositionablePoint>();
        if(editorCombo.getSelectedIndex()==0){
            linkPointsBox.setEnabled(false);
            return;
        }
        int ourDir = getConnect1Dir();
        linkPointsBox.setEnabled(true);
        for (PositionablePoint p:((LayoutEditor)editorCombo.getSelectedItem()).pointList) {
            if(p.getType()==EDGE_CONNECTOR) {
                if(p.getLinkedPoint()==this){
                    pointList.add(p);
                    linkPointsBox.addItem(p.getConnect2().getLayoutBlock().getDisplayName());
                    linkPointsBox.setSelectedItem(p.getConnect2().getLayoutBlock().getDisplayName());
                }
                else if (p.getLinkedPoint()==null){
                    if(p.getConnect1()!=null && p.getConnect1().getLayoutBlock()!=null){
                        if(p.getConnect1().getLayoutBlock()!=getConnect1().getLayoutBlock() && ourDir!=p.getConnect1Dir()){
                            pointList.add(p);
                            linkPointsBox.addItem(p.getConnect1().getLayoutBlock().getDisplayName());
                        }
                    }
                }
            }
        }
        editLink.pack();
    }
    
    public void updateLink(){
        if(editorCombo.getSelectedIndex()==0 || linkPointsBox.getSelectedIndex()==-1){
            setLinkedPoint(null);
        } else {
            setLinkedPoint(pointList.get(linkPointsBox.getSelectedIndex()));
        }
        editLink.setVisible(false);
    
    }

    JDialog editLink = null;
    
    static Logger log = LoggerFactory.getLogger(PositionablePoint.class.getName());

}
