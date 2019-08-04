package jmri.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * PropertyChangeListener that resends the event on the passed in thread when
 * constructed.
 * 
 * @author Randall Wood Copyright 2019
 */
public class ThreadingPropertyChangeListener implements PropertyChangeListener {

    private PropertyChangeListener listener;
    private Thread thread;

    public ThreadingPropertyChangeListener(PropertyChangeListener listener, Thread thread) {
        this.listener = listener;
        this.thread = thread;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (thread) {
            case GUI:
                ThreadingUtil.runOnGUI(() -> {
                    listener.propertyChange(evt);
                });
                break;
            case LAYOUT:
            default:
                ThreadingUtil.runOnLayout(() -> {
                    listener.propertyChange(evt);
                });
                break;
        }
    }

    public static ThreadingPropertyChangeListener guiListener(PropertyChangeListener listener) {
        return new ThreadingPropertyChangeListener(listener, Thread.GUI);
    }

    public static ThreadingPropertyChangeListener layoutListener(PropertyChangeListener listener) {
        return new ThreadingPropertyChangeListener(listener, Thread.LAYOUT);
    }

    public enum Thread {
        GUI,
        LAYOUT
    }
}