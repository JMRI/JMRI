package jmri.jmrix.cmri.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public abstract class CMRISystemConnectionAction extends AbstractSystemConnectionAction<CMRISystemConnectionMemo> {

    public CMRISystemConnectionAction(String name, CMRISystemConnectionMemo memo) {
        super(name, memo);
    }

    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(CMRISystemConnectionMemo.class));
    }
    
}
