package jmri.util.iharder.dnd;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

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
@API(status = EXPERIMENTAL)
public interface URIDropListener extends java.util.EventListener {

    /**
     * Fired by the source when files are dropped onto a drop target.
     *
     * @param evt The {@link URIDropEvent} associated with this event
     * @since 1.1
     */
    public abstract void urisDropped(URIDropEvent evt);

}   // end interface FileDropListener
