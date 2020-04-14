package jmri.jmrix.openlcb.swing.monitor;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanPanelInterface;

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
    }

    CanSystemConnectionMemo memo;
    AliasMap aliasMap;
    MessageBuilder messageBuilder;
    OlcbInterface olcbInterface;
    JCheckBox nodeNameCheckBox = new JCheckBox();
    JCheckBox eventCheckBox = new JCheckBox();
    JCheckBox eventAllCheckBox = new JCheckBox();
    final String nodeNameCheck = this.getClass().getName() + ".NodeName";
    final String eventCheck = this.getClass().getName() + ".Event";
    final String eventAllCheck = this.getClass().getName() + ".EventAll";
    
    @Override
    public void initContext(Object context) {
        if (context instanceof CanSystemConnectionMemo) {
            initComponents((CanSystemConnectionMemo) context);
        }
    }

    @Override
    public void initComponents(CanSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addCanListener(this);

        aliasMap = memo.get(org.openlcb.can.AliasMap.class);
        messageBuilder = new MessageBuilder(aliasMap);
        olcbInterface = memo.get(OlcbInterface.class);

        setFixedWidthFont();
    }

    @Override
    public String getTitle() {
        return Bundle.getMessage("MonitorTitle");
    }

    @Override
    protected void init() {
    }

    @Override
    public void dispose() {
        memo.getTrafficController().removeCanListener(this);

        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        pm.setSimplePreferenceState(nodeNameCheck, nodeNameCheckBox.isSelected());
        pm.setSimplePreferenceState(eventCheck, eventCheckBox.isSelected());
        pm.setSimplePreferenceState(eventAllCheck, eventAllCheckBox.isSelected());

        super.dispose();
    }

    @Override
    protected void addCustomControlPanes(JPanel parent) {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);

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
        StringBuilder formatted = new StringBuilder("");
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
        java.util.List<Message> list = messageBuilder.processFrame(frame);
        return list;
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
                    String name = olcbInterface.getNodeStore().findNode(list.get(0).getSourceNodeID()).getSimpleNodeIdent().getUserName();
                    if (name != null && !name.equals("")) {
                        sb.append("\n  Src: ");
                        sb.append(name);
                    }
                }
                if ((eventCheckBox.isSelected() || eventAllCheckBox.isSelected()) && olcbInterface != null && msg instanceof EventMessage) {
                    EventID ev = ((EventMessage) msg).getEventID();
                    EventTable.EventTableEntry[] descr =
                            olcbInterface.getEventTable().getEventInfo(ev).getAllEntries();
                    if (descr.length > 0) {
                        sb.append("\n  Event: ");
                        sb.append(descr[0].getDescription());
                    }
                    if (eventAllCheckBox.isSelected()) {
                        for (int i = 1; i < descr.length; i++) {
                            sb.append("\n  Event: ");
                            sb.append(descr[i].getDescription());
                        }
                    }
                }
                formatted = sb.toString();
            }
        } else {
            // control type
            String alias = "0x" + Integer.toHexString(header & 0xFFF).toUpperCase();
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
