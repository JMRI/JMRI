package jmri.jmrix.ieee802154.xbee.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public abstract class XBeeSystemConnectionAction extends AbstractSystemConnectionAction<XBeeConnectionMemo> {

    public XBeeSystemConnectionAction(String name, XBeeConnectionMemo memo) {
        super(name, memo);
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(XBeeConnectionMemo.class));
    }
    
}
