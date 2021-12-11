package jmri.jmrix.dccpp.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;

/**
 *
 * @author Randall Wood Copyright 2020
 * @author mstevetodd 2021
 * 
 */
public abstract class DCCppSystemConnectionAction extends AbstractSystemConnectionAction<DCCppSystemConnectionMemo> {

    public DCCppSystemConnectionAction(String name, DCCppSystemConnectionMemo memo) {
        super(name, memo);
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(DCCppSystemConnectionMemo.class));
    }
    
}
