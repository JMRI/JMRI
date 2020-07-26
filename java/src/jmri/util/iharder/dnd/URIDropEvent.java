package jmri.util.iharder.dnd;

import java.util.Arrays;

/**
 * This is the event that is passed to the
 * {@link URIDropListener#urisDropped URIsDropped(...)} method in your
 * {@link URIDropListener} when URIs are dropped onto a registered drop
 * target.
 * <p>
 * I'm releasing this code into the Public Domain. Enjoy.
 * <p>
 * <em>Original author: Robert Harder, rharder@usa.net</em>
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * 
 * @version 1.2
 */
public class URIDropEvent extends java.util.EventObject {

    private final java.net.URI[] uris;

    /**
     * Constructs a {@link URIDropEvent} with the array of files that were
     * dropped and the object that initiated the event.
     *
     * @param uris list of URIs
     * @param source The event source
     * @since 1.1
     */
    public URIDropEvent(java.net.URI[] uris, Object source) {
        super(source);
        this.uris = Arrays.copyOf(uris, uris.length);
    }   // end constructor

    /**
     * Returns an array of URIs that were dropped on a registered drop target.
     *
     * @return array of URIs that were dropped
     * @since 1.1
     */
    public java.net.URI[] getURIs() {
        return Arrays.copyOf(uris, uris.length);
    }   // end getFiles

}
