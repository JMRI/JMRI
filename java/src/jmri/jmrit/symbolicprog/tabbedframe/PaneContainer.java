package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.util.BusyGlassPane;

/**
 * Interface for the container of a set of PaneProgPanes. The panes use services
 * provided here to work with buttons and the busy cursor.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public interface PaneContainer {

    public boolean isBusy();

    public void paneFinished();

    /**
     * Enable the read/write buttons.
     * <p>
     * In addition, if a programming mode pane is present, it's "set" button is
     * enabled.
     *
     * @param enable Are reads possible? If false, so not enable the read
     *               buttons.
     */
    public void enableButtons(boolean enable);

    public void prepGlassPane(javax.swing.AbstractButton activeButton);

    public BusyGlassPane getBusyGlassPane();
}
