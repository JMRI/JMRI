package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.OlcbInterface;
import org.openlcb.NodeID;
import org.openlcb.cdi.CdiRep;
import org.openlcb.cdi.impl.ConfigRepresentation;

/**
 * Action to download the CV values from a TCS CS-105
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2023
 * @author Dave Heap Copyright (C) 2015
 */
public class TcsDownloadAction extends AbstractAction implements PropertyChangeListener {

    public TcsDownloadAction(String actionName, CvTableModel pModel, PaneProgFrame pParent, JLabel pStatus, RosterEntry re) {
        super(actionName);
        this.cvTable = pModel;
        this.rosterEntry = re;
        this.frame = pParent;
    }

    // will this be enabled if created?
    static public boolean willBeEnabled() {
        // see if there's an openlcb connection
        var cscm = getSystemConnectionMemo();
        if (cscm == null) {
            return false;
        }
        if (cscm.get(org.openlcb.NodeID.class) == null) {
            return false;
        }
        return true;
    }

    static CanSystemConnectionMemo getSystemConnectionMemo() {
        return jmri.InstanceManager.getNullableDefault(CanSystemConnectionMemo.class);
    }

    PaneProgFrame frame;
    RosterEntry rosterEntry;
    CvTableModel cvTable;
    ConfigRepresentation configRep;

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isLong = cvTable.holdsLongAddress();
        int addr = cvTable.holdsAddress();
        log.debug("computed address is {} long: {}", addr, isLong);

        // Create the train's node ID from current values in GUI
        byte upperAddressByte = (byte) (isLong ? (192+(addr>>8)) : 0);
        byte lowerAddressByte = (byte) (addr & 0xFF);
        var nodeID = new NodeID(new byte[]{6,1,0,0,upperAddressByte, lowerAddressByte});
        log.debug("node ID {}", nodeID);

        // check for Node ID already known
        var nodeStore = getSystemConnectionMemo().get(org.openlcb.MimicNodeStore.class);
        var nodeMemo = nodeStore.findNode(nodeID);
        if (nodeMemo == null) {
            JOptionPane.showMessageDialog(frame, "Entry "+addr+" not found in CS-105, canceling");
            return;
        }

        // get the CD/CDI information
        configRep = new ConfigRepresentation(getSystemConnectionMemo().get(OlcbInterface.class),nodeID);
        configRep.addPropertyChangeListener(this);

    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case ConfigRepresentation.UPDATE_STATE :
                // Normal. Indicates that the load is proceeding.
            case ConfigRepresentation.UPDATE_REP :
                // Normal, CDI is read in, loading caches next
                return;
            case ConfigRepresentation.UPDATE_CACHE_COMPLETE :
                log.debug("CDI read done");

                // look for values
                processValuesToGUI();
                return;
            default:
                log.error("Unexpected PropertyChangeEvent {}", event);
                return;
        }
    }

    /**
     * Construct and execute a listener that processses
     * the relevant CDI elements into RosterEntry.
     *
     * Intended for a bulk importer eventually.
     *
     * TODO: Should save the RosterEntry at end? Doesn't.
     * TODO: Code further up takes initial address from GUI, not RosterEntry.
     */
    void processValuesToRosterElement() {
        configRep.visit(new ConfigRepresentation.Visitor() {
            @Override
            public void visitString(ConfigRepresentation.StringEntry e) {
                log.trace("String entry {} is {}", e.key, e.getValue());

                if (e.key.startsWith("Train.User Description")) {
                    log.info("setComment {}", e.getValue());
                    rosterEntry.setComment(e.getValue());
                 } else if (e.key.startsWith("Train.Functions")) {
                    int index = getNumberField(e.key);
                    if (index == -1) {
                        log.warn("Unexpected format \"{}\"", e.key);
                        return;
                    }
                    if (e.key.endsWith("Description")) {
                        String value = e.getValue();
                        if (value==null) {
                            value = "";
                        }
                        // Display has already written contents
                        // to this.  If content here is empty, we defer to that;
                        // if there are content here, overrides what Display wrote.
                        if (!value.isEmpty()) {
                            rosterEntry.setFunctionLabel(index+1, value);
                            log.trace("Description sets {} {} {}", index, e.getValue(), value);
                        }
                    } else {
                        log.warn("Unexpected content \"{}\"", e.key);
                    }
                }
            }

            // TODO: Have to update the Function Pane contents on every change
            //       so that the data is present for viewing and saving
            @Override
            public void visitInt(ConfigRepresentation.IntegerEntry e) {
                log.trace("Integer entry {} is {}", e.key, e.getValue());

                // is this the last entry?
                if (e.key.startsWith("Train.Delete From Roster")) {
                    // TODO: This is firing much too soon
                    JOptionPane.showMessageDialog(frame, "Download complete.");
                } else if (e.key.startsWith("Train.Functions")) {
                    int index = getNumberField(e.key);
                    if (index == -1) {
                        log.warn("Unexpected format \"{}\"", e.key);
                        return;
                    }
                    if (e.key.endsWith(".Momentary")) {
                        boolean lockable = (e.getValue() == 0);
                        rosterEntry.setFunctionLockable(index+1, lockable);
                    } else if (e.key.endsWith(".Consist Behavior")) {
                        // TODO: set value in consisting CVs
                    } else if (e.key.endsWith(".Display")) {
                        // do a reverse lookup and store every time,
                        // will be overwritten by Description if needed
                        var description = TcsImporter.unpackDescription("", ""+e.getValue());
                        log.trace("Display sets {} {} {}", index, e.getValue(), description);
                        rosterEntry.setFunctionLabel(index+1, description);
                    } else {
                        log.warn("Unexpected content \"{}\"", e.key);
                    }
                }
            }

            @Override
            public void visitEvent(ConfigRepresentation.EventEntry e) {
                log.trace("Event entry {} is {}", e.key, e.getValue());
            }
        });
    }

    /**
     * Construct and execute a listener that processses
     * the relevant CDI elements into the Roster and Function Label
     * GUI elements.
     */
    void processValuesToGUI() {
        configRep.visit(new ConfigRepresentation.Visitor() {
            @Override
            public void visitString(ConfigRepresentation.StringEntry e) {
                log.trace("String entry {} is {}", e.key, e.getValue());

                if (e.key.startsWith("Train.User Description")) {
                    log.info("setComment {}", e.getValue());
                    frame.getRosterPane().setComment(e.getValue());
                 } else if (e.key.startsWith("Train.Functions")) {
                    int index = getNumberField(e.key);
                    if (index == -1) {
                        log.warn("Unexpected format \"{}\"", e.key);
                        return;
                    }
                    if (e.key.endsWith("Description")) {
                        String value = e.getValue();
                        if (value==null) {
                            value = "";
                        }
                        // Display has already written contents
                        // to this.  If content here is empty, we defer to that;
                        // if there are content here, overrides what Display wrote.
                        if (!value.isEmpty()) {
                            frame.getFnLabelPane().getLabel(index+1).setText(value);
                            log.trace("Description sets {} {} {}", index, e.getValue(), value);
                        }
                    } else {
                        log.warn("Unexpected content \"{}\"", e.key);
                    }
                }
            }

            // TODO: Have to update the Function Pane contents on every change
            //       so that the data is present for viewing and saving
            @Override
            public void visitInt(ConfigRepresentation.IntegerEntry e) {
                log.trace("Integer entry {} is {}", e.key, e.getValue());

                // is this the last entry?
                if (e.key.startsWith("Train.Delete From Roster")) {
                    // TODO: This is firing much too soon
                    JOptionPane.showMessageDialog(frame, "Download complete.");
                } else if (e.key.startsWith("Train.Functions")) {
                    int index = getNumberField(e.key);
                    if (index == -1) {
                        log.warn("Unexpected format \"{}\"", e.key);
                        return;
                    }
                    if (e.key.endsWith(".Momentary")) {
                        boolean lockable = (e.getValue() == 0);
                        frame.getFnLabelPane().getLockable(index+1).setSelected(lockable);
                    } else if (e.key.endsWith(".Consist Behavior")) {
                        // TODO: set value in consisting CVs
                    } else if (e.key.endsWith(".Display")) {
                        // do a reverse lookup and store every time,
                        // will be overwritten by Description if needed
                        var description = TcsImporter.unpackDescription("", ""+e.getValue());
                        log.trace("Display sets {} {} {}", index, e.getValue(), description);
                        frame.getFnLabelPane().getLabel(index+1).setText(description);
                    } else {
                        log.warn("Unexpected content \"{}\"", e.key);
                    }
                }
            }

            @Override
            public void visitEvent(ConfigRepresentation.EventEntry e) {
                log.trace("Event entry {} is {}", e.key, e.getValue());
            }
        });
    }

    // Extract the number from e.g. Train.Functions(25).Momentary
    static int getNumberField(String value) {
        int first = value.indexOf("(");
        int last = value.indexOf(")");
        if (first > 0 && last > 0 && last > first + 1) {
            var digits = value.substring(first+1, last);
            return Integer.parseInt(digits);
        }
        return -1;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TcsDownloadAction.class);

}
