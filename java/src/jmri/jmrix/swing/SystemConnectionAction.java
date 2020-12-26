package jmri.jmrix.swing;

import java.util.Set;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.SystemConnectionMemo;

/**
 * Interface for a {@link javax.swing.Action} that is bound to a
 * {@link SystemConnectionMemo}.
 *
 * @author Randall Wood (c) 2016
 * @param <M> the supported subclass of SystemConnectionMemo
 */
public interface SystemConnectionAction<M extends SystemConnectionMemo> {

    /**
     * Get the {@link SystemConnectionMemo} this action is bound to.
     * 
     * @return the SystemConnectionMemo or null if not bound.
     */
    @CheckForNull
    public M getSystemConnectionMemo();

    /**
     * Set the {@link SystemConnectionMemo} this action is bound to.
     * <p>
     * Implementing classes may throw an IllegalArgumentException if the
     * implementing class requires a specific subclass of SystemConnectionMemo.
     *
     * @param memo the SystemConnectionMemo
     * @throws IllegalArgumentException if the SystemConnectionMemo is invalid
     */
    public void setSystemConnectionMemo(@Nonnull M memo);

    /**
     * Get a list of {@link SystemConnectionMemo} subclasses that the
     * implementing class accepts.
     * <p>
     * If the implementing class is a subclass of a class that does accept
     * SystemConnectionMemos, but the implementing class does not accept any,
     * return an empty array instead of null.
     *
     * @return Set of SystemConnectionMemo subclasses or empty array.
     */
    @Nonnull
    public Set<Class<? extends SystemConnectionMemo>> getSystemConnectionMemoClasses();
}
