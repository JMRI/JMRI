package code;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import jmri.jmrit.ctcserialdata.ProjectsCommonSubs;

/**
 *
 * @author Gregory J. Bedlek Copyright (C) 2018, 2019
 * 
 *  Technically, there should be ONLY ONE of these in the entire system!
 *  You can have more than one, you only "pollute" the file system with numerous other file(s).
 *  This object "maintains" certain properties of windows so that users when they restart
 *  the program can find the windows at the same place.
 */
public class AwtWindowProperties {
    private final Properties _mProperties;
    private final String _mFilename;
    private final java.awt.Window _mMasterWindow;
    
    public AwtWindowProperties(java.awt.Window window, String filename, String windowName) {
        _mProperties = new Properties();
        _mFilename = filename;
        _mMasterWindow = window;
        try {
            File file = new File(filename);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            _mProperties.load(bufferedReader);
        } catch (IOException e) {}
        setWindowState(window, windowName);
    }
    
    public void close() {
        try {
            File file = new File(_mFilename);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            _mProperties.store(bufferedWriter, "All Ant Windows Properties");
        } catch (IOException e) {}
    }
    
    public final void setWindowState(java.awt.Window window, String windowName) {
        Rectangle currentWindowRectangle = window.getBounds();  // In case any width/heigth below "fail" (i.e. new file, problems of any kind),
                                                                // we will default to what the programmer designed the window size for.
//  If the "default" window size as created by the programmer is larger than what the user specified the size as from the
//  last time they closed this window, they we will override that users smaller value.  This is in case the programmer
//  increased it's size between versions of the program:
        int windowWidth = ProjectsCommonSubs.getIntFromStringNoThrow(_mProperties.getProperty(windowName + ".Width"), currentWindowRectangle.width);
        int windowHeight = ProjectsCommonSubs.getIntFromStringNoThrow(_mProperties.getProperty(windowName + ".Height"), currentWindowRectangle.height);
        if (currentWindowRectangle.width > windowWidth) windowWidth = currentWindowRectangle.width;
        if (currentWindowRectangle.height > windowHeight) windowHeight = currentWindowRectangle.height;
        window.setBounds(new Rectangle( ProjectsCommonSubs.getIntFromStringNoThrow(_mProperties.getProperty(windowName + ".X"), _mMasterWindow.getX()),
                                        ProjectsCommonSubs.getIntFromStringNoThrow(_mProperties.getProperty(windowName + ".Y"), _mMasterWindow.getY()),
                                        windowWidth,
                                        windowHeight));
    }
    
    public void saveWindowState(java.awt.Window window, String windowName) {
        Rectangle rectangle = window.getBounds();
        _mProperties.setProperty(windowName + ".X", Integer.toString((int)rectangle.getX()));
        _mProperties.setProperty(windowName + ".Y", Integer.toString((int)rectangle.getY()));
        _mProperties.setProperty(windowName + ".Width", Integer.toString((int)rectangle.getWidth()));
        _mProperties.setProperty(windowName + ".Height", Integer.toString((int)rectangle.getHeight()));
    }
    
    public void saveWindowStateAndClose(java.awt.Window window, String windowName) {
        saveWindowState(window, windowName);
        close();
    }
}
