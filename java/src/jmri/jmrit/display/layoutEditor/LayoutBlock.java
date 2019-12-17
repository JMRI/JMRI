package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.*;
import java.util.*;
import javax.annotation.*;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import jmri.*;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.beantable.beanedit.*;
import jmri.jmrit.roster.RosterEntry;
import jmri.swing.NamedBeanComboBox;
import jmri.util.*;
import jmri.util.swing.*;
import org.slf4j.*;

/**
 * A LayoutBlock is a group of track segments and turnouts on a LayoutEditor
 * panel corresponding to a 'block'. LayoutBlock is a LayoutEditor specific
 * extension of the JMRI Block object.
 * <p>
 * LayoutBlocks may have an occupancy Sensor. The getOccupancy method returns
 * the occupancy state of the LayoutBlock - OCCUPIED, EMPTY, or UNKNOWN. If no
 * occupancy sensor is provided, UNKNOWN is returned. The occupancy sensor if
 * there is one, is the same as the occupancy sensor of the corresponding JMRI
 * Block.
 * <p>
 * The name of each Layout Block is the same as that of the corresponding block
 * as defined in Layout Editor. A corresponding JMRI Block object is created
 * when a LayoutBlock is created. The JMRI Block uses the name of the block
 * defined in Layout Editor as its user name and a unique IBnnn system name. The
 * JMRI Block object and its associated Path objects are useful in tracking a
 * train around the layout. Blocks may be viewed in the Block Table.
 * <p>
 * A LayoutBlock may have an associated Memory object. This Memory object
 * contains a string representing the current "value" of the corresponding JMRI
 * Block object. If the value contains a train name, for example, displaying
 * Memory objects associated with LayoutBlocks, and displayed near each Layout
 * Block can follow a train around the layout, displaying its name when it is in
 * the LayoutBlock.
 * <p>
 * LayoutBlocks are "cross-panel", similar to sensors and turnouts. A
 * LayoutBlock may be used by more than one Layout Editor panel simultaneously.
 * As a consequence, LayoutBlocks are saved with the configuration, not with a
 * panel.
 * <p>
 * LayoutBlocks are used by TrackSegments, LevelXings, and LayoutTurnouts.
 * LevelXings carry two LayoutBlock designations, which may be the same.
 * LayoutTurnouts carry LayoutBlock designations also, one per turnout, except
 * for double crossovers and slips which can have up to four.
 * <p>
 * LayoutBlocks carry a use count. The use count counts the number of track
 * segments, layout turnouts, and levelcrossings which use the LayoutBlock. Only
 * LayoutBlocks which have a use count greater than zero are saved when the
 * configuration is saved.
 *
 * @author Dave Duchamp Copyright (c) 2004-2008
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutBlock extends AbstractNamedBean implements PropertyChangeListener {

    private final boolean enableAddRouteLogging = false;
    private final boolean enableUpdateRouteLogging = false;
    private boolean enableDeleteRouteLogging = false;
    private final boolean enableSearchRouteLogging = false;

    private static final List<Integer> updateReferences = new ArrayList<>(500);

    //might want to use the jmri ordered HashMap, so that we can add at the top
    //and remove at the bottom.
    private final List<Integer> actedUponUpdates = new ArrayList<>(500);

    public void enableDeleteRouteLog() {
        enableDeleteRouteLogging = false;
    }

    public void disableDeleteRouteLog() {
        enableDeleteRouteLogging = false;
    }

    //constants
    public static final int OCCUPIED = Block.OCCUPIED;
    public static final int EMPTY = Block.UNOCCUPIED;
    //operational instance variables (not saved to disk)
    private int useCount = 0;
    private NamedBeanHandle<Sensor> occupancyNamedSensor = null;
    private NamedBeanHandle<Memory> namedMemory = null;

    private Block block = null;

    private final List<LayoutEditor> panels = new ArrayList<>(); //panels using this block
    private PropertyChangeListener mBlockListener = null;
    private int jmriblknum = 1;
    private boolean useExtraColor = false;
    private boolean suppressNameUpdate = false;

    //persistent instances variables (saved between sessions)
    private String occupancySensorName = "";
    private String memoryName = "";
    private int occupiedSense = Sensor.ACTIVE;
    private Color blockTrackColor = Color.black;
    private Color blockOccupiedColor = Color.black;
    private Color blockExtraColor = Color.black;

    /*
     * Creates a LayoutBlock object.
     *
     * Note: initializeLayoutBlock() must be called to complete the process. They are split
     *       so  that loading of panel files will be independent of whether LayoutBlocks or
     *       Blocks are loaded first.
     * @param sName System name of this LayoutBlock
     * @param uName User name of this LayoutBlock but also the user name of the associated Block
     */
    public LayoutBlock(String sName, String uName) {
        super(sName, uName);
        //_instance = this;
    }

    /*
     * Completes the creation of a LayoutBlock object by adding a Block to it.
     *
     * The block create process takes into account that the _bean register
     * process considers IB1 and IB01 to be the same name which results in a
     * silent failure.
     */
    @SuppressWarnings("deprecation") // needs careful unwinding for Set operations, generics
    public void initializeLayoutBlock() {
        //get/create a Block object corresponding to this LayoutBlock
        block = null;   // assume failure (pessimist!)
        String userName = getUserName();
        if ((userName != null) && !userName.isEmpty()) {
            block = InstanceManager.getDefault(BlockManager.class).getByUserName(userName);
        }

        if (block == null) {
            // Not found, create a new Block
            BlockManager bm = InstanceManager.getDefault(BlockManager.class);
            String s;
            while (true) {
                if (jmriblknum > 50000) {
                    throw new IndexOutOfBoundsException("Run away prevented while trying to create a block");
                }
                s = "IB" + jmriblknum;
                jmriblknum++;

                // Find an unused system name
                block = bm.getBySystemName(s);
                if (block != null) {
                    log.debug("System name is already used: {}", s);
                    continue;
                }

                // Create a new block.  User name is null to prevent user name checking.
                block = bm.createNewBlock(s, null);
                if (block == null) {
                    log.debug("Null block returned: {}", s);
                    continue;
                }

                // Verify registration
                if (bm.getSystemNameList().contains(s)) {
                    log.debug("Block is valid: {}", s);
                    break;
                }
                log.debug("Registration failed: {}", s);
            }
            block.setUserName(getUserName());
        }

        //attach a listener for changes in the Block
        mBlockListener = (PropertyChangeEvent e) -> {
            handleBlockChange(e);
        };
        block.addPropertyChangeListener(mBlockListener,
                getUserName(), "Layout Block:" + getUserName());
        if (occupancyNamedSensor != null) {
            block.setNamedSensor(occupancyNamedSensor);
        }
    }

    /* initializeLayoutBlockRouting */
    public void initializeLayoutBlockRouting() {
        if (!InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            return;
        }
        setBlockMetric();

        block.getPaths().stream().forEach((p) -> {
            addAdjacency(p);
        });
    }

    /*
     * Accessor methods
     */
    // TODO: deprecate and just use getUserName() directly
    public String getId() {
        return getUserName();
    }

    public Color getBlockTrackColor() {
        return blockTrackColor;
    }

    public void setBlockTrackColor(Color color) {
        blockTrackColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    public Color getBlockOccupiedColor() {
        return blockOccupiedColor;
    }

    public void setBlockOccupiedColor(Color color) {
        blockOccupiedColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    public Color getBlockExtraColor() {
        return blockExtraColor;
    }

    public void setBlockExtraColor(Color color) {
        blockExtraColor = color;
        JmriColorChooser.addRecentColor(color);
    }

    //TODO: @Deprecated // Java standard pattern for boolean getters is "UseExtraColor()"
    public boolean getUseExtraColor() {
        return useExtraColor;
    }

    public void setUseExtraColor(boolean b) {
        useExtraColor = b;

        if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            stateUpdate();
        }
        if (getBlock() != null) {
            getBlock().setAllocated(b);
        }
    }

    /* setUseExtraColor */
    public void incrementUse() {
        useCount++;
    }

    public void decrementUse() {
        --useCount;
        if (useCount <= 0) {
            useCount = 0;
        }
    }

    public int getUseCount() {
        return useCount;
    }

    /**
     * Keep track of LayoutEditor panels that are using this LayoutBlock.
     *
     * @param panel to keep track of
     */
    public void addLayoutEditor(LayoutEditor panel) {
        //add to the panels list if not already there
        if (!panels.contains(panel)) {
            panels.add(panel);
        }
    }

    public void deleteLayoutEditor(LayoutEditor panel) {
        //remove from the panels list if there
        if (panels.contains(panel)) {
            panels.remove(panel);
        }
    }

    public boolean isOnPanel(LayoutEditor panel) {
        //returns true if this Layout Block is used on panel
        return panels.contains(panel);
    }

    /**
     * Redraw panels using this layout block.
     */
    public void redrawLayoutBlockPanels() {
        panels.stream().forEach((le) -> {
            le.redrawPanel();
        });
        firePropertyChange("redraw", null, null);
    }

    /**
     * Validate that the supplied occupancy sensor name corresponds to an
     * existing sensor and is unique among all blocks. If valid, returns the
     * sensor and sets the block sensor name in the block. Else returns null,
     * and does nothing to the block.
     *
     * @param sensorName to check
     * @param openFrame  determines the <code>Frame</code> in which the dialog
     *                   is displayed; if <code>null</code>, or if the
     *                   <code>parentComponent</code> has no <code>Frame</code>,
     *                   a default <code>Frame</code> is used
     * @return the validated sensor
     */
    public Sensor validateSensor(String sensorName, Component openFrame) {
        String theSensorName = sensorName;
        //check if anything entered
        if ((theSensorName == null) || theSensorName.isEmpty()) {
            //no sensor name entered
            if (occupancyNamedSensor != null) {
                setOccupancySensorName(null);
            }
            return null;
        }

        //get the sensor corresponding to this name
        Sensor s = InstanceManager.sensorManagerInstance().getSensor(theSensorName);
        if (s == null) {
            //There is no sensor corresponding to this name
            JOptionPane.showMessageDialog(openFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error7"),
                            new Object[]{theSensorName}),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return null;
        }

        //ensure that this sensor is unique among defined Layout Blocks
        NamedBeanHandle<Sensor> savedNamedSensor = occupancyNamedSensor;
        occupancyNamedSensor = null;
        LayoutBlock b = InstanceManager.getDefault(LayoutBlockManager.class).
                getBlockWithSensorAssigned(s);

        if (b != null) {
            if (b.getUseCount() > 0) {
                //new sensor is not unique, return to the old one
                occupancyNamedSensor = savedNamedSensor;
                JOptionPane.showMessageDialog(openFrame,
                        java.text.MessageFormat.format(Bundle.getMessage("Error6"),
                                new Object[]{theSensorName, b.getId()}),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return null;
            } else {
                //the user is assigning a sensor which is already assigned to
                //layout block b. Layout block b is no longer in use so this
                //should be fine but it's technically possible to put
                //this discarded layout block back into service (possibly
                //by mistake) by entering its name in any edit layout block window.
                //That would cause a problem with the sensor being in use in
                //two active blocks, so as a precaution we remove the sensor
                //from the discarded block here.
                b.setOccupancySensorName(null);
            }
        }
        //sensor is unique, or was only in use on a layout block not in use
        setOccupancySensorName(theSensorName);
        return s;
    }

    /**
     * Validate that the memory name corresponds to an existing memory. If
     * valid, returns the memory. Else returns null, and notifies the user.
     *
     * @param memName   the memory name
     * @param openFrame the frame to display any error dialog in
     * @return the memory
     */
    public Memory validateMemory(String memName, Component openFrame) {
        //check if anything entered
        if ((memName == null) || memName.isEmpty()) {
            //no memory entered
            return null;
        }
        //get the memory corresponding to this name
        Memory m = InstanceManager.memoryManagerInstance().getMemory(memName);
        if (m == null) {
            //There is no memory corresponding to this name
            JOptionPane.showMessageDialog(openFrame,
                    java.text.MessageFormat.format(Bundle.getMessage("Error16"),
                            new Object[]{memName}),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return null;
        }
        memoryName = memName;

        //Go through the memory icons on the panel and see if any are linked to this layout block
        if ((m != getMemory()) && (panels.size() > 0)) {
            boolean updateall = false;
            boolean found = false;
            for (LayoutEditor panel : panels) {
                for (MemoryIcon memIcon : panel.memoryLabelList) {
                    if (memIcon.getLayoutBlock() == this) {
                        if (!updateall && !found) {
                            int n = JOptionPane.showConfirmDialog(
                                    openFrame,
                                    "Would you like to update all memory icons on the panel linked to the block to use the new one?",
                                    "Update Memory Icons",
                                    JOptionPane.YES_NO_OPTION);
                            //TODO I18N in Bundle.properties
                            found = true;
                            if (n == 0) {
                                updateall = true;
                            }
                        }
                        if (updateall) {
                            memIcon.setMemory(memoryName);
                        }
                    }
                }
            }
        }
        return m;
    }

    /**
     * Get the color for drawing items in this block. Returns color based on
     * block occupancy.
     *
     * @return color for block
     */
    public Color getBlockColor() {
        if (getOccupancy() == OCCUPIED) {
            return blockOccupiedColor;
        } else if (useExtraColor) {
            return blockExtraColor;
        } else {
            return blockTrackColor;
        }
    }

    /**
     * Get the Block corresponding to this LayoutBlock.
     *
     * @return block
     */
    public Block getBlock() {
        return block;
    }

    /**
     * Returns Memory name
     *
     * @return name of memory
     */
    public String getMemoryName() {
        if (namedMemory != null) {
            return namedMemory.getName();
        }
        return memoryName;
    }

    /**
     * Get Memory.
     *
     * @return memory bean
     */
    public Memory getMemory() {
        if (namedMemory == null) {
            setMemoryName(memoryName);
        }
        if (namedMemory != null) {
            return namedMemory.getBean();
        }
        return null;
    }

    /**
     * Add Memory by name.
     *
     * @param name for memory
     */
    public void setMemoryName(String name) {
        if ((name == null) || name.isEmpty()) {
            namedMemory = null;
            memoryName = "";
            return;
        }
        memoryName = name;
        Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        if (memory != null) {
            namedMemory = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, memory);
        }
    }

    public void setMemory(Memory m, String name) {
        if (m == null) {
            namedMemory = null;
            memoryName = name == null ? "" : name;
            return;
        }
        namedMemory = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(name, m);
    }

    /**
     * Get occupancy Sensor name.
     *
     * @return name of occupancy sensor
     */
    public String getOccupancySensorName() {
        if (occupancyNamedSensor != null) {
            return occupancyNamedSensor.getName();
        }
        return occupancySensorName;
    }

    /**
     * Get occupancy Sensor.
     *
     * @return occ sensor name
     */
    public Sensor getOccupancySensor() {
        if (occupancyNamedSensor != null) {
            return occupancyNamedSensor.getBean();
        }
        return null;
    }

    /**
     * Add occupancy sensor by name.
     *
     * @param name for senor to add
     */
    public void setOccupancySensorName(String name) {
        if ((name == null) || name.isEmpty()) {
            if (occupancyNamedSensor != null) {
                occupancyNamedSensor.getBean().removePropertyChangeListener(mBlockListener);
            }
            occupancyNamedSensor = null;
            occupancySensorName = "";

            if (block != null) {
                block.setNamedSensor(null);
            }
            return;
        }
        occupancySensorName = name;
        Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
        if (sensor != null) {
            occupancyNamedSensor = InstanceManager.getDefault(
                    NamedBeanHandleManager.class).getNamedBeanHandle(name, sensor);
            if (block != null) {
                block.setNamedSensor(occupancyNamedSensor);
            }
        }
    }

    /**
     * Get occupied sensor state.
     *
     * @return occupied sensor state, defaults to Sensor.ACTIVE
     */
    public int getOccupiedSense() {
        return occupiedSense;
    }

    /**
     * Set occupied sensor state.
     *
     * @param sense eg. Sensor.INACTIVE
     */
    public void setOccupiedSense(int sense) {
        occupiedSense = sense;
    }

    /**
     * Test block occupancy.
     *
     * @return occupancy state
     */
    public int getOccupancy() {
        if (occupancyNamedSensor == null) {
            Sensor s = null;
            if (!occupancySensorName.isEmpty()) {
                s = InstanceManager.sensorManagerInstance().getSensor(occupancySensorName);
            }
            if (s == null) {
                //no occupancy sensor, so base upon block occupancy state
                if (block != null) {
                    return block.getState();
                }
                //if no block or sensor return unknown
                return UNKNOWN;
            }
            occupancyNamedSensor = InstanceManager.getDefault(
                    NamedBeanHandleManager.class).getNamedBeanHandle(occupancySensorName, s);
            if (block != null) {
                block.setNamedSensor(occupancyNamedSensor);
            }
        }

        if (getOccupancySensor() == null) {
            return UNKNOWN;
        }

        if (getOccupancySensor().getKnownState() != occupiedSense) {
            return EMPTY;
        } else if (getOccupancySensor().getKnownState() == occupiedSense) {
            return OCCUPIED;
        }
        return UNKNOWN;
    }

    @Override
    public int getState() {
        return getOccupancy();
    }

    /**
     * Does nothing, do not use.
     * Dummy for completion of NamedBean interface
     */
    @Override
    public void setState(int i) {
        log.error("this state does nothing {}", getDisplayName());
    }

    /**
     * Get the panel with the highest connectivity to this Layout Block.
     *
     * @return panel with most connections to this block
     */
    public LayoutEditor getMaxConnectedPanel() {
        LayoutEditor result = null;
        //a block is attached and this LayoutBlock is used
        if ((block != null) && (panels.size() > 0)) {
            //initialize connectivity as defined in first Layout Editor panel
            int maxConnectivity = Integer.MIN_VALUE;
            for (LayoutEditor panel : panels) {
                List<LayoutConnectivity> c = panel.getLEAuxTools().getConnectivityList(this);
                if (maxConnectivity < c.size()) {
                    maxConnectivity = c.size();
                    result = panel;
                }
            }
        }
        return result;
    }

    /**
     * Check/Update Path objects for the attached Block
     * <p>
     * If multiple panels are present, Paths are set according to the panel with
     * the highest connectivity (most LayoutConnectivity objects).
     */
    public void updatePaths() {
        //Update paths is called by the panel, turnouts, xings, track segments etc
        if ((block != null) && !panels.isEmpty()) {
            //a block is attached and this LayoutBlock is used
            //initialize connectivity as defined in first Layout Editor panel
            LayoutEditor panel = panels.get(0);
            List<LayoutConnectivity> c = panel.getLEAuxTools().getConnectivityList(this);

            //if more than one panel, find panel with the highest connectivity
            if (panels.size() > 1) {
                for (int i = 1; i < panels.size(); i++) {
                    if (c.size() < panels.get(i).getLEAuxTools().
                            getConnectivityList(this).size()) {
                        panel = panels.get(i);
                        c = panel.getLEAuxTools().getConnectivityList(this);
                    }
                }

                //Now try to determine if this block is across two panels due to a linked point
                PositionablePoint point = panel.getFinder().findPositionableLinkPoint(this);
                if ((point != null) && (point.getLinkedEditor() != null) && panels.contains(point.getLinkedEditor())) {
                    c = panel.getLEAuxTools().getConnectivityList(this);
                    c.addAll(point.getLinkedEditor().getLEAuxTools().getConnectivityList(this));
                } else {
                    //check that this connectivity is compatible with that of other panels.
                    for (LayoutEditor tPanel : panels) {
                        if ((tPanel != panel) && InstanceManager.getDefault(
                                LayoutBlockManager.class).warn()
                                && (!compareConnectivity(c, tPanel.getLEAuxTools().getConnectivityList(this)))) {
                            //send user an error message
                            int response = JOptionPane.showOptionDialog(null,
                                    java.text.MessageFormat.format(Bundle.getMessage("Warn1"),
                                            new Object[]{getUserName(), tPanel.getLayoutName(),
                                                panel.getLayoutName()}), Bundle.getMessage("WarningTitle"),
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                    null, new Object[]{Bundle.getMessage("ButtonOK"),
                                        Bundle.getMessage("ButtonOKPlus")}, Bundle.getMessage("ButtonOK"));
                            if (response != 0) {    //user elected to disable messages
                                InstanceManager.getDefault(
                                        LayoutBlockManager.class).turnOffWarning();
                            }
                        }
                    }
                }
            }
            //update block Paths to reflect connectivity as needed
            updateBlockPaths(c, panel);
        }
    }

    /**
     * Check/Update Path objects for the attached Block using the connectivity
     * in the specified Layout Editor panel.
     *
     * @param panel to extract paths
     */
    public void updatePathsUsingPanel(LayoutEditor panel) {
        if (panel == null) {
            log.error("Null panel in call to updatePathsUsingPanel");
            return;
        }
        List<LayoutConnectivity> c = panel.getLEAuxTools().getConnectivityList(this);
        updateBlockPaths(c, panel);

    }

    private void updateBlockPaths(List<LayoutConnectivity> c, LayoutEditor panel) {
        if (enableAddRouteLogging) {
            log.info("From {} updateBlockPaths Called", this.getDisplayName());
        }
        auxTools = panel.getLEAuxTools();
        List<Path> paths = block.getPaths();
        boolean[] used = new boolean[c.size()];
        int[] need = new int[paths.size()];
        Arrays.fill(used, false);
        Arrays.fill(need, -1);

        //cycle over existing Paths, checking against LayoutConnectivity
        for (int i = 0; i < paths.size(); i++) {
            Path p = paths.get(i);

            //cycle over LayoutConnectivity matching to this Path
            for (int j = 0; ((j < c.size()) && (need[i] == -1)); j++) {
                if (!used[j]) {
                    //this LayoutConnectivity not used yet
                    LayoutConnectivity lc = c.get(j);
                    if ((lc.getBlock1().getBlock() == p.getBlock()) || (lc.getBlock2().getBlock() == p.getBlock())) {
                        //blocks match - record
                        used[j] = true;
                        need[i] = j;
                    }
                }
            }
        }

        //update needed Paths
        for (int i = 0; i < paths.size(); i++) {
            if (need[i] >= 0) {
                Path p = paths.get(i);
                LayoutConnectivity lc = c.get(need[i]);
                if (lc.getBlock1() == this) {
                    p.setToBlockDirection(lc.getDirection());
                    p.setFromBlockDirection(lc.getReverseDirection());
                } else {
                    p.setToBlockDirection(lc.getReverseDirection());
                    p.setFromBlockDirection(lc.getDirection());
                }
                List<BeanSetting> beans = p.getSettings();
                for (int j = 0; j < beans.size(); j++) {
                    p.removeSetting(beans.get(j));
                }
                auxTools.addBeanSettings(p, lc, this);
            }
        }
        //delete unneeded Paths
        for (int i = 0; i < paths.size(); i++) {
            if (need[i] < 0) {
                block.removePath(paths.get(i));
                if (InstanceManager.getDefault(
                        LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                    removeAdjacency(paths.get(i));
                }
            }
        }

        //add Paths as required
        for (int j = 0; j < c.size(); j++) {
            if (!used[j]) {
                //there is no corresponding Path, add one.
                LayoutConnectivity lc = c.get(j);
                Path newp;

                if (lc.getBlock1() == this) {
                    newp = new Path(lc.getBlock2().getBlock(), lc.getDirection(),
                            lc.getReverseDirection());
                } else {
                    newp = new Path(lc.getBlock1().getBlock(), lc.getReverseDirection(),
                            lc.getDirection());
                }
                block.addPath(newp);

                if (enableAddRouteLogging) {
                    log.info("From {} addPath({})", this.getDisplayName(), newp.toString());
                }

                if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                    addAdjacency(newp);
                }
                auxTools.addBeanSettings(newp, lc, this);
            }
        }

        //djd debugging - lists results of automatic initialization of Paths and BeanSettings
        if (log.isDebugEnabled()) {
            block.getPaths().stream().forEach((p) -> {
                log.debug("From {} to {}", getDisplayName(), p.toString());
            });
        }
    }

    /**
     * Make sure all the layout connectivity objects in test are in main.
     *
     * @param main the main list of LayoutConnectivity objects
     * @param test the test list of LayoutConnectivity objects
     * @return true if all test layout connectivity objects are in main
     */
    private boolean compareConnectivity(List<LayoutConnectivity> main, List<LayoutConnectivity> test) {
        boolean result = false;     //assume failure (pessimsit!)
        if (!main.isEmpty() && !test.isEmpty()) {
            result = true;          //assume success (optimist!)
            //loop over connectivities in test list
            for (LayoutConnectivity tc : test) {
                LayoutBlock tlb1 = tc.getBlock1(), tlb2 = tc.getBlock2();
                //loop over main list to make sure the same blocks are connected
                boolean found = false;  //assume failure (pessimsit!)
                for (LayoutConnectivity mc : main) {
                    LayoutBlock mlb1 = mc.getBlock1(), mlb2 = mc.getBlock2();
                    if (((tlb1 == mlb1) && (tlb2 == mlb2))
                            || ((tlb1 == mlb2) && (tlb2 == mlb1))) {
                        found = true;   //success!
                        break;
                    }
                }
                if (!found) {
                    result = false;
                    break;
                }
            }
        } else if (main.isEmpty() && test.isEmpty()) {
            result = true;          // OK if both have no neighbors, common for turntable rays
        }
        return result;
    }

    /**
     * Handle tasks when block changes
     *
     * @param e propChgEvent
     */
    void handleBlockChange(PropertyChangeEvent e) {
        //Update memory object if there is one
        if ((getMemory() != null) && (block != null) && !suppressNameUpdate) {
            //copy block value to memory if there is a value
            Object val = block.getValue();
            if (val != null) {
                if (!(val instanceof RosterEntry) && !(val instanceof Reportable)) {
                    val = val.toString();
                }
            }
            getMemory().setValue(val);
        }
        if (e.getPropertyName().equals("UserName")) {
            setUserName(e.getNewValue().toString());
            jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).
                    renameBean(e.getOldValue().toString(), e.getNewValue().toString(), this);
        }
        //Redraw all Layout Editor panels using this Layout Block
        redrawLayoutBlockPanels();

        if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
            stateUpdate();
        }
    }

    /**
     * Deactivate block listener for redraw of panels and update of memories on
     * change of state
     */
    private void deactivateBlock() {
        if ((mBlockListener != null) && (block != null)) {
            block.removePropertyChangeListener(mBlockListener);
        }
        mBlockListener = null;
    }

    /**
     * Set/reset update of memory name when block goes from occupied to
     * unoccupied or vice versa. If set is true, name update is suppressed. If
     * set is false, name update works normally.
     *
     * @param set true, update suppress. false, update normal
     */
    public void setSuppressNameUpdate(boolean set) {
        suppressNameUpdate = set;
    }

    //variables for Edit Layout Block pane
    private JmriJFrame editLayoutBlockFrame = null;
    private final JTextField sensorNameField = new JTextField(16);
    private final JTextField sensorDebounceInactiveField = new JTextField(5);
    private final JTextField sensorDebounceActiveField = new JTextField(5);
    private final JCheckBox sensorDebounceGlobalCheck = new JCheckBox(Bundle.getMessage("SensorUseGlobalDebounce"));

    private final NamedBeanComboBox<Memory> memoryComboBox = new NamedBeanComboBox<>(
            InstanceManager.getDefault(MemoryManager.class), null, DisplayOptions.DISPLAYNAME);

    private final JTextField metricField = new JTextField(10);

    private final JComboBox<String> senseBox = new JComboBox<>();

    private final JCheckBox permissiveCheck = new JCheckBox("Permissive Working Allowed");

    //TODO I18N in Bundle.properties
    private int senseActiveIndex;
    private int senseInactiveIndex;

    private JColorChooser trackColorChooser = null;
    private JColorChooser occupiedColorChooser = null;
    private JColorChooser extraColorChooser = null;

    protected void editLayoutBlock(Component callingPane) {
        LayoutBlockEditAction beanEdit = new LayoutBlockEditAction();
        if (block == null) {
            //Block may not have been initialised due to an error so manually set it in the edit window
            String userName = getUserName();
            if ((userName != null) && !userName.isEmpty()) {
                Block b = InstanceManager.getDefault(BlockManager.class).getBlock(userName);
                if (b != null) {
                    beanEdit.setBean(b);
                }
            }
        } else {
            beanEdit.setBean(block);
        }
        beanEdit.actionPerformed(null);
    }

    private final String[] working = {"Bi-Directional", "Receive Only", "Send Only"};

    //TODO I18N in ManagersBundle.properties
    protected List<JComboBox<String>> neighbourDir;

    void blockEditDonePressed(ActionEvent a) {
        boolean needsRedraw = false;
        //check if Sensor changed
        String newName = NamedBean.normalizeUserName(sensorNameField.getText());
        if (!(getOccupancySensorName()).equals(newName)) {
            //sensor has changed
            if ((newName == null) || newName.isEmpty()) {
                setOccupancySensorName(newName);
                sensorNameField.setText("");
                needsRedraw = true;
            } else if (validateSensor(newName, editLayoutBlockFrame) == null) {
                //invalid sensor entered
                occupancyNamedSensor = null;
                occupancySensorName = "";
                sensorNameField.setText("");
                return;
            } else {
                sensorNameField.setText(newName);
                needsRedraw = true;
            }
        }

        if (getOccupancySensor() != null) {
            if (sensorDebounceGlobalCheck.isSelected()) {
                getOccupancySensor().setUseDefaultTimerSettings(true);
            } else {
                getOccupancySensor().setUseDefaultTimerSettings(false);
                if (!sensorDebounceInactiveField.getText().trim().isEmpty()) {
                    getOccupancySensor().setSensorDebounceGoingInActiveTimer(Long.parseLong(sensorDebounceInactiveField.getText().trim()));
                }
                if (!sensorDebounceActiveField.getText().trim().isEmpty()) {
                    getOccupancySensor().setSensorDebounceGoingActiveTimer(Long.parseLong(sensorDebounceActiveField.getText().trim()));
                }
            }
            Reporter reporter = getOccupancySensor().getReporter();
            if (reporter != null && block != null) {
                String msg = java.text.MessageFormat.format(
                        Bundle.getMessage("BlockAssignReporter"),
                        new Object[]{getOccupancySensor().getDisplayName(),
                            reporter.getDisplayName()});
                if (JOptionPane.showConfirmDialog(editLayoutBlockFrame,
                        msg, Bundle.getMessage("BlockAssignReporterTitle"),
                        JOptionPane.YES_NO_OPTION) == 0) {
                    block.setReporter(reporter);
                }
            }
        }

        //check if occupied sense changed
        int k = senseBox.getSelectedIndex();
        int oldSense = occupiedSense;
        if (k == senseActiveIndex) {
            occupiedSense = Sensor.ACTIVE;
        } else {
            occupiedSense = Sensor.INACTIVE;
        }
        if (oldSense != occupiedSense) {
            needsRedraw = true;
        }

        //check if track color changed
        Color oldColor = blockTrackColor;
        blockTrackColor = trackColorChooser.getColor();
        if (oldColor != blockTrackColor) {
            needsRedraw = true;
        }
        //check if occupied color changed
        oldColor = blockOccupiedColor;
        blockOccupiedColor = occupiedColorChooser.getColor();
        if (oldColor != blockOccupiedColor) {
            needsRedraw = true;
        }
        //check if extra color changed
        oldColor = blockExtraColor;
        blockExtraColor = extraColorChooser.getColor();
        if (oldColor != blockExtraColor) {
            needsRedraw = true;
        }

        //check if Memory changed
        newName = memoryComboBox.getSelectedItemDisplayName();
        if (newName == null) newName = "";
        if (!memoryName.equals(newName)) {
            //memory has changed
            setMemory(validateMemory(newName, editLayoutBlockFrame), newName);
            if (getMemory() == null) {
                //invalid memory entered
                memoryName = "";
                memoryComboBox.setSelectedItem(null);
                return;
            } else {
                memoryComboBox.setSelectedItem(getMemory());
                needsRedraw = true;
            }
        }
        int m = Integer.parseInt(metricField.getText().trim());
        if (m != metric) {
            setBlockMetric(m);
        }
        block.setPermissiveWorking(permissiveCheck.isSelected());
        if (neighbourDir != null) {
            for (int i = 0; i < neighbourDir.size(); i++) {
                int neigh = neighbourDir.get(i).getSelectedIndex();
                neighbours.get(i).getBlock().removeBlockDenyList(this.block);
                this.block.removeBlockDenyList(neighbours.get(i).getBlock());
                switch (neigh) {
                    case 0: {
                        updateNeighbourPacketFlow(neighbours.get(i), RXTX);
                        break;
                    }

                    case 1: {
                        neighbours.get(i).getBlock().addBlockDenyList(this.block.getDisplayName());
                        updateNeighbourPacketFlow(neighbours.get(i), TXONLY);
                        break;
                    }

                    case 2: {
                        this.block.addBlockDenyList(neighbours.get(i).getBlock().getDisplayName());
                        updateNeighbourPacketFlow(neighbours.get(i), RXONLY);
                        break;
                    }

                    default: {
                        break;
                    }
                }
                /* switch */
            }
        }
        //complete
        editLayoutBlockFrame.setVisible(false);
        editLayoutBlockFrame.dispose();
        editLayoutBlockFrame = null;

        if (needsRedraw) {
            redrawLayoutBlockPanels();
        }
    }

    void blockEditCancelPressed(ActionEvent a) {
        editLayoutBlockFrame.setVisible(false);
        editLayoutBlockFrame.dispose();
        editLayoutBlockFrame = null;
    }

    protected class LayoutBlockEditAction extends BlockEditAction {

        @Override
        public String helpTarget() {
            return "package.jmri.jmrit.display.EditLayoutBlock";
        }  //IN18N

        @Override
        protected void initPanels() {
            super.initPanels();
            BeanItemPanel ld = layoutDetails();
            if (InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                blockRoutingDetails();
            }
            setSelectedComponent(ld);
        }

        BeanItemPanel layoutDetails() {
            BeanItemPanel layout = new BeanItemPanel();
            layout.setName(Bundle.getMessage("LayoutEditor"));

            LayoutEditor.setupComboBox(memoryComboBox, false, true, false);

            layout.addItem(new BeanEditItem(new JLabel("" + useCount), Bundle.getMessage("UseCount"), null));
            layout.addItem(new BeanEditItem(memoryComboBox, Bundle.getMessage("BeanNameMemory"),
                    Bundle.getMessage("MemoryVariableTip")));

            senseBox.removeAllItems();
            senseBox.addItem(Bundle.getMessage("SensorStateActive"));
            senseActiveIndex = 0;
            senseBox.addItem(Bundle.getMessage("SensorStateInactive"));
            senseInactiveIndex = 1;

            layout.addItem(new BeanEditItem(senseBox, Bundle.getMessage("OccupiedSense"), Bundle.getMessage("OccupiedSenseHint")));

            trackColorChooser = new JColorChooser(blockTrackColor);
            trackColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel
            AbstractColorChooserPanel trackColorPanels[] = {new SplitButtonColorChooserPanel()};
            trackColorChooser.setChooserPanels(trackColorPanels);
            layout.addItem(new BeanEditItem(trackColorChooser, Bundle.getMessage("TrackColor"), Bundle.getMessage("TrackColorHint")));

            occupiedColorChooser = new JColorChooser(blockOccupiedColor);
            occupiedColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel
            AbstractColorChooserPanel occupiedColorPanels[] = {new SplitButtonColorChooserPanel()};
            occupiedColorChooser.setChooserPanels(occupiedColorPanels);
            layout.addItem(new BeanEditItem(occupiedColorChooser, Bundle.getMessage("OccupiedColor"), Bundle.getMessage("OccupiedColorHint")));

            extraColorChooser = new JColorChooser(blockExtraColor);
            extraColorChooser.setPreviewPanel(new JPanel()); // remove the preview panel
            AbstractColorChooserPanel extraColorPanels[] = {new SplitButtonColorChooserPanel()};
            extraColorChooser.setChooserPanels(extraColorPanels);
            layout.addItem(new BeanEditItem(extraColorChooser, Bundle.getMessage("ExtraColor"), Bundle.getMessage("ExtraColorHint")));

            layout.setSaveItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean needsRedraw = false;
                    int k = senseBox.getSelectedIndex();
                    int oldSense = occupiedSense;

                    if (k == senseActiveIndex) {
                        occupiedSense = Sensor.ACTIVE;
                    } else {
                        occupiedSense = Sensor.INACTIVE;
                    }

                    if (oldSense != occupiedSense) {
                        needsRedraw = true;
                    }
                    //check if track color changed
                    Color oldColor = blockTrackColor;
                    blockTrackColor = trackColorChooser.getColor();
                    if (oldColor != blockTrackColor) {
                        needsRedraw = true;
                        JmriColorChooser.addRecentColor(blockTrackColor);
                    }
                    //check if occupied color changed
                    oldColor = blockOccupiedColor;
                    blockOccupiedColor = occupiedColorChooser.getColor();
                    if (oldColor != blockOccupiedColor) {
                        needsRedraw = true;
                        JmriColorChooser.addRecentColor(blockOccupiedColor);
                    }
                    //check if extra color changed
                    oldColor = blockExtraColor;
                    blockExtraColor = extraColorChooser.getColor();
                    if (oldColor != blockExtraColor) {
                        needsRedraw = true;
                        JmriColorChooser.addRecentColor(blockExtraColor);
                    }
                    //check if Memory changed
                    String newName = memoryComboBox.getSelectedItemDisplayName();
                    if (newName == null) newName = "";
                    if (!memoryName.equals(newName)) {
                        //memory has changed
                        setMemory(validateMemory(newName, editLayoutBlockFrame), newName);
                        if (getMemory() == null) {
                            //invalid memory entered
                            memoryName = "";
                            memoryComboBox.setSelectedItem(null);
                            return;
                        } else {
                            memoryComboBox.setSelectedItem(getMemory());
                            needsRedraw = true;
                        }
                    }

                    if (needsRedraw) {
                        redrawLayoutBlockPanels();
                    }
                }
            });

            layout.setResetItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    memoryComboBox.setSelectedItem(getMemory());
                    trackColorChooser.setColor(blockTrackColor);
                    occupiedColorChooser.setColor(blockOccupiedColor);
                    extraColorChooser.setColor(blockExtraColor);
                    if (occupiedSense == Sensor.ACTIVE) {
                        senseBox.setSelectedIndex(senseActiveIndex);
                    } else {
                        senseBox.setSelectedIndex(senseInactiveIndex);
                    }
                }
            });
            bei.add(layout);
            return layout;
        }

        BeanItemPanel blockRoutingDetails() {
            BeanItemPanel routing = new BeanItemPanel();
            routing.setName("Routing");

            routing.addItem(new BeanEditItem(metricField, "Block Metric", "set the cost for going over this block"));

            routing.addItem(new BeanEditItem(null, null, "Set the direction of the connection to the neighbouring block"));
            neighbourDir = new ArrayList<>(getNumberOfNeighbours());
            for (int i = 0; i < getNumberOfNeighbours(); i++) {
                JComboBox<String> dir = new JComboBox<>(working);
                routing.addItem(new BeanEditItem(dir, getNeighbourAtIndex(i).getDisplayName(), null));
                neighbourDir.add(dir);
            }

            routing.setResetItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    metricField.setText(Integer.toString(metric));
                    for (int i = 0; i < getNumberOfNeighbours(); i++) {
                        JComboBox<String> dir = neighbourDir.get(i);
                        Block blk = neighbours.get(i).getBlock();
                        if (block.isBlockDenied(blk)) {
                            dir.setSelectedIndex(2);
                        } else if (blk.isBlockDenied(block)) {
                            dir.setSelectedIndex(1);
                        } else {
                            dir.setSelectedIndex(0);
                        }
                    }
                }
            });

            routing.setSaveItem(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int m = Integer.parseInt(metricField.getText().trim());
                    if (m != metric) {
                        setBlockMetric(m);
                    }
                    block.setPermissiveWorking(permissiveCheck.isSelected());
                    if (neighbourDir != null) {
                        for (int i = 0; i < neighbourDir.size(); i++) {
                            int neigh = neighbourDir.get(i).getSelectedIndex();
                            neighbours.get(i).getBlock().removeBlockDenyList(block);
                            block.removeBlockDenyList(neighbours.get(i).getBlock());
                            switch (neigh) {
                                case 0: {
                                    updateNeighbourPacketFlow(neighbours.get(i), RXTX);
                                    break;
                                }

                                case 1: {
                                    neighbours.get(i).getBlock().addBlockDenyList(block.getDisplayName());
                                    updateNeighbourPacketFlow(neighbours.get(i), TXONLY);
                                    break;
                                }

                                case 2: {
                                    block.addBlockDenyList(neighbours.get(i).getBlock().getDisplayName());
                                    updateNeighbourPacketFlow(neighbours.get(i), RXONLY);
                                    break;
                                }

                                default: {
                                    break;
                                }
                            }
                            /* switch */
                        }
                    }
                }
            });
            bei.add(routing);
            return routing;
        }
    }

    /**
     * Remove this object from display and persistance.
     */
    void remove() {
        //if an occupancy sensor has been activated, deactivate it
        deactivateBlock();
        //remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;

    /**
     * "active" is true if the object is still displayed, and should be stored.
     *
     * @return active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * The code below relates to the layout block routing protocol
     */

    /**
     * Set the block metric based upon the track segment that the block
     * is associated with if the (200 if Side, 50 if Main). If the block is
     * assigned against multiple track segments all with different types then
     * the highest type will be used. In theory no reason why it couldn't be a
     * compromise.
     */
    void setBlockMetric() {
        if (!defaultMetric) {
            return;
        }
        if (enableUpdateRouteLogging) {
            log.info("From '{}' default set block metric called", this.getDisplayName());
        }
        LayoutEditor panel = getMaxConnectedPanel();
        if (panel == null) {
            if (enableUpdateRouteLogging) {
                log.info("From '{}' unable to set metric as we are not connected to a panel yet", this.getDisplayName());
            }
            return;
        }
        String userName = getUserName();
        if (userName == null) {
            log.info("From '{}': unable to get user name", this.getDisplayName());
            return;
        }
        List<TrackSegment> ts = panel.getFinder().findTrackSegmentByBlock(userName);
        int mainline = 0;
        int side = 0;

        for (int i = 0; i < ts.size(); i++) {
            if (ts.get(i).isMainline()) {
                mainline++;
            } else {
                side++;
            }
        }

        if (mainline > side) {
            metric = 50;
        } else if (mainline < side) {
            metric = 200;
        } else {
            //They must both be equal so will set as a mainline.
            metric = 50;
        }

        if (enableUpdateRouteLogging) {
            log.info("From '{}' metric set to {}", this.getDisplayName(), metric);
        }

        // What we need to do here, is resend our routing packets with the new metric
        RoutingPacket update = new RoutingPacket(UPDATE, this.getBlock(), -1, metric, -1, -1, getNextPacketID());
        firePropertyChange("routing", null, update);
    }

    private boolean defaultMetric = true;

    public boolean useDefaultMetric() {
        return defaultMetric;
    }

    public void useDefaultMetric(boolean boo) {
        if (boo == defaultMetric) {
            return;
        }
        defaultMetric = boo;
        if (boo) {
            setBlockMetric();
        }
    }

    /**
     * Set a metric cost against a block, this is used in the calculation of a
     * path between two location on the layout, a lower path cost is always
     * preferred For Layout blocks defined as Mainline the default metric is 50.
     * For Layout blocks defined as a Siding the default metric is 200.
     *
     * @param m metric value
     */
    public void setBlockMetric(int m) {
        if (metric == m) {
            return;
        }
        metric = m;
        defaultMetric = false;
        RoutingPacket update = new RoutingPacket(UPDATE, this.getBlock(), -1, metric, -1, -1, getNextPacketID());
        firePropertyChange("routing", null, update);
    }

    /**
     * Get the layout block metric cost
     *
     * @return metric cost of block
     */
    public int getBlockMetric() {
        return metric;
    }

    //re work this so that is makes beter us of existing code.
    //This is no longer required currently, but might be used at a later date.
    public void addAllThroughPaths() {
        if (enableAddRouteLogging) {
            log.info("Add all ThroughPaths {}", this.getDisplayName());
        }

        if ((block != null) && (panels.size() > 0)) {
            //a block is attached and this LayoutBlock is used
            //initialize connectivity as defined in first Layout Editor panel
            LayoutEditor panel = panels.get(0);
            List<LayoutConnectivity> c = panel.getLEAuxTools().getConnectivityList(this);

            //if more than one panel, find panel with the highest connectivity
            if (panels.size() > 1) {
                for (int i = 1; i < panels.size(); i++) {
                    if (c.size() < panels.get(i).getLEAuxTools().
                            getConnectivityList(this).size()) {
                        panel = panels.get(i);
                        c = panel.getLEAuxTools().getConnectivityList(this);
                    }
                }

                //check that this connectivity is compatible with that of other panels.
                for (LayoutEditor tPanel : panels) {
                    if ((tPanel != panel)
                            && InstanceManager.getDefault(LayoutBlockManager.class).
                                    warn() && (!compareConnectivity(c, tPanel.getLEAuxTools().getConnectivityList(this)))) {

                        //send user an error message
                        int response = JOptionPane.showOptionDialog(null,
                                java.text.MessageFormat.format(Bundle.getMessage("Warn1"),
                                        new Object[]{getUserName(), tPanel.getLayoutName(),
                                            panel.getLayoutName()}), Bundle.getMessage("WarningTitle"),
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, new Object[]{Bundle.getMessage("ButtonOK"),
                                    Bundle.getMessage("ButtonOKPlus")}, Bundle.getMessage("ButtonOK"));
                        if (response != 0) {    //user elected to disable messages
                            InstanceManager.getDefault(LayoutBlockManager.class).turnOffWarning();
                        }
                    }
                }
            }
            auxTools = panel.getLEAuxTools();
            List<LayoutConnectivity> d = auxTools.getConnectivityList(this);
            List<LayoutBlock> attachedBlocks = new ArrayList<>();

            for (int i = 0; i < d.size(); i++) {
                if (d.get(i).getBlock1() != this) {
                    attachedBlocks.add(d.get(i).getBlock1());
                } else {
                    attachedBlocks.add(d.get(i).getBlock2());
                }
            }
            //Will need to re-look at this to cover both way and single way routes
            List<LayoutBlock> attachedBlocks2 = attachedBlocks;
            for (int i = 0; i < attachedBlocks.size(); i++) {
                if (enableAddRouteLogging) {
                    log.info("From {} block is attached {}", this.getDisplayName(), attachedBlocks.get(i).getDisplayName());
                }

                for (int x = 0; x < attachedBlocks2.size(); x++) {
                    addThroughPath(attachedBlocks.get(i).getBlock(), attachedBlocks2.get(x).getBlock(), panel);
                }
            }
        }
    }

    //TODO: if the block already exists, we still may want to re-work the through paths
    // With this bit we need to get our neighbour to send new routes
    private void addNeighbour(Block addBlock, int direction, int workingDirection) {
        boolean layoutConnectivityBefore = layoutConnectivity;

        if (enableAddRouteLogging) {
            log.info("From {} asked to add block {} as new neighbour {}", this.getDisplayName(),
                    addBlock.getDisplayName(), decodePacketFlow(workingDirection));
        }

        if (getAdjacency(addBlock) != null) {
            if (enableAddRouteLogging) {
                log.info("Block is already registered");
            }
            addThroughPath(getAdjacency(addBlock));
        } else {
            Adjacencies adj = new Adjacencies(addBlock, direction, workingDirection);
            neighbours.add(adj);

            //Add the neighbour to our routing table.
            LayoutBlock blk = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(addBlock);
            LayoutEditor editor = getMaxConnectedPanel();

            if ((editor != null) && (connection == null)) {
                //We should be able to determine block metric now as the tracksegment should be valid
                connection = new ConnectivityUtil(editor);
            }

            //Need to inform our neighbours of our new addition
            //We only add an entry into the routing table if we are able to reach the next working block.
            //If we only transmit routes to it, then we can not route to it therefore it is not added
            Routes route = null;

            if ((workingDirection == RXTX) || (workingDirection == RXONLY)) {
                if (blk != null) {
                    route = new Routes(addBlock, this.getBlock(), 1, direction, blk.getBlockMetric(), addBlock.getLengthMm());
                } else {
                    route = new Routes(addBlock, this.getBlock(), 1, direction, 0, 0);
                }
                routes.add(route);
            }

            if (blk != null) {
                boolean mutual = blk.informNeighbourOfAttachment(this, this.getBlock(), workingDirection);

                //The propertychange listener will have to be modified depending upon RX or TX selection.
                //if we only transmit routes to this neighbour then we do not want to listen to thier broadcast messages
                if ((workingDirection == RXTX) || (workingDirection == RXONLY)) {
                    blk.addPropertyChangeListener(this);
                    //log.info("From {} add property change {}", this.getDisplayName(), blk.getDisplayName());
                } else {
                    blk.removePropertyChangeListener(this);
                }

                int neighwork = blk.getAdjacencyPacketFlow(this.getBlock());
                if (enableAddRouteLogging) {
                    log.info("{}.getAdjacencyPacketFlow({}): {}, {}",
                            blk.getDisplayName(), this.getBlock().getDisplayName(), decodePacketFlow(neighwork), neighwork);
                }

                if (neighwork != -1) {
                    if (enableAddRouteLogging) {
                        log.info("From {} Updating flow direction to {} for block {} choice of {} {}", this.getDisplayName(),
                                decodePacketFlow(determineAdjPacketFlow(workingDirection, neighwork)),
                                blk.getDisplayName(), decodePacketFlow(workingDirection), decodePacketFlow(neighwork));
                    }
                    int newPacketFlow = determineAdjPacketFlow(workingDirection, neighwork);
                    adj.setPacketFlow(newPacketFlow);

                    if (newPacketFlow == TXONLY) {
                        for (int j = routes.size() - 1; j > -1; j--) {
                            Routes ro = routes.get(j);
                            if ((ro.getDestBlock() == addBlock)
                                    && (ro.getNextBlock() == this.getBlock())) {
                                adj.removeRouteAdvertisedToNeighbour(ro);
                                routes.remove(j);
                            }
                        }
                        RoutingPacket newUpdate = new RoutingPacket(REMOVAL, addBlock, -1, -1, -1, -1, getNextPacketID());
                        neighbours.forEach((adja) -> {
                            adja.removeRouteAdvertisedToNeighbour(addBlock);
                        });
                        firePropertyChange("routing", null, newUpdate);
                    }
                } else {
                    if (enableAddRouteLogging) {
                        log.info("From {} neighbour {} working direction is not valid",
                                this.getDisplayName(), addBlock.getDisplayName());
                    }
                    return;
                }
                adj.setMutual(mutual);

                if (route != null) {
                    route.stateChange();
                }
                addThroughPath(getAdjacency(addBlock));
                //We get our new neighbour to send us a list of valid routes that they have.
                //This might have to be re-written as a property change event?
                //Also only inform our neighbour if they have us down as a mutual, otherwise it will just reject the packet.
                if (((workingDirection == RXTX) || (workingDirection == TXONLY)) && mutual) {
                    blk.informNeighbourOfValidRoutes(getBlock());
                }
            } else if (enableAddRouteLogging) {
                log.info("From {} neighbour {} has no layoutBlock associated, metric set to {}",
                        this.getDisplayName(), addBlock.getDisplayName(), adj.getMetric());
            }
        }

        /* If the connectivity before has not completed and produced an error with
           setting up through Paths, we will cycle through them */
        if (enableAddRouteLogging) {
            log.info("From {} layout connectivity before {}", this.getDisplayName(), layoutConnectivityBefore);
        }
        if (!layoutConnectivityBefore) {
            for (int i = 0; i < neighbours.size(); i++) {
                addThroughPath(neighbours.get(i));
            }
        }
        /* We need to send our new neighbour our copy of the routing table however
           we can only send valid routes that would be able to traverse as definded by
           through paths table */
    }

    private boolean informNeighbourOfAttachment(LayoutBlock lBlock, Block block, int workingDirection) {
        Adjacencies adj = getAdjacency(block);
        if (adj == null) {
            if (enableAddRouteLogging) {
                log.info("From {} neighbour {} has informed us of its attachment to us, however we do not yet have it registered",
                        this.getDisplayName(), lBlock.getDisplayName());
            }
            return false;
        }

        if (!adj.isMutual()) {
            if (enableAddRouteLogging) {
                log.info("From {} neighbour {} wants us to {}; we have it set as {}",
                        this.getDisplayName(), block.getDisplayName(),
                        decodePacketFlow(workingDirection), decodePacketFlow(adj.getPacketFlow()));
            }

            // Simply if both the neighbour and us both want to do the same thing with sending routing information,
            // in one direction then no routes will be passed
            int newPacketFlow = determineAdjPacketFlow(adj.getPacketFlow(), workingDirection);
            if (enableAddRouteLogging) {
                log.info("From " + this.getDisplayName() + " neighbour " + block.getDisplayName()
                        + " passed " + decodePacketFlow(workingDirection) + " we have "
                        + decodePacketFlow(adj.getPacketFlow()) + " this will be updated to "
                        + decodePacketFlow(newPacketFlow));
            }
            adj.setPacketFlow(newPacketFlow);

            //If we are only set to transmit routing information to the adj, then
            //we will not have it appearing in the routing table
            if (newPacketFlow != TXONLY) {
                Routes neighRoute = getValidRoute(this.getBlock(), adj.getBlock());
                //log.info("From " + this.getDisplayName() + " neighbour " + adj.getBlock().getDisplayName() + " valid routes returned as " + neighRoute);
                if (neighRoute == null) {
                    log.info("Null route so will bomb out");
                    return false;
                }

                if (neighRoute.getMetric() != adj.getMetric()) {
                    if (enableAddRouteLogging) {
                        log.info("From " + this.getDisplayName() + " The value of the metric we have for this route is not correct " + this.getBlock().getDisplayName() + ", stored " + neighRoute.getMetric() + " v "
                                + adj.getMetric());
                    }
                    neighRoute.setMetric(adj.getMetric());
                    //This update might need to be more selective
                    RoutingPacket update = new RoutingPacket(UPDATE, adj.getBlock(), -1, (adj.getMetric() + metric), -1, -1, getNextPacketID());
                    firePropertyChange("routing", null, update);
                }

                if (neighRoute.getMetric() != (int) adj.getLength()) {
                    if (enableAddRouteLogging) {
                        log.info("From " + this.getDisplayName()
                                + " The value of the length we have for this route is not correct "
                                + this.getBlock().getDisplayName() + ", stored "
                                + neighRoute.getMetric() + " v " + adj.getMetric());
                    }
                    neighRoute.setLength(adj.getLength());
                    //This update might need to be more selective
                    RoutingPacket update = new RoutingPacket(UPDATE, adj.getBlock(), -1, -1,
                            adj.getLength() + block.getLengthMm(), -1, getNextPacketID());
                    firePropertyChange("routing", null, update);
                }
                Routes r = getRouteByDestBlock(block);
                if (r != null) {
                    r.setMetric(lBlock.getBlockMetric());
                } else {
                    log.warn("No getRouteByDestBlock('{}')", block.getDisplayName());
                }
            }

            if (enableAddRouteLogging) {
                log.info("From " + this.getDisplayName() + " We were not a mutual adjacency with "
                        + lBlock.getDisplayName() + " but now are");
            }

            if ((newPacketFlow == RXTX) || (newPacketFlow == RXONLY)) {
                lBlock.addPropertyChangeListener(this);
            } else {
                lBlock.removePropertyChangeListener(this);
            }

            if (newPacketFlow == TXONLY) {
                for (int j = routes.size() - 1; j > -1; j--) {
                    Routes ro = routes.get(j);
                    if ((ro.getDestBlock() == block) && (ro.getNextBlock() == this.getBlock())) {
                        adj.removeRouteAdvertisedToNeighbour(ro);
                        routes.remove(j);
                    }
                }

                for (int j = throughPaths.size() - 1; j > -1; j--) {
                    if ((throughPaths.get(j).getDestinationBlock() == block)) {
                        if (enableAddRouteLogging) {
                            log.info("From " + this.getDisplayName() + " removed throughpath " + throughPaths.get(
                                    j).getSourceBlock().getDisplayName() + " " + throughPaths.get(
                                            j).getDestinationBlock().getDisplayName());
                        }
                        throughPaths.remove(j);
                    }
                }
                RoutingPacket newUpdate = new RoutingPacket(REMOVAL, block, -1, -1, -1, -1, getNextPacketID());
                neighbours.forEach((adja) -> {
                    adja.removeRouteAdvertisedToNeighbour(block);
                });
                firePropertyChange("routing", null, newUpdate);
            }

            adj.setMutual(true);
            addThroughPath(adj);

            //As we are now mutual we will send our neigh a list of valid routes.
            if ((newPacketFlow == RXTX) || (newPacketFlow == TXONLY)) {
                if (enableAddRouteLogging) {
                    log.info("From " + this.getDisplayName() + " inform neighbour of valid routes");
                }
                informNeighbourOfValidRoutes(block);
            }
        }
        return true;
    }

    private int determineAdjPacketFlow(int our, int neigh) {
        //Both are the same
        if (enableUpdateRouteLogging) {
            log.info("From " + this.getDisplayName() + " values passed our "
                    + decodePacketFlow(our) + " neigh " + decodePacketFlow(neigh));
        }
        if ((our == RXTX) && (neigh == RXTX)) {
            return RXTX;
        }

        /*First off reverse the neighbour flow, as it will be telling us if it will allow or deny traffic from us.
           So if it is set to RX, then we can TX to it.*/
        if (neigh == RXONLY) {
            neigh = TXONLY;
        } else if (neigh == TXONLY) {
            neigh = RXONLY;
        }

        if (our == neigh) {
            return our;
        }
        return NONE;
    }

    private void informNeighbourOfValidRoutes(Block newblock) {
        //java.sql.Timestamp t1 = new java.sql.Timestamp(System.nanoTime());
        List<Block> validFromPath = new ArrayList<>();
        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " new block " + newblock.getDisplayName());
        }

        for (ThroughPaths tp : throughPaths) {
            if (enableAddRouteLogging) {
                log.info("From " + this.getDisplayName() + " B through routes "
                        + tp.getSourceBlock().getDisplayName() + " "
                        + tp.getDestinationBlock().getDisplayName());
            }

            if (tp.getSourceBlock() == newblock) {
                validFromPath.add(tp.getDestinationBlock());
            } else if (tp.getDestinationBlock() == newblock) {
                validFromPath.add(tp.getSourceBlock());
            }
        }

        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " ===== valid from size path " + validFromPath.size() + " ====");
            log.info(newblock.getDisplayName());
        }

        //We only send packets on to our neighbour that are registered as being on a valid through path and are mutual.
        LayoutBlock lBnewblock = null;
        Adjacencies adj = getAdjacency(newblock);
        if (adj.isMutual()) {
            if (enableAddRouteLogging) {
                log.info("From " + this.getDisplayName() + " adj with " + newblock.getDisplayName() + " is mutual");
            }
            lBnewblock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(newblock);
        } else if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " adj with " + newblock.getDisplayName() + " is NOT mutual");
        }

        if (lBnewblock == null) {
            return;
        }

        for (int i = 0; i < routes.size(); i++) {
            Routes ro = routes.get(i);
            if (enableAddRouteLogging) {
                log.info("next:" + ro.getNextBlock().getDisplayName() + " dest:" + ro.getDestBlock().getDisplayName());
            }

            if (ro.getNextBlock() == getBlock()) {
                if (enableAddRouteLogging) {
                    log.info("From " + this.getDisplayName() + " ro next block is this");
                }
                if (validFromPath.contains(ro.getDestBlock())) {
                    if (enableAddRouteLogging) {
                        log.info("From " + this.getDisplayName()
                                + " route to " + ro.getDestBlock().getDisplayName()
                                + " we have it with a metric of " + ro.getMetric()
                                + " we will add our metric of " + metric
                                + " this will be sent to " + lBnewblock.getDisplayName() + " a");
                    } //we added +1 to hop count and our metric.

                    RoutingPacket update = new RoutingPacket(ADDITION, ro.getDestBlock(), ro.getHopCount() + 1,
                            (ro.getMetric() + metric), (ro.getLength() + block.getLengthMm()), -1, getNextPacketID());
                    lBnewblock.addRouteFromNeighbour(this, update);
                }
            } else {
                //Don't know if this might need changing so that we only send out our best
                //route to the neighbour, rather than cycling through them all.
                if (validFromPath.contains(ro.getNextBlock())) {
                    if (enableAddRouteLogging) {
                        log.info("From " + this.getDisplayName()
                                + " route to " + ro.getDestBlock().getDisplayName()
                                + " we have it with a metric of " + ro.getMetric()
                                + " we will add our metric of " + metric
                                + " this will be sent to " + lBnewblock.getDisplayName() + " b");
                    } //we added +1 to hop count and our metric.
                    if (adj.advertiseRouteToNeighbour(ro)) {
                        if (enableAddRouteLogging) {
                            log.info("Told to advertise to neighbour");
                        }
                        //this should keep track of the routes we sent to our neighbour.
                        adj.addRouteAdvertisedToNeighbour(ro);
                        RoutingPacket update = new RoutingPacket(ADDITION, ro.getDestBlock(), ro.getHopCount() + 1,
                                (ro.getMetric() + metric), (ro.getLength() + block.getLengthMm()), -1, getNextPacketID());
                        lBnewblock.addRouteFromNeighbour(this, update);
                    } else {
                        if (enableAddRouteLogging) {
                            log.info("Not advertised to neighbour");
                        }
                    }
                } else if (enableAddRouteLogging) {
                    log.info("failed valid from path Not advertised/added");
                }
            }
        }
    }

    static long time = 0;

    /**
     * Work out our direction of route flow correctly.
     */
    private void addAdjacency(Path addPath) {
        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName()
                    + " path to be added " + addPath.getBlock().getDisplayName()
                    + " " + Path.decodeDirection(addPath.getToBlockDirection()));
        }

        Block destBlockToAdd = addPath.getBlock();
        int ourWorkingDirection = RXTX;
        if (destBlockToAdd == null) {
            log.error("Found null destination block for path from " + this.getDisplayName());
            return;
        }

        if (this.getBlock().isBlockDenied(destBlockToAdd.getDisplayName())) {
            ourWorkingDirection = RXONLY;
        } else if (destBlockToAdd.isBlockDenied(this.getBlock().getDisplayName())) {
            ourWorkingDirection = TXONLY;
        }

        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName()
                    + " to block " + addPath.getBlock().getDisplayName()
                    + " we should therefore be... " + decodePacketFlow(ourWorkingDirection));
        }
        addNeighbour(addPath.getBlock(), addPath.getToBlockDirection(), ourWorkingDirection);

    }

    //Might be possible to refactor the removal to use a bit of common code.
    private void removeAdjacency(Path removedPath) {
        Block block = removedPath.getBlock();
        if (block != null) {
            if (enableDeleteRouteLogging) {
                log.info("From " + this.getDisplayName()
                        + " Adjacency to be removed " + block.getDisplayName()
                        + " " + Path.decodeDirection(removedPath.getToBlockDirection()));
            }
            LayoutBlock layoutBlock = InstanceManager.getDefault(
                    LayoutBlockManager.class).getLayoutBlock(block);
            if (layoutBlock != null) {
                removeAdjacency(layoutBlock);
            }
        } else {
            log.debug("removeAdjacency() removedPath.getBlock() is null");
        }
    }

    private void removeAdjacency(LayoutBlock layoutBlock) {
        if (enableDeleteRouteLogging) {
            log.info("From " + this.getDisplayName() + " Adjacency to be removed " + layoutBlock.getDisplayName());
        }
        Block removedBlock = layoutBlock.getBlock();

        //Work our way backward through the list of neighbours
        //We need to work out which routes to remove first.
        //here we simply remove the routes which are advertised from the removed neighbour
        List<Routes> tmpBlock = removeRouteReceivedFromNeighbour(removedBlock);

        for (int i = neighbours.size() - 1; i > -1; i--) {
            //Use to check against direction but don't now.
            if ((neighbours.get(i).getBlock() == removedBlock)) {
                //Was previously before the for loop.
                //Pos move the remove list and remove thoughpath out of this for loop.
                layoutBlock.removePropertyChangeListener(this);
                if (enableDeleteRouteLogging) {
                    log.info("From " + this.getDisplayName() + " block " + removedBlock.getDisplayName() + " found and removed");
                }
                LayoutBlock layoutBlockToNotify = InstanceManager.getDefault(
                        LayoutBlockManager.class).getLayoutBlock(neighbours.get(i).getBlock());
                getAdjacency(neighbours.get(i).getBlock()).dispose();
                neighbours.remove(i);
                layoutBlockToNotify.notifiedNeighbourNoLongerMutual(this);
            }
        }

        for (int i = throughPaths.size() - 1; i > -1; i--) {
            if (throughPaths.get(i).getSourceBlock() == removedBlock) {
                //only mark for removal if the source isn't in the adjcency table
                if (getAdjacency(throughPaths.get(i).getSourceBlock()) == null) {
                    if (enableDeleteRouteLogging) {
                        log.info("remove " + throughPaths.get(i).getSourceBlock().getDisplayName()
                                + " to " + throughPaths.get(i).getDestinationBlock().getDisplayName());
                    }
                    throughPaths.remove(i);
                }
            } else if (throughPaths.get(i).getDestinationBlock() == removedBlock) {
                //only mark for removal if the destination isn't in the adjcency table
                if (getAdjacency(throughPaths.get(i).getDestinationBlock()) == null) {
                    if (enableDeleteRouteLogging) {
                        log.info("remove " + throughPaths.get(i).getSourceBlock().getDisplayName()
                                + " to " + throughPaths.get(i).getDestinationBlock().getDisplayName());
                    }
                    throughPaths.remove(i);
                }
            }
        }

        if (enableDeleteRouteLogging) {
            log.info("From " + this.getDisplayName()
                    + " neighbour has been removed - Number of routes to this neighbour removed" + tmpBlock.size());
        }
        notifyNeighboursOfRemoval(tmpBlock, removedBlock);
    }

    //This is used when a property event change is triggered for a removed route.
    //Not sure that bulk removals will be necessary
    private void removeRouteFromNeighbour(LayoutBlock src, RoutingPacket update) {
        InstanceManager.getDefault(LayoutBlockManager.class).setLastRoutingChange();
        Block srcblk = src.getBlock();
        Block destblk = update.getBlock();
        String msgPrefix = "From " + this.getDisplayName() + " notify block " + srcblk.getDisplayName() + " ";

        if (enableDeleteRouteLogging) {
            log.info(msgPrefix + " remove route from neighbour called");
        }

        if (InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(srcblk) == this) {
            if (enableDeleteRouteLogging) {
                log.info("From " + this.getDisplayName() + " source block is the same as our block! "
                        + destblk.getDisplayName());
            }
            return;
        }

        if (enableDeleteRouteLogging) {
            log.info(msgPrefix + " (Direct Notification) neighbour "
                    + srcblk.getDisplayName() + " has removed route to " + destblk.getDisplayName());
            log.info(msgPrefix + " routes in table " + routes.size() + " Remove route from neighbour");
        }
        List<Routes> routesToRemove = new ArrayList<>();
        for (int i = routes.size() - 1; i > -1; i--) {
            Routes ro = routes.get(i);
            if ((ro.getNextBlock() == srcblk) && (ro.getDestBlock() == destblk)) {
                routesToRemove.add(new Routes(routes.get(i).getDestBlock(), routes.get(i).getNextBlock(), 0, 0, 0, 0));
                if (enableDeleteRouteLogging) {
                    log.info(msgPrefix + " route to " + ro.getDestBlock().getDisplayName()
                            + " from block " + ro.getNextBlock().getDisplayName()
                            + " to be removed triggered by propertyChange");
                }
                routes.remove(i);
                //We only fire off routing update the once
            }
        }
        notifyNeighboursOfRemoval(routesToRemove, srcblk);
    }

    private List<Routes> removeRouteReceivedFromNeighbour(Block removedBlock) {
        List<Routes> tmpBlock = new ArrayList<>();

        //here we simply remove the routes which are advertised from the removed neighbour
        for (int j = routes.size() - 1; j > -1; j--) {
            Routes ro = routes.get(j);
            if (enableDeleteRouteLogging) {
                log.info("From " + this.getDisplayName()
                        + " route to check " + routes.get(j).getDestBlock().getDisplayName()
                        + " from Block " + routes.get(j).getNextBlock().getDisplayName());
            }

            if (ro.getDestBlock() == removedBlock) {
                if (enableDeleteRouteLogging) {
                    log.info("From " + this.getDisplayName()
                            + " route to " + routes.get(j).getDestBlock().getDisplayName()
                            + " from block " + routes.get(j).getNextBlock().getDisplayName()
                            + " to be removed triggered by adjancey removal as dest block has been removed");
                }

                if (!tmpBlock.contains(ro)) {
                    tmpBlock.add(ro);
                }
                routes.remove(j);
                //This will need to be removed fromth directly connected
            } else if (ro.getNextBlock() == removedBlock) {
                if (enableDeleteRouteLogging) {
                    log.info("From " + this.getDisplayName()
                            + " route to " + routes.get(j).getDestBlock().getDisplayName()
                            + " from block " + routes.get(j).getNextBlock().getDisplayName()
                            + " to be removed triggered by adjancey removal");
                }

                if (!tmpBlock.contains(ro)) {
                    tmpBlock.add(ro);
                }
                routes.remove(j);
                //This will also need to be removed from the directly connected list as well.
            }
        }
        return tmpBlock;
    }

    private void updateNeighbourPacketFlow(Block neighbour, int flow) {
        //Packet flow from neighbour will need to be reversed.
        Adjacencies neighAdj = getAdjacency(neighbour);

        if (flow == RXONLY) {
            flow = TXONLY;
        } else if (flow == TXONLY) {
            flow = RXONLY;
        }

        if (neighAdj.getPacketFlow() == flow) {
            return;
        }
        updateNeighbourPacketFlow(neighAdj, flow);
    }

    protected void updateNeighbourPacketFlow(Adjacencies neighbour, final int flow) {
        if (neighbour.getPacketFlow() == flow) {
            return;
        }

        final LayoutBlock neighLBlock = neighbour.getLayoutBlock();
        Runnable r = () -> {
            neighLBlock.updateNeighbourPacketFlow(block, flow);
        };

        Block neighBlock = neighbour.getBlock();
        int oldPacketFlow = neighbour.getPacketFlow();

        neighbour.setPacketFlow(flow);

        SwingUtilities.invokeLater(r);

        if (flow == TXONLY) {
            neighBlock.addBlockDenyList(this.block);
            neighLBlock.removePropertyChangeListener(this);

            //This should remove routes learned from our neighbour
            List<Routes> tmpBlock = removeRouteReceivedFromNeighbour(neighBlock);

            notifyNeighboursOfRemoval(tmpBlock, neighBlock);

            //Need to also remove all through paths to this neighbour
            for (int i = throughPaths.size() - 1; i > -1; i--) {
                if (throughPaths.get(i).getDestinationBlock() == neighBlock) {
                    throughPaths.remove(i);
                    firePropertyChange("through-path-removed", null, null);
                }
            }

            //We potentially will need to re-advertise routes to this neighbour
            if (oldPacketFlow == RXONLY) {
                addThroughPath(neighbour);
            }
        } else if (flow == RXONLY) {
            neighLBlock.addPropertyChangeListener(this);
            neighBlock.removeBlockDenyList(this.block);
            this.block.addBlockDenyList(neighBlock);

            for (int i = throughPaths.size() - 1; i > -1; i--) {
                if (throughPaths.get(i).getSourceBlock() == neighBlock) {
                    throughPaths.remove(i);
                    firePropertyChange("through-path-removed", null, null);
                }
            }

            //Might need to rebuild through paths.
            if (oldPacketFlow == TXONLY) {
                routes.add(new Routes(neighBlock, this.getBlock(),
                        1, neighbour.getDirection(), neighLBlock.getBlockMetric(), neighBlock.getLengthMm()));
                addThroughPath(neighbour);
            }
            //We would need to withdraw the routes that we advertise to the neighbour
        } else if (flow == RXTX) {
            neighBlock.removeBlockDenyList(this.block);
            this.block.removeBlockDenyList(neighBlock);
            neighLBlock.addPropertyChangeListener(this);

            //Might need to rebuild through paths.
            if (oldPacketFlow == TXONLY) {
                routes.add(new Routes(neighBlock, this.getBlock(),
                        1, neighbour.getDirection(), neighLBlock.getBlockMetric(), neighBlock.getLengthMm()));
            }
            addThroughPath(neighbour);
        }
    }

    private void notifyNeighboursOfRemoval(List<Routes> routesToRemove, Block notifyingblk) {
        String msgPrefix = "From " + this.getDisplayName() + " notify block " + notifyingblk.getDisplayName() + " ";

        if (enableDeleteRouteLogging) {
            log.info(msgPrefix + " notifyNeighboursOfRemoval called for routes from " + notifyingblk.getDisplayName() + " ===");
        }
        boolean notifyvalid = false;

        for (int i = neighbours.size() - 1; i > -1; i--) {
            if (neighbours.get(i).getBlock() == notifyingblk) {
                notifyvalid = true;
            }
        }

        if (enableDeleteRouteLogging) {
            log.info(msgPrefix + " The notifying block is still valid? " + notifyvalid);
        }

        for (int j = routesToRemove.size() - 1; j > -1; j--) {
            boolean stillexist = false;
            Block destBlock = routesToRemove.get(j).getDestBlock();
            Block sourceBlock = routesToRemove.get(j).getNextBlock();
            RoutingPacket newUpdate = new RoutingPacket(REMOVAL, destBlock, -1, -1, -1, -1, getNextPacketID());

            if (enableDeleteRouteLogging) {
                log.info("From " + this.getDisplayName()
                        + " notify block " + notifyingblk.getDisplayName()
                        + " checking " + destBlock.getDisplayName()
                        + " from " + sourceBlock.getDisplayName());
            }
            List<Routes> validroute = new ArrayList<>();
            List<Routes> destRoutes = getDestRoutes(destBlock);
            for (Routes r : destRoutes) {
                //We now know that we still have a valid route to the dest
                if (r.getNextBlock() == this.getBlock()) {
                    if (enableDeleteRouteLogging) {
                        log.info(msgPrefix + " The destBlock " + destBlock.getDisplayName() + " is our neighbour");
                    }
                    validroute.add(new Routes(r.getDestBlock(), r.getNextBlock(), 0, 0, 0, 0));
                    stillexist = true;
                } else {
                    //At this stage do we need to check if the valid route comes from a neighbour?
                    if (enableDeleteRouteLogging) {
                        log.info(msgPrefix + " we still have a route to "
                                + destBlock.getDisplayName() + " via "
                                + r.getNextBlock().getDisplayName() + " in our list");
                    }
                    validroute.add(new Routes(destBlock, r.getNextBlock(), 0, 0, 0, 0));
                    stillexist = true;
                }
            }
            //We may need to find out who else we could of sent the route to by checking in the through paths

            if (stillexist) {
                if (enableDeleteRouteLogging) {
                    log.info(msgPrefix + "A Route still exists");
                    log.info(msgPrefix + " the number of routes installed to block "
                            + destBlock.getDisplayName() + " is " + validroute.size());
                }

                if (validroute.size() == 1) {
                    //Specific routing update.
                    Block nextHop = validroute.get(0).getNextBlock();
                    LayoutBlock layoutBlock;
                    if (validroute.get(0).getNextBlock() != this.getBlock()) {
                        layoutBlock = InstanceManager.getDefault(
                                LayoutBlockManager.class).getLayoutBlock(nextHop);
                        if (enableDeleteRouteLogging) {
                            log.info(msgPrefix + " We only have a single valid route left to "
                                    + destBlock.getDisplayName() + " So will tell "
                                    + layoutBlock.getDisplayName() + " we no longer have it");
                        }

                        if (layoutBlock != null) {
                            layoutBlock.removeRouteFromNeighbour(this, newUpdate);
                        }
                        getAdjacency(nextHop).removeRouteAdvertisedToNeighbour(routesToRemove.get(j));
                    }

                    //At this point we could probably do with checking for other valid paths from the notifyingblock
                    //Have a feeling that this is pretty much the same as above!
                    List<Block> validNeighboursToNotify = new ArrayList<>();

                    //Problem we have here is that although we only have one valid route, one of our neighbours
                    //could still hold a valid through path.
                    for (int i = neighbours.size() - 1; i > -1; i--) {
                        //Need to ignore if the dest block is our neighour in this instance
                        if ((neighbours.get(i).getBlock() != destBlock) && (neighbours.get(i).getBlock() != nextHop)) {
                            if (validThroughPath(notifyingblk, neighbours.get(i).getBlock())) {
                                Block neighblock = neighbours.get(i).getBlock();

                                if (enableDeleteRouteLogging) {
                                    log.info(msgPrefix + " we could of potentially sent the route to "
                                            + neighblock.getDisplayName());
                                }

                                if (!validThroughPath(nextHop, neighblock)) {
                                    if (enableDeleteRouteLogging) {
                                        log.info(msgPrefix + " there is no other valid path so will mark for removal");
                                    }
                                    validNeighboursToNotify.add(neighblock);
                                } else {
                                    if (enableDeleteRouteLogging) {
                                        log.info(msgPrefix + " there is another valid path so will NOT mark for removal");
                                    }
                                }
                            }
                        }
                    }

                    if (enableDeleteRouteLogging) {
                        log.info(msgPrefix + " the next block is our selves so we won't remove!");
                        log.info(msgPrefix + " do we need to find out if we could of send the route to another neighbour such as?");
                    }

                    for (int i = 0; i < validNeighboursToNotify.size(); i++) {
                        //If the neighbour has a valid through path to the dest
                        //we will not notify the neighbour of our loss of route
                        if (!validThroughPath(validNeighboursToNotify.get(i), destBlock)) {
                            layoutBlock
                                    = InstanceManager.getDefault(
                                            LayoutBlockManager.class).
                                            getLayoutBlock(validNeighboursToNotify.get(i));
                            if (layoutBlock != null) {
                                layoutBlock.removeRouteFromNeighbour(this, newUpdate);
                            }
                            getAdjacency(validNeighboursToNotify.get(i)).removeRouteAdvertisedToNeighbour(routesToRemove.get(j));
                        } else {
                            if (enableDeleteRouteLogging) {
                                log.info(msgPrefix + validNeighboursToNotify.get(i).getDisplayName()
                                        + " has a valid path to " + destBlock.getDisplayName());
                            }
                        }
                    }
                } else {
                    //Need to deal with having multiple routes left.
                    if (enableDeleteRouteLogging) {
                        log.info(msgPrefix + " routes left to block " + destBlock.getDisplayName());
                    }

                    for (int i = 0; i < validroute.size(); i++) {
                        //We need to see if we have valid routes.
                        if (validThroughPath(notifyingblk, validroute.get(i).getNextBlock())) {
                            if (enableDeleteRouteLogging) {
                                log.info(msgPrefix + " to " + validroute.get(i).getNextBlock().getDisplayName()
                                        + " Is a valid route");
                            }
                            //Will mark the route for potential removal
                            validroute.get(i).setMiscFlags(0x02);
                        } else {
                            if (enableDeleteRouteLogging) {
                                log.info(msgPrefix + " to " + validroute.get(i).getNextBlock().getDisplayName()
                                        + " Is not a valid route");
                            }
                            //Mark the route to not be removed.
                            validroute.get(i).setMiscFlags(0x01);

                            //Given that the route to this is not valid, we do not want to
                            //be notifying this next block about the loss of route.
                        }
                    }

                    //We have marked all the routes for either potential notification of route removal, or definate no removal;
                    //Now need to get through the list and cross reference each one.
                    for (int i = 0; i < validroute.size(); i++) {
                        if (validroute.get(i).getMiscFlags() == 0x02) {
                            Block nextblk = validroute.get(i).getNextBlock();

                            if (enableDeleteRouteLogging) {
                                log.info(msgPrefix + " route from " + nextblk.getDisplayName() + " has been flagged for removal");
                            }

                            //Need to cross reference it with the routes that are left.
                            boolean leaveroute = false;
                            for (int k = 0; k < validroute.size(); k++) {
                                if (validroute.get(k).getMiscFlags() == 0x01) {
                                    if (validThroughPath(nextblk, validroute.get(k).getNextBlock())) {
                                        if (enableDeleteRouteLogging) {
                                            log.info(msgPrefix + " we have a valid path from " + nextblk.getDisplayName()
                                                    + " to " + validroute.get(k).getNextBlock());
                                        }
                                        leaveroute = true;
                                    }
                                }
                            }

                            if (!leaveroute) {
                                LayoutBlock layoutBlock = InstanceManager.getDefault(
                                        LayoutBlockManager.class).getLayoutBlock(nextblk);
                                if (enableDeleteRouteLogging) {
                                    log.info(msgPrefix + "############ We need to send notification to "
                                            + nextblk.getDisplayName()
                                            + " to remove route ########### haven't found an example of this yet!");
                                }
                                layoutBlock.removeRouteFromNeighbour(this, newUpdate);
                                getAdjacency(nextblk).removeRouteAdvertisedToNeighbour(routesToRemove.get(j));
                            } else {
                                if (enableDeleteRouteLogging) {
                                    log.info(msgPrefix + " a valid path through exists "
                                            + nextblk.getDisplayName() + " so we will not remove route.");
                                }
                            }
                        }
                    }
                }
            } else {
                if (enableDeleteRouteLogging) {
                    log.info(msgPrefix + " We have no other routes to " + destBlock.getDisplayName()
                            + " Therefore we will broadast this to our neighbours");
                }

                for (Adjacencies adj : neighbours) {
                    adj.removeRouteAdvertisedToNeighbour(destBlock);
                }
                firePropertyChange("routing", null, newUpdate);
            }
        }

        if (enableDeleteRouteLogging) {
            log.info(msgPrefix + " finshed check and notifying of removed routes from " + notifyingblk.getDisplayName() + " ===");
        }
    }

    private void addThroughPath(Adjacencies adj) {
        Block newAdj = adj.getBlock();
        int packetFlow = adj.getPacketFlow();

        if (enableAddRouteLogging) {
            log.debug("From " + this.getDisplayName() + " addThroughPathCalled with adj " + adj.getBlock().getDisplayName());
        }

        for (int i = 0; i < neighbours.size(); i++) {
            //cycle through all the neighbours
            if (neighbours.get(i).getBlock() != newAdj) {
                int neighPacketFlow = neighbours.get(i).getPacketFlow();

                if (enableAddRouteLogging) {
                    log.info("From " + this.getDisplayName() + " our direction: "
                            + decodePacketFlow(packetFlow) + ", neighbour direction: " + decodePacketFlow(neighPacketFlow));
                }

                if ((packetFlow == RXTX) && (neighPacketFlow == RXTX)) {
                    //if both are RXTX then add flow in both directions
                    addThroughPath(neighbours.get(i).getBlock(), newAdj);
                    addThroughPath(newAdj, neighbours.get(i).getBlock());
                } else if ((packetFlow == RXONLY) && (neighPacketFlow == TXONLY)) {
                    addThroughPath(neighbours.get(i).getBlock(), newAdj);
                } else if ((packetFlow == TXONLY) && (neighPacketFlow == RXONLY)) {
                    addThroughPath(newAdj, neighbours.get(i).getBlock());
                } else if ((packetFlow == RXTX) && (neighPacketFlow == TXONLY)) {   //was RX
                    addThroughPath(neighbours.get(i).getBlock(), newAdj);
                } else if ((packetFlow == RXTX) && (neighPacketFlow == RXONLY)) {   //was TX
                    addThroughPath(newAdj, neighbours.get(i).getBlock());
                } else if ((packetFlow == RXONLY) && (neighPacketFlow == RXTX)) {
                    addThroughPath(neighbours.get(i).getBlock(), newAdj);
                } else if ((packetFlow == TXONLY) && (neighPacketFlow == RXTX)) {
                    addThroughPath(newAdj, neighbours.get(i).getBlock());
                } else {
                    if (enableAddRouteLogging) {
                        log.info("Invalid combination " + decodePacketFlow(packetFlow)
                                + " and " + decodePacketFlow(neighPacketFlow));
                    }
                }
            }
        }
    }

    /**
     * Add a path between two blocks, but without spec a panel.
     */
    private void addThroughPath(Block srcBlock, Block dstBlock) {
        if (enableAddRouteLogging) {
            log.info("Block {}.addThroughPath(src:{}, dst: {})",
                    this.getDisplayName(), srcBlock.getDisplayName(), dstBlock.getDisplayName());
        }

        if ((block != null) && (panels.size() > 0)) {
            //a block is attached and this LayoutBlock is used
            //initialize connectivity as defined in first Layout Editor panel
            LayoutEditor panel = panels.get(0);
            List<LayoutConnectivity> c = panel.getLEAuxTools().getConnectivityList(this);

            //if more than one panel, find panel with the highest connectivity
            if (panels.size() > 1) {
                for (int i = 1; i < panels.size(); i++) {
                    if (c.size() < panels.get(i).getLEAuxTools().
                            getConnectivityList(this).size()) {
                        panel = panels.get(i);
                        c = panel.getLEAuxTools().getConnectivityList(this);
                    }
                }

                //check that this connectivity is compatible with that of other panels.
                for (LayoutEditor tPanel : panels) {
                    if ((tPanel != panel) && InstanceManager.getDefault(LayoutBlockManager.class).
                            warn() && (!compareConnectivity(c,
                                    tPanel.getLEAuxTools().getConnectivityList(this)))) {
                        //send user an error message
                        int response = JOptionPane.showOptionDialog(null,
                                java.text.MessageFormat.format(Bundle.getMessage("Warn1"),
                                        new Object[]{getUserName(), tPanel.getLayoutName(),
                                            panel.getLayoutName()}), Bundle.getMessage("WarningTitle"),
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                                null, new Object[]{Bundle.getMessage("ButtonOK"),
                                    Bundle.getMessage("ButtonOKPlus")}, Bundle.getMessage("ButtonOK"));
                        if (response != 0) //user elected to disable messages
                        {
                            InstanceManager.getDefault(LayoutBlockManager.class).turnOffWarning();
                        }
                    }
                }
            }
            //update block Paths to reflect connectivity as needed
            addThroughPath(srcBlock, dstBlock, panel);
        }
    }

    private LayoutEditorAuxTools auxTools = null;
    private ConnectivityUtil connection = null;
    private boolean layoutConnectivity = true;

    /**
     * Add a through path on this layout block, going from the
     * source block to the destination block, using a specific panel. Note:
     * If the reverse path is required, then this needs to be added seperately.
     */
    //Was public
    private void addThroughPath(Block srcBlock, Block dstBlock, LayoutEditor panel) {
        //Reset connectivity flag.
        layoutConnectivity = true;

        if (srcBlock == dstBlock) {
            //Do not do anything if the blocks are the same!
            return;
        }

        if (enableAddRouteLogging) {
            log.info("Block {}.addThroughPath(src:{}, dst: {}, <panel>)",
                    this.getDisplayName(), srcBlock.getDisplayName(), dstBlock.getDisplayName());
        }

        //Initally check to make sure that the through path doesn't already exist.
        //no point in going through the checks if the path already exists.
        boolean add = true;
        for (int i = 0; i < throughPaths.size(); i++) {
            if (throughPaths.get(i).getSourceBlock() == srcBlock) {
                if (throughPaths.get(i).getDestinationBlock() == dstBlock) {
                    add = false;
                }
            }
        }

        if (!add) {
            return;
        }

        if (enableAddRouteLogging) {
            log.info("Block {}, src: {}, dst: {}",
                    block.getDisplayName(), srcBlock.getDisplayName(), dstBlock.getDisplayName());
        }
        connection = new ConnectivityUtil(panel);
        List<LayoutTrackExpectedState<LayoutTurnout>> stod;

        try {
            MDC.put("loggingDisabled", connection.getClass().getCanonicalName());
            stod = connection.getTurnoutList(block, srcBlock, dstBlock, true);
            MDC.remove("loggingDisabled");
        } catch (java.lang.NullPointerException ex) {
            MDC.remove("loggingDisabled");
            if (enableAddRouteLogging) {
                log.error("Exception (" + ex.toString() + ") caught while trying to discover turnout connectivity\n"
                        + "Block: " + block.getDisplayName()
                        + ", srcBlock (" + srcBlock.getDisplayName()
                        + ") to dstBlock (" + dstBlock.getDisplayName() + ")");
                log.error("@ Line # " + ex.getStackTrace()[1].getLineNumber());
            }
            return;
        }

        if (!connection.isTurnoutConnectivityComplete()) {
            layoutConnectivity = false;
        }
        List<LayoutTrackExpectedState<LayoutTurnout>> tmpdtos;

        try {
            MDC.put("loggingDisabled", connection.getClass().getName());
            tmpdtos = connection.getTurnoutList(block, dstBlock, srcBlock, true);
            MDC.remove("loggingDisabled");
        } catch (java.lang.NullPointerException ex) {
            MDC.remove("loggingDisabled");
            if (enableAddRouteLogging) {
                log.error("Exception (" + ex.toString() + ") caught while trying to discover turnout connectivity\n"
                        + "Block: " + block.getDisplayName()
                        + ", dstBlock (" + dstBlock.getDisplayName()
                        + ") to  srcBlock (" + srcBlock.getDisplayName() + ")");
                log.error("@ Line # " + ex.getStackTrace()[1].getLineNumber());
            }
            return;
        }

        if (!connection.isTurnoutConnectivityComplete()) {
            layoutConnectivity = false;
        }

        if (stod.size() == tmpdtos.size()) {
            //Need to reorder the tmplist (dst-src) to be the same order as src-dst
            List<LayoutTrackExpectedState<LayoutTurnout>> dtos = new ArrayList<>();
            for (int i = tmpdtos.size(); i > 0; i--) {
                dtos.add(tmpdtos.get(i - 1));
            }

            //check to make sure that we pass through the same turnouts
            if (enableAddRouteLogging) {
                log.info("From {} destination size {} v source size {}", this.getDisplayName(), dtos.size(), stod.size());
            }

            for (int i = 0; i < dtos.size(); i++) {
                if (dtos.get(i).getObject() != stod.get(i).getObject()) {
                    if (enableAddRouteLogging) {
                        log.info("{} != {}: will quit", dtos.get(i).getObject(), stod.get(i).getObject());
                    }
                    return;
                }
            }

            for (int i = 0; i < dtos.size(); i++) {
                int x = stod.get(i).getExpectedState();
                int y = dtos.get(i).getExpectedState();

                if (x != y) {
                    if (enableAddRouteLogging) {
                        log.info("{} not on setting equal will quit {}, {}", block.getDisplayName(), x, y);
                    }
                    return;
                } else if (x == Turnout.UNKNOWN) {
                    if (enableAddRouteLogging) {
                        log.info("{} turnout state returned as UNKNOWN", block.getDisplayName());
                    }
                    return;
                }
            }
            Set<LayoutTurnout> set = new HashSet<>();

            for (int i = 0; i < stod.size(); i++) {
                boolean val = set.add(stod.get(i).getObject());
                if (val == false) {
                    //Duplicate found. will not add
                    return;
                }
            }
            //for (LayoutTurnout turn : stod) {
            //    if (turn.type == LayoutTurnout.DOUBLE_XOVER) {
            //        //Further checks might be required.
            //    }
            //}
            addThroughPathPostChecks(srcBlock, dstBlock, stod);
        } else {
            //We know that a path that contains a double cross-over, is not reported correctly,
            //therefore we shall do some additional checks and add it.
            if (enableAddRouteLogging) {
                log.info("sizes are not the same therefore, we will do some further checks");
            }
            List<LayoutTrackExpectedState<LayoutTurnout>> maxt;
            if (stod.size() >= tmpdtos.size()) {
                maxt = stod;
            } else {
                maxt = tmpdtos;
            }

            Set<LayoutTrackExpectedState<LayoutTurnout>> set = new HashSet<>(maxt);

            if (set.size() == maxt.size()) {
                if (enableAddRouteLogging) {
                    log.info("All turnouts are unique so potentially a valid path");
                }
                boolean allowAddition = false;
                for (int i = 0; i < maxt.size(); i++) {
                    LayoutTurnout turn = maxt.get(i).getObject();
                    if (turn.type == LayoutTurnout.DOUBLE_XOVER) {
                        allowAddition = true;
                        //The double crossover gets reported in the opposite setting.
                        if (maxt.get(i).getExpectedState() == 2) {
                            maxt.get(i).setExpectedState(4);
                        } else {
                            maxt.get(i).setExpectedState(2);
                        }
                    }
                }

                if (allowAddition) {
                    if (enableAddRouteLogging) {
                        log.info("addition allowed");
                    }
                    addThroughPathPostChecks(srcBlock, dstBlock, maxt);
                } else if (enableAddRouteLogging) {
                    log.info("No double cross-over so not a valid path");
                }
            }
        }
    }   // addThroughPath

    private void addThroughPathPostChecks(Block srcBlock,
            Block dstBlock, List<LayoutTrackExpectedState<LayoutTurnout>> stod) {
        List<Path> paths = block.getPaths();
        Path srcPath = null;

        for (int i = 0; i < paths.size(); i++) {
            if (paths.get(i).getBlock() == srcBlock) {
                srcPath = paths.get(i);
            }
        }
        Path dstPath = null;

        for (int i = 0; i < paths.size(); i++) {
            if (paths.get(i).getBlock() == dstBlock) {
                dstPath = paths.get(i);
            }
        }
        ThroughPaths path = new ThroughPaths(srcBlock, srcPath, dstBlock, dstPath);
        path.setTurnoutList(stod);

        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " added Throughpath "
                    + path.getSourceBlock().getDisplayName() + " " + path.getDestinationBlock().getDisplayName());
        }
        throughPaths.add(path);
        firePropertyChange("through-path-added", null, null);

        //update our neighbours of the new valid paths;
        informNeighbourOfValidRoutes(srcBlock);
        informNeighbourOfValidRoutes(dstBlock);
    }

    void notifiedNeighbourNoLongerMutual(LayoutBlock srcBlock) {
        if (enableDeleteRouteLogging) {
            log.info("From " + this.getDisplayName()
                    + "Notification from neighbour that it is no longer our friend " + srcBlock.getDisplayName());
        }
        Block blk = srcBlock.getBlock();

        for (int i = neighbours.size() - 1; i > -1; i--) {
            // Need to check if the block we are being informed about has already been removed or not
            if (neighbours.get(i).getBlock() == blk) {
                removeAdjacency(srcBlock);
                break;
            }
        }
    }

    public static final int RESERVED = 0x08;

    void stateUpdate() {
        // Need to find a way to fire off updates to the various tables
        if (enableUpdateRouteLogging) {
            log.debug("From " + this.getDisplayName() + " A block state change (" + getBlockStatusString() + ") has occurred");
        }
        RoutingPacket update = new RoutingPacket(UPDATE, this.getBlock(), -1, -1, -1, getBlockStatus(), getNextPacketID());
        firePropertyChange("routing", null, update);
    }

    int getBlockStatus() {
        if (getOccupancy() == OCCUPIED) {
            useExtraColor = false;
            //Our section of track is occupied
            return OCCUPIED;
        } else if (useExtraColor) {
            return RESERVED;
        } else if (getOccupancy() == EMPTY) {
            return EMPTY;
        } else {
            return UNKNOWN;
        }
    }

    String getBlockStatusString() {
        String result = "UNKNOWN";
        if (getOccupancy() == OCCUPIED) {
            result = "OCCUPIED";
        } else if (useExtraColor) {
            result = "RESERVED";
        } else if (getOccupancy() == EMPTY) {
            result = "EMPTY";
        }
        return result;
    }

    Integer getNextPacketID() {
        Integer lastID;

        if (updateReferences.isEmpty()) {
            lastID = 0;
        } else {
            int lastIDPos = updateReferences.size() - 1;
            lastID = updateReferences.get(lastIDPos) + 1;
        }

        if (lastID > 2000) {
            lastID = 0;
        }
        updateReferences.add(lastID);

        /*As we are originating a packet, we will added to the acted upion list
         thus making sure if the packet gets back to us we do knowing with it.*/
        actedUponUpdates.add(lastID);

        if (updateReferences.size() > 500) {
            //log.info("flush update references");
            updateReferences.subList(0, 250).clear();
        }

        if (actedUponUpdates.size() > 500) {
            actedUponUpdates.subList(0, 250).clear();
        }
        return lastID;
    }

    boolean updatePacketActedUpon(Integer packetID) {
        return actedUponUpdates.contains(packetID);
    }

    public List<Block> getActiveNextBlocks(Block source) {
        List<Block> currentPath = new ArrayList<>();

        for (int i = 0; i < throughPaths.size(); i++) {
            ThroughPaths path = throughPaths.get(i);
            if ((path.getSourceBlock() == source) && (path.isPathActive())) {
                currentPath.add(throughPaths.get(i).getDestinationBlock());
            }
        }
        return currentPath;
    }

    public Path getThroughPathSourcePathAtIndex(int i) {
        return throughPaths.get(i).getSourcePath();
    }

    public Path getThroughPathDestinationPathAtIndex(int i) {
        return throughPaths.get(i).getDestinationPath();
    }

    public boolean validThroughPath(Block sourceBlock, Block destinationBlock) {
        for (int i = 0; i < throughPaths.size(); i++) {
            if ((throughPaths.get(i).getSourceBlock() == sourceBlock)
                    && (throughPaths.get(i).getDestinationBlock() == destinationBlock)) {
                return true;
            } else if ((throughPaths.get(i).getSourceBlock() == destinationBlock)
                    && (throughPaths.get(i).getDestinationBlock() == sourceBlock)) {
                return true;
            }
        }
        return false;
    }

    public int getThroughPathIndex(Block sourceBlock, Block destinationBlock) {
        for (int i = 0; i < throughPaths.size(); i++) {
            if ((throughPaths.get(i).getSourceBlock() == sourceBlock)
                    && (throughPaths.get(i).getDestinationBlock() == destinationBlock)) {
                return i;
            } else if ((throughPaths.get(i).getSourceBlock() == destinationBlock)
                    && (throughPaths.get(i).getDestinationBlock() == sourceBlock)) {
                return i;
            }
        }
        return -1;
    }

    List<Adjacencies> neighbours = new ArrayList<>();

    List<ThroughPaths> throughPaths = new ArrayList<>();

    //A sub class that holds valid routes through the block.
    //Possibly want to store the path direction in here as well.
    //or we store the ref to the path, so we can get the directions.
    List<Routes> routes = new ArrayList<>();

    String decodePacketFlow(int value) {
        switch (value) {
            case RXTX: {
                return "Bi-Direction Operation";
            }

            case RXONLY: {
                return "Uni-Directional - Trains can only exit to this block (RX) ";
            }

            case TXONLY: {
                return "Uni-Directional - Trains can not be sent down this block (TX) ";
            }

            case NONE: {
                return "None routing updates will be passed";
            }
            default:
                log.warn("Unhandled packet flow value: {}", value);
                break;
        }
        return "Unknown";
    }

    /**
     * Provide an output to the console of all the valid paths through this
     * block.
     */
    public void printValidThroughPaths() {
        log.info("Through paths for block " + this.getDisplayName());
        log.info("Current Block, From Block, To Block");
        for (ThroughPaths tp : throughPaths) {
            String activeStr = "";
            if (tp.isPathActive()) {
                activeStr = ", *";
            }
            log.info("From " + this.getDisplayName() + ", "
                    + (tp.getSourceBlock()).getDisplayName() + ", "
                    + (tp.getDestinationBlock()).getDisplayName() + activeStr);
        }
    }

    /**
     * Provide an output to the console of all our neighbouring blocks.
     */
    public void printAdjacencies() {
        log.info("");
        log.info("Adjacencies for block " + this.getDisplayName());
        log.info("Neighbour, Direction, mutual, relationship, metric");
        for (int i = 0; i < neighbours.size(); i++) {
            log.info(neighbours.get(i).getBlock().getDisplayName() + ", "
                    + Path.decodeDirection(neighbours.get(i).getDirection()) + ", "
                    + neighbours.get(i).isMutual() + ", " + decodePacketFlow(neighbours.get(i).getPacketFlow())
                    + ", " + neighbours.get(i).getMetric());
        }
    }

    /**
     * Provide an output to the console of all the remote blocks reachable from
     * our block.
     */
    public void printRoutes() {
        log.info("Routes for block " + this.getDisplayName());
        log.info("Destination, Next Block, Hop Count, Direction, State, Metric");
        for (Routes r : routes) {
            String nexthop = r.getNextBlock().getDisplayName();

            if (r.getNextBlock() == this.getBlock()) {
                nexthop = "Directly Connected";
            }
            String activeString = "";
            if (r.isRouteCurrentlyValid()) {
                activeString = ", *";
            }

            log.info((r.getDestBlock()).getDisplayName() + ", " + nexthop + ", " + r.getHopCount() + ", "
                    + Path.decodeDirection(r.getDirection()) + ", " + r.getState() + ", " + r.getMetric() + activeString);
        }
    }

    /**
     * Provide an output to the console of how to reach a specific block from
     * our block.
     *
     * @param inBlockName to find in route
     */
    public void printRoutes(String inBlockName) {
        log.info("Routes for block " + this.getDisplayName());
        log.info("Our Block, Destination, Next Block, Hop Count, Direction, Metric");
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getDestBlock().getDisplayName().equals(inBlockName)) {
                log.info("From " + this.getDisplayName() + ", " + (routes.get(i).getDestBlock()).getDisplayName()
                        + ", " + (routes.get(i).getNextBlock()).getDisplayName() + ", " + routes.get(i).getHopCount() + ", "
                        + Path.decodeDirection(routes.get(i).getDirection()) + ", " + routes.get(i).getMetric());
            }
        }
    }

    /**
     * @param destBlock  is the destination of the block we are following
     * @param direction  is the direction of travel from the previous block
     * @return next block
     */
    public Block getNextBlock(Block destBlock, int direction) {
        int bestMetric = 965000;
        Block bestBlock = null;

        for (Routes r : routes) {
            if ((r.getDestBlock() == destBlock) && (r.getDirection() == direction)) {
                if (r.getMetric() < bestMetric) {
                    bestMetric = r.getMetric();
                    bestBlock = r.getNextBlock();
                    //bestBlock=r.getDestBlock();
                }
            }
        }
        return bestBlock;
    }

    /**
     * Used if we already know the block prior to our block, and the destination
     * block. direction, is optional and is used where the previousBlock is
     * equal to our block.
     *
     * @param previousBlock start block
     * @param destBlock     finish block
     * @return next block
     */
    @CheckForNull
    public Block getNextBlock(Block previousBlock, Block destBlock) {
        int bestMetric = 965000;
        Block bestBlock = null;

        for (Routes r : routes) {
            if (r.getDestBlock() == destBlock) {
                //Check that the route through from the previous block, to the next hop is valid
                if (validThroughPath(previousBlock, r.getNextBlock())) {
                    if (r.getMetric() < bestMetric) {
                        bestMetric = r.getMetric();
                        //bestBlock=r.getDestBlock();
                        bestBlock = r.getNextBlock();
                    }
                }
            }
        }
        return bestBlock;
    }

    public int getConnectedBlockRouteIndex(Block destBlock, int direction) {
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getNextBlock() == this.getBlock()) {
                log.info("Found a block that is directly connected");

                if ((routes.get(i).getDestBlock() == destBlock)) {
                    log.info(Integer.toString(routes.get(i).getDirection() & direction));
                    if ((routes.get(i).getDirection() & direction) != 0) {
                        return i;
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("From " + this.getDisplayName() + ", " + (routes.get(i).getDestBlock()).getDisplayName()
                        + ", nexthop " + routes.get(i).getHopCount() + ", "
                        + Path.decodeDirection(routes.get(i).getDirection()) + ", "
                        + routes.get(i).getState() + ", " + routes.get(i).getMetric());
            }
        }
        return -1;
    }

    //Need to work on this to deal with the method of routing
    public int getNextBlockByIndex(Block destBlock, int direction, int offSet) {
        for (int i = offSet; i < routes.size(); i++) {
            Routes ro = routes.get(i);
            if ((ro.getDestBlock() == destBlock)) {
                log.info(Integer.toString(ro.getDirection() & direction));
                if ((ro.getDirection() & direction) != 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    //Need to work on this to deal with the method of routing
    /*
     *
     */
    public int getNextBlockByIndex(Block previousBlock, Block destBlock, int offSet) {
        for (int i = offSet; i < routes.size(); i++) {
            Routes ro = routes.get(i);
            //log.info(r.getDestBlock().getDisplayName() + " vs " + destBlock.getDisplayName());
            if (ro.getDestBlock() == destBlock) {
                //Check that the route through from the previous block, to the next hop is valid
                if (validThroughPath(previousBlock, ro.getNextBlock())) {
                    log.debug("valid through path");
                    return i;
                }

                if (ro.getNextBlock() == this.getBlock()) {
                    log.debug("getNextBlock is this block therefore directly connected");
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * last index - the index of the last block we returned ie we last returned
     * index 10, so we don't want to return it again. The block returned will
     * have a hopcount or metric equal to or greater than the one of the last
     * block returned. if the exclude block list is empty this is the first
     * time, it has been used. The parameters for the best last block are based
     * upon the last entry in the excludedBlock list.
     *
     * @param previousBlock starting block
     * @param destBlock     finish block
     * @param excludeBlock  blocks to skip
     * @param routingMethod value to match metric
     * @return next block
     */
    public int getNextBestBlock(Block previousBlock, Block destBlock, List<Integer> excludeBlock, int routingMethod) {
        if (enableSearchRouteLogging) {
            log.info("From " + this.getDisplayName() + " find best route from "
                    + previousBlock.getDisplayName() + " to " + destBlock.getDisplayName()
                    + " index " + excludeBlock + " routingMethod " + routingMethod);
        }
        int bestCount = 965255; //set stupidly high
        int bestIndex = -1;
        int lastValue = 0;
        List<Block> nextBlocks = new ArrayList<>(5);
        if (!excludeBlock.isEmpty() && (excludeBlock.get(excludeBlock.size() - 1) < routes.size())) {
            if (routingMethod == LayoutBlockConnectivityTools.METRIC) {
                lastValue = routes.get(excludeBlock.get(excludeBlock.size() - 1)).getMetric();
            } else /* if (routingMethod==LayoutBlockManager.HOPCOUNT)*/ {
                lastValue = routes.get(excludeBlock.get(excludeBlock.size() - 1)).getHopCount();
            }

            for (int i : excludeBlock) {
                nextBlocks.add(routes.get(i).getNextBlock());
            }

            if (enableSearchRouteLogging) {
                log.info("last index is {} {}", excludeBlock.get(excludeBlock.size() - 1),
                        routes.get(excludeBlock.get(excludeBlock.size() - 1)).getDestBlock().getDisplayName());
            }
        }

        for (int i = 0; i < routes.size(); i++) {
            if (!excludeBlock.contains(i)) {
                Routes ro = routes.get(i);
                if (!nextBlocks.contains(ro.getNextBlock())) {
                    //if(ro.getNextBlock()!=nextBlock){
                    int currentValue;
                    if (routingMethod == LayoutBlockConnectivityTools.METRIC) {
                        currentValue = routes.get(i).getMetric();
                    } else /*if (routingMethod==InstanceManager.getDefault(
                        LayoutBlockManager.class).HOPCOUNT)*/ {
                        currentValue = routes.get(i).getHopCount();  //was lastindex changed to i
                    }

                    if (currentValue >= lastValue) {
                        if (ro.getDestBlock() == destBlock) {
                            if (enableSearchRouteLogging) {
                                log.info("Match on dest blocks");
                                //Check that the route through from the previous block, to the next hop is valid
                                log.info("Is valid through path previous block " + previousBlock.getDisplayName()
                                        + " to " + ro.getNextBlock().getDisplayName());
                            }

                            if (validThroughPath(previousBlock, ro.getNextBlock())) {
                                if (enableSearchRouteLogging) {
                                    log.info("valid through path");
                                }

                                if (routingMethod == LayoutBlockConnectivityTools.METRIC) {
                                    if (ro.getMetric() < bestCount) {
                                        bestIndex = i;
                                        bestCount = ro.getMetric();
                                    }
                                } else /*if (routingMethod==InstanceManager.getDefault(
                                    LayoutBlockManager.class).HOPCOUNT)*/ {
                                    if (ro.getHopCount() < bestCount) {
                                        bestIndex = i;
                                        bestCount = ro.getHopCount();
                                    }
                                }
                            }

                            if (ro.getNextBlock() == this.getBlock()) {
                                if (enableSearchRouteLogging) {
                                    log.info("getNextBlock is this block therefore directly connected");
                                }
                                return i;
                            }
                        }
                    }
                }
            }
        }

        if (enableSearchRouteLogging) {
            log.info("returning " + bestIndex + " best count " + bestCount);
        }
        return bestIndex;
    }

    @CheckForNull
    Routes getRouteByDestBlock(Block blk) {
        for (int i = routes.size() - 1; i > -1; i--) {
            if (routes.get(i).getDestBlock() == blk) {
                return routes.get(i);
            }
        }
        return null;
    }

    @Nonnull
    List<Routes> getRouteByNeighbour(Block blk) {
        List<Routes> rtr = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getNextBlock() == blk) {
                rtr.add(routes.get(i));
            }
        }
        return rtr;
    }

    int getAdjacencyPacketFlow(Block blk) {
        for (int i = 0; i < neighbours.size(); i++) {
            if (neighbours.get(i).getBlock() == blk) {
                return neighbours.get(i).getPacketFlow();
            }
        }
        return -1;
    }

    boolean isValidNeighbour(Block blk) {
        for (int i = 0; i < neighbours.size(); i++) {
            if (neighbours.get(i).getBlock() == blk) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (listener == this) {
            log.debug("adding ourselves as a listener for some strange reason! Skipping");
            return;
        }
        super.addPropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {

        switch (e.getPropertyName()) {
            case "NewRoute": {
                if (enableUpdateRouteLogging) {
                    log.info("==Event type {} New {}", e.getPropertyName(), ((LayoutBlock) e.getNewValue()).getDisplayName());
                }
                break;
            }
            case "through-path-added": {
                if (enableUpdateRouteLogging) {
                    log.info("neighbour has new through path");
                }
                break;
            }
            case "through-path-removed": {
                if (enableUpdateRouteLogging) {
                    log.info("neighbour has through removed");
                }
                break;
            }
            case "routing": {
                if (e.getSource() instanceof LayoutBlock) {
                    LayoutBlock sourceLayoutBlock = (LayoutBlock) e.getSource();
                    if (enableUpdateRouteLogging) {
                        log.info("From " + this.getDisplayName()
                                + " we have a routing packet update from neighbour " + sourceLayoutBlock.getDisplayName());
                    }
                    RoutingPacket update = (RoutingPacket) e.getNewValue();
                    int updateType = update.getPacketType();
                    switch (updateType) {
                        case ADDITION: {
                            if (enableUpdateRouteLogging) {
                                log.info("\t    updateType: Addition");
                            }
                            //InstanceManager.getDefault(
                            //LayoutBlockManager.class).setLastRoutingChange();
                            addRouteFromNeighbour(sourceLayoutBlock, update);
                            break;
                        }
                        case UPDATE: {
                            if (enableUpdateRouteLogging) {
                                log.info("\t    updateType: Update");
                            }
                            updateRoutingInfo(sourceLayoutBlock, update);
                            break;
                        }
                        case REMOVAL: {
                            if (enableUpdateRouteLogging) {
                                log.info("\t    updateType: Removal");
                            }
                            InstanceManager.getDefault(LayoutBlockManager.class).setLastRoutingChange();
                            removeRouteFromNeighbour(sourceLayoutBlock, update);
                            break;
                        }
                        default: {
                            break;
                        }
                    }   //switch (updateType)
                }   //if (e.getSource() instanceof LayoutBlock)
                break;
            }
            default: {
                log.debug("Unhandled propertyChange({}): ", e);
                break;
            }
        }   // switch (e.getPropertyName())
    }   //propertyChange

    /**
     * Get valid Routes, based upon the next block and destination block
     *
     * @param nxtBlock next block
     * @param dstBlock final block
     * @return routes that fit, or null
     */
    @CheckForNull
    Routes getValidRoute(Block nxtBlock, Block dstBlock) {
        if ((nxtBlock != null) && (dstBlock != null)) {
            List<Routes> rtr = getRouteByNeighbour(nxtBlock);

            if (rtr.isEmpty()) {
                log.debug("From {}, no routes returned for getRouteByNeighbour({})",
                        this.getDisplayName(),
                        nxtBlock.getDisplayName());
                return null;
            }

            for (Routes rt : rtr) {
                if (rt.getDestBlock() == dstBlock) {
                    log.debug("From " + this.getDisplayName() + ", found dest " + dstBlock.getDisplayName() + ".");
                    return rt;
                }
            }
            log.debug("From {}, no routes to {}.", this.getDisplayName(), nxtBlock.getDisplayName());
        } else {
            log.warn("getValidRoute({}, {}",
                    (nxtBlock != null) ? nxtBlock.getDisplayName() : "<null>",
                    (dstBlock != null) ? dstBlock.getDisplayName() : "<null>");
        }
        return null;
    }

    /**
     * Is the route to the destination block, going via our neighbouring block
     * valid. ie Does the block have a route registered via neighbour
     * "protecting" to the destination block.
     *
     * @param protecting  neighbour block that might protect
     * @param destination block
     * @return true if we have valid path to block
     */
    public boolean isRouteToDestValid(Block protecting, Block destination) {
        if (protecting == destination) {
            log.debug("protecting and destination blocks are the same therefore we need to check if we have a valid neighbour");

            //We are testing for a directly connected block.
            if (getAdjacency(protecting) != null) {
                return true;
            }
        } else if (getValidRoute(protecting, destination) != null) {
            return true;
        }
        return false;
    }

    /**
     * Get a list of valid Routes to our destination block
     *
     * @param dstBlock target to find
     * @return routes between this and dstBlock
     */
    List<Routes> getDestRoutes(Block dstBlock) {
        List<Routes> rtr = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getDestBlock() == dstBlock) {
                rtr.add(routes.get(i));
            }
        }
        return rtr;
    }

    /**
     * Get a list of valid Routes via our next block
     *
     * @param nxtBlock target block
     * @return list of routes to target block
     */
    List<Routes> getNextRoutes(Block nxtBlock) {
        List<Routes> rtr = new ArrayList<>();
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getNextBlock() == nxtBlock) {
                rtr.add(routes.get(i));
            }
        }
        return rtr;
    }

    void updateRoutingInfo(Routes route) {
        if (route.getHopCount() >= 254) {
            return;
        }
        Block destBlock = route.getDestBlock();

        RoutingPacket update = new RoutingPacket(UPDATE, destBlock, getBestRouteByHop(destBlock).getHopCount() + 1,
                ((getBestRouteByMetric(destBlock).getMetric()) + metric),
                ((getBestRouteByMetric(destBlock).getMetric())
                + block.getLengthMm()), -1,
                getNextPacketID());
        firePropertyChange("routing", null, update);
    }

    // This lot might need changing to only forward on the best route details.
    void updateRoutingInfo(LayoutBlock src, RoutingPacket update) {
        if (enableUpdateRouteLogging) {
            log.info("From " + this.getDisplayName() + " src: " + src.getDisplayName()
                    + ", block: " + update.getBlock().getDisplayName() + ", hopCount: " + update.getHopCount()
                    + ", metric: " + update.getMetric() + ", status: " + update.getBlockState()
                    + ", packetID: " + update.getPacketId());
        }
        Block srcblk = src.getBlock();
        Adjacencies adj = getAdjacency(srcblk);

        if (adj == null) {
            if (enableUpdateRouteLogging) {
                log.info("From " + this.getDisplayName()
                        + " packet is from a src that is not registered " + srcblk.getDisplayName());
            }
            //If the packet is from a src that is not registered as a neighbour
            //Then we will simply reject it.
            return;
        }

        if (updatePacketActedUpon(update.getPacketId())) {
            if (adj.updatePacketActedUpon(update.getPacketId())) {
                if (enableUpdateRouteLogging) {
                    log.info("Reject packet update as we have already acted up on it from this neighbour");
                }
                return;
            }
        }

        if (enableUpdateRouteLogging) {
            log.info("From " + this.getDisplayName() + " an Update packet from neighbour " + src.getDisplayName());
        }

        Block updateBlock = update.getBlock();

        //Block srcblk = src.getBlock();
        //Need to add in a check to make sure that we have a route registered from the source neighbour
        //for the block that they are referring too.
        if (updateBlock == this.getBlock()) {
            if (enableUpdateRouteLogging) {
                log.info("Reject packet update as it is a route advertised by our selves");
            }
            return;
        }

        Routes ro;
        boolean neighbour = false;
        if (updateBlock == srcblk) {
            //Very likely that this update is from a neighbour about its own status.
            ro = getValidRoute(this.getBlock(), updateBlock);
            neighbour = true;
        } else {
            ro = getValidRoute(srcblk, updateBlock);
        }

        if (ro == null) {
            if (enableUpdateRouteLogging) {
                log.info("From " + this.getDisplayName()
                        + " update is from a source that we do not have listed as a route to the destination");
                log.info("From " + this.getDisplayName()
                        + " update packet is for a block that we do not have route registered for " + updateBlock.getDisplayName());
            }
            //If the packet is for a dest that is not in the routing table
            //Then we will simply reject it.
            return;
        }
        /*This prevents us from entering into an update loop.
           We only add it to our list once it has passed through as being a valid
           packet, otherwise we may get the same packet id back, but from a valid source
           which would end up be rejected*/

        actedUponUpdates.add(update.getPacketId());
        adj.addPacketReceivedFromNeighbour(update.getPacketId());

        int hopCount = update.getHopCount();
        int packetmetric = update.getMetric();
        int blockstate = update.getBlockState();
        float length = update.getLength();

        //Need to add in a check for a block that is directly connected.
        if (hopCount != -1) {
            //Was increase hop count before setting it
            //int oldHop = ro.getHopCount();
            if (ro.getHopCount() != hopCount) {
                if (enableUpdateRouteLogging) {
                    log.info(this.getDisplayName() + " Hop counts to " + ro.getDestBlock().getDisplayName()
                            + " not the same so will change from " + ro.getHopCount() + " to " + hopCount);
                }
                ro.setHopCount(hopCount);
                hopCount++;
            } else {
                //No point in forwarding on the update if the hopcount hasn't changed
                hopCount = -1;
            }
        }

        //bad to use values as errors, but it's pre-existing code, and code wins
        if ((int) length != -1) {
            //Length is added at source
            float oldLength = ro.getLength();
            if (!MathUtil.equals(oldLength, length)) {
                ro.setLength(length);
                boolean forwardUpdate = true;

                if (ro != getBestRouteByLength(update.getBlock())) {
                    forwardUpdate = false;
                }

                if (enableUpdateRouteLogging) {
                    log.info("From " + this.getDisplayName() + " updating length from " + oldLength + " to " + length);
                }

                if (neighbour) {
                    length = srcblk.getLengthMm();
                    adj.setLength(length);

                    //ro.setLength(length);
                    //Also if neighbour we need to update the cost of the routes via it to reflect the new metric 02/20/2011
                    if (forwardUpdate) {
                        List<Routes> neighbourRoute = getNextRoutes(srcblk);

                        //neighbourRoutes, contains all the routes that have been advertised by the neighbour
                        //that will need to have their metric updated to reflect the change.
                        for (int i = 0; i < neighbourRoute.size(); i++) {
                            Routes nRo = neighbourRoute.get(i);

                            //Need to remove old metric to the neigbour, then add the new one on
                            float updateLength = nRo.getLength();
                            updateLength = (updateLength - oldLength) + length;

                            if (enableUpdateRouteLogging) {
                                log.info("From " + this.getDisplayName() + " update metric for route "
                                        + nRo.getDestBlock().getDisplayName() + " from " + nRo.getLength() + " to "
                                        + updateLength);
                            }
                            nRo.setLength(updateLength);
                            List<Block> messageRecipients = getThroughPathDestinationBySource(srcblk);
                            RoutingPacket newUpdate = new RoutingPacket(UPDATE, nRo.getDestBlock(), -1, -1, updateLength
                                    + block.getLengthMm(), -1, getNextPacketID());
                            updateRoutesToNeighbours(messageRecipients, nRo, newUpdate);
                        }
                    }
                } else if (forwardUpdate) {
                    //This can cause a loop, if the layout is in a loop, so we send out the same packetID.
                    List<Block> messageRecipients = getThroughPathSourceByDestination(srcblk);
                    RoutingPacket newUpdate = new RoutingPacket(UPDATE, updateBlock, -1, -1,
                            length + block.getLengthMm(), -1, update.getPacketId());
                    updateRoutesToNeighbours(messageRecipients, ro, newUpdate);
                }
                length += metric;
            } else {
                length = -1;
            }
        }

        if (packetmetric != -1) {
            //Metric is added at source
            //Keep a reference of the old metric.
            int oldmetric = ro.getMetric();
            if (oldmetric != packetmetric) {
                ro.setMetric(packetmetric);

                if (enableUpdateRouteLogging) {
                    log.info("From " + this.getDisplayName() + " updating metric from " + oldmetric + " to " + packetmetric);
                }
                boolean forwardUpdate = true;

                if (ro != getBestRouteByMetric(update.getBlock())) {
                    forwardUpdate = false;
                }

                //if the metric update is for a neighbour then we will go directly to the neighbour for the value,
                //rather than trust what is in the message at this stage.
                if (neighbour) {
                    packetmetric = src.getBlockMetric();
                    adj.setMetric(packetmetric);

                    if (forwardUpdate) {
                        //ro.setMetric(packetmetric);
                        //Also if neighbour we need to update the cost of the routes via it to
                        //reflect the new metric 02/20/2011
                        List<Routes> neighbourRoute = getNextRoutes(srcblk);

                        //neighbourRoutes, contains all the routes that have been advertised by the neighbour that
                        //will need to have their metric updated to reflect the change.
                        for (int i = 0; i < neighbourRoute.size(); i++) {
                            Routes nRo = neighbourRoute.get(i);

                            //Need to remove old metric to the neigbour, then add the new one on
                            int updatemet = nRo.getMetric();
                            updatemet = (updatemet - oldmetric) + packetmetric;

                            if (enableUpdateRouteLogging) {
                                log.info("From " + this.getDisplayName() + " update metric for route "
                                        + nRo.getDestBlock().getDisplayName() + " from " + nRo.getMetric() + " to " + updatemet);
                            }
                            nRo.setMetric(updatemet);
                            List<Block> messageRecipients = getThroughPathDestinationBySource(srcblk);
                            RoutingPacket newUpdate = new RoutingPacket(UPDATE, nRo.getDestBlock(),
                                    hopCount, updatemet + metric, -1, -1,
                                    getNextPacketID());
                            updateRoutesToNeighbours(messageRecipients, nRo, newUpdate);
                        }
                    }
                } else if (forwardUpdate) {
                    //This can cause a loop, if the layout is in a loop, so we send out the same packetID.
                    List<Block> messageRecipients = getThroughPathSourceByDestination(srcblk);
                    RoutingPacket newUpdate = new RoutingPacket(UPDATE, updateBlock, hopCount,
                            packetmetric + metric, -1, -1, update.getPacketId());
                    updateRoutesToNeighbours(messageRecipients, ro, newUpdate);
                }
                packetmetric += metric;
                //Think we need a list of routes that originate from this source neighbour
            } else {
                //No point in forwarding on the update if the metric hasn't changed
                packetmetric = -1;
                //Potentially when we do this we need to update all the routes that go via this block, not just this route.
            }
        }

        if (blockstate != -1) {
            // We will update all the destination blocks with the new state, it
            // saves re-firing off new updates block status
            boolean stateUpdated = false;
            List<Routes> rtr = getDestRoutes(updateBlock);

            for (Routes rt : rtr) {
                if (rt.getState() != blockstate) {
                    stateUpdated = true;
                    rt.stateChange();
                }
            }

            if (stateUpdated) {
                RoutingPacket newUpdate = new RoutingPacket(UPDATE, updateBlock, -1, -1, -1, blockstate, getNextPacketID());
                firePropertyChange("routing", null, newUpdate);
            }
        }

        //We need to expand on this so that any update to routing metric is propergated correctly
        if ((packetmetric != -1) || (hopCount != -1) || (length != -1)) {
            //We only want to send the update on to neighbours that we have advertised the route to.
            List<Block> messageRecipients = getThroughPathSourceByDestination(srcblk);
            RoutingPacket newUpdate = new RoutingPacket(UPDATE, updateBlock, hopCount, packetmetric,
                    length, blockstate, update.getPacketId());
            updateRoutesToNeighbours(messageRecipients, ro, newUpdate);
        }
        // Was just pass on hop count
    }

    void updateRoutesToNeighbours(List<Block> messageRecipients, Routes ro, RoutingPacket update) {
        for (int i = 0; i < messageRecipients.size(); i++) {
            Adjacencies adj = getAdjacency(messageRecipients.get(i));
            if (adj.advertiseRouteToNeighbour(ro)) {
                adj.addRouteAdvertisedToNeighbour(ro);
                LayoutBlock recipient = InstanceManager.getDefault(
                        LayoutBlockManager.class).getLayoutBlock(messageRecipients.get(i));
                if (recipient != null) {
                    recipient.updateRoutingInfo(this, update);
                }
            }
        }
    }

    Routes getBestRouteByMetric(Block dest) {
        //int bestHopCount = 255;
        int bestMetric = 965000;
        int bestIndex = -1;

        List<Routes> destRoutes = getDestRoutes(dest);
        for (int i = 0; i < destRoutes.size(); i++) {
            if (destRoutes.get(i).getMetric() < bestMetric) {
                bestMetric = destRoutes.get(i).getMetric();
                bestIndex = i;
            }
        }

        if (bestIndex == -1) {
            return null;
        }
        return destRoutes.get(bestIndex);
    }

    Routes getBestRouteByHop(Block dest) {
        int bestHopCount = 255;
        //int bestMetric = 965000;
        int bestIndex = -1;

        List<Routes> destRoutes = getDestRoutes(dest);
        for (int i = 0; i < destRoutes.size(); i++) {
            if (destRoutes.get(i).getHopCount() < bestHopCount) {
                bestHopCount = destRoutes.get(i).getHopCount();
                bestIndex = i;
            }
        }

        if (bestIndex == -1) {
            return null;
        }
        return destRoutes.get(bestIndex);
    }

    Routes getBestRouteByLength(Block dest) {
        //int bestHopCount = 255;
        //int bestMetric = 965000;
        //long bestLength = 999999999;
        int bestIndex = -1;
        List<Routes> destRoutes = getDestRoutes(dest);
        float bestLength = destRoutes.get(0).getLength();

        for (int i = 0; i < destRoutes.size(); i++) {
            if (destRoutes.get(i).getLength() < bestLength) {
                bestLength = destRoutes.get(i).getLength();
                bestIndex = i;
            }
        }

        if (bestIndex == -1) {
            return null;
        }
        return destRoutes.get(bestIndex);
    }

    void addRouteToNeighbours(Routes ro) {
        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " Add route to neighbour");
        }
        Block nextHop = ro.getNextBlock();
        List<LayoutBlock> validFromPath = new ArrayList<>();

        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " new block " + nextHop.getDisplayName());
        }

        for (int i = 0; i < throughPaths.size(); i++) {
            LayoutBlock validBlock = null;

            if (enableAddRouteLogging) {
                log.info("Through routes index " + i);
                log.info("From " + this.getDisplayName() + " A through routes "
                        + throughPaths.get(i).getSourceBlock().getDisplayName() + " "
                        + throughPaths.get(i).getDestinationBlock().getDisplayName());
            }

            /*As the through paths include each possible path, ie 2 > 3 and 3 > 2
               as seperate entries then we only need to forward the new route to those
               source blocks that have a desination of the next hop*/
            if (throughPaths.get(i).getDestinationBlock() == nextHop) {
                if (getAdjacency(throughPaths.get(i).getSourceBlock()).isMutual()) {
                    validBlock = InstanceManager.getDefault(
                            LayoutBlockManager.class).
                            getLayoutBlock(throughPaths.get(i).getSourceBlock());
                }
            }

            //only need to add it the once.  Not sure if the contains is required.
            if ((validBlock != null) && (!validFromPath.contains(validBlock))) {
                validFromPath.add(validBlock);
            }
        }

        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " ===== valid from size path "
                    + validFromPath.size() + " ==== (addroutetoneigh)");

            validFromPath.forEach((valid) -> {
                log.info(valid.getDisplayName());
            });
            log.info("Next Hop " + nextHop.getDisplayName());
        }
        RoutingPacket update = new RoutingPacket(ADDITION, ro.getDestBlock(), ro.getHopCount() + 1,
                ro.getMetric() + metric,
                (ro.getLength() + getBlock().getLengthMm()), -1, getNextPacketID());

        for (int i = 0; i < validFromPath.size(); i++) {
            Adjacencies adj = getAdjacency(validFromPath.get(i).getBlock());
            if (adj.advertiseRouteToNeighbour(ro)) {
                //getBestRouteByHop(destBlock).getHopCount()+1, ((getBestRouteByMetric(destBlock).getMetric())+metric),
                //((getBestRouteByMetric(destBlock).getMetric())+block.getLengthMm())
                if (enableAddRouteLogging) {
                    log.info("From " + this.getDisplayName() + " Sending update to "
                            + validFromPath.get(i).getDisplayName() + " As this has a better hop count or metric");
                }
                adj.addRouteAdvertisedToNeighbour(ro);
                validFromPath.get(i).addRouteFromNeighbour(this, update);
            }
        }
    }

    void addRouteFromNeighbour(LayoutBlock src, RoutingPacket update) {
        if (enableAddRouteLogging) {
            //log.info("From " + this.getDisplayName() + " packet to be added from neighbour " + src.getDisplayName());
            log.info("From " + this.getDisplayName() + " src: " + src.getDisplayName()
                    + ", block: " + update.getBlock().getDisplayName() + ", hopCount: " + update.getHopCount()
                    + ", metric: " + update.getMetric() + ", status: " + update.getBlockState()
                    + ", packetID: " + update.getPacketId());
        }
        InstanceManager.getDefault(LayoutBlockManager.class).setLastRoutingChange();
        Block destBlock = update.getBlock();
        Block srcblk = src.getBlock();

        if (destBlock == this.getBlock()) {
            if (enableAddRouteLogging) {
                log.info("Reject packet update as it is to a route advertised by our selves");
            }
            return;
        }

        Adjacencies adj = getAdjacency(srcblk);
        if (adj == null) {
            if (enableAddRouteLogging) {
                log.info("From " + this.getDisplayName()
                        + " packet is from a src that is not registered " + srcblk.getDisplayName());
            }
            //If the packet is from a src that is not registered as a neighbour
            //Then we will simply reject it.
            return;
        } else if (adj.getPacketFlow() == TXONLY) {
            if (enableAddRouteLogging) {
                log.info("From " + this.getDisplayName() + " packet is from a src "
                        + src.getDisplayName() + " that is registered as one that we should be transmitting to only");
            }
            //we should only be transmitting routes to this neighbour not receiving them
            return;
        }
        int hopCount = update.getHopCount();
        int updatemetric = update.getMetric();
        float length = update.getLength();

        if (hopCount > 255) {
            if (enableAddRouteLogging) {
                log.info("From " + this.getDisplayName() + " hop count exceeded " + destBlock.getDisplayName());
            }
            return;
        }

        for (Routes ro : routes) {
            if ((ro.getNextBlock() == srcblk) && (ro.getDestBlock() == destBlock)) {
                if (enableAddRouteLogging) {
                    log.info("From " + this.getDisplayName()
                            + " Route to " + destBlock.getDisplayName() + " is already configured");
                    log.info(ro.getHopCount() + " v " + hopCount);
                    log.info(ro.getMetric() + " v " + updatemetric);
                }
                updateRoutingInfo(src, update);
                return;
            }
        }

        if (enableAddRouteLogging) {
            log.info("From " + this.getDisplayName() + " We should be adding route " + destBlock.getDisplayName());
        }

        //We need to propergate out the routes that we have added to our neighbour
        int direction = adj.getDirection();
        Routes route = new Routes(destBlock, srcblk, hopCount, direction, updatemetric, length);
        routes.add(route);

        //Need to propergate the route down to our neighbours
        addRouteToNeighbours(route);
    }

    /* this should look after removal of a specific next hop from our neighbour*/
    /**
     * Get the direction of travel to our neighbouring block.
     *
     * @param neigh neighbor block
     * @return direction to get to neighbor block
     */
    public int getNeighbourDirection(LayoutBlock neigh) {
        if (neigh == null) {
            return Path.NONE;
        }
        Block neighbourBlock = neigh.getBlock();
        return getNeighbourDirection(neighbourBlock);
    }

    public int getNeighbourDirection(Block neighbourBlock) {
        for (int i = 0; i < neighbours.size(); i++) {
            if (neighbours.get(i).getBlock() == neighbourBlock) {
                return neighbours.get(i).getDirection();
            }
        }
        return Path.NONE;
    }

    Adjacencies getAdjacency(Block blk) {
        for (int i = 0; i < neighbours.size(); i++) {
            if (neighbours.get(i).getBlock() == blk) {
                return neighbours.get(i);
            }
        }
        return null;
    }

    final static int ADDITION = 0x00;
    final static int UPDATE = 0x02;
    final static int REMOVAL = 0x04;

    final static int RXTX = 0x00;
    final static int RXONLY = 0x02;
    final static int TXONLY = 0x04;
    final static int NONE = 0x08;
    int metric = 100;

    private static class RoutingPacket {

        int packetType;
        Block block;
        int hopCount = -1;
        int packetMetric = -1;
        int blockstate = -1;
        float length = -1;
        Integer packetRef = -1;

        RoutingPacket(int packetType, Block blk, int hopCount, int packetMetric, float length, int blockstate, Integer packetRef) {
            this.packetType = packetType;
            this.block = blk;
            this.hopCount = hopCount;
            this.packetMetric = packetMetric;
            this.blockstate = blockstate;
            this.packetRef = packetRef;
            this.length = length;
        }

        int getPacketType() {
            return packetType;
        }

        Block getBlock() {
            return block;
        }

        int getHopCount() {
            return hopCount;
        }

        int getMetric() {
            return packetMetric;
        }

        int getBlockState() {
            return blockstate;
        }

        float getLength() {
            return length;
        }

        Integer getPacketId() {
            return packetRef;
        }
    }

    /**
     * Get the number of neighbor blocks attached to this block.
     *
     * @return count of neighbor
     */
    public int getNumberOfNeighbours() {
        return neighbours.size();
    }

    /**
     * Get the neighboring block at index i.
     *
     * @param i index to neighbor
     * @return neighbor block
     */
    public Block getNeighbourAtIndex(int i) {
        return neighbours.get(i).getBlock();
    }

    /**
     * Get the direction of travel to neighbouring block at index i.
     *
     * @param i index in neighbors
     * @return neighbor block
     */
    public int getNeighbourDirection(int i) {
        return neighbours.get(i).getDirection();
    }

    /**
     * Get the metric/cost to neighbouring block at index i.
     *
     * @param i index in neighbors
     * @return metric of neighbor
     */
    public int getNeighbourMetric(int i) {
        return neighbours.get(i).getMetric();
    }

    /**
     * Get the flow of traffic to and from neighbouring block at index i RXTX -
     * Means Traffic can flow both ways between the blocks RXONLY - Means we can
     * only receive traffic from our neighbour, we can not send traffic to it
     * TXONLY - Means we do not receive traffic from our neighbour, but can send
     * traffic to it.
     *
     * @param i index in neighbors
     * @return direction of traffic
     */
    public String getNeighbourPacketFlowAsString(int i) {
        return decodePacketFlow(neighbours.get(i).getPacketFlow());
    }

    /**
     * Is our neighbouring block at index i a mutual neighbour, ie both blocks
     * have each other registered as neighbours and are exchanging information.
     *
     * @param i index of neighbor
     * @return true if both are mutual neighbors
     */
    public boolean isNeighbourMutual(int i) {
        return neighbours.get(i).isMutual();
    }

    int getNeighbourIndex(Adjacencies adj) {
        for (int i = 0; i < neighbours.size(); i++) {
            if (neighbours.get(i) == adj) {
                return i;
            }
        }
        return -1;
    }

    private class Adjacencies {

        Block adjBlock;
        LayoutBlock adjLayoutBlock;
        int direction;
        int packetFlow = RXTX;
        boolean mutualAdjacency = false;

        HashMap<Block, Routes> adjDestRoutes = new HashMap<>();
        List<Integer> actedUponUpdates = new ArrayList<>(501);

        Adjacencies(Block block, int dir, int packetFlow) {
            adjBlock = block;
            direction = dir;
            this.packetFlow = packetFlow;
        }

        Block getBlock() {
            return adjBlock;
        }

        LayoutBlock getLayoutBlock() {
            return adjLayoutBlock;
        }

        int getDirection() {
            return direction;
        }

        // If a set true on mutual, then we could go through the list of what to send out to neighbour
        void setMutual(boolean mut) {
            if (mut == mutualAdjacency) {   //No change will exit
                return;
            }
            mutualAdjacency = mut;
            if (mutualAdjacency) {
                adjLayoutBlock = InstanceManager.getDefault(
                        LayoutBlockManager.class).getLayoutBlock(adjBlock);
            }
        }

        boolean isMutual() {
            return mutualAdjacency;
        }

        int getPacketFlow() {
            return packetFlow;
        }

        void setPacketFlow(int flow) {
            if (flow != packetFlow) {
                int oldFlow = packetFlow;
                packetFlow = flow;
                firePropertyChange("neighbourpacketflow", oldFlow, packetFlow);
            }
        }

        // The metric could just be read directly from the neighbour as we have no
        // need to specifically keep a copy of it here this is here just to fire off the change
        void setMetric(int met) {
            firePropertyChange("neighbourmetric", null, getNeighbourIndex(this));
        }

        int getMetric() {
            if (adjLayoutBlock != null) {
                return adjLayoutBlock.getBlockMetric();
            }
            adjLayoutBlock = InstanceManager.getDefault(
                    LayoutBlockManager.class).getLayoutBlock(adjBlock);
            if (adjLayoutBlock != null) {
                return adjLayoutBlock.getBlockMetric();
            }

            if (log.isDebugEnabled()) {
                log.debug("Layout Block " + adjBlock.getDisplayName() + " returned as null");
            }
            return -1;
        }

        void setLength(float len) {
            firePropertyChange("neighbourlength", null, getNeighbourIndex(this));
        }

        float getLength() {
            if (adjLayoutBlock != null) {
                return adjLayoutBlock.getBlock().getLengthMm();
            }
            adjLayoutBlock = InstanceManager.getDefault(
                    LayoutBlockManager.class).getLayoutBlock(adjBlock);
            if (adjLayoutBlock != null) {
                return adjLayoutBlock.getBlock().getLengthMm();
            }

            if (log.isDebugEnabled()) {
                log.debug("Layout Block " + adjBlock.getDisplayName() + " returned as null");
            }
            return -1;
        }

        void removeRouteAdvertisedToNeighbour(Routes removeRoute) {
            Block dest = removeRoute.getDestBlock();

            if (adjDestRoutes.get(dest) == removeRoute) {
                adjDestRoutes.remove(dest);
            }
        }

        void removeRouteAdvertisedToNeighbour(Block block) {
            adjDestRoutes.remove(block);
        }

        void addRouteAdvertisedToNeighbour(Routes addedRoute) {
            adjDestRoutes.put(addedRoute.getDestBlock(), addedRoute);
        }

        boolean advertiseRouteToNeighbour(Routes routeToAdd) {
            if (!isMutual()) {
                log.debug("In block " + getDisplayName()
                        + ": Neighbour is not mutual so will not advertise it (Routes " + routeToAdd + ")");

                return false;
            }

            //Just wonder if this should forward on the new packet to the neighbour?
            Block dest = routeToAdd.getDestBlock();
            if (!adjDestRoutes.containsKey(dest)) {
                log.debug("In block " + getDisplayName()
                        + ": We are not currently advertising a route to the destination to neighbour: "
                        + dest.getDisplayName());

                return true;
            }

            if (routeToAdd.getHopCount() > 255) {
                log.debug("Hop count is gereater than 255 we will therefore do nothing with this route");
                return false;
            }
            Routes existingRoute = adjDestRoutes.get(dest);
            if (existingRoute.getMetric() > routeToAdd.getMetric()) {
                return true;
            }
            if (existingRoute.getHopCount() > routeToAdd.getHopCount()) {
                return true;
            }

            if (existingRoute == routeToAdd) {
                //We return true as the metric might have changed
                return false;
            }
            return false;
        }

        boolean updatePacketActedUpon(Integer packetID) {
            return actedUponUpdates.contains(packetID);
        }

        void addPacketReceivedFromNeighbour(Integer packetID) {
            actedUponUpdates.add(packetID);
            if (actedUponUpdates.size() > 500) {
                actedUponUpdates.subList(0, 250).clear();
            }
        }

        void dispose() {
            adjBlock = null;
            adjLayoutBlock = null;
            mutualAdjacency = false;
            adjDestRoutes = null;
            actedUponUpdates = null;
        }
    }

    /**
     * Get the number of routes that the block has registered.
     *
     * @return count of routes
     */
    public int getNumberOfRoutes() {
        return routes.size();
    }

    /**
     * Get the direction of route i.
     *
     * @param i index in routes
     * @return direction
     */
    public int getRouteDirectionAtIndex(int i) {
        return routes.get(i).getDirection();
    }

    /**
     * Get the destination block at route i
     *
     * @param i index in routes
     * @return dest block from route
     */
    public Block getRouteDestBlockAtIndex(int i) {
        return routes.get(i).getDestBlock();
    }

    /**
     * Get the next block at route i
     *
     * @param i index in routes
     * @return next block from route
     */
    public Block getRouteNextBlockAtIndex(int i) {
        return routes.get(i).getNextBlock();
    }

    /**
     * Get the hop count of route i.<br>
     * The Hop count is the number of other blocks that we traverse to get to
     * the destination
     *
     * @param i index in routes
     * @return hop count
     */
    public int getRouteHopCountAtIndex(int i) {
        return routes.get(i).getHopCount();
    }

    /**
     * Get the length of route i.<br>
     * The length is the combined length of all the blocks that we traverse to
     * get to the destination
     *
     * @param i index in routes
     * @return length of block in route
     */
    public float getRouteLengthAtIndex(int i) {
        return routes.get(i).getLength();
    }

    /**
     * Get the metric/cost at route i
     *
     * @param i index in routes
     * @return metric
     */
    public int getRouteMetric(int i) {
        return routes.get(i).getMetric();
    }

    /**
     * Get the state (Occupied, unoccupied) of the destination layout block at
     * index i
     *
     * @param i index in routes
     * @return state of block
     */
    public int getRouteState(int i) {
        return routes.get(i).getState();
    }

    /**
     * Is the route to the destination potentially valid from our block.
     *
     * @param i index in route
     * @return true if route is valid
     */
    // TODO: @Deprecated // Java standard pattern for boolean getters is "isRouteValid()"
    public boolean getRouteValid(int i) {
        return routes.get(i).isRouteCurrentlyValid();
    }

    /**
     * Get the state of the destination layout block at index i as a string.
     *
     * @param i index in routes
     * @return dest status
     */
    public String getRouteStateAsString(int i) {
        int state = routes.get(i).getState();
        switch (state) {
            case OCCUPIED: {
                return Bundle.getMessage("TrackOccupied"); // i18n using NamedBeanBundle.properties TODO remove duplicate keys
            }

            case RESERVED: {
                return Bundle.getMessage("StateReserved"); // "Reserved"
            }

            case EMPTY: {
                return Bundle.getMessage("StateFree");  // "Free"
            }

            default: {
                return Bundle.getMessage("BeanStateUnknown"); // "Unknown"
            }
        }
    }

    int getRouteIndex(Routes r) {
        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i) == r) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the number of layout blocks to our desintation block going from
     * the next directly connected block. If the destination block and nextblock
     * are the same and the block is also registered as a neighbour then 1 is
     * returned. If no valid route to the destination block can be found via the
     * next block then -1 is returned. If more than one route exists to the
     * destination then the route with the lowest count is returned.
     *
     * @param destination final block
     * @param nextBlock   adjcent block
     * @return hop count to final, -1 if not available
     */
    public int getBlockHopCount(Block destination, Block nextBlock) {
        if ((destination == nextBlock) && (isValidNeighbour(nextBlock))) {
            return 1;
        }

        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getDestBlock() == destination) {
                if (routes.get(i).getNextBlock() == nextBlock) {
                    return routes.get(i).getHopCount();
                }
            }
        }
        return -1;
    }

    /**
     * Get the metric to our desintation block going from the next directly
     * connected block. If the destination block and nextblock are the same and
     * the block is also registered as a neighbour then 1 is returned. If no
     * valid route to the destination block can be found via the next block then
     * -1 is returned. If more than one route exists to the destination then the
     * route with the lowest count is returned.
     *
     * @param destination final block
     * @param nextBlock   adjcent block
     * @return metric to final block, -1 if not available
     */
    public int getBlockMetric(Block destination, Block nextBlock) {
        if ((destination == nextBlock) && (isValidNeighbour(nextBlock))) {
            return 1;
        }

        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getDestBlock() == destination) {
                if (routes.get(i).getNextBlock() == nextBlock) {
                    return routes.get(i).getMetric();
                }
            }
        }
        return -1;
    }

    /**
     * Get the distance to our desintation block going from the next
     * directly connected block. If the destination block and nextblock are the
     * same and the block is also registered as a neighbour then 1 is returned.
     * If no valid route to the destination block can be found via the next
     * block then -1 is returned. If more than one route exists to the
     * destination then the route with the lowest count is returned.
     *
     * @param destination final block
     * @param nextBlock   adjcent block
     * @return lenght to final, -1 if not viable
     */
    public float getBlockLength(Block destination, Block nextBlock) {
        if ((destination == nextBlock) && (isValidNeighbour(nextBlock))) {
            return 1;
        }

        for (int i = 0; i < routes.size(); i++) {
            if (routes.get(i).getDestBlock() == destination) {
                if (routes.get(i).getNextBlock() == nextBlock) {
                    return routes.get(i).getLength();
                }
            }
        }
        return -1;
    }

    // TODO This needs a propertychange listener adding
    private class Routes implements PropertyChangeListener {

        int direction;
        Block destBlock;
        Block nextBlock;
        int hopCount;
        int routeMetric;
        float length;

        //int state =-1;
        int miscflags = 0x00;
        boolean validCurrentRoute = false;

        public Routes(Block dstBlock, Block nxtBlock, int hop, int dir, int met, float len) {
            destBlock = dstBlock;
            nextBlock = nxtBlock;
            hopCount = hop;
            direction = dir;
            routeMetric = met;
            length = len;
            validCurrentRoute = checkIsRouteOnValidThroughPath(this);
            firePropertyChange("length", null, null);
            destBlock.addPropertyChangeListener(this);
        }

        @Override
        public String toString() {
            return "Routes(dst:" + destBlock + ", nxt:" + nextBlock
                    + ", hop:" + hopCount + ", dir:" + direction
                    + ", met:" + routeMetric + ", len: " + length + ")";
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("state")) {
                stateChange();
            }
        }

        public Block getDestBlock() {
            return destBlock;
        }

        public Block getNextBlock() {
            return nextBlock;
        }

        public int getHopCount() {
            return hopCount;
        }

        public int getDirection() {
            return direction;
        }

        public int getMetric() {
            return routeMetric;
        }

        public float getLength() {
            return length;
        }

        public void setMetric(int met) {
            if (met == routeMetric) {
                return;
            }
            routeMetric = met;
            firePropertyChange("metric", null, getRouteIndex(this));
        }

        public void setHopCount(int hop) {
            if (hopCount == hop) {
                return;
            }
            hopCount = hop;
            firePropertyChange("hop", null, getRouteIndex(this));
        }

        public void setLength(float len) {
            if (len == length) {
                return;
            }
            length = len;
            firePropertyChange("length", null, getRouteIndex(this));
        }

        // This state change is only here for the routing table view
        void stateChange() {
            firePropertyChange("state", null, getRouteIndex(this));
        }

        int getState() {
            LayoutBlock destLBlock = InstanceManager.getDefault(
                    LayoutBlockManager.class).getLayoutBlock(destBlock);
            if (destLBlock != null) {
                return destLBlock.getBlockStatus();
            }

            if (log.isDebugEnabled()) {
                log.debug("Layout Block {} returned as null", destBlock.getDisplayName());
            }
            return -1;
        }

        void setValidCurrentRoute(boolean boo) {
            if (validCurrentRoute == boo) {
                return;
            }
            validCurrentRoute = boo;
            firePropertyChange("valid", null, getRouteIndex(this));
        }

        boolean isRouteCurrentlyValid() {
            return validCurrentRoute;
        }

        // Misc flags is not used in general routing, but is used for determining route removals
        void setMiscFlags(int f) {
            miscflags = f;
        }

        int getMiscFlags() {
            return miscflags;
        }
    }

    /**
     * Get the number of valid through paths on this block.
     *
     * @return count of paths through this block
     */
    public int getNumberOfThroughPaths() {
        return throughPaths.size();
    }

    /**
     * Get the source block at index i
     *
     * @param i index in throughPaths
     * @return source block
     */
    public Block getThroughPathSource(int i) {
        return throughPaths.get(i).getSourceBlock();
    }

    /**
     * Get the destination block at index i
     *
     * @param i index in throughPaths
     * @return final block
     */
    public Block getThroughPathDestination(int i) {
        return throughPaths.get(i).getDestinationBlock();
    }

    /**
     * Is the through path at index i active?
     *
     * @param i index in path
     * @return active or not
     */
    public Boolean isThroughPathActive(int i) {
        return throughPaths.get(i).isPathActive();
    }

    private class ThroughPaths implements PropertyChangeListener {

        Block sourceBlock;
        Block destinationBlock;
        Path sourcePath;
        Path destinationPath;

        boolean pathActive = false;

        HashMap<Turnout, Integer> _turnouts = new HashMap<>();

        ThroughPaths(Block srcBlock, Path srcPath, Block destBlock, Path dstPath) {
            sourceBlock = srcBlock;
            destinationBlock = destBlock;
            sourcePath = srcPath;
            destinationPath = dstPath;
        }

        Block getSourceBlock() {
            return sourceBlock;
        }

        Block getDestinationBlock() {
            return destinationBlock;
        }

        Path getSourcePath() {
            return sourcePath;
        }

        Path getDestinationPath() {
            return destinationPath;
        }

        boolean isPathActive() {
            return pathActive;
        }

        void setTurnoutList(List<LayoutTrackExpectedState<LayoutTurnout>> turnouts) {
            if (!_turnouts.isEmpty()) {
                Set<Turnout> en = _turnouts.keySet();
                en.forEach((listTurnout) -> {
                    listTurnout.removePropertyChangeListener(this);
                });
            }

            // If we have no turnouts in this path, then this path is always active
            if (turnouts.isEmpty()) {
                pathActive = true;
                setRoutesValid(sourceBlock, true);
                setRoutesValid(destinationBlock, true);
                return;
            }
            _turnouts = new HashMap<>(turnouts.size());
            for (int i = 0; i < turnouts.size(); i++) {
                if (turnouts.get(i).getObject() instanceof LayoutSlip) {
                    int slipState = turnouts.get(i).getExpectedState();
                    LayoutSlip ls = (LayoutSlip) turnouts.get(i).getObject();
                    int taState = ls.getTurnoutState(slipState);
                    _turnouts.put(ls.getTurnout(), taState);
                    ls.getTurnout().addPropertyChangeListener(this, ls.getTurnoutName(), "Layout Block Routing");

                    int tbState = ls.getTurnoutBState(slipState);
                    _turnouts.put(ls.getTurnoutB(), tbState);
                    ls.getTurnoutB().addPropertyChangeListener(this, ls.getTurnoutBName(), "Layout Block Routing");
                } else {
                    LayoutTurnout lt = turnouts.get(i).getObject();
                    if (lt.getTurnout() != null) {
                        _turnouts.put(lt.getTurnout(), turnouts.get(i).getExpectedState());
                        lt.getTurnout().addPropertyChangeListener(this, lt.getTurnoutName(), "Layout Block Routing");
                    } else {
                        log.error("{} has no physical turnout allocated, block = {}", lt, block.getDisplayName());
                    }
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("KnownState")) {
                Turnout srcTurnout = (Turnout) e.getSource();
                int newVal = (Integer) e.getNewValue();
                int values = _turnouts.get(srcTurnout);
                boolean allset = false;
                pathActive = false;

                if (newVal == values) {
                    allset = true;

                    if (_turnouts.size() > 1) {
                        for (Map.Entry<Turnout, Integer> entry : _turnouts.entrySet()) {
                            if (srcTurnout != entry.getKey()) {
                                int state = entry.getKey().getState();
                                if (state != entry.getValue()) {
                                    allset = false;
                                    break;
                                }
                            }
                        }
                    }
                }
                updateActiveThroughPaths(this, allset);
                pathActive = allset;
            }
        }
    }

    List<Block> getThroughPathSourceByDestination(Block dest) {
        List<Block> a = new ArrayList<>();

        for (int i = 0; i < throughPaths.size(); i++) {
            if (throughPaths.get(i).getDestinationBlock() == dest) {
                a.add(throughPaths.get(i).getSourceBlock());
            }
        }
        return a;
    }

    List<Block> getThroughPathDestinationBySource(Block source) {
        List<Block> a = new ArrayList<>();

        for (int i = 0; i < throughPaths.size(); i++) {
            if (throughPaths.get(i).getSourceBlock() == source) {
                a.add(throughPaths.get(i).getDestinationBlock());
            }
        }
        return a;
    }

    /**
     * When a route is created, check to see if the through path that this
     * route relates to is active.
     */
    boolean checkIsRouteOnValidThroughPath(Routes r) {
        for (ThroughPaths t : throughPaths) {
            if (t.isPathActive()) {
                if (t.getDestinationBlock() == r.getNextBlock()) {
                    return true;
                }
                if (t.getSourceBlock() == r.getNextBlock()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Go through all the routes and refresh the valid flag.
     */
    public void refreshValidRoutes() {
        for (int i = 0; i < throughPaths.size(); i++) {
            ThroughPaths t = throughPaths.get(i);
            setRoutesValid(t.getDestinationBlock(), t.isPathActive());
            setRoutesValid(t.getSourceBlock(), t.isPathActive());
            firePropertyChange("path", null, i);
        }
    }

    //We keep a track of what is paths are active, only so that we can easily mark
    //which routes are also potentially valid
    List<ThroughPaths> activePaths;

    void updateActiveThroughPaths(ThroughPaths tp, boolean active) {
        if (enableUpdateRouteLogging) {
            log.info("We have been notified that a through path has changed state");
        }

        if (activePaths == null) {
            activePaths = new ArrayList<>();
        }

        if (active) {
            activePaths.add(tp);
            setRoutesValid(tp.getSourceBlock(), active);
            setRoutesValid(tp.getDestinationBlock(), active);
        } else {
            //We need to check if either our source or des is in use by another path.
            activePaths.remove(tp);
            boolean SourceInUse = false;
            boolean DestinationInUse = false;

            for (int i = 0; i < activePaths.size(); i++) {
                Block testSour = activePaths.get(i).getSourceBlock();
                Block testDest = activePaths.get(i).getDestinationBlock();
                if ((testSour == tp.getSourceBlock()) || (testDest == tp.getSourceBlock())) {
                    SourceInUse = true;
                }
                if ((testSour == tp.getDestinationBlock()) || (testDest == tp.getDestinationBlock())) {
                    DestinationInUse = true;
                }
            }

            if (!SourceInUse) {
                setRoutesValid(tp.getSourceBlock(), active);
            }

            if (!DestinationInUse) {
                setRoutesValid(tp.getDestinationBlock(), active);
            }
        }

        for (int i = 0; i < throughPaths.size(); i++) {
            //This is processed simply for the throughpath table.
            if (tp == throughPaths.get(i)) {
                firePropertyChange("path", null, i);
            }
        }
    }

    /**
     * Set the valid flag for routes that are on a valid through path.
     */
    void setRoutesValid(Block nxtHopActive, boolean state) {
        List<Routes> rtr = getRouteByNeighbour(nxtHopActive);
        rtr.forEach((rt) -> {
            rt.setValidCurrentRoute(state);
        });
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) {    //IN18N
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getOccupancySensor())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }

            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) {  //IN18N
            //Do nothing at this stage
            if (evt.getOldValue() instanceof Sensor) {
                if (evt.getOldValue().equals(getOccupancySensor())) {
                    setOccupancySensorName(null);
                }
            }

            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory())) {
                    setMemoryName(null);
                }
            }
        }
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameLayoutBlock");
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutBlock.class);

}
