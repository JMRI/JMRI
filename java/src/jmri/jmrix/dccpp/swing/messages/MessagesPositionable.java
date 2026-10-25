package jmri.jmrix.dccpp.swing.messages;

import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import jmri.jmrit.display.*;

/** A DCC-EX Messages display that can be put on a panel. */
public class MessagesPositionable extends PositionableJComponent {

    private final MessagesPanel messagesPanel;

    public MessagesPositionable(Editor editor) {
        super(editor);
        messagesPanel = new MessagesPanel(editor, this, true);
    }

    public void initComponents() {
        messagesPanel.initComponents();
        add(messagesPanel);
        var d = messagesPanel.getPreferredSize();
        messagesPanel.setSize(d);
        setSize(d);
    }

    @Override
    public Positionable deepClone() {
        MessagesPositionable pos = new MessagesPositionable(_editor);
        if (messagesPanel.getMemo() != null) {
            pos.messagesPanel.setMemo(messagesPanel.getMemo());
        }
        pos.messagesPanel.setExpirySeconds(messagesPanel.getExpirySeconds());
        pos.messagesPanel.setMaxLines(messagesPanel.getMaxLines());
        pos.messagesPanel.setAutoOpen(messagesPanel.isAutoOpen());
        pos.messagesPanel.setPlaySound(messagesPanel.isPlaySound());
        pos.messagesPanel.setSoundFile(messagesPanel.getSoundFile());
        pos.messagesPanel.setMessageForeground(messagesPanel.getMessageForeground());
        pos.messagesPanel.setMessageBackground(messagesPanel.getMessageBackground());
        pos.messagesPanel.setMessagesSize(messagesPanel.getMessagesSize());
        pos.initComponents();
        return finishClone(pos);
    }

    public MessagesPanel getMessagesPanel() {
        return messagesPanel;
    }

    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            popup.add(new AbstractAction(Bundle.getMessage("EditMessages")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ConfigureMessages.editConfigureMessages(getEditor(), messagesPanel);
                }
            });
        }
        return true;
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_Messages");
    }

    @Override
    public String getNameString() {
        return messagesPanel.getNameString();
    }

    @Override
    public void remove() {
        super.remove();
        messagesPanel.dispose();
    }

    public void dispose() {
        messagesPanel.dispose();
    }

}
