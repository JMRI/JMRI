package jmri.jmrix.grapevine.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public abstract class GrapevineSystemConnectionAction extends AbstractSystemConnectionAction<GrapevineSystemConnectionMemo> {

    public GrapevineSystemConnectionAction(String name, GrapevineSystemConnectionMemo memo) {
        super(name, memo);
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(GrapevineSystemConnectionMemo.class));
    }
    
}
