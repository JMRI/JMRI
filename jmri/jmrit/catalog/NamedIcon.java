package jmri.jmrit.catalog;

import javax.swing.ImageIcon;
import java.net.URL;

/**
 * Extend an ImageIcon to remember the name from which it was created
 * @author Bob Jacobsen  Copyright 2002
 * @version $Revision: 1.1 $
 */

public class NamedIcon extends ImageIcon {
    public NamedIcon(URL pUrl, String pName) {
        super(pUrl);
        mName = pName;
    }

    public NamedIcon(String pUrl, String pName) {
        super(pUrl);
        mName = pName;
    }

    public String getName() { return mName; }

    private String mName=null;
}