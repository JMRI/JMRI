package jmri.util.iharder.dnd;

/**
 * When using the FileDrop package in its JavaBean form, this listener will
 * receive events when files are dropped onto registered targets.
 * <p>
 * I'm releasing this code into the Public Domain. Enjoy.
 * <p>
 * <em>Original author: Robert Harder, rharder@usa.net</em>
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @version 1.1
 */
public interface FileDropListener extends java.util.EventListener {

    /**
     * Fired by the source when files are dropped onto a drop target.
     *
     * @param evt The {@link FileDropEvent} associated with this event
     * @since 1.1
     */
    public abstract void filesDropped(FileDropEvent evt);

}   // end interface FileDropListener
