package jmri.jmrix.dccpp.swing.messages;

import java.awt.Color;
import java.awt.Dimension;

import javax.annotation.CheckForNull;

import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;

/** Settings for a DCC-EX Messages display, shared by the window and the panel icon. */
public interface MessagesConfiguration {

    void setMemo(DCCppSystemConnectionMemo memo);

    DCCppSystemConnectionMemo getMemo();

    boolean isMemoEditable();

    void setExpirySeconds(int seconds);

    int getExpirySeconds();

    void setMaxLines(int lines);

    int getMaxLines();

    void setRaiseOnMessage(boolean raise);

    boolean isRaiseOnMessage();

    void setAutoOpen(boolean autoOpen);

    boolean isAutoOpen();

    void setPlaySound(boolean playSound);

    boolean isPlaySound();

    void setSoundFile(String path);

    String getSoundFile();

    void setMessageForeground(Color color);

    Color getMessageForeground();

    void setMessageBackground(Color color);

    Color getMessageBackground();

    /** Fixed size as columns (width) and lines (height), or null to size to fit. */
    void setMessagesSize(@CheckForNull Dimension size);

    @CheckForNull
    Dimension getMessagesSize();

}
