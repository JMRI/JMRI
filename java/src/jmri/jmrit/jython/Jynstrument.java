package jmri.jmrit.jython;

import java.awt.Container;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jynstrument is a Jython script and associated other resources that can
 * decorate a Java class.
 *
 * @see JynstrumentFactory
 * @author Lionel Jeanson Copyright 2009
 * @since 2.7.8
 */
public abstract class Jynstrument extends JPanel {

    private Object mContext;  // Object being extended
    private String jythonFile;  // Name of the Jython file being run
    private String jynstrumentFolder; // Folder where the script seats (to retrieve resources)
    private String className; // name of the JYnstrument class
    private JPopupMenu myPopUpMenu; // a popup menu

    /**
     * Access to the context object to which this Jynstrument was attached when
     * it was created.
     *
     * @return the context object
     */
    public Object getContext() {
        return mContext;
    }

    public void setContext(Object context) {
        mContext = context;
    }

    public String getJythonFile() {
        return jythonFile;
    }

    public void setJythonFile(String jythonFile) {
        this.jythonFile = jythonFile;
    }

    /**
     * Get the folder containing the defining Jython script.
     *
     * @return the parent folder of the script
     */
    public String getFolder() {
        return jynstrumentFolder;
    }

    public void setFolder(String jynstrumentFolder) {
        this.jynstrumentFolder = jynstrumentFolder;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void exit() {
        Container cnt = getParent();
        log.debug("getParent() is " + cnt);
        if (cnt != null) {
            cnt.remove(this);
            cnt.repaint();
        }
        quit();
        setPopUpMenu(null);
    }

    public boolean validateContext() {
        if (getExpectedContextClassName() == null || mContext == null) {
            return false;
        }
        try {
            return (Class.forName(getExpectedContextClassName()).isAssignableFrom(mContext.getClass()));
        } catch (ClassNotFoundException e) {
            log.error("Class {} not found.", getExpectedContextClassName(), e);
        }
        return false;
    }

    public abstract String getExpectedContextClassName();

    public abstract void init();

    protected abstract void quit();

    private final static Logger log = LoggerFactory.getLogger(Jynstrument.class);

    public JPopupMenu getPopUpMenu() {
        return myPopUpMenu;
    }

    public void setPopUpMenu(JPopupMenu myPopUpMenu) {
        this.myPopUpMenu = myPopUpMenu;
    }

    public void setXml(Element e) {
        // do nothing by default
    }

    public Element getXml() {
        return null;
    }
}
