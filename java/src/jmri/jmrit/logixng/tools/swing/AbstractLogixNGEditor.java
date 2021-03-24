package jmri.jmrit.logixng.tools.swing;

import java.util.EventListener;
import java.util.HashMap;

import jmri.NamedBean;

/**
 *
 * @author Daniel Bergqvist copyright (c) 2019
 * 
 * @param <E> the type of NamedBean supported by this editor
 */
public interface AbstractLogixNGEditor<E extends NamedBean> {
    
    /**
     * Create a custom listener event.
     */
    public interface EditorEventListener extends EventListener {

        /**
         * An event that gets delivered from the editor
         * @param data Contains a list of commands to be processed by the
         *             listener recipient.
         */
        void editorEventOccurred(HashMap<String, String> data);
    }
    
    
    /**
     * Add a listener.
     *
     * @param listener The recipient
     */
    public void addEditorEventListener(EditorEventListener listener);
    
    /**
     * Remove a listener -- not used.
     *
     * @param listener The recipient
     */
    public void removeEditorEventListener(EditorEventListener listener);
    
    public void bringToFront();
    
}
