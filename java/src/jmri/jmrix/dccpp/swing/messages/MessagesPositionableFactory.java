package jmri.jmrix.dccpp.swing.messages;

import javax.annotation.Nonnull;

import jmri.jmrit.display.*;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;

import org.openide.util.lookup.ServiceProvider;

/** A factory for a DCC-EX Messages display that can be put on a panel. */
@ServiceProvider(service = PositionableFactory.class)
public class MessagesPositionableFactory implements PositionableFactory {

    public MessagesPositionableFactory() {
    }

    @Nonnull
    @Override
    public String getIdentifier() {
        return "DCC-EX-Messages";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return Bundle.getMessage("MessagesFrameTitle");
    }

    /** Only offered when there's at least one DCC-EX connection. */
    @Override
    public boolean isEnabled() {
        return !jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class).isEmpty();
    }

    @Override
    public void addPositionable(@Nonnull Editor editor, DoAfter doAfter) {
        ConfigureMessages.createConfigureMessages(editor, doAfter);
    }

}
