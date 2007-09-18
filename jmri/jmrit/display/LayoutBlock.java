// jmri.jmrit.display.LayoutBlock.java

package jmri.jmrit.display;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.*;

import jmri.Sensor;
import jmri.AbstractNamedBean;

/**
 * A LayoutBlock is a group of track segments and turnouts on a layout 
 *       corresponding to a 'block'. LayoutBlock is an extension of the normal
 *       Block object, with its occupancy sensor added. 
 * <P>
 * LayoutBlocks may have an occupancy Sensor. The getOccupancy method returns 
 *		the occupancy state of the LayoutBlock - OCCUPIED, EMPTY, or UNKNOWN.
 *		If no occupancy sensor is provided, UNKNOWN is returned.
 * <P>
 * The name of each Layout Block is the same as that of the corresponding Block.
 * <P>
 * LayoutBlocks are "cross-panel", similar to sensors and turnouts.  A LayoutBlock
 *		may be used by more than one panel simultaneously.  As a consequence, 
 *		LayoutBlocks are saved with the configuration, not with a panel.
 * <P>
 * LayoutBlocks are used by TrackSegments, LevelXings, and LayoutTurnouts.
 *		LevelXings carry two LayoutBlock designations, which may be the same.
 *      LayoutTurnouts carry LayoutBlock designations also, one per turnout,
 *			except for double crossovers which can have up to four.
 * <P>
 * LayoutBlocks carry a use count.  The use count counts the number of track
 *		segments, layout turnouts, and levelcrossings which use the layout block.
 *		Only LayoutBlocks which have a use count greater than zero are saved when
 *		the configuration is saved.
 * <P>
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @version $Revision: 1.2 $
 */

public class LayoutBlock extends AbstractNamedBean
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.LayoutEditorBundle");

	// constants
	public static final int OCCUPIED = jmri.Block.OCCUPIED;
	public static final int EMPTY = jmri.Block.UNOCCUPIED;
	public static final int UNKNOWN = 0x08;  // must be a different bit from the above two

	// operational instance variables (not saved to disk)
	private int useCount = 0;
	private Sensor occupancySensor = null;
	private jmri.Block block = null;
	private int maxBlockNumber = 0;
	private LayoutBlock _instance = null;
    private ArrayList panels = new ArrayList();  // panels using this block
	
	private java.beans.PropertyChangeListener mSensorListener = null;

	// persistent instances variables (saved between sessions)
	public String blockName = "";
	public String occupancySensorName = "";
	public int occupiedSense = Sensor.ACTIVE;
	public Color blockTrackColor = Color.black;
	public Color blockOccupiedColor = Color.black;
	
	public LayoutBlock(String sName, String uName) {
		super (sName,uName);
		_instance = this;
		blockName = uName;
	}
	
	/**
	 * Accessor methods
	*/
	public String getID() {return blockName;}	
	public Color getBlockTrackColor() {return blockTrackColor;};
	public void setBlockTrackColor(Color color) {blockTrackColor = color;};	
	public Color getBlockOccupiedColor() {return blockOccupiedColor;};
	public void setBlockOccupiedColor(Color color) {blockOccupiedColor = color;};	
	public void incrementUse() {useCount ++;}
	public void decrementUse() {
		useCount --;
		if (useCount<=0) {
			useCount = 0;
		}
	}
	public int getUseCount() {return useCount;}
	
	/**
	 *  Keeps track of LayoutEditor panels that are using this LayoutBlock
	 */
	public void addLayoutEditor(LayoutEditor panel) {
		// add to the panels list if not already there
		if (panels.size()>0) {
			for (int i=0;i<panels.size();i++) {
				LayoutEditor ed = (LayoutEditor)panels.get(i);
				// simply return if already in list
				if (ed == panel) return;
			}
		}
		// not found, add it
		panels.add(panel);
	}
	public void deleteLayoutEditor(LayoutEditor panel) {
		// remove from the panels list if there
		if (panels.size()>0) {
			for (int i=0;i<panels.size();i++) {
				LayoutEditor ed = (LayoutEditor)panels.get(i);
				if (ed == panel) {
					panels.remove(i);
					return;
				}
			}
		}
	}
	public boolean isOnPanel(LayoutEditor panel) {
		// returns true if this Layout Block is used on panel
		if (panels.size()>0) {
			for (int i=0;i<panels.size();i++) {
				LayoutEditor ed = (LayoutEditor)panels.get(i);
				if (ed == panel) {
					return true;
				}
			}
		}
		return false;
	}			
	
	/**
	 *  Redraws panels using this layout block
	 */
	public void redrawLayoutBlockPanels() {
		if (panels.size()>0) {
			for (int i=0;i<panels.size();i++) {
				((LayoutEditor)panels.get(i)).redrawPanel();
			}
		}
	}

	/**
	 * Validates that the supplied occupancy sensor name corresponds to an existing sensor
	 *   and is unique among all blocks.  If valid, returns the sensor and sets the block 
	 *   sensor name in the block.  Else returns null, and does nothing to the block.
	 * This method also converts the sensor name to upper case if it is a system name.
	 */
	public Sensor validateSensor(String sensorName, Component openFrame) {
		// check if anything entered	
		if (sensorName.length()<1) {
			// no sensor entered
			return null;
		}
		// get the sensor corresponding to this name
		Sensor s = InstanceManager.sensorManagerInstance().getSensor(sensorName);
		if (s==null) {
			// There is no sensor corresponding to this name
			JOptionPane.showMessageDialog(openFrame,
					java.text.MessageFormat.format(rb.getString("Error7"),
					new String[]{sensorName}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		// ensure that this sensor is unique among defined Layout Blocks
		Sensor savedSensor = occupancySensor;
		String savedName = occupancySensorName;
		deactivateSensor();
		occupancySensor = null;
		LayoutBlock b = InstanceManager.layoutBlockManagerInstance().
											getBlockWithSensorAssigned(s);
		if (b!=null) {
			// sensor is not unique
			occupancySensor = savedSensor;
			if (occupancySensor!=null) activateSensor();
			JOptionPane.showMessageDialog(openFrame,
					java.text.MessageFormat.format(rb.getString("Error6"),
					new String[]{sensorName,b.getID()}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		// sensor is unique
		setOccupancySensorName(sensorName);
		return s;
	}
		
	/**
	 * Returns the color for drawing items in this block.  Returns color
	 *   based on block occupancy.
	 */
	public Color getBlockColor() {
		if (getOccupancy() == OCCUPIED) {
			return blockOccupiedColor;
		}
		else {
			return blockTrackColor;
		}
	}

	/**
	 * Get a Block corresponding to the block id passed on construction
	 */
	public jmri.Block getBlock() {
		// block id cannot be null.  If so return null;
		if (blockName == "") {
			return null;
		}
		// check if block already exists.  If so, don't create a new one.
		block = jmri.BlockManager.instance().getBlock(blockName);
		if (block==null) {
			// this is a normal case - Block may need to be created
			if ( (blockName.length()>2) && (blockName.charAt(0)=='I') && 
										(blockName.charAt(1)=='B') ) {
				// 'id' is a system name
				block = jmri.BlockManager.instance().createNewBlock(blockName,"");
			}
			else {
				// 'id' is a user name
				String sName = "IB" + blockName;
				block = jmri.BlockManager.instance().createNewBlock(sName,blockName);
			}
		}
		return block;
	}
			
	/**
	 * Returns occupancy Sensor name
	*/
	public String getOccupancySensorName() {
		return (occupancySensorName);
	}
			
	/**
	 * Returns occupancy Sensor
	*/
	public Sensor getOccupancySensor() {
		return (occupancySensor);
	}

	/**
	 * Add occupancy sensor by name
	 */
	public void setOccupancySensorName(String name) {
		occupancySensorName = name;
		occupancySensor = jmri.InstanceManager.sensorManagerInstance().
                            getSensor(name);
		activateSensor();
	}
	
	/**
	 * Get/Set occupied sense
	 */
	public int getOccupiedSense() {return occupiedSense;}
	public void setOccupiedSense(int sense) {occupiedSense = sense;}
	
	/**
	 * Test block occupancy
	 */
	public int getOccupancy() {
		if ( (occupancySensor == null) || (occupancySensorName.equals("")) ) {
			occupancySensor = jmri.InstanceManager.sensorManagerInstance().
                            getSensor(occupancySensorName);
			if (occupancySensor == null) {			
				// no occupancy sensor
				return (UNKNOWN);
			}
		}
		if (occupancySensor.getKnownState() != occupiedSense) {
			return (EMPTY);
		}
		else if (occupancySensor.getKnownState() == occupiedSense) {
			return (OCCUPIED);
		}
		return (UNKNOWN);
	}
	public int getState() {return getOccupancy();}
	// dummy for completion of NamedBean interface
	public void setState(int i) {}
	
	/** 
	 * Activate/deactivate occupancy sensor for redraw of panels on 
	 *	change of state
	 */
	private void activateSensor() {
		if (occupancySensor!=null) {
			occupancySensor.addPropertyChangeListener(mSensorListener =
								new java.beans.PropertyChangeListener() {
					public void propertyChange(java.beans.PropertyChangeEvent e) {
						if (panels.size()>0) {
							for (int i=0;i<panels.size();i++) {
								LayoutEditor ed = (LayoutEditor)panels.get(i);
								ed.redrawPanel();
							}
						}
					}
				});
		}
	}
	private void deactivateSensor() {
		if (mSensorListener!=null) {
			occupancySensor.removePropertyChangeListener(mSensorListener);
			mSensorListener = null;
		}
	}
			
	// variables for Edit Layout Block pane
	JmriJFrame editLayoutBlockFrame = null;
	Component callingPane;
	JTextField sensorNameField = new JTextField(16);
    JComboBox senseBox = new JComboBox();
    int senseActiveIndex;
    int senseInactiveIndex;
    JComboBox trackColorBox = new JComboBox();
	JComboBox occupiedColorBox = new JComboBox();
	JLabel blockUseLabel= new JLabel( rb.getString("UseCount"));
	JButton blockEditDone;
	JButton blockEditCancel;
	boolean editOpen = false;

    /**
     * Edit a Layout Block 
     */
	protected void editLayoutBlock(Component callingPane) {
		if (editOpen) {
			editLayoutBlockFrame.setVisible(true);
			return;
		}
		// Initialize if needed
		if (editLayoutBlockFrame == null) {
            editLayoutBlockFrame = new JmriJFrame( rb.getString("EditBlock") );
            editLayoutBlockFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutBlock", true);
            editLayoutBlockFrame.setLocation(80,40);
            Container contentPane = editLayoutBlockFrame.getContentPane();        
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
			// show block ID (not changeable)
			JPanel panel1 = new JPanel(); 
            panel1.setLayout(new FlowLayout());
			JLabel blockNameLabel = new JLabel( rb.getString("Name")+": "+blockName );
            panel1.add(blockNameLabel);
            contentPane.add(panel1);
			// show current use count (not editable)
			JPanel panel2 = new JPanel(); 
            panel2.setLayout(new FlowLayout());
            panel2.add(blockUseLabel);
            contentPane.add(panel2);
			// set up occupancy sensor (changeable)
			contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			JPanel panel3 = new JPanel(); 
            panel3.setLayout(new FlowLayout());
			JLabel sensorLabel = new JLabel( rb.getString("OccupancySensor")+":");
            panel3.add(sensorLabel);
            panel3.add(sensorNameField);
            sensorNameField.setToolTipText( rb.getString("OccupancySensorToolTip") );
            contentPane.add(panel3);
			// set up occupied sense (changeable)
			JPanel panel4 = new JPanel(); 
            panel4.setLayout(new FlowLayout());
			JLabel sensorSenseLabel = new JLabel( rb.getString("OccupiedSense")+":");
            panel4.add(sensorSenseLabel);
			senseBox.removeAllItems();
			senseBox.addItem( rb.getString("SensorActive") );
			senseActiveIndex = 0;
			senseBox.addItem( rb.getString("SensorInactive") );
			senseInactiveIndex = 1;
			panel4.add(senseBox);
            senseBox.setToolTipText( rb.getString("OccupiedSenseHint") );
            contentPane.add(panel4);
			// set up track color (changeable)
			contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			JPanel panel6 = new JPanel(); 
            panel6.setLayout(new FlowLayout());
			JLabel trackColorLabel = new JLabel( rb.getString("TrackColor") );
			panel6.add(trackColorLabel);
			initializeColorCombo(trackColorBox);
			panel6.add(trackColorBox);
            trackColorBox.setToolTipText( rb.getString("TrackColorHint") );
            contentPane.add(panel6);			
			// set up occupied color (changeable)
			JPanel panel7 = new JPanel(); 
            panel7.setLayout(new FlowLayout());
			JLabel occupiedColorLabel = new JLabel( rb.getString("OccupiedColor") );
			panel7.add(occupiedColorLabel);
			initializeColorCombo(occupiedColorBox);
			panel7.add(occupiedColorBox);
            occupiedColorBox.setToolTipText( rb.getString("OccupiedColorHint") );
            contentPane.add(panel7);			
			// set up Done and Cancel buttons
			contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
			// Done
            panel5.add(blockEditDone = new JButton(rb.getString("Done")));
            blockEditDone.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    blockEditDonePressed(e);
                }
            });
            blockEditDone.setToolTipText( rb.getString("DoneHint") );
			// Cancel
            panel5.add(blockEditCancel = new JButton(rb.getString("Cancel")));
            blockEditCancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    blockEditCancelPressed(e);
                }
            });
            blockEditCancel.setToolTipText( rb.getString("CancelHint") );
            contentPane.add(panel5);		
		}
		// Set up for Edit
		blockUseLabel.setText(rb.getString("UseCount")+": "+useCount );
		sensorNameField.setText(occupancySensorName);
		if (occupiedSense==Sensor.ACTIVE) {
			senseBox.setSelectedIndex(senseActiveIndex);
		}
		else {
			senseBox.setSelectedIndex(senseInactiveIndex);
		}
		setColorCombo(trackColorBox,blockTrackColor);
		setColorCombo(occupiedColorBox,blockOccupiedColor);
		editLayoutBlockFrame.addWindowListener(new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent e) {
					blockEditCancelPressed(null);
				}
			});
        editLayoutBlockFrame.pack();
        editLayoutBlockFrame.setVisible(true);		
		editOpen = true;
	}	
	void blockEditDonePressed(ActionEvent a) {
		boolean needsRedraw = false;
		// check if Sensor changed
		if ( !occupancySensorName.equals(sensorNameField.getText()) ) {
			// sensor has changed
			String newName = sensorNameField.getText();
			if (validateSensor(newName,editLayoutBlockFrame)==null) {
				// invalid sensor entered
				occupancySensor = null;
				occupancySensorName = "";
				sensorNameField.setText("");
			}
			else needsRedraw = true;
		}
		// check if occupied sense changed
		int k = senseBox.getSelectedIndex();
		int oldSense = occupiedSense;
		if (k==senseActiveIndex) occupiedSense = Sensor.ACTIVE;
		else occupiedSense = Sensor.INACTIVE;
		if (oldSense!=occupiedSense) needsRedraw = true;
		// check if track color changed
		Color oldColor = blockTrackColor;
		blockTrackColor = getSelectedColor(trackColorBox);
		if (oldColor!=blockTrackColor) needsRedraw = true;
		// check if occupied color changed
		oldColor = blockOccupiedColor;
		blockOccupiedColor = getSelectedColor(occupiedColorBox);
		if (oldColor!=blockOccupiedColor) needsRedraw = true;
		// complete
		editOpen = false;
		editLayoutBlockFrame.setVisible(false);
		if (needsRedraw) redrawLayoutBlockPanels();
	}
	void blockEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editLayoutBlockFrame.setVisible(false);
	}
	
	/** 
	 * Methods and data to support initialization of color Combo box
	 */
	String[] colorText = {"Black","DarkGray","Gray", 
			"LightGray","White","Red","Pink","Orange",
			"Yellow","Green","Blue","Magenta","Cyan"};
	Color[] colorCode = {Color.black,Color.darkGray,Color.gray,
			Color.lightGray,Color.white,Color.red,Color.pink,Color.orange,
			Color.yellow,Color.green,Color.blue,Color.magenta,Color.cyan};
	int numColors = 13;  // number of entries in the above arrays
	private void initializeColorCombo(JComboBox colorCombo) {
		colorCombo.removeAllItems();
		for (int i = 0;i<numColors;i++) {
			colorCombo.addItem( rb.getString(colorText[i]) );
		}
	}
	private void setColorCombo(JComboBox colorCombo,Color color) {
		for (int i = 0;i<numColors;i++) {
			if (color==colorCode[i]) {
				colorCombo.setSelectedIndex(i);
				return;
			}
		}
	}
	private Color getSelectedColor(JComboBox colorCombo) {
		return (colorCode[colorCombo.getSelectedIndex()]);
	}
	
	/**
	 * Utility methods for converting between string and color
	 * Note: These names are only used internally, so don't need a resource bundle
	 */
	public static String colorToString(Color color) {
		if(color == Color.black) return "black";
		else if (color == Color.darkGray) return "darkGray";
		else if (color == Color.gray) return "gray";
		else if (color == Color.lightGray) return "lightGray";
		else if (color == Color.white) return "white";
		else if (color == Color.red) return "red";
		else if (color == Color.pink) return "pink";
		else if (color == Color.orange) return "orange";
		else if (color == Color.yellow) return "yellow";
		else if (color == Color.green) return "green";
		else if (color == Color.blue) return "blue";
		else if (color == Color.magenta) return "magenta";
		else if (color == Color.cyan) return "cyan";
		log.error ("unknown color sent to colorToString");
		return "black";
	}
	public static Color stringToColor(String string) {
		if(string.equals("black")) return Color.black;
		else if (string.equals("darkGray")) return Color.darkGray;	
		else if (string.equals("gray")) return Color.gray;	
		else if (string.equals("lightGray")) return Color.lightGray;	
		else if (string.equals("white")) return Color.white;	
		else if (string.equals("red")) return Color.red;	
		else if (string.equals("pink")) return Color.pink;	
		else if (string.equals("orange")) return Color.orange;	
		else if (string.equals("yellow")) return Color.yellow;	
		else if (string.equals("green")) return Color.green;
		else if (string.equals("blue")) return Color.blue;	
		else if (string.equals("magenta")) return Color.magenta;	
		else if (string.equals("cyan")) return Color.cyan;	
		log.error("unknown color text '"+string+"' sent to stringToColor");
		return Color.black;
	}

    /**
     * Clean up when this object is no longer needed.  Should not
     * be called while the object is still displayed; see remove()
     */
    public void dispose() {
    }

    /**
     * Removes this object from display and persistance
     */
    void remove() {
		// if an occupancy sensor has been activated, deactivate it
		deactivateSensor();
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutBlock.class.getName());

}