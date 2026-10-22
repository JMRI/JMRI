package jmri.jmrix.dccpp.swing.messages;

import java.awt.Frame;

import javax.annotation.CheckForNull;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.UserPreferencesManager;
import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.messages.configurexml.MessagesConfigurationXml;
import jmri.util.JmriJFrame;
import jmri.util.swing.*;

import org.jdom2.Element;

/** Window showing EXRAIL MESSAGE() broadcasts, one per DCC-EX connection. */
public class MessagesFrame extends JmriJFrame {

    private final DCCppSystemConnectionMemo _memo;
    private final MessagesPanel _messagesPanel;
    private JPopupMenu popup;

    private static String getElementName(DCCppSystemConnectionMemo memo) {
        return "dccex_messages_config" + "___" + memo.getSystemPrefix();
    }

    public MessagesFrame(DCCppSystemConnectionMemo memo) {
        // save window position but not size, include prefix in title for multiple connections
        super(false, true, Bundle.getMessage("MessagesFrameTitle") + " (" + memo.getSystemPrefix() + ")");
        _memo = memo;
        _messagesPanel = new MessagesPanel(this, false);
        _messagesPanel.setMemo(memo);
    }

    public DCCppSystemConnectionMemo getMemo() {
        return _memo;
    }

    public MessagesPanel getMessagesPanel() {
        return _messagesPanel;
    }

    /** Find the open Messages window for this connection, or null. */
    @CheckForNull
    public static MessagesFrame findFrame(DCCppSystemConnectionMemo memo) {
        for (MessagesFrame f : JmriJFrame.getFrameList(MessagesFrame.class)) {
            if (f.getMemo() == memo) {
                return f;
            }
        }
        return null;
    }

    /** Open (or bring forward) the Messages window; initialMessage is only added to a newly created window. */
    @InvokeOnGuiThread
    public static MessagesFrame showFrame(DCCppSystemConnectionMemo memo, @CheckForNull String initialMessage) {
        MessagesFrame frame = findFrame(memo);
        if (frame == null) {
            frame = new MessagesFrame(memo);
            frame.initComponents();
            if (initialMessage != null) {
                frame.getMessagesPanel().addMessage(initialMessage);
            }
            frame.setVisible(true);
        }
        frame.setExtendedState(Frame.NORMAL);
        frame.toFront();
        return frame;
    }

    /** Whether the window should open itself when a message arrives; true unless the user turned it off. */
    public static boolean isAutoOpen(DCCppSystemConnectionMemo memo) {
        MessagesFrame frame = findFrame(memo);
        if (frame != null) {
            return frame.getMessagesPanel().isAutoOpen();
        }
        UserPreferencesManager p = InstanceManager.getNullableDefault(UserPreferencesManager.class);
        if (p == null) {
            return true;
        }
        Element e = p.loadElement(getElementName(memo));
        if (e == null) {
            return true;
        }
        Element config = e.getChild("dccex_messages_config");
        String value = config != null ? config.getChildTextTrim("autoOpen") : null;
        return value == null || "true".equals(value);
    }

    @Override
    public void dispose() {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(p -> {
            Element parent = new Element(getElementName(_memo), UserPreferencesManager.GENERIC_NAMESPACE);
            parent.addContent(MessagesConfigurationXml.store(_messagesPanel));
            p.storeElement(parent);
        });
        _messagesPanel.dispose();
        super.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void initComponents() {
        super.initComponents();
        _messagesPanel.initComponents();
        add(_messagesPanel);

        popup = new JPopupMenu();
        JMenuItem editItem = new JMenuItem(Bundle.getMessage("EditMessages"));
        editItem.addActionListener(evt -> ConfigureMessages.editConfigureMessages(null, _messagesPanel));
        popup.add(editItem);
        JMenuItem clearItem = new JMenuItem(Bundle.getMessage("ClearMessages"));
        clearItem.addActionListener(evt -> _messagesPanel.clearMessages());
        popup.add(clearItem);

        JmriMouseListener popupListener = new JmriMouseAdapter() {
            @Override
            public void mousePressed(JmriMouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(JmriMouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(JmriMouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        };
        _messagesPanel.addMouseListener(JmriMouseListener.adapt(popupListener));

        _messagesPanel.setOnNewMessage(() -> {
            if (_messagesPanel.isRaiseOnMessage()) {
                setExtendedState(Frame.NORMAL);
                toFront();
            }
        });

        pack();

        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent(p -> {
            Element e = p.loadElement(getElementName(_memo));
            if (e != null) {
                try {
                    MessagesConfigurationXml.load(_messagesPanel, e, true);
                } catch (JmriConfigureXmlException ex) {
                    log.error("Unexpected exception during loading of settings", ex);
                }
            }
        });
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MessagesFrame.class);

}
