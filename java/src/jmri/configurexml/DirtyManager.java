package jmri.configurexml;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.configurexml.swing.DirtyManagerDialog;
import jmri.jmrit.display.*;
import jmri.jmrit.simpleclock.SimpleTimebase;

/**
 * The Dirty Manager is notified whenever potential PanelPro data content is added, deleted or modified.
 * During shutdown, a dialog will be displayed if any of the potential data is 'dirty'.
 * The user will have an opportunity to store the data before shutdown proceeds.
 *
 * The basic process is to be notified by the various GUI tools when a change has been made.
 *
 * @author Dave Sand Copyright (c) 2022
 */
public class DirtyManager implements InstanceManagerAutoDefault {

    boolean _dirty = false;
    boolean _loading = false;

    public DirtyManager() {
    }

    /**
     * Set the dirty flag true or false.
     * @param dirty Either true or false.
     * @param tag A descriptive phrase for debugging purposes. The tag can be null.
     */
    public void setDirty(boolean dirty, String tag) {
        if (!_loading) {
            log.debug("## dirty flag = {}, tag = {}", dirty, tag);
            _dirty = dirty;
        }
    }

    /**
     * This is used by storeIfNeeded which is invoked by the shutdown manager.  If true, provide an
     * option to do the Store process before the shutdown occurs.
     * @return the dirty state.
     */
    public boolean isDirty() {
        return _dirty;
    }

    /**
      * This is used to ignore the changes that naturally occur data loading.
      * Set by the PanelPro data load processes to before loading and false when done.
      * @param loading Either true or false.
      */
     public void setLoading(boolean loading) {
         log.debug("@@ loading flag = {}", loading);
         _loading = loading;
     }

    /**
     * If the dirty state is true, provide the user with the opporutnity to do a Store before
     * the shutdown completes.  This is invoked by the shutdown manager.
     */
    public void storeIfNeeded() {
        if (isDirty() && !GraphicsEnvironment.isHeadless()) {
            DirtyManagerDialog.showDialog();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirtyManager.class);

}
