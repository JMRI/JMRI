package jmri.jmrit.symbolicprog;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.symbolicprog.tabbedframe.PaneProgFrame;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.swing.JmriJOptionPane;

import org.openlcb.OlcbInterface;
import org.openlcb.NodeID;
import org.openlcb.cdi.impl.ConfigRepresentation;
/**
 * Action to upload the function labels from a roster entry to a TCS CS-105.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class TcsUploadAction extends AbstractAction implements PropertyChangeListener {

    public TcsUploadAction(String actionName, CvTableModel pModel, VariableTableModel vModel, RosterEntry rosterEntry, PaneProgFrame pParent) {
        super(actionName);
        cvTable = pModel;
        this.vModel = vModel;
        frame = pParent;
        this.rosterEntry = rosterEntry;
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
    VariableTableModel vModel;
    ConfigRepresentation configRep;

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean isLong = cvTable.holdsLongAddress();
        int addr = cvTable.holdsAddress();
        log.debug("computed address is {} long: {}", addr, isLong);

        // Create the train's node ID
        byte upperAddressByte = (byte) (isLong ? (192+(addr>>8)) : 0);
        byte lowerAddressByte = (byte) (addr & 0xFF);
        var nodeID = new NodeID(new byte[]{6,1,0,0,upperAddressByte, lowerAddressByte});
        log.debug("node ID {}", nodeID);

        // check for Node ID already known
        var nodeStore = getSystemConnectionMemo().get(org.openlcb.MimicNodeStore.class);
        var nodeMemo = nodeStore.findNode(nodeID);
        if (nodeMemo == null) {
            JmriJOptionPane.showMessageDialog(frame, "Entry "+addr+" not found in CS-105, canceling");
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
                processValuesFromGUI();
                return;
            default:
                log.error("Unexpected PropertyChangeEvent {}", event);
                return;
        }
    }

    /**
     * Construct and execute a listener that sets
     * the appropriate values from the GUI elements.
     */
    void processValuesFromGUI() {
        log.trace("processValuesFromGUI");
        configRep.visit(new ConfigRepresentation.Visitor() {
            @Override
            public void visitString(ConfigRepresentation.StringEntry e) {
                log.trace("String entry {} is {}", e.key, e.getValue());

                if (e.key.startsWith("Train.User Description")) {
                    e.setValue(frame.getRosterPane().getComment());
                 } else if (e.key.startsWith("Train.Functions")) {
                    int index = getNumberField(e.key);
                    if (index == -1) {
                        log.warn("Unexpected format \"{}\"", e.key);
                        return;
                    }
                    if (e.key.endsWith("Description")) {
                        String value = frame.getFnLabelPane().getLabel(index+1).getText();
                        if (value==null) {
                            value = "";
                        }
                        log.debug(".Description found function {} roster description \"{}\"", index, value);
                        log.trace("   mapping gives {}", TcsExportAction.intFromFunctionString(
                                        rosterEntry.getFunctionLabel(index+1)) );
                        if (TcsExportAction.intFromFunctionString(
                                        frame.getFnLabelPane().getLabel(index+1).getText()
                                    ) == 0) {
                            e.setValue(value);
                        }
                    } else {
                        log.warn("Unexpected content \"{}\"", e.key);
                    }
                }
            }

            @Override
            public void visitInt(ConfigRepresentation.IntegerEntry e) {
                log.trace("Integer entry {} is {}", e.key, e.getValue());

                // is this the last entry?
                if (e.key.startsWith("Train.Delete From Roster")) {
                    // TODO: This is firing much too soon
                    JmriJOptionPane.showMessageDialog(frame, "Upload complete.");
                } else if (e.key.startsWith("Train.Functions")) {
                    int index = getNumberField(e.key);
                    if (index == -1) {
                        log.warn("Unexpected format \"{}\"", e.key);
                        return;
                    }
                    if (e.key.endsWith(".Momentary")) {
                        long value = 1;
                        if (frame.getFnLabelPane().getLockable(index+1).isSelected()) {
                            value = 0;  // lockable is not Momentary
                        }
                        e.setValue(value);
                    } else if (e.key.endsWith(".Consist Behavior")) {

                        // process consist bit
                        // first, see if function variable exists
                        var variable = vModel.findVar("Consist Address Active For F"+(index+1));
                        if (variable != null) {
                            // it exists, so we transfer that to the consist info
                            int value = variable.getIntValue();
                            e.setValue(value);
                        } else {
                            log.debug("Variable {} not found", "Consist Address Active For F"+(index+1) );
                        }

                    } else if (e.key.endsWith(".Display")) {
                        // do a reverse lookup and store
                        int value = TcsExportAction.intFromFunctionString(
                                        frame.getFnLabelPane().getLabel(index+1).getText()
                                    );
                        e.setValue(value);
                        log.debug(".display found function {} roster description \"{}\"", index, value);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TcsUploadAction.class);
}
