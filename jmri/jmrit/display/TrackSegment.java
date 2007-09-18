package jmri.jmrit.display;

import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.geom.*;
import java.lang.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.ResourceBundle;

import javax.swing.*;

/**
 * TrackSegment is a segment of track on a layout linking two nodes of the
 *     layout.  A node may be a PositionableTurnout, a LevelXing or a PositionablePoint.
 * <P>
 * PositionablePoints have only one possible connection point.  Positionable Turnouts
 *		have 3 or 4 (double crossover) connection points, designated A, B, C, and D.
 *		LevelXing's have 4 connection points, designated A, B, C, and D.
 * <P>
 * TrackSegments carry Block information, as do Positionable Turnouts.
 * <P>
 * TrackSegments may be drawn as dashed lines or solid lines.  In addition TrackSegments
 *		may be hidden when the panel is not in EditMode. 
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision: 1.2 $
 */

public class TrackSegment 
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

	// defined constants
	
	// operational instance variables (not saved between sessions)
	private LayoutBlock block = null;
	private TrackSegment instance = null;
	private LayoutEditor layoutEditor = null;
	
	// persistent instances variables (saved between sessions)
	private String ident = "";
	private String blockName = "";
	private Object connect1 = null;
	private int type1 = 0;
	private Object connect2 = null;
    private int type2 = 0;
	private boolean dashed = false;
	private boolean mainline = false;
	private boolean hidden = false;
	
    public TrackSegment(String id, Object c1, int t1, Object c2, int t2, boolean dash,
							boolean main, LayoutEditor myPanel) {
		layoutEditor = myPanel;
		// validate input
		if ( (c1==null) || (c2==null) ) {
			log.error("Invalid object in TrackSegment constructor call - "+id);
		}
		connect1 = c1;
		connect2 = c2;
		if ( (t1<LayoutEditor.POS_POINT) || (t1>LayoutEditor.LEVEL_XING_D) ) { 
			log.error("Invalid connect type 1 in TrackSegment constructor - "+id);
		}
		else {
			type1 = t1;
		}
		if ( (t2<LayoutEditor.POS_POINT) || (t2>LayoutEditor.LEVEL_XING_D) ) {
			log.error("Invalid connect type 2 in TrackSegment constructor - "+id);
		}
		else {
			type2 = t2;
		}
		instance = this;
		ident = id;
		dashed = dash;
		mainline = main;
    }
	// alternate constructor for loading layout editor panels
	public TrackSegment(String id, String c1Name, int t1, String c2Name, int t2, boolean dash,
							boolean main, boolean hide, LayoutEditor myPanel) {
		layoutEditor = myPanel;
		tConnect1Name = c1Name;
		type1 = t1;
		tConnect2Name = c2Name;
		type2 = t2;
		instance = this;
		ident = id;
		dashed = dash;
		mainline = main;
		hidden = hide;
	}	

	/**
	 * Accessor methods
	*/
	public String getID() {return ident;}
	public String getBlockName() {return blockName;}
	public int getType1() {return type1;}
	public int getType2() {return type2;}
	public Object getConnect1() {return connect1;}
	public Object getConnect2() {return connect2;}
	public boolean getDashed() {return dashed;}
	public void setDashed(boolean dash) {dashed = dash;} 
	public boolean getHidden() {return hidden;}
	public void setHidden(boolean hide) {hidden = hide;} 	
	public boolean getMainline() {return mainline;}
	public void setMainline(boolean main) {mainline = main;} 
	public LayoutBlock getLayoutBlock() {
		if ( (block==null) && (blockName!=null) && (blockName!="") ) {
			block = layoutEditor.provideLayoutBlock(blockName);
		}
		return block;
	}
	public String getConnect1Name() {return getConnectName(connect1,type1);}
	public String getConnect2Name() {return getConnectName(connect2,type2);}
	
	private String getConnectName(Object o,int type) {
		if (type == LayoutEditor.POS_POINT) {
			return ((PositionablePoint)o).getID();
		}
		if ( (type == LayoutEditor.TURNOUT_A) || (type == LayoutEditor.TURNOUT_B) ||
				(type == LayoutEditor.TURNOUT_C) || (type == LayoutEditor.TURNOUT_D) ) {
			return ((LayoutTurnout)o).getName();
		}
		if ( (type == LayoutEditor.LEVEL_XING_A) || (type == LayoutEditor.LEVEL_XING_B) ||
				(type == LayoutEditor.LEVEL_XING_C) || (type == LayoutEditor.LEVEL_XING_D) ) {
			return ((LevelXing)o).getID();
		}
		return "";
	}
	
	// initialization instance variables (used when loading a LayoutEditor)
	public String tBlockName = "";
	public String tConnect1Name = "";
	public String tConnect2Name = "";
	/**
	 * Initialization method
	 *   The above variables are initialized by PositionablePointXml, then the following
	 *        method is called after the entire LayoutEditor is loaded to set the specific
	 *        TrackSegment objects.
	 */
	public void setObjects(LayoutEditor p) {
		if (tBlockName.length()>0) {
			block = p.getLayoutBlock(tBlockName);
			if (block!=null) {
				blockName = tBlockName;
				block.incrementUse();
			}
			else {
				log.error("bad blockname '"+tBlockName+"' in tracksegment "+ident);
			}
		}
		connect1 = p.findObjectByTypeAndName(type1,tConnect1Name);
		connect2 = p.findObjectByTypeAndName(type2,tConnect2Name);
	}

	/**
	 * Set Up a Layout Block for a Track Segment
	 */
	public void setLayoutBlock (LayoutBlock b) {
		block = b;
		if (b!=null) {
			blockName = b.getID();
		}
	}
	public void setLayoutBlockByName (String name) {
		blockName = name;
	}

    JPopupMenu popup = null;

    /**
     * Display popup menu for information and editing
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null ) {
			popup.removeAll();
		}
		else {
            popup = new JPopupMenu();
		}
		popup.add(rb.getString("TrackSegment"));
		if (!dashed) popup.add(rb.getString("Style")+" - "+rb.getString("Solid"));
		else popup.add(rb.getString("Style")+" - "+rb.getString("Dashed"));
		if (!mainline) popup.add(rb.getString("NotMainline"));
		else popup.add(rb.getString("Mainline"));
		if (blockName=="") popup.add(rb.getString("NoBlock"));
		else popup.add(rb.getString("Block")+": "+getLayoutBlock().getID());
		if (hidden) popup.add(rb.getString("Hidden"));
		else popup.add(rb.getString("NotHidden"));
		popup.add(new JSeparator(JSeparator.HORIZONTAL));
		popup.add(new AbstractAction(rb.getString("Edit")) {
				public void actionPerformed(ActionEvent e) {
					editTrackSegment();
				}
			});
		popup.add(new AbstractAction(rb.getString("Remove")) {
				public void actionPerformed(ActionEvent e) {
					layoutEditor.removeTrackSegment(instance);
					remove();
					dispose();
				}
			});
		popup.show(e.getComponent(), e.getX(), e.getY());
    }

	// variables for Edit Track Segment pane
	private JmriJFrame editTrackSegmentFrame = null;
	private JComboBox dashedBox = new JComboBox();
    private int dashedIndex;
    private int solidIndex;
	private JComboBox mainlineBox = new JComboBox();
    private int mainlineTrackIndex;
    private int sideTrackIndex;
	private JTextField blockNameField = new JTextField(16);
	private JCheckBox hiddenBox = new JCheckBox(rb.getString("HideTrack"));
	private JButton segmentEditBlock;
	private JButton segmentEditDone;
	private JButton segmentEditCancel;
	private boolean editOpen = false;
	private boolean needsRedraw = false;

    /**
     * Edit a Track Segment 
     */
	protected void editTrackSegment() {
		if (editOpen) {
			editTrackSegmentFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (editTrackSegmentFrame == null) {
            editTrackSegmentFrame = new JmriJFrame( rb.getString("EditTrackSegment") );
			editTrackSegmentFrame.addHelpMenu("package.jmri.jmrit.display.EditTrackSegment", true);
            editTrackSegmentFrame.setLocation(50,30);
            Container contentPane = editTrackSegmentFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			// add dashed choice 
            JPanel panel31 = new JPanel(); 
            panel31.setLayout(new FlowLayout());
			dashedBox.removeAllItems();
			dashedBox.addItem( rb.getString("Solid") );
			solidIndex = 0;
			dashedBox.addItem( rb.getString("Dashed") );
			dashedIndex = 1;
			dashedBox.setToolTipText(rb.getString("DashedToolTip"));
			panel31.add (new JLabel(rb.getString("Style")+" : "));
			panel31.add (dashedBox);
            contentPane.add(panel31);			
			// add mainline choice 
            JPanel panel32 = new JPanel(); 
            panel32.setLayout(new FlowLayout());
			mainlineBox.removeAllItems();
			mainlineBox.addItem( rb.getString("Mainline") );
			mainlineTrackIndex = 0;
			mainlineBox.addItem( rb.getString("NotMainline") );
			sideTrackIndex = 1;
			mainlineBox.setToolTipText(rb.getString("MainlineToolTip"));
			panel32.add (mainlineBox);
            contentPane.add(panel32);			
			// add hidden choice 
            JPanel panel33 = new JPanel(); 
            panel33.setLayout(new FlowLayout());
			hiddenBox.setToolTipText(rb.getString("HiddenToolTip"));
			panel33.add (hiddenBox);
            contentPane.add(panel33);			
			// setup block name
            JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
			JLabel blockNameLabel = new JLabel( rb.getString("BlockID"));
            panel2.add(blockNameLabel);
            panel2.add(blockNameField);
            blockNameField.setToolTipText( rb.getString("EditBlockNameHint") );
            contentPane.add(panel2);
			// set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
			// Edit Block
            panel5.add(segmentEditBlock = new JButton(rb.getString("EditBlock")));
            segmentEditBlock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    segmentEditBlockPressed(e);
                }
            });
            segmentEditBlock.setToolTipText( rb.getString("EditBlockHint") );
            panel5.add(segmentEditDone = new JButton(rb.getString("Done")));
            segmentEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    segmentEditDonePressed(e);
                }
            });
            segmentEditDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(segmentEditCancel = new JButton(rb.getString("Cancel")));
            segmentEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    segmentEditCancelPressed(e);
                }
            });
            segmentEditCancel.setToolTipText( rb.getString("CancelHint") );
            contentPane.add(panel5);		
		}
		// Set up for Edit
		if (mainline) 
			mainlineBox.setSelectedIndex(mainlineTrackIndex);
		else
			mainlineBox.setSelectedIndex(sideTrackIndex);
		if (dashed)
			dashedBox.setSelectedIndex(dashedIndex);
		else
			dashedBox.setSelectedIndex(solidIndex);
		hiddenBox.setSelected(hidden);
		blockNameField.setText(blockName);
		editTrackSegmentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					segmentEditCancelPressed(null);
				}
			});
        editTrackSegmentFrame.pack();
        editTrackSegmentFrame.setVisible(true);		
		editOpen = true;
	}	
	void segmentEditBlockPressed(ActionEvent a) {
		// check if a block name has been entered
		if (!blockName.equals(blockNameField.getText()) ) {
			// block has changed, if old block exists, decrement use
			if (block!=null) {
				block.decrementUse();
			}
			// get new block, or null if block has been removed
			blockName = blockNameField.getText();
			block = layoutEditor.provideLayoutBlock(blockName);
			if (block == null) {
				blockName = "";
			}
			needsRedraw = true;
		}
		// check if a block exists to edit
		if (block==null) {
			JOptionPane.showMessageDialog(editTrackSegmentFrame,
					rb.getString("Error1"),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return;
		}
		block.editLayoutBlock(editTrackSegmentFrame);
		layoutEditor.setDirty();
		needsRedraw = true;
	}
	void segmentEditDonePressed(ActionEvent a) {
		// set dashed
		boolean oldDashed = dashed;
		if (dashedBox.getSelectedIndex() == dashedIndex) dashed = true;
		else dashed = false;
		// set mainline
		boolean oldMainline = mainline;
		if (mainlineBox.getSelectedIndex() == mainlineTrackIndex) mainline = true;
		else mainline = false;
		// set hidden
		boolean oldHidden = hidden;
		hidden = hiddenBox.isSelected();
		// check if anything changed
		if ( (oldDashed!=dashed) || (oldMainline!=mainline) || (oldHidden!=hidden) )
			needsRedraw = true;
		// check if Block changed
		if ( !blockName.equals(blockNameField.getText()) ) {
			// block has changed, if old block exists, decrement use
			if (block!=null) {
				block.decrementUse();
			}
			// get new block, or null if block has been removed
			blockName = blockNameField.getText();
			block = layoutEditor.provideLayoutBlock(blockName);
			if (block == null) {
				blockName = "";
			}
			needsRedraw = true;
		}
		editOpen = false;
		editTrackSegmentFrame.setVisible(false);
		if (needsRedraw) layoutEditor.redrawPanel();
		layoutEditor.setDirty();
	}
	void segmentEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editTrackSegmentFrame.setVisible(false);
		if (needsRedraw) {
			layoutEditor.setDirty();
			layoutEditor.redrawPanel();
		}
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrackSegment.class.getName());

}