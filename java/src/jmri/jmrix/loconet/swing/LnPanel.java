// LnPanel.java
package jmri.jmrix.loconet.swing;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * JPanel extension to handle automatic creation of window title and help
 * reference for LocoNet panels
 * <p>
 * For use with JmriAbstractAction, etc
 *
 * @author Bob Jacobsen Copyright 2010, 2015
 * @since 2.9.4
 * @version $Revision$
 */
abstract public class LnPanel extends jmri.util.swing.JmriPanel implements LnPanelInterface {

    /**
     * make "memo" object available as convenience
     */
    protected LocoNetSystemConnectionMemo memo;

    public void initComponents(LocoNetSystemConnectionMemo memo) {
        this.memo = memo;
    }

    public void initContext(Object context) {
        if (context instanceof LocoNetSystemConnectionMemo) {
            initComponents((LocoNetSystemConnectionMemo) context);
        }
    }

    public String getTitle(String menuTitle) { return getTitleHelper(memo, menuTitle); }
    
    /**
     * Static to allow code to be shared by other LnPanelInterface implementations
     */
    public static String getTitleHelper(LocoNetSystemConnectionMemo memo, String menuTitle) {
        String uName = "";
        if (memo != null) {
            uName = memo.getUserName();
            // string "LocoNet" is hard coded
            if (!"LocoNet".equals(uName)) {
                uName = uName + ": ";
            } else {
                uName = "";
            }
        }
        return uName + menuTitle;
    }

}
