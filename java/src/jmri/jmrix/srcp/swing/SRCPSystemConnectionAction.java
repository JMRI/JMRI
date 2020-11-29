package jmri.jmrix.srcp.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public abstract class SRCPSystemConnectionAction extends AbstractSystemConnectionAction<SRCPSystemConnectionMemo> {

    public SRCPSystemConnectionAction(String name, SRCPSystemConnectionMemo memo) {
        super(name, memo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(SRCPSystemConnectionMemo.class));
    }
    
}
