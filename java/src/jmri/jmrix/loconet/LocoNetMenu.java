// LocoNetMenu.java
package jmri.jmrix.loconet;

/**
 * Migration class for compatibility after LocoNetMenu class was moved to
 * {@link jmri.jmrix.loconet.swing}.
 *
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 * @deprecated 2.9.5
 */
@Deprecated
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class LocoNetMenu extends jmri.jmrix.loconet.swing.LocoNetMenu {

    /**
     *
     */
    private static final long serialVersionUID = -1194141042822247444L;

    public LocoNetMenu(LocoNetSystemConnectionMemo memo) {
        super(memo);
    }
}
