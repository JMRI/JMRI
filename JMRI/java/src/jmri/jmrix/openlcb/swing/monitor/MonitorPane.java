package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.swing.CanPanelInterface;
import org.openlcb.Message;
import org.openlcb.can.AliasMap;
import org.openlcb.can.MessageBuilder;
import org.openlcb.can.OpenLcbCanFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        super.dispose();
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
                formatted = prefix + ": " + list.get(0).toString();
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
