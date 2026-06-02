package jmri.jmrix.dccpp.swing.virtuallcd;

// import java.awt.event.ActionListener;
import java.util.List;

import javax.annotation.Nonnull;

// import jmri.jmrit.catalog.NamedIcon;

import jmri.jmrit.display.*;
// import static jmri.jmrit.display.Editor.CLOCK;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JmriJFrame;

import org.openide.util.lookup.ServiceProvider;

/**
 * A factory for a VirtualLCD that can be put on a panel.
 *
 * @author Daniel Bergqvist (C) 2026
 */
@ServiceProvider(service = PositionableFactory.class)
public class VirtualLcdPositionableFactory implements PositionableFactory {

    private JmriJFrame addPositionableFrame = null;


    public VirtualLcdPositionableFactory() {
    }

    @Nonnull
    @Override
    public String getIdentifier() {
        return "DCC-EX-VirtualDisplay";
    }

    @Nonnull
    @Override
    public String getDescription() {
        return Bundle.getMessage("VirtualLCDFrameTitle");
    }

    @Override
    public boolean isEnabled() {
        return hasDccEx();
    }

    /**
     * Do we have a LocoNet connection?
     * @return true if we have LocoNet, false otherwise
     */
    public static boolean hasDccEx() {
        List<DCCppSystemConnectionMemo> list = jmri.InstanceManager.getList(DCCppSystemConnectionMemo.class);

        // We have at least one DCC-EX connection if the list is not empty
        return !list.isEmpty();
    }

    @Override
    public void addPositionable(@Nonnull Editor editor, DoAfter doAfter) {
        ConfigureVirtualLCD.createConfigureVirtualLCD(editor, doAfter);
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(VirtualLcdPositionableFactory.class);
}
