package jmri.jmrix.lenz.swing;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;

/**
 * Abstract action to create and register a swing object for XpressNet systems.
 *
 * @author Paul Bender Copyright (C) 2016 
 */
abstract public class AbstractXPressNetAction extends AbstractAction implements jmri.jmrix.swing.SystemConnectionAction {

    protected XNetSystemConnectionMemo _memo = null;

    public AbstractXPressNetAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public AbstractXPressNetAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemLI101ConfigurationManager"), memo);
    }

    /**
     * Get the {@link jmri.jmrix.SystemConnectionMemo} this action is bound to.
     *
     * @return the SystemConnectionMemo or null if not bound
     */
    @CheckForNull
    @Override
    public SystemConnectionMemo getSystemConnectionMemo(){
       return _memo;
    }

    /**
     * Set the {@link jmri.jmrix.SystemConnectionMemo} this action is bound to.
     * <p>
     * Implementing classes may throw an IllegalArgumentException if the
     * implementing class requires a specific subclass of SystemConnectionMemo.
     *
     * @param memo the SystemConnectionMemo
     * @throws IllegalArgumentException if the SystemConnectionMemo is invalid
     */
    @Override
    public void setSystemConnectionMemo(@Nonnull SystemConnectionMemo memo) throws IllegalArgumentException{
         if(memo == null) {
            throw new IllegalArgumentException("Attempt to set null system connection");
         }
         if(!(memo instanceof XNetSystemConnectionMemo)){
            throw new IllegalArgumentException("Attempt to set null system connection"); 
         }
         _memo = (XNetSystemConnectionMemo) memo;
    }

    /**
     * Get a list of {@link jmri.jmrix.SystemConnectionMemo} subclasses that the
     * implementing class accepts.
     * <p>
     * If the implementing class is a subclass of a class that does accept
     * SystemConnectionMemos, but the implementing class does not accept any,
     * return an empty array instead of null.
     *
     * @return Set of SystemConnectionMemo subclasses or empty array.
     */
    @Nonnull
    @Override
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses(){
        return new HashSet<>(Arrays.asList(XNetSystemConnectionMemo.class));
    }

}
