package jmri.jmrit.display.layoutEditor;

import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

/**
 * PositionablePoint is a Point defining a node in the Track that can be dragged around the
 * inside of the enclosing LayoutEditor panel using a right-drag (drag with meta key).
 * <P>
 * Two types of Positionable Point are supported:
 *		Anchor - point on track - two track connections
 *		End Bumper - end of track point - one track connection
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
    private String eastBoundSensorName = "";
    private String westBoundSensorName = "";
    private String eastBoundSignalMastName = "";
    private String westBoundSignalMastName = "";
	
    public PositionablePoint(String id, int t, Point2D p, LayoutEditor myPanel) {
		instance = this;
		layoutEditor = myPanel;
		if ( (t==ANCHOR) || (t==END_BUMPER) ) {
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
	public TrackSegment getConnect2() {return connect2;}
	public Point2D getCoords() {return coords;}
	public void setCoords(Point2D p) {coords = p;}
	public String getEastBoundSignal() {return eastBoundSignalName;}
	public void setEastBoundSignal(String signalName) {eastBoundSignalName = signalName;}
	public String getWestBoundSignal() {return westBoundSignalName;}
	public void setWestBoundSignal(String signalName) {westBoundSignalName = signalName;}
    
    public String getEastBoundSensor() {return eastBoundSensorName;}
	public void setEastBoundSensor(String sensorName) {eastBoundSensorName = sensorName;}
	public String getWestBoundSensor() {return westBoundSensorName;}
	public void setWestBoundSensor(String sensorName) {westBoundSensorName = sensorName;}
    
	public String getEastBoundSignalMast() {return eastBoundSignalMastName;}
	public void setEastBoundSignalMast(String signalMastName) {eastBoundSignalMastName = signalMastName;}
	public String getWestBoundSignalMast() {return westBoundSignalMastName;}
	public void setWestBoundSignalMast(String signalMastName) {westBoundSignalMastName = signalMastName;}
    
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
		connect1 = p.findTrackSegmentByName(trackSegment1Name);
		connect2 = p.findTrackSegmentByName(trackSegment2Name);
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
			else if ( (type!=END_BUMPER) && (connect2==null) ) {
				connect2 = track;
                if(connect1.getLayoutBlock()==connect2.getLayoutBlock()){
                    setWestBoundSignalMast("");
                    setEastBoundSignalMast("");
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
		}
		else if (track==connect2) {
			connect2 = null;
		}
		else {
			log.error ("Attempt to remove non-existant track connection");
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PositionablePoint.class.getName());

}