package jmri.jmrix.swing;

import jmri.jmrix.SystemConnectionMemo;

/**
 * Interface for a {@link javax.swing.Action} that is bound to a
 * {@link jmri.jmrix.SystemConnectionMemo}.
 *
 * @author Randall Wood (c) 2016
 */
public interface SystemConnectionAction {

    public SystemConnectionMemo getSystemConnectionMemo();

    public void setSystemConnectionMemo(SystemConnectionMemo memo);

    /**
     * Get a list of {@link jmri.jmrix.SystemConnectionMemo} subclasses that the
     * implementing class accepts.
     *
     * @return Array of SystemConnectionMemo subclasses.
     */
    public Class<? extends SystemConnectionMemo>[] getSystemConnectionMemoClasses();
}
