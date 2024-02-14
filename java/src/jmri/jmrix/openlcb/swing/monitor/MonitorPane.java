package jmri.jmrix.openlcb.swing.monitor;

import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanPanelInterface;
import jmri.jmrix.openlcb.OlcbConstants;

import org.openlcb.EventID;
import org.openlcb.EventMessage;
import org.openlcb.Message;
import org.openlcb.OlcbInterface;
import org.openlcb.can.AliasMap;
import org.openlcb.can.MessageBuilder;
import org.openlcb.can.OpenLcbCanFrame;
import org.openlcb.implementations.EventTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * Frame displaying (and logging) OpenLCB (CAN) frames
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2010
 */
public class MonitorPane extends jmri.jmrix.AbstractMonPane implements CanListener, CanPanelInterface {

    public MonitorPane() {
        super();
        pm = InstanceManager.getDefault(UserPreferencesManager.class);
        tagManager = InstanceManager.getDefault(IdTagManager.class);
    }

    CanSystemConnectionMemo memo;
    AliasMap aliasMap;
    MessageBuilder messageBuilder;
    OlcbInterface olcbInterface;

    IdTagManager tagManager;

    /** show source node name on a separate line when available */
    final JCheckBox nodeNameCheckBox = new JCheckBox();

    /** Show the first EventID in the message on a separate line */
    final JCheckBox eventCheckBox = new JCheckBox();

    /** Show all EventIDs in the message each on a separate line */
    final JCheckBox eventAllCheckBox = new JCheckBox();

    /* Preferences setup */
    final String nodeNameCheck = this.getClass().getName() + ".NodeName";
    final String eventCheck = this.getClass().getName() + ".Event";
    final String eventAllCheck = this.getClass().getName() + ".EventAll";
    private final UserPreferencesManager pm;

    @Override
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanConsoleListener(this);

        aliasMap = memo.get(org.openlcb.can.AliasMap.class);
        messageBuilder = new MessageBuilder(aliasMap);
        olcbInterface = memo.get(OlcbInterface.class);

        setFixedWidthFont();
    }

    @Override
    public String getTitle() {
        if (memo != null) {
            return (memo.getUserName() + " Monitor");
        }
        return Bundle.getMessage("MonitorTitle");
    }

    @Override
    protected void init() {
    }

    @Override
    public void dispose() {
        try {
            memo.getTrafficController().removeCanListener(this);
        } catch(NullPointerException npe){
            log.debug("Null Pointer Exception while attempting to remove Can Listener",npe);
        }

        pm.setSimplePreferenceState(nodeNameCheck, nodeNameCheckBox.isSelected());
        pm.setSimplePreferenceState(eventCheck, eventCheckBox.isSelected());
        pm.setSimplePreferenceState(eventAllCheck, eventAllCheckBox.isSelected());

        super.dispose();
    }

    @Override
    protected void addCustomControlPanes(JPanel parent) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

        nodeNameCheckBox.setText(Bundle.getMessage("CheckBoxShowNodeName"));
        nodeNameCheckBox.setVisible(true);
        nodeNameCheckBox.setSelected(pm.getSimplePreferenceState(nodeNameCheck));
        p.add(nodeNameCheckBox);

        eventCheckBox.setText(Bundle.getMessage("CheckBoxShowEvent"));
        eventCheckBox.setVisible(true);
        eventCheckBox.setSelected(pm.getSimplePreferenceState(eventCheck));
        p.add(eventCheckBox);

        eventAllCheckBox.setText(Bundle.getMessage("CheckBoxShowEventAll"));
        eventAllCheckBox.setVisible(true);
        eventAllCheckBox.setSelected(pm.getSimplePreferenceState(eventAllCheck));
        p.add(eventAllCheckBox);

        parent.add(p);
        super.addCustomControlPanes(parent);
    }

    String formatFrame(boolean extended, int header, int len, int[] content) {
        StringBuilder formatted = new StringBuilder();
        formatted.append(extended ? "[" : "(");
        formatted.append(Integer.toHexString(header));
        formatted.append((extended ? "]" : ")"));
        for (int i = 0; i < len; i++) {
            formatted.append(" ");
            formatted.append(jmri.util.StringUtil.twoHexFromInt(content[i]));
        }
        for (int i = len; i < 8; i++) {
            formatted.append("   ");
        }
        return new String(formatted);
    }

    // see jmri.jmrix.openlcb.OlcbConfigurationManager
    java.util.List<Message> frameToMessages(int header, int len, int[] content) {
        OpenLcbCanFrame frame = new OpenLcbCanFrame(header & 0xFFF);
        frame.setHeader(header);
        if (len != 0) {
            byte[] data = new byte[len];
            for (int i = 0; i < data.length; i++) {
                data[i] = (byte) content[i];
            }
            frame.setData(data);
        }

        aliasMap.processFrame(frame);
        return messageBuilder.processFrame(frame);
    }

    void format(String prefix, boolean extended, int header, int len, int[] content) {
        String raw = formatFrame(extended, header, len, content);
        String formatted;
        if (extended && (header & 0x08000000) != 0) {
            // is a message type
            java.util.List<Message> list = frameToMessages(header, len, content);
            if (list == null || list.isEmpty()) {
                // didn't format, check for partial datagram
                if ((header & 0x0F000000) == 0x0B000000) {
                    formatted = prefix + ": (Start of Datagram)";
                } else if ((header & 0x0F000000) == 0x0C000000) {
                    formatted = prefix + ": (Middle of Datagram)";
                } else if (((header & 0x0FFFF000) == 0x09A08000) && (content.length > 0)) {
                    // SNIP multi frame reply
                    switch (content[0] & 0xF0) {
                        case 0x10:
                            formatted = prefix + ": SNIP Reply 1st frame";
                            break;
                        case 0x20:
                            formatted = prefix + ": SNIP Reply last frame";
                            break;
                        case 0x30:
                            formatted = prefix + ": SNIP Reply middle frame";
                            break;
                        default:
                            formatted = prefix + ": SNIP Reply unknown";
                            break;
                    }
                } else if (((header & 0x0FFFF000) == 0x095EB000) && (content.length > 0)) {
                    // Traction Control Command multi frame reply
                    switch (content[0] & 0xF0) {
                        case 0x10:
                            formatted = prefix + ": Traction Control Command 1st frame";
                            break;
                        case 0x20:
                            formatted = prefix + ": Traction Control Command last frame";
                            break;
                        case 0x30:
                            formatted = prefix + ": Traction Control Command middle frame";
                            break;
                        default:
                            formatted = prefix + ": Traction Control Command unknown";
                            break;
                    }
                } else if (((header & 0x0FFFF000) == 0x091E9000) && (content.length > 0)) {
                    // Traction Control Reply multi frame reply
                    switch (content[0] & 0xF0) {
                        case 0x10:
                            formatted = prefix + ": Traction Control Reply 1st frame";
                            break;
                        case 0x20:
                            formatted = prefix + ": Traction Control Reply last frame";
                            break;
                        case 0x30:
                            formatted = prefix + ": Traction Control Reply middle frame";
                            break;
                        default:
                            formatted = prefix + ": Traction Control Reply unknown";
                            break;
                    }
                } else {
                    formatted = prefix + ": Unknown message " + raw;
                }
            } else {
                Message msg = list.get(0);
                StringBuilder sb = new StringBuilder();
                sb.append(prefix);
                sb.append(": ");
                sb.append(list.get(0).toString());
                if (nodeNameCheckBox.isSelected() && olcbInterface != null) {
                    var ptr = olcbInterface.getNodeStore().findNode(list.get(0).getSourceNodeID());
                    if (ptr != null && ptr.getSimpleNodeIdent() != null) {
                        String name = "";
                        var ident = ptr.getSimpleNodeIdent();
                        if (ident != null) {
                            name = ident.getUserName();
                            if (name.isEmpty()) {
                                name = ident.getMfgName()+" - "+ident.getModelName();
                            }
                        }
                        if (!name.isBlank()) {
                            sb.append("\n  Src: ");
                            sb.append(name);
                        }
                    }
                }
                if ((eventCheckBox.isSelected() || eventAllCheckBox.isSelected()) && olcbInterface != null && msg instanceof EventMessage) {
                    EventID ev = ((EventMessage) msg).getEventID();
                    log.debug("event message with event {}", ev);
                    EventTable.EventTableEntry[] descr =
                            olcbInterface.getEventTable().getEventInfo(ev).getAllEntries();
                    if (descr.length > 0) {
                        sb.append("\n  Event: ");
                        var tag = tagManager.getIdTag(OlcbConstants.tagPrefix+ev.toShortString());
                        String name;
                        if (tag != null
                                && (name = tag.getUserName()) != null) {
                            if (! name.isEmpty()) {
                                sb.append(name);
                                sb.append("\n         ");
                            }
                        }
                        sb.append(descr[0].getDescription());

                        if (eventAllCheckBox.isSelected()) {
                            for (int i = 1; i < descr.length; i++) {  // entry 0 done above, so skipped here
                                sb.append("\n         ");
                                sb.append(descr[i].getDescription());
                            }
                        }
                    } else {
                        var tag = tagManager.getIdTag(OlcbConstants.tagPrefix+ev.toShortString());
                        String name;
                        if (tag != null
                                && (name = tag.getUserName()) != null) {
                            if (! name.isEmpty()) {
                                sb.append("\n  Event: ");
                                sb.append(name);
                            }
                        } else {
                            if ((content[0] == 1) && (content[1] == 1) && (content[2] == 0) && (content[3] == 0) && (content[4] == 1)) {
                                sb.append("\n  Event: ");
                                sb.append(formatTimeMessage(content));
                            }
                        }
                    }
                }
                formatted = sb.toString();
            }
        } else {
            // control type
            String alias = String.format("0x%03X", header & 0xFFF);
            if ((header & 0x07000000) == 0x00000000) {
                int[] data = new int[len];
                System.arraycopy(content, 0, data, 0, len);
                switch (header & 0x00FFF000) {
                    case 0x00700000:
                        formatted = prefix + ": Alias " + alias + " RID frame";
                        break;
                    case 0x00701000:
                        formatted = prefix + ": Alias " + alias + " AMD frame for node " + org.openlcb.Utilities.toHexDotsString(data);
                        break;
                    case 0x00702000:
                        formatted = prefix + ": Alias " + alias + " AME frame for node " + org.openlcb.Utilities.toHexDotsString(data);
                        break;
                    case 0x00703000:
                        formatted = prefix + ": Alias " + alias + " AMR frame for node " + org.openlcb.Utilities.toHexDotsString(data);
                        break;
                    default:
                        formatted = prefix + ": Unknown CAN control frame: " + raw;
                        break;
                }
            } else {
                formatted = prefix + ": Alias " + alias + " CID " + ((header & 0x7000000) / 0x1000000) + " frame";
            }
        }
        nextLine(formatted + "\n", raw);
    }
    
    /*
     * format a time message
     */
    String formatTimeMessage(int[] content) {
        StringBuilder sb = new StringBuilder();
        int clock = content[5];
        switch (clock) {
            case 0:
                sb.append(Bundle.getMessage("TimeClockDefault"));
                break;
            case 1:
                sb.append(Bundle.getMessage("TimeClockReal"));
                break;
            case 2:
                sb.append(Bundle.getMessage("TimeClockAlt1"));
                break;
            case 3:
                sb.append(Bundle.getMessage("TimeClockAlt2"));
                break;
            default:
                sb.append(Bundle.getMessage("TimeClockUnkClock"));
                sb.append(' ');
                sb.append(jmri.util.StringUtil.twoHexFromInt(clock));
                break;
        }
        sb.append(' ');
        int msgType = (0xF0 & content[6]) >> 4;
        int nib = (0x0F & content[6]);
        int hour = (content[6] & 0x1F);
        switch (msgType) {
            case 0:
            case 1:
                sb.append(Bundle.getMessage("TimeClockTimeMsg") + " ");
                sb.append(hour);
                sb.append(':');
                if (content[7] < 10) {
                    sb.append("0");
                    sb.append(content[7]);
                } else {
                    sb.append(content[7]);
                }
                break;
            case 2:     // month day
                sb.append(Bundle.getMessage("TimeClockDateMsg") + " ");
                if (nib < 10) {
                    sb.append('0');
                }
                sb.append(nib);
                sb.append('/');
                if (content[7] < 10) {
                    sb.append('0');
                }
                sb.append(content[7]);
                break;
            case 3:     // year
                sb.append(Bundle.getMessage("TimeClockYearMsg") + " ");
                sb.append(nib << 8 | content[7]);
                break;
            case 4:     // rate
                sb.append(Bundle.getMessage("TimeClockRateMsg") + " ");
                sb.append(' ');
                sb.append(cvtFastClockRate(content[6], content[7]));
                break;
            case 8:
            case 9:
                sb.append(Bundle.getMessage("TimeClockSetTimeMsg") + " ");
                sb.append(hour);
                sb.append(':');
                if (content[7] < 10) {
                    sb.append("0");
                    sb.append(content[7]);
                } else {
                    sb.append(content[7]);
                }
                break;
            case 0xA:  // set date
                sb.append(Bundle.getMessage("TimeClockSetDateMsg") + " ");
                if (nib < 10) {
                    sb.append('0');
                }
                sb.append(nib);
                sb.append('/');
                if (content[7] < 10) {
                    sb.append('0');
                }
                sb.append(content[7]);
                break;
            case 0xB:  // set year
                sb.append(Bundle.getMessage("TimeClockSetYearMsg") + " ");
                sb.append(nib << 8 | content[7]);
                break;
            case 0xC:  // set rate
                sb.append(Bundle.getMessage("TimeClockSetRateMsg") + " ");
                sb.append(cvtFastClockRate(content[6], content[7]));
                break;
            case 0xF:   // specials
                if (nib == 0 && content[7] ==0) {
                    sb.append(Bundle.getMessage("TimeClockQueryMsg"));
                } else if (nib == 0 && content[7] == 1) {
                    sb.append(Bundle.getMessage("TimeClockStopMsg"));
                } else if (nib == 0 && content[7] == 2) {
                    sb.append(Bundle.getMessage("TimeClockStartMsg"));
                } else if (nib == 0 && content[7] == 3) {
                    sb.append(Bundle.getMessage("TimeClockDateRollMsg"));
                } else {
                    sb.append(Bundle.getMessage("TimeClockUnkData"));
                    sb.append(' ');
                    sb.append(jmri.util.StringUtil.twoHexFromInt(content[6]));
                    sb.append(' ');
                    sb.append(jmri.util.StringUtil.twoHexFromInt(content[7]));
                }
                break;
            default:
                sb.append(Bundle.getMessage("TimeClockUnkData"));
                sb.append(' ');
                sb.append(jmri.util.StringUtil.twoHexFromInt(content[6]));
                sb.append(' ');
                sb.append(jmri.util.StringUtil.twoHexFromInt(content[7]));
                break;
        }
        return(sb.toString());
    }

    /*
     * Convert the 12 bit signed, fixed format rate value
     * That's 11 data and 1 sign bit
     * Values are increments of 0.25, between 511.75 and -512.00
     */
    private float cvtFastClockRate(int byte6, int byte7) {
        int data = 0;
        boolean sign = false;
        float rate = 0;
        
        data = ((byte6 & 0x3) << 8 | byte7);
        sign = (((byte6 & 0x4) >> 3) == 0) ? false : true;
        if (sign) {
            rate = (float) (data / 4.0);
        } else {
            rate = (float) ((-1 * (~data + 1)) /4.0);
        }
        return rate;
    }

    @Override
    public synchronized void message(CanMessage l) {  // receive a message and log it
        log.debug("Message: {}", l);
        format("S", l.isExtended(), l.getHeader(), l.getNumDataElements(), l.getData());
    }

    @Override
    public synchronized void reply(CanReply l) {  // receive a reply and log it
        log.debug("Reply: {}", l);
        format("R", l.isExtended(), l.getHeader(), l.getNumDataElements(), l.getData());
    }

    private final static Logger log = LoggerFactory.getLogger(MonitorPane.class);

}
