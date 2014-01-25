package jmri.jmrit.display.layoutEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.JmriJFrame;
import jmri.Turnout;
import jmri.SignalMast;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import jmri.NamedBeanHandle;
import jmri.InstanceManager;
import jmri.util.swing.JmriBeanComboBox;

import java.util.ResourceBundle;
import java.util.Hashtable;
import java.util.Map.Entry;


import javax.swing.*;

/**
 * A LayoutSlip is two track segment on a layout that cross at an angle.
 * <P>
 * A LayoutSlip has four connection points, designated A, B, C, and D.
 *		At the crossing, A-C and B-D are straight segments.  A train proceeds
 *		through the crossing on either of these segments.
 * <P>
 * For drawing purposes, each LayoutSlip carries a center point and displacements
 *		for A and B.  The displacements for C = - the displacement for A, and the
 *		displacement for D = - the displacement for B.  The center point and these
 *      displacements may be adjusted by the user when in edit mode.
 * <P>
 * When LayoutSlips are first created, there are no connections.  Block information
 *		and connections are added when available.  
 * <P>
 * Signal Head names are saved here to keep track of where signals are. LayoutSlip 
 *		only serves as a storage place for signal head names. The names are placed here
 *		by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision: 19729 $
 */

public class LayoutSlip extends LayoutTurnout
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");
    
	// operational instance variables (not saved between sessions)
    
    final public static int UNKNOWN = Turnout.UNKNOWN;
    final public static int STATE_AC = 0x02;
    final public static int STATE_BD = 0x04;
    final public static int STATE_AD = 0x06;
    final public static int STATE_BC = 0x08;
    
    public int currentState = UNKNOWN;
    
    private String turnoutBName="";
    private NamedBeanHandle<Turnout> namedTurnoutB = null;
    private java.beans.PropertyChangeListener mTurnoutListener = null;
	/** 
	 * constructor method
	 */  
	public LayoutSlip(String id, Point2D c, double rot, LayoutEditor myPanel, int type) {
		instance = this;
		layoutEditor = myPanel;
		ident = id;
		center = c;
        dispC = new Point2D.Double(-20.0,0.0);
        dispB = new Point2D.Double(-14.0,14.0);
        setTurnoutType(type);
        rotateCoords(rot);
    }
    
    public void setTurnoutType(int slipType){
        setSlipType(slipType);
    }
    
    public void setSlipType(int slipType){
        if(type==slipType)
            return;
        type=slipType;
        if(type==DOUBLE_SLIP){
            turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.CLOSED));
            turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
            turnoutStates.put(STATE_AD, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
            turnoutStates.put(STATE_BC, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
        } else {
            turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
            turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
            turnoutStates.put(STATE_AD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
            turnoutStates.remove(STATE_BC);
        }
    }
    
    public int getSlipType(){
        return type;
    }
    
    public int getSlipState(){
        return currentState;
    }
    
    public String getTurnoutBName() {
        if (namedTurnoutB!=null)
            return namedTurnoutB.getName();
        return turnoutBName;
    }
    
    public Turnout getTurnoutB() {
		if (namedTurnoutB==null) {
			// set physical turnout if possible and needed
			setTurnoutB(turnoutBName);
            if (namedTurnoutB==null)
                return null;
		}
		return namedTurnoutB.getBean();
	}
    
    public void setTurnoutB(String tName) {
		if (namedTurnoutB!=null) deactivateTurnout();
		turnoutBName = tName;
		Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().
                            getTurnout(turnoutBName);
		if (turnout!=null) {
            namedTurnoutB = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutBName, turnout);
			activateTurnout();
		}
		else {
			turnoutBName = "";
            namedTurnoutB = null;
		}
	}
    
    public String getDisplayName(){
        String name = "Slip :";
        if(getTurnoutName()!=null){
            name += "("+getTurnoutName();
            if(getTurnoutBName()!=null)
                name+=":"+getTurnoutBName()+")";
        }
        else if(getTurnoutBName()!=null){
            name += "("+getTurnoutBName()+")";
        }
        return name;
    }
    
    /**
	 * Toggle slip states if clicked on, physical turnout exists, and
	 *    not disabled
	 */
    public void toggleState() {
        switch(currentState) {
            case STATE_AC : if(singleSlipStraightEqual()) {
                                setTurnoutState(turnoutStates.get(STATE_AD));
                                currentState = STATE_AD;
                            } else {
                                setTurnoutState(turnoutStates.get(STATE_BD));
                                currentState = STATE_BD;
                            }
                            break;
            case STATE_BD :
                            setTurnoutState(turnoutStates.get(STATE_AD));
                            currentState = STATE_AD;
                            break;
            case STATE_AD : if(type==SINGLE_SLIP){
                                setTurnoutState(turnoutStates.get(STATE_AC));
                                currentState = STATE_AC;
                            } else {
                                setTurnoutState(turnoutStates.get(STATE_BC));
                                currentState = STATE_BC;
                            }
                            break;
            case STATE_BC : 
                            setTurnoutState(turnoutStates.get(STATE_AC));
                            currentState = STATE_AC;
                            break;
            default       : 
                            setTurnoutState(turnoutStates.get(STATE_BD));
                            currentState = STATE_BD;
                            break;
        }
        
    }
    
    void setTurnoutState(TurnoutState ts){
        if(getTurnout()!=null)
            getTurnout().setCommandedState(ts.getTurnoutAState());
        if(getTurnoutB()!=null)
            getTurnoutB().setCommandedState(ts.getTurnoutBState());
    }
    
    /**
	 * Activate/Deactivate turnout to redraw when turnout state changes
	 */
	private void activateTurnout() {
		if (namedTurnout!=null) {
			namedTurnout.getBean().addPropertyChangeListener(mTurnoutListener =
								new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
                    updateState();
				}
			}, namedTurnout.getName(), "Layout Editor Slip");
		}
		if (namedTurnoutB!=null) {
			namedTurnoutB.getBean().addPropertyChangeListener(mTurnoutListener =
								new java.beans.PropertyChangeListener() {
				public void propertyChange(java.beans.PropertyChangeEvent e) {
					updateState();
				}
			}, namedTurnoutB.getName(), "Layout Editor Slip");
		}
	}
	private void deactivateTurnout() {
		if (mTurnoutListener!=null) {
			namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            if(namedTurnoutB!=null){
                namedTurnoutB.getBean().removePropertyChangeListener(mTurnoutListener);
            }
			mTurnoutListener = null;
		}
	}

	public Point2D getCoordsCenter() {return center;}
	public Point2D getCoordsA() {
		double x = center.getX() + dispC.getX();
		double y = center.getY() + dispC.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsB() {
		double x = center.getX() + dispB.getX();
		double y = center.getY() + dispB.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsC() {
		double x = center.getX() - dispC.getX();
		double y = center.getY() - dispC.getY();
		return new Point2D.Double(x,y);
	}
	public Point2D getCoordsD() {
		double x = center.getX() - dispB.getX();
		double y = center.getY() - dispB.getY();
		return new Point2D.Double(x,y);
	}

	private void updateBlockInfo() {
		LayoutBlock b1 = null;
		LayoutBlock b2 = null;
		if (block!=null) block.updatePaths();
		if (connectA!=null) {
			b1 = ((TrackSegment)connectA).getLayoutBlock();
			if ((b1!=null)&&(b1!=block)) b1.updatePaths();
		}
		if (connectC!=null) {
			b2 = ((TrackSegment)connectC).getLayoutBlock();
			if ((b2!=null)&&(b2!=block)&&(b2!=b1)) b2.updatePaths();
		}

		if (connectB!=null) {
			b1 = ((TrackSegment)connectB).getLayoutBlock();
			if ((b1!=null)&&(b1!=block)) b1.updatePaths();
		}
		if (connectD!=null) {
			b2 = ((TrackSegment)connectD).getLayoutBlock();
			if ((b2!=null)&&(b2!=block)&&(b2!=b1)) b2.updatePaths();
		}
        reCheckBlockBoundary();
	}
    
    public void reCheckBlockBoundary(){
        if(connectA==null && connectB==null && connectC==null && connectD==null){
            //This is no longer a block boundary, therefore will remove signal masts and sensors if present
            if(signalAMastNamed!=null)
                removeSML(getSignalAMast());
            if(signalBMastNamed!=null)
                removeSML(getSignalBMast());
            if(signalCMastNamed!=null)
                removeSML(getSignalCMast());
            if(signalDMastNamed!=null)
                removeSML(getSignalDMast());
            signalAMastNamed = null;
            signalBMastNamed = null;
            signalCMastNamed = null;
            signalDMastNamed = null;
            sensorANamed=null;
            sensorBNamed=null;
            sensorCNamed=null;
            sensorDNamed=null;
            return;
            //May want to look at a method to remove the assigned mast from the panel and potentially any logics generated
        }  else if(connectA==null || connectB==null || connectC==null || connectD==null){
            //could still be in the process of rebuilding the point details
            return;
        } 
        
        TrackSegment trkA;
        TrackSegment trkB;
        TrackSegment trkC;
        TrackSegment trkD;
        
        if(connectA instanceof TrackSegment){
            trkA = (TrackSegment)connectA;
            if(trkA.getLayoutBlock()==block){
               if(signalAMastNamed!=null)
                    removeSML(getSignalAMast());
                signalAMastNamed = null;
                sensorANamed=null;
            }
        }
        if(connectC instanceof TrackSegment) {
            trkC = (TrackSegment)connectC;
            if(trkC.getLayoutBlock()==block){
               if(signalCMastNamed!=null)
                    removeSML(getSignalCMast());
                signalCMastNamed = null;
                sensorCNamed=null;
            }
        }
        if(connectB instanceof TrackSegment){
            trkB = (TrackSegment)connectB;
            if(trkB.getLayoutBlock()==block){
               if(signalBMastNamed!=null)
                    removeSML(getSignalBMast());
                signalBMastNamed = null;
                sensorBNamed=null;
            }
        }

        if(connectD instanceof TrackSegment) {
            trkD = (TrackSegment)connectC;
            if(trkD.getLayoutBlock()==block){
               if(signalDMastNamed!=null)
                    removeSML(getSignalDMast());
                signalDMastNamed = null;
                sensorDNamed=null;
            }
        }
    }
    
	/** 
	 * Methods to test if mainline track or not
	 *  Returns true if either connecting track segment is mainline
	 *  Defaults to not mainline if connecting track segments are missing
	 */
	public boolean isMainline() {
		if ( ((connectA != null) && (((TrackSegment)connectA).getMainline())) ||
			((connectB != null) && (((TrackSegment)connectB).getMainline())) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Modify coordinates methods
	 */
	public void setCoordsCenter(Point2D p) {
		center = p;
	}
	public void setCoordsA(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispC = new Point2D.Double(-x,-y);
	}
	public void setCoordsB(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(-x,-y);
	}
	public void setCoordsC(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispC = new Point2D.Double(x,y);
	}
	public void setCoordsD(Point2D p) {
		double x = center.getX() - p.getX();
		double y = center.getY() - p.getY();
		dispB = new Point2D.Double(x,y);
	}
	public void scaleCoords(float xFactor, float yFactor) {
		Point2D pt = new Point2D.Double(round(center.getX()*xFactor),
										round(center.getY()*yFactor));
		center = pt;
		pt = new Point2D.Double(round(dispC.getX()*xFactor),
										round(dispC.getY()*yFactor));
		dispC = pt;
		pt = new Point2D.Double(round(dispB.getX()*xFactor),
										round(dispB.getY()*yFactor));
		dispB = pt;
	}
	double round (double x) {
		int i = (int)(x+0.5);
		return i;
	}

	/**
	 * Initialization method
	 *   The above variables are initialized by PositionablePointXml, then the following
	 *        method is called after the entire LayoutEditor is loaded to set the specific
	 *        TrackSegment objects.
	 */
	public void setObjects(LayoutEditor p) {
		connectA = p.findTrackSegmentByName(connectAName);
        connectB = p.findTrackSegmentByName(connectBName);
		connectC = p.findTrackSegmentByName(connectCName);
		connectD = p.findTrackSegmentByName(connectDName);
		if (tBlockName.length()>0) {
			block = p.getLayoutBlock(tBlockName);
			if (block!=null) {
				blockName = tBlockName;
				block.incrementUse();
			}
			else {
				log.error("bad blocknameac '"+tBlockName+"' in slip "+ident);
			}
		}
	}

    JPopupMenu popup = null;
	LayoutEditorTools tools = null;
    /**
     * Display popup menu for information and editing
     */
    protected void showPopUp(MouseEvent e, boolean editable) {
        if (popup != null ) {
			popup.removeAll();
		}
		else {
            popup = new JPopupMenu();
		}
        if(editable){
            popup.add(getName());
            boolean blockAssigned = false;
            if ( (blockName==null) || (blockName.equals("")) ) popup.add(rb.getString("NoBlock"));
            else {
                popup.add(rb.getString("BlockID")+": "+getLayoutBlock().getID());
                blockAssigned = true;
            }

            popup.add(new JSeparator(JSeparator.HORIZONTAL));
            popup.add(new AbstractAction(rb.getString("Edit")) {
                    public void actionPerformed(ActionEvent e) {
                        editLayoutSlip(instance);
                    }
                });
            popup.add(new AbstractAction(rb.getString("Remove")) {
                    public void actionPerformed(ActionEvent e) {
                        if (layoutEditor.removeLayoutSlip(instance)) {
                            // Returned true if user did not cancel
                            remove();
                            dispose();
                        }
                    }
                });
            if ( (connectA==null) && (connectB==null) &&
                        (connectC==null) && (connectD==null) ) {
                JMenuItem rotateItem = new JMenuItem(rb.getString("Rotate")+"...");
                popup.add(rotateItem);
                rotateItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        boolean entering = true;
                        boolean error = false;
                        String newAngle = "";
                        while (entering) {
                            // prompt for rotation angle
                            error = false;
                            newAngle = JOptionPane.showInputDialog(layoutEditor, 
                                                rb.getString("EnterRotation")+" :");
                            if (newAngle.length()<1) return;  // cancelled
                            double rot = 0.0;
                            try {
                                rot = Double.parseDouble(newAngle);
                            }
                            catch (Exception e) {
                                JOptionPane.showMessageDialog(layoutEditor,rb.getString("Error3")+
                                    " "+e,rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
                                error = true;
                                newAngle = "";
                            }
                            if (!error) {
                                entering = false;
                                if (rot!=0.0) {
                                   rotateCoords(rot);
                                   layoutEditor.redrawPanel();
                                }
                            }
                        }
                    }
                });
            }
            if (blockAssigned) {
                popup.add(new AbstractAction(rb.getString("SetSignals")) {
                    public void actionPerformed(ActionEvent e) {
                            if (tools == null) {
                                tools = new LayoutEditorTools(layoutEditor);
                            }
                        tools.setSlipFromMenu((LayoutSlip)instance,
                                layoutEditor.signalIconEditor,layoutEditor.signalFrame);
                    }
                });
            }

            final String[] boundaryBetween = getBlockBoundaries();
            boolean blockBoundaries = false;
            
            for (int i = 0; i<4; i++){
                if(boundaryBetween[i]!=null)
                    blockBoundaries=true;
            }
            if (blockBoundaries){
                 popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                    public void actionPerformed(ActionEvent e) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        tools.setSignalMastsAtSlipFromMenu((LayoutSlip)instance, boundaryBetween, layoutEditor.signalFrame);
                    }
                });
                 popup.add(new AbstractAction(rb.getString("SetSensors")) {
                    public void actionPerformed(ActionEvent e) {
                        if (tools == null) {
                            tools = new LayoutEditorTools(layoutEditor);
                        }
                        tools.setSensorsAtSlipFromMenu((LayoutSlip)instance, boundaryBetween, layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                    }
                });
            }
            
            if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()){
                if(blockAssigned){
                    popup.add(new AbstractAction(rb.getString("ViewBlockRouting")) {
                        public void actionPerformed(ActionEvent e) {
                            AbstractAction  routeTableAction = new  LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                            routeTableAction.actionPerformed(e);
                        }
                    });
                }
            }
            setAdditionalEditPopUpMenu(popup);
            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        } else if(!viewAdditionalMenu.isEmpty()){
            setAdditionalViewPopUpMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    
    public String[] getBlockBoundaries(){
        final String[] boundaryBetween = new String[4];
        
        if ( (blockName!=null) && (!blockName.equals("")) && (block!=null) ){
            if ((connectA instanceof TrackSegment) && (((TrackSegment)connectA).getLayoutBlock()!=block)){
                try {
                	boundaryBetween[0]=(((TrackSegment)connectA).getLayoutBlock().getDisplayName()+ " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e){
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection A doesn't contain a layout block");
                }
            }        
            if ((connectC instanceof TrackSegment) && (((TrackSegment)connectC).getLayoutBlock()!=block)){
            	try {
                	boundaryBetween[2]=(((TrackSegment)connectC).getLayoutBlock().getDisplayName()+ " - " + block.getDisplayName());
	            } catch (java.lang.NullPointerException e){
	                //Can be considered normal if tracksegement hasn't yet been allocated a block
	                log.debug("TrackSegement at connection C doesn't contain a layout block");
	            }
            }
            if ((connectB instanceof TrackSegment) && (((TrackSegment)connectB).getLayoutBlock()!=block)){
            	try {
                	boundaryBetween[1]=(((TrackSegment)connectB).getLayoutBlock().getDisplayName()+ " - " + block.getDisplayName());
	            } catch (java.lang.NullPointerException e){
	                //Can be considered normal if tracksegement hasn't yet been allocated a block
	                log.debug("TrackSegement at connection B doesn't contain a layout block");
	            }
            }
            if ((connectD instanceof TrackSegment) && (((TrackSegment)connectD).getLayoutBlock()!=block)){
            	try {
                	boundaryBetween[3]=(((TrackSegment)connectD).getLayoutBlock().getDisplayName()+ " - " + block.getDisplayName());
	            } catch (java.lang.NullPointerException e){
	                //Can be considered normal if tracksegement hasn't yet been allocated a block
	                log.debug("TrackSegement at connection D doesn't contain a layout block");
	            }
            }
        }
        return boundaryBetween;
    }

	// variables for Edit slip Crossing pane
	JButton slipEditDone;
	JButton slipEditCancel;
	JButton turnoutEditBlock;
	boolean editOpen = false;
    private JmriBeanComboBox turnoutAComboBox;
    private JmriBeanComboBox turnoutBComboBox;
	
    /**
     * Edit a Slip
     */
	protected void editLayoutSlip(LayoutTurnout o) {
		if (editOpen) {
			editLayoutTurnoutFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (editLayoutTurnoutFrame == null) {
            editLayoutTurnoutFrame = new JmriJFrame( rb.getString("EditSlip"), false, true );
            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutSlip", true);
            editLayoutTurnoutFrame.setLocation(50,30);
            Container contentPane = editLayoutTurnoutFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel turnoutNameLabel = new JLabel( rb.getString("Turnout")+" A "+rb.getString("Name") );
            turnoutAComboBox = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance(), getTurnout(), JmriBeanComboBox.DISPLAYNAME);
            panel1.add(turnoutNameLabel);
            panel1.add(turnoutAComboBox);
            contentPane.add(panel1);
            JPanel panel1a = new JPanel(); 
            panel1a.setLayout(new FlowLayout());
			JLabel turnoutBNameLabel = new JLabel( rb.getString("Turnout")+" B "+rb.getString("Name") );
            turnoutBComboBox = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance(), getTurnoutB(), JmriBeanComboBox.DISPLAYNAME);
            panel1a.add(turnoutBNameLabel);
            panel1a.add(turnoutBComboBox);
            contentPane.add(panel1a);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayout(0,3, 2, 2));
            
            panel2.add(new Label("   "));
            panel2.add(new Label(rb.getString("Turnout")+" A:"));
            panel2.add(new Label(rb.getString("Turnout")+" B:"));
            for(Entry <Integer, TurnoutState> ts: turnoutStates.entrySet()){
                SampleStates draw = new SampleStates(ts.getKey());
                draw.repaint();
                draw.setPreferredSize(new Dimension(40,40));
                panel2.add(draw);
                
                panel2.add(ts.getValue().getComboA());
                panel2.add(ts.getValue().getComboB());
            }
            

            testPanel = new TestState();
            testPanel.setSize(40,40);
            testPanel.setPreferredSize(new Dimension(40,40));
            panel2.add(testPanel);
            JButton testButton = new JButton("Test");
            testButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    toggleStateTest();
                }
            });
            panel2.add(testButton);
            contentPane.add(panel2);
			// setup block name
            JPanel panel3 = new JPanel(); 
            panel3.setLayout(new FlowLayout());
			JLabel block1NameLabel = new JLabel( rb.getString("BlockID") );
            panel3.add(block1NameLabel);
            panel3.add(blockNameField);
            blockNameField.setToolTipText( rb.getString("EditBlockNameHint") );
            contentPane.add(panel3);
			// set up Edit Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
			// Edit Block
            panel4.add(turnoutEditBlock = new JButton(rb.getString("EditBlock")));
            turnoutEditBlock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    turnoutEditBlockPressed(e);
                }
            });
            turnoutEditBlock.setToolTipText( rb.getString("EditBlockHint") );

            contentPane.add(panel4);		
			// set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(slipEditDone = new JButton(rb.getString("Done")));
            slipEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    slipEditDonePressed(e);
                }
            });
            slipEditDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(slipEditCancel = new JButton(rb.getString("Cancel")));
            slipEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    slipEditCancelPressed(e);
                }
            });
            slipEditCancel.setToolTipText( rb.getString("CancelHint") );
            contentPane.add(panel5);		
		}
		// Set up for Edit
		blockNameField.setText(blockName);

		editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					slipEditCancelPressed(null);
				}
			});
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);	
		editOpen = true;
		needsBlockUpdate = false;	
	}

    void drawSlipState(int state, Graphics2D g2){
        int ctrX = 20;
        int ctrY = 20;
        Point2D ldispA = new Point2D.Double(-20.0,0.0);
        Point2D ldispB = new Point2D.Double(-14.0,14.0);
        g2.setColor(Color.black);
        
        Point2D A = new Point2D.Double(ctrX+ldispA.getX(), ctrY+ldispA.getY());
        Point2D B = new Point2D.Double(ctrX+ldispB.getX(), ctrY+ldispB.getY());
        Point2D C = new Point2D.Double(ctrX-ldispA.getX(), ctrY-ldispA.getY());
        Point2D D = new Point2D.Double(ctrX-ldispB.getX(), ctrY-ldispB.getY());
        
        g2.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,
															BasicStroke.JOIN_ROUND));
                                                            
        g2.draw(new Line2D.Double(A,
            layoutEditor.third(A,C)));
        g2.draw(new Line2D.Double(C,
            layoutEditor.third(C,A)));
        
        if(state==STATE_AC || state==STATE_BD || state==UNKNOWN){
            g2.draw(new Line2D.Double(A,
            layoutEditor.third(A,D)));
            
            g2.draw(new Line2D.Double(D,
                layoutEditor.third(D,A)));
            
            if(getSlipType()==LayoutSlip.DOUBLE_SLIP){
                g2.draw(new Line2D.Double(B,
                    layoutEditor.third(B,C)));
                    
                g2.draw(new Line2D.Double(C,
                    layoutEditor.third(C,B)));
            }
        } else {
            g2.draw(new Line2D.Double(B,
                layoutEditor.third(B,D)));
            g2.draw(new Line2D.Double(D,
                layoutEditor.third(D,B)));
        }
         
        if(getSlipType()==LayoutSlip.DOUBLE_SLIP){
            if (state==LayoutSlip.STATE_AC){
                g2.draw(new Line2D.Double(B,
                    layoutEditor.third(B,D)));
                g2.draw(new Line2D.Double(D,
                    layoutEditor.third(D,B)));
                
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A,C));

            } else if (state==LayoutSlip.STATE_BD){
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B,D));
            
            } else if (state==LayoutSlip.STATE_AD){
                g2.draw(new Line2D.Double(B,
                    layoutEditor.third(B,C)));
                
                g2.draw(new Line2D.Double(C,
                    layoutEditor.third(C,B)));
                    
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A,D));
            
            } else if (state==LayoutSlip.STATE_BC){
            
                g2.draw(new Line2D.Double(A,
                layoutEditor.third(A,D)));
                
                g2.draw(new Line2D.Double(D,
                    layoutEditor.third(D,A)));
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B,C));
            }
            else {
                g2.draw(new Line2D.Double(B,
                    layoutEditor.third(B,D)));
                g2.draw(new Line2D.Double(D,
                    layoutEditor.third(D,B)));
            }
        } else {
            g2.draw(new Line2D.Double(A,
                layoutEditor.third(A,D)));
                
            g2.draw(new Line2D.Double(D,
                layoutEditor.third(D,A)));
            if (state==LayoutSlip.STATE_AD){
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A,D));
            
            } else if (state==LayoutSlip.STATE_AC){
                g2.draw(new Line2D.Double(B,
                    layoutEditor.third(B,D)));
                g2.draw(new Line2D.Double(D,
                    layoutEditor.third(D,B)));
                
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A,C));
            
            } else if (state==LayoutSlip.STATE_BD){
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B,D));
            
            } else {
                g2.draw(new Line2D.Double(B,
                    layoutEditor.third(B,D)));
                g2.draw(new Line2D.Double(D,
                    layoutEditor.third(D,B)));
            }
        }
    
    }
    
    class SampleStates extends JPanel {
    // Methods, constructors, fields.
        SampleStates(int state){
            super();
            this.state = state;
        }
        int state;
        @Override 
        public void paintComponent(Graphics g) {
            super.paintComponent(g);    // paints background
            Graphics2D g2 = (Graphics2D) g;
            drawSlipState(state, g2);
        }
    }
    
    int testState = UNKNOWN;
    /**
	 * Toggle slip states if clicked on, physical turnout exists, and
	 *    not disabled
	 */
    public void toggleStateTest() {
        int turnAState;
        int turnBState;
        switch(testState) {
            case STATE_AC : turnAState = turnoutStates.get(STATE_BD).getTestTurnoutAState();
                            turnBState = turnoutStates.get(STATE_BD).getTestTurnoutBState();
                            testState = STATE_BD;
                            break;
            case STATE_BD :
                            turnAState = turnoutStates.get(STATE_AD).getTestTurnoutAState();
                            turnBState = turnoutStates.get(STATE_AD).getTestTurnoutBState();
                            testState = STATE_AD;
                            break;
            case STATE_AD : if(type==SINGLE_SLIP){
                                turnAState = turnoutStates.get(STATE_AC).getTestTurnoutAState();
                                turnBState = turnoutStates.get(STATE_AC).getTestTurnoutBState();
                                testState = STATE_AC;
                            } else {
                                turnAState = turnoutStates.get(STATE_BC).getTestTurnoutAState();
                                turnBState = turnoutStates.get(STATE_BC).getTestTurnoutBState();
                                testState = STATE_BC;
                            }
                            break;
            case STATE_BC : 
                            turnAState = turnoutStates.get(STATE_AC).getTestTurnoutAState();
                            turnBState = turnoutStates.get(STATE_AC).getTestTurnoutBState();
                            testState = STATE_AC;
                            break;
            default       : 
                            turnAState = turnoutStates.get(STATE_BD).getTestTurnoutAState();
                            turnBState = turnoutStates.get(STATE_BD).getTestTurnoutBState();
                            testState = STATE_BD;
                            break;
        
        }
        ((Turnout)turnoutAComboBox.getSelectedBean()).setCommandedState(turnAState);
        ((Turnout)turnoutBComboBox.getSelectedBean()).setCommandedState(turnBState);
        /*if(getTurnout()!=null)
            getTurnout().setCommandedState(turnAState);
        if(getTurnoutB()!=null)
            getTurnoutB().setCommandedState(turnBState);*/
        if(testPanel!=null)
            testPanel.repaint();
    }
    
    class TestState extends JPanel {
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            drawSlipState(testState, g2);
        }
    }
    
    TestState testPanel;
    
	void slipEditDonePressed(ActionEvent a) {
        if ( !turnoutName.equals(turnoutAComboBox.getSelectedDisplayName()) ) {
            String newName = turnoutAComboBox.getSelectedDisplayName();
            if ( layoutEditor.validatePhysicalTurnout(newName,
                            editLayoutTurnoutFrame) ) {
                setTurnout(newName);
            }
            else {
                namedTurnout = null;
                turnoutName = "";
            }
            needRedraw = true;
        }
        if ( !turnoutBName.equals(turnoutBComboBox.getSelectedDisplayName()) ) {
            String newName = turnoutBComboBox.getSelectedDisplayName();
            if ( layoutEditor.validatePhysicalTurnout(newName,
                            editLayoutTurnoutFrame) ) {
                setTurnoutB(newName);
            }
            else {
                namedTurnoutB = null;
                turnoutBName = "";
            }
            needRedraw = true;
        }
		if ( !blockName.equals(blockNameField.getText().trim()) ) {
			// block 1 has changed, if old block exists, decrement use
			if ( (block!=null)) {
				block.decrementUse();
			}
			// get new block, or null if block has been removed
			blockName = blockNameField.getText().trim();
			//if ( (blockName!=null) && (blockName.length()>0)) {
				block = layoutEditor.provideLayoutBlock(blockName);

				if(block==null) {
					blockName = "";
					blockNameField.setText("");
				}
			//}
			//else {
			//	block = null;
			//	blockName = "";
			//}
			needRedraw = true;
			layoutEditor.auxTools.setBlockConnectivityChanged();
			needsBlockUpdate = true;
		}
        for(TurnoutState ts: turnoutStates.values()){
            ts.updateStatesFromCombo();
        }
		editOpen = false;
		editLayoutTurnoutFrame.setVisible(false);
		editLayoutTurnoutFrame.dispose();
		editLayoutTurnoutFrame = null;
		if (needsBlockUpdate) updateBlockInfo();
		if (needRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
		}
	}
	void slipEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editLayoutTurnoutFrame.setVisible(false);
		editLayoutTurnoutFrame.dispose();
		editLayoutTurnoutFrame = null;
		if (needsBlockUpdate) updateBlockInfo();
		if (needRedraw) {
			layoutEditor.redrawPanel();
			layoutEditor.setDirty();
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
        
        disableSML(getSignalAMast());
        disableSML(getSignalBMast());
        disableSML(getSignalCMast());
        disableSML(getSignalDMast());
        removeSML(getSignalAMast());
        removeSML(getSignalBMast());
        removeSML(getSignalCMast());
        removeSML(getSignalDMast());
        // remove from persistance by flagging inactive
        active = false;
    }
    
    void disableSML(SignalMast signalMast){
        if(signalMast==null)
            return;
        InstanceManager.signalMastLogicManagerInstance().disableLayoutEditorUse(signalMast);
    }

    boolean active = true;
    /**
     * "active" means that the object is still displayed, and should be stored.
     */
    public boolean isActive() {
        return active;
    }
    
    public boolean singleSlipStraightEqual(){
        if(type!=SINGLE_SLIP)
            return false;
        return turnoutStates.get(STATE_AC).equals(turnoutStates.get(STATE_BD));
    }
    
    Hashtable <Integer, TurnoutState> turnoutStates = new Hashtable<Integer, TurnoutState>(4);
    
    public int getTurnoutState(Turnout turn, int state){
        if(turn==getTurnout()){
            return getTurnoutState(state);
        }
        return getTurnoutBState(state);
    }
    
    public int getTurnoutState(int state){
        return turnoutStates.get(Integer.valueOf(state)).getTurnoutAState();
    }
    
    public int getTurnoutBState(int state){
        return turnoutStates.get(Integer.valueOf(state)).getTurnoutBState();
    }
    
    public void setTurnoutStates(int state, String turnStateA, String turnStateB){
        if(!turnoutStates.containsKey(state)){
            log.error("Trying to set invalid state for slip " + getDisplayName());
            return;
        }
        turnoutStates.get(state).setTurnoutAState(Integer.valueOf(turnStateA));
        turnoutStates.get(state).setTurnoutBState(Integer.valueOf(turnStateB));
    }
    
    //Internal call to update the state of the slip depending upon the turnout states.
    void updateState(){
        int state_a = getTurnout().getKnownState();
        int state_b = getTurnoutB().getKnownState();
        for(Entry<Integer, TurnoutState> en: turnoutStates.entrySet()){
            if(en.getValue().getTurnoutAState()==state_a){
                if(en.getValue().getTurnoutBState()==state_b){
                    currentState=en.getKey();
                    layoutEditor.redrawPanel();
                    return;
                }
            }
        }
    }
    
    static class TurnoutState{
        int turnoutA = Turnout.CLOSED;
        int turnoutB = Turnout.CLOSED;
        JComboBox turnoutABox;
        JComboBox turnoutBBox;
    
        TurnoutState(int turnoutA, int turnoutB){
            this.turnoutA = turnoutA;
            this.turnoutB = turnoutB;
        }
        
        int getTurnoutAState(){
            return turnoutA;
        }
        
        int getTurnoutBState(){
            return turnoutB;
        }
        
        void setTurnoutAState(int state){
            turnoutA = state;
        }
        
        void setTurnoutBState(int state){
            turnoutB = state;
        }
        
        JComboBox getComboA(){
            if(turnoutABox==null){
                String state[] = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                                InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutABox = new JComboBox(state);
                if(turnoutA == Turnout.THROWN)
                    turnoutABox.setSelectedIndex(1);
            }
            return turnoutABox;
        }
        
        JComboBox getComboB(){
            if(turnoutBBox==null){
                String state[] = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                                InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutBBox = new JComboBox(state);
                if(turnoutB == Turnout.THROWN)
                    turnoutBBox.setSelectedIndex(1);
            }
            return turnoutBBox;
        }
        
        int getTestTurnoutAState(){
            if(turnoutABox.getSelectedIndex()==0)
                return Turnout.CLOSED;
            return Turnout.THROWN;
        }
        
        int getTestTurnoutBState(){
            if(turnoutBBox.getSelectedIndex()==0)
                return Turnout.CLOSED;
            return Turnout.THROWN;
        }
        
        void updateStatesFromCombo(){
            if(turnoutABox==null || turnoutBBox==null){
                return;
            }
            if(turnoutABox.getSelectedIndex()==0){
                turnoutA= Turnout.CLOSED;
            } else {
                turnoutA=Turnout.THROWN;
            }
            if(turnoutBBox.getSelectedIndex()==0){
                turnoutB= Turnout.CLOSED;
            } else {
                turnoutB=Turnout.THROWN;
            }
        }
        
        boolean equals(TurnoutState ts){
            if(ts.getTurnoutAState()!=this.getTurnoutAState())
                return false;
            if(ts.getTurnoutBState()!=this.getTurnoutBState())
                return false;
            return true;
        }
    
    }

    static Logger log = LoggerFactory.getLogger(LayoutSlip.class.getName());

}
