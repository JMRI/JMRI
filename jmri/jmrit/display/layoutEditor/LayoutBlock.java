// jmri.jmrit.display.LayoutBlock.java
package jmri.jmrit.display.layoutEditor;

import jmri.InstanceManager;
import jmri.util.JmriJFrame;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.ResourceBundle;
import java.util.ArrayList;

import javax.swing.*;

import jmri.Sensor;
import jmri.implementation.AbstractNamedBean;

/**
 * A LayoutBlock is a group of track segments and turnouts on a LayoutEditor panel 
 *      corresponding to a 'block'. LayoutBlock is a LayoutEditor specific extension 
 *		of the JMRI Block object. 
 * <P>
 * LayoutBlocks may have an occupancy Sensor. The getOccupancy method returns 
 *		the occupancy state of the LayoutBlock - OCCUPIED, EMPTY, or UNKNOWN.
 *		If no occupancy sensor is provided, UNKNOWN is returned. The occupancy sensor
 *      if there is one, is the same as the occupancy sensor of the corresponding 
 *      JMRI Block.
 * <P>
 * The name of each Layout Block is the same as that of the corresponding block as
 *		defined in Layout Editor . A corresponding JMRI Block object is created when a
 *		LayoutBlock is created. The JMRI Block uses the name of the block defined in
 *      Layout Editor as its user name and a unique IBnnn system name. The JMRI Block 
 *		object and its associated Path objects are useful in tracking a train around 
 *      the layout. Blocks may be viewed in the Block Table.
 * <P>
 * A LayoutBlock may have an associated Memory object. This Memory object contains a 
 *		string representing the current "value" of the corresponding JMRI Block object. 
 *		If the value contains a train name, for example, displaying Memory objects 
 *		associated with LayoutBlocks, and displayed near each Layout Block can 
 *		follow a train around the layout, displaying its name when it is in the
 *.		LayoutBlock.
 * <P>
 * LayoutBlocks are "cross-panel", similar to sensors and turnouts.  A LayoutBlock
 *		may be used by more than one Layout Editor panel simultaneously.  As a consequence, 
 *		LayoutBlocks are saved with the configuration, not with a panel.
 * <P>
 * LayoutBlocks are used by TrackSegments, LevelXings, and LayoutTurnouts.
 *		LevelXings carry two LayoutBlock designations, which may be the same.
 *      LayoutTurnouts carry LayoutBlock designations also, one per turnout,
 *			except for double crossovers which can have up to four.
 * <P>
 * LayoutBlocks carry a use count.  The use count counts the number of track
 *		segments, layout turnouts, and levelcrossings which use the LayoutBlock.
 *		Only LayoutBlocks which have a use count greater than zero are saved when
 *		the configuration is saved.
 * <P>
 * @author Dave Duchamp Copyright (c) 2004-2008
 * @version $Revision: 1.5 $
 */

public class LayoutBlock extends AbstractNamedBean
{

	// Defined text resource
	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

	// constants
	public static final int OCCUPIED = jmri.Block.OCCUPIED;
	public static final int EMPTY = jmri.Block.UNOCCUPIED;
	public static final int UNKNOWN = jmri.Sensor.UNKNOWN;  // must be a different bit
	// operational instance variables (not saved to disk)
	private int useCount = 0;
	private Sensor occupancySensor = null;
	private jmri.Memory memory = null;
	private jmri.Block block = null;
	//private int maxBlockNumber = 0;
	private LayoutBlock _instance = null;
    private ArrayList<LayoutEditor> panels = new ArrayList<LayoutEditor>();  // panels using this block
	private java.beans.PropertyChangeListener mBlockListener = null;
	private	int jmriblknum = 1;
	private boolean useExtraColor = false;
	private boolean suppressNameUpdate = false;

	// persistent instances variables (saved between sessions)
	public String blockName = "";
	public String lbSystemName = "";
	public String occupancySensorName = "";
	public String memoryName = "";
	public int occupiedSense = Sensor.ACTIVE;
	public Color blockTrackColor = Color.black;
	public Color blockOccupiedColor = Color.black;
	public Color blockExtraColor = Color.black;
	
	/* 
	 * Creates a LayoutBlock object.
	 *  
	 * Note: initializeLayoutBlock() must be called to complete the process. They are split 
	 *       so  that loading of panel files will be independent of whether LayoutBlocks or 
	 *		 Blocks are loaded first.
	 */
	public LayoutBlock(String sName, String uName) {
		super (sName.toUpperCase(),uName);
		_instance = this;
		blockName = uName;
		lbSystemName = sName;
	}
	/*
	 * Completes the creation of a LayoutBlock object by adding a Block to it
	 */
	protected void initializeLayoutBlock() {
		// get/create a jmri.Block object corresponding to this LayoutBlock
		block = InstanceManager.blockManagerInstance().getByUserName(blockName);
		if (block==null) {
			// not found, create a new jmri.Block
			String s = "";
			boolean found = true;
			// create a unique system name
			while (found) {
				s = "IB"+jmriblknum;
				jmriblknum ++;
				block = InstanceManager.blockManagerInstance().getBySystemName(s);
				if (block == null) found = false;
			}
			block = InstanceManager.blockManagerInstance().createNewBlock(s,blockName);
			if (block==null) log.error("Failure to get/create Block: "+s+","+blockName);
		}
		if (block!=null) {
			// attach a listener for changes in the Block
			block.addPropertyChangeListener(mBlockListener = 
								new java.beans.PropertyChangeListener() {
					public void propertyChange(java.beans.PropertyChangeEvent e) {
						handleBlockChange(e);
					}
				});
			if (occupancySensor!=null) {			
				block.setSensor(occupancySensor);
			}
		}
	}
	
	/**
	 * Accessor methods
	 */
	public String getID() {return blockName;}	
	public Color getBlockTrackColor() {return blockTrackColor;}
	public void setBlockTrackColor(Color color) {blockTrackColor = color;}	
	public Color getBlockOccupiedColor() {return blockOccupiedColor;}
	public void setBlockOccupiedColor(Color color) {blockOccupiedColor = color;}	
	public Color getBlockExtraColor() {return blockExtraColor;}
	public void setBlockExtraColor(Color color) {blockExtraColor = color;}
	public boolean getUseExtraColor() {return useExtraColor;}
	public void setUseExtraColor(boolean b) {useExtraColor = b;}
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
				LayoutEditor ed = panels.get(i);
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
				LayoutEditor ed = panels.get(i);
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
				LayoutEditor ed = panels.get(i);
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
				panels.get(i).redrawPanel();
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
					new Object[]{sensorName}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if ( !sensorName.equals(s.getUserName()) ) {
			sensorName = sensorName.toUpperCase();
		}
		// ensure that this sensor is unique among defined Layout Blocks
		Sensor savedSensor = occupancySensor;
		//String savedName = occupancySensorName;
		occupancySensor = null;
		LayoutBlock b = InstanceManager.layoutBlockManagerInstance().
											getBlockWithSensorAssigned(s);
		if (b!=null) {
			// new sensor is not unique, return to the old one
			occupancySensor = savedSensor;
			JOptionPane.showMessageDialog(openFrame,
					java.text.MessageFormat.format(rb.getString("Error6"),
					new Object[]{sensorName,b.getID()}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		// sensor is unique
		setOccupancySensorName(sensorName);
		return s;
	}

	/**
	 * Validates that the memory name corresponds to an existing memory.
	 *   If valid, returns the memory. Else returns null, and notifies the user.
	 * This method also converts the memory name to upper case if it is a system name.
	 */
	public jmri.Memory validateMemory(String memName, Component openFrame) {
		// check if anything entered	
		if (memName.length()<1) {
			// no memory entered
			return null;
		}
		// get the memory corresponding to this name
		jmri.Memory m = InstanceManager.memoryManagerInstance().getMemory(memName);
		if (m==null) {
			// There is no memory corresponding to this name
			JOptionPane.showMessageDialog(openFrame,
					java.text.MessageFormat.format(rb.getString("Error16"),
					new Object[]{memName}),
					rb.getString("Error"),JOptionPane.ERROR_MESSAGE);
			return null;
		}
		if ( !(memName.equals(m.getUserName())) ) {
			memName = memName.toUpperCase();
		}
		memoryName = memName;
		return m;
	}
		
	/**
	 * Returns the color for drawing items in this block.  Returns color
	 *   based on block occupancy.
	 */
	public Color getBlockColor() {
		if (getOccupancy() == OCCUPIED) {
			return blockOccupiedColor;
		}
		else if (useExtraColor) {
			return blockExtraColor;
		}
		else {
			return blockTrackColor;
		}
	}

	/**
	 * Get the jmri.Block corresponding to this LayoutBlock
	 */
	public jmri.Block getBlock() {return block;}
			
	/**
	 * Returns Memory name
	*/
	public String getMemoryName() {return (memoryName);}
			
	/**
	 * Returns Memory
	*/
	public jmri.Memory getMemory() {return (memory);}

	/**
	 * Add Memory by name
	 */
	public void setMemoryName(String name) {
		memoryName = name;
		memory = jmri.InstanceManager.memoryManagerInstance().
                            getMemory(name);
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
		if (block!=null) {
			block.setSensor(occupancySensor);
		}
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
	 * Get the Layout Editor panel with the highest connectivity to this Layout Block
	 */
	protected LayoutEditor getMaxConnectedPanel() {
		LayoutEditor panel = null;
		if ( (block!=null) && (panels.size()>0) ) {
			// a block is attached and this LayoutBlock is used
			// initialize connectivity as defined in first Layout Editor panel
			panel = panels.get(0);
			ArrayList<LayoutConnectivity> c = panel.auxTools.getConnectivityList(_instance);
			// if more than one panel, find panel with the highest connectivity
			if (panels.size()>1) {
				for (int i = 1;i < panels.size();i++) {
					if (c.size()<panels.get(i).auxTools.
										getConnectivityList(_instance).size()) {
						panel = panels.get(i);
						c = panel.auxTools.getConnectivityList(_instance);
					}
				}
			}
		}
		return panel;
	}
			
	/**
	 * Check/Update Path objects for the attached jmri.Block
	 * <P>
	 * If multiple panels are present, Paths are set according to the panel with 
	 *		the highest connectivity (most LayoutConnectivity objects);
	 */
	public void updatePaths() {
		if ( (block!=null) && (panels.size()>0) ) {
			// a block is attached and this LayoutBlock is used
			// initialize connectivity as defined in first Layout Editor panel
			LayoutEditor panel = panels.get(0);
			ArrayList<LayoutConnectivity> c = panel.auxTools.getConnectivityList(_instance);
			// if more than one panel, find panel with the highest connectivity
			if (panels.size()>1) {
				for (int i = 1;i < panels.size();i++) {
					if (c.size()<panels.get(i).auxTools.
										getConnectivityList(_instance).size()) {
						panel = panels.get(i);
						c = panel.auxTools.getConnectivityList(_instance);
					}
				}
				// check that this connectivity is compatible with that of other panels.
				for (int j = 0;j < panels.size();j++) {
					LayoutEditor tPanel = panels.get(j);
					if ( (tPanel!=panel) && InstanceManager.layoutBlockManagerInstance().
								warn() && ( !compareConnectivity(c,
										tPanel.auxTools.getConnectivityList(_instance)) )  ) {
						// send user an error message
						int response = JOptionPane.showOptionDialog(null,
								java.text.MessageFormat.format(rb.getString("Warn1"),
								new Object[]{blockName,tPanel.getLayoutName(),
								panel.getLayoutName()}),rb.getString("WarningTitle"),
								JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,
								null,new Object[] {rb.getString("ButtonOK"),
								rb.getString("ButtonOKPlus")},rb.getString("ButtonOK"));
						if (response!=0)
							// user elected to disable messages
							InstanceManager.layoutBlockManagerInstance().turnOffWarning();						
					}
				}
			}
			// update block Paths to reflect connectivity as needed
			updateBlockPaths(c,panel);
		}	
	}			
	/**
	 * Check/Update Path objects for the attached jmri.Block using the 
	 *		connectivity in the specified Layout Editor panel.
	 */
	public void updatePathsUsingPanel(LayoutEditor panel) {
		if (panel==null) {
			log.error("Null panel in call to updatePathsUsingPanel");
		}
		else {
		    ArrayList<LayoutConnectivity> c = panel.auxTools.getConnectivityList(_instance);
			updateBlockPaths(c, panel);
		}
	}		
	private void updateBlockPaths(ArrayList<LayoutConnectivity> c, LayoutEditor panel) {
		LayoutEditorAuxTools auxTools = new LayoutEditorAuxTools(panel);
		java.util.List<jmri.Path> paths = block.getPaths();
		boolean[] used = new boolean[c.size()];
		int[] need = new int[paths.size()];
		for (int j=0;j<c.size();j++) {used[j] = false;}
		for (int j=0;j<paths.size();j++) {need[j] = -1;}
		// cycle over existing Paths, checking against LayoutConnectivity
		for (int i = 0;i<paths.size();i++) {
			jmri.Path p = paths.get(i);
			// cycle over LayoutConnectivity matching to this Path
			for (int j = 0;((j<c.size())&&(need[i]==-1));j++) {
				if (!used[j]) {
					// this LayoutConnectivity not used yet
					LayoutConnectivity lc = c.get(j);
					if ( (lc.getBlock1().getBlock()==p.getBlock()) ||
								(lc.getBlock2().getBlock()==p.getBlock()) ) {
						// blocks match - record
						used[j] = true;
						need[i] = j;
					}
				}
			}
		}
		// update needed Paths
		for (int i = 0;i<paths.size();i++) {
			if (need[i]>=0) {
				jmri.Path p = paths.get(i);
				LayoutConnectivity lc = c.get(need[i]);
				if (lc.getBlock1()==_instance) {
					p.setToBlockDirection(lc.getDirection());
					p.setFromBlockDirection(lc.getReverseDirection());
				}
				else {
					p.setToBlockDirection(lc.getReverseDirection());
					p.setFromBlockDirection(lc.getDirection());
				}
				if (c.size()>2) auxTools.addBeanSettings(p,lc,_instance);
			}
		}	
		// delete unneeded Paths
		for (int i = 0;i<paths.size();i++) {
			if (need[i]<0) {				
				block.removePath(paths.get(i));
			}
		}	
		// add Paths as required
		for (int j = 0;j<c.size();j++) {
			if (!used[j]) {
				// there is no corresponding Path, add one.
				LayoutConnectivity lc = c.get(j);
				jmri.Path newp = null;
				if (lc.getBlock1()==_instance) {
					newp = new jmri.Path(lc.getBlock2().getBlock(),lc.getDirection(),
									lc.getReverseDirection());
				}
				else {
					newp = new jmri.Path(lc.getBlock1().getBlock(),lc.getReverseDirection(),
									lc.getDirection());					
				}
				//if (newp != null) 
				block.addPath(newp);				
				//else log.error("Trouble adding Path to block '"+blockName+"'.");
				if (c.size()>2) auxTools.addBeanSettings(newp,lc,_instance);
			}				
		}
// djd debugging - lists results of automatic initialization of Paths and BeanSettings			
/*		paths = block.getPaths();
		for (int i = 0;i<paths.size();i++) {
			jmri.Path p = (jmri.Path)paths.get(i);
			log.error("Block "+blockName+"- Path to "+p.getBlock().getUserName()+
						" - "+p.decodeDirection(p.getToBlockDirection()) );
			java.util.List beans = p.getSettings();
			for (int j=0;j<beans.size();j++) {
				jmri.BeanSetting be = (jmri.BeanSetting)beans.get(j);
				log.error("   BeanSetting - "+((jmri.Turnout)be.getBean()).getSystemName()+
								" with state "+be.getSetting()+" (2=CLOSED,4=THROWN)");
			}
		} */
// end debugging
	}
	private boolean compareConnectivity(ArrayList<LayoutConnectivity> main, ArrayList<LayoutConnectivity> test) {
		// loop over connectivities in test list 
		for (int i = 0;i<test.size();i++) {
			LayoutConnectivity lc = test.get(i);
			// loop over main list to make sure the same blocks are connected
			boolean found = false;
			for (int j = 0;(j<main.size())&&!found;j++) {
				LayoutConnectivity mc = main.get(j);
				if ( ((lc.getBlock1()==mc.getBlock1()) && (lc.getBlock2()==mc.getBlock2())) ||
					((lc.getBlock1()==mc.getBlock2()) && (lc.getBlock2()==mc.getBlock1())) )
					found = true;
			}
			if (!found) return false;
		}
		// connectivities are compatible - all connections in test are present in main
		return (true);
	}
	
	/**
	 * Handle tasks when block changes
	 */
	void handleBlockChange(java.beans.PropertyChangeEvent e) {
		// Update memory object if there is one
		if ( (memory==null) && (memoryName!="") ) {
			// initialize if needed 
			memory = jmri.InstanceManager.memoryManagerInstance().
                            getMemory(memoryName);
		}
		if ( (memory!=null) && (block!=null) && !suppressNameUpdate ) {
			// copy block value to memory if there is a value
			Object val = block.getValue();
			if (val!=null) val = val.toString();
			memory.setValue(val);
		}				
		// Redraw all Layout Editor panels using this Layout Block
		redrawLayoutBlockPanels();
	}
			
	/** 
	 * Deactivate block listener for redraw of panels and update of memories on 
	 *	change of state
	 */
	private void deactivateBlock() {
		if ( (mBlockListener!=null) && (block!=null) ) {
			block.removePropertyChangeListener(mBlockListener);
		}
		mBlockListener = null;
	}
	
	/**
	 * Sets/resets update of memory name when block goes from occupied to unoccupied or vice versa.
	 * If set is true, name update is suppressed.
	 * If set is false, name update works normally.
	 */
	public void setSuppressNameUpdate(boolean set) {suppressNameUpdate = set;}
			
	// variables for Edit Layout Block pane
	JmriJFrame editLayoutBlockFrame = null;
	Component callingPane;
	JTextField sensorNameField = new JTextField(16);
	JTextField memoryNameField = new JTextField(16);
    JComboBox senseBox = new JComboBox();
    int senseActiveIndex;
    int senseInactiveIndex;
    JComboBox trackColorBox = new JComboBox();
	JComboBox occupiedColorBox = new JComboBox();
	JComboBox extraColorBox = new JComboBox();
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
			// set up extra color (changeable)
			JPanel panel7a = new JPanel(); 
            panel7a.setLayout(new FlowLayout());
			JLabel extraColorLabel = new JLabel( rb.getString("ExtraColor") );
			panel7a.add(extraColorLabel);
			initializeColorCombo(extraColorBox);
			panel7a.add(extraColorBox);
            extraColorBox.setToolTipText( rb.getString("ExtraColorHint") );
            contentPane.add(panel7a);
			// set up Memory entry (changeable)
			contentPane.add(new JSeparator(JSeparator.HORIZONTAL));
			JPanel panel8 = new JPanel(); 
            panel8.setLayout(new FlowLayout());
			JLabel memoryLabel = new JLabel( rb.getString("MemoryVariable")+":");
            panel8.add(memoryLabel);
            panel8.add(memoryNameField);
            memoryNameField.setToolTipText( rb.getString("MemoryVariableTip") );
            contentPane.add(panel8);			
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
		setColorCombo(extraColorBox,blockExtraColor);
		memoryNameField.setText(memoryName);
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
		if ( !occupancySensorName.equals(sensorNameField.getText().trim()) ) {
			// sensor has changed
			String newName = sensorNameField.getText().trim();
			if (validateSensor(newName,editLayoutBlockFrame)==null) {
				// invalid sensor entered
				occupancySensor = null;
				occupancySensorName = "";
				sensorNameField.setText("");
				return;
			}
			else {
				sensorNameField.setText(newName);
				needsRedraw = true;
			}
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
		// check if extra color changed
		oldColor = blockExtraColor;
		blockExtraColor = getSelectedColor(extraColorBox);
		if (oldColor!=blockExtraColor) needsRedraw = true;
		// check if Memory changed
		if ( !memoryName.equals(memoryNameField.getText().trim()) ) {
			// memory has changed
			String newName = memoryNameField.getText().trim();
			if ((memory = validateMemory(newName,editLayoutBlockFrame))==null) {
				// invalid memory entered
				memoryName = "";
				memoryNameField.setText("");
				return;
			}
			else {
				memoryNameField.setText(memoryName);
				needsRedraw = true;
			}
		}
		// complete
		editOpen = false;
		editLayoutBlockFrame.setVisible(false);
		editLayoutBlockFrame.dispose();
		editLayoutBlockFrame = null;
		if (needsRedraw) redrawLayoutBlockPanels();
	}
	void blockEditCancelPressed(ActionEvent a) {
		editOpen = false;
		editLayoutBlockFrame.setVisible(false);
		editLayoutBlockFrame.dispose();
		editLayoutBlockFrame = null;
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
     * Removes this object from display and persistance
     */
    void remove() {
		// if an occupancy sensor has been activated, deactivate it
		deactivateBlock();
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

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LayoutBlock.class.getName());

}
