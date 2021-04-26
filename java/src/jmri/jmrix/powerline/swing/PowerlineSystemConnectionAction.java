package jmri.jmrix.powerline.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public abstract class PowerlineSystemConnectionAction extends AbstractSystemConnectionAction<SerialSystemConnectionMemo> {

    public PowerlineSystemConnectionAction(String name, SerialSystemConnectionMemo memo) {
        super(name, memo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(SerialSystemConnectionMemo.class));
    }
    
}
