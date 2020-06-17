package jmri.jmrix.qsi.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jmri.SystemConnectionMemo;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;
import jmri.jmrix.swing.AbstractSystemConnectionAction;

/**
 *
 * @author Randall Wood Copyright 2020
 */
public abstract class QsiSystemConnectionAction extends AbstractSystemConnectionAction<QsiSystemConnectionMemo> {

    public QsiSystemConnectionAction(String name, QsiSystemConnectionMemo memo) {
        super(name, memo);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses() {
        return new HashSet<>(Arrays.asList(QsiSystemConnectionMemo.class));
    }
    
}
