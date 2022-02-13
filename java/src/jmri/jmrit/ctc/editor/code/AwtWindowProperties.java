package jmri.jmrit.ctc.editor.code;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.ctc.CTCFiles;
import jmri.jmrit.ctc.ctcserialdata.ProjectsCommonSubs;

/**
 * Use the JMRI UserPreferencesManager to store the size and location for
 * the Editor windows.
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * @author Dave Sand Copyright (C) 2021
 */
public class AwtWindowProperties {
    private final java.awt.Window _mMasterWindow;
    private final String CTC_PACKAGE;

    Point pt = null;
    Dimension dim = null;

    public AwtWindowProperties(java.awt.Window window, String filename, String windowName) {
        _mMasterWindow = window;
        CTC_PACKAGE = this.getClass().getPackage().getName();
        setWindowState(window, windowName);
    }

    public final void setWindowState(java.awt.Window window, String windowName) {
        Rectangle currentWindowRectangle = window.getBounds();
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            pt = prefMgr.getWindowLocation(CTC_PACKAGE + windowName);
            dim = prefMgr.getWindowSize(CTC_PACKAGE + windowName);
        });
        log.debug("window {} :: {} :: {}", windowName, pt, dim);
        if (pt == null) {
            pt = new Point(_mMasterWindow.getX(), _mMasterWindow.getY());
        }
        if (dim == null) {
            int windowWidth = (int) currentWindowRectangle.getWidth();
            int windowHeight = (int) currentWindowRectangle.getHeight();
            dim = new Dimension(windowWidth, windowHeight);
        }
        window.setBounds(new Rectangle(pt, dim));
    }

    public void saveWindowState(java.awt.Window window, String windowName) {
        Rectangle rectangle = window.getBounds();
        InstanceManager.getOptionalDefault(UserPreferencesManager.class).ifPresent((prefMgr) -> {
            prefMgr.setWindowLocation(CTC_PACKAGE + windowName, new Point((int)rectangle.getX(), (int)rectangle.getY()));
            prefMgr.setWindowSize(CTC_PACKAGE + windowName, new Dimension((int)rectangle.getWidth(), (int)rectangle.getHeight()));
        });
    }

    public void saveWindowStateAndClose(java.awt.Window window, String windowName) {
        saveWindowState(window, windowName);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AwtWindowProperties.class);
}
