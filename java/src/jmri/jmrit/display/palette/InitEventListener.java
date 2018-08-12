package jmri.jmrit.display.palette;

/**
 * Custom listener to respond to redisplay of a tabbed pane.
 */
public interface InitEventListener {
    void onInitEvent(int choice, int selectedPane);
}
