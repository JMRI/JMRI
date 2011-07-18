// Menu.java

package jmri.jmrix.ecos;

/**
 * Create a "Systems" menu containing the Jmri ECOS-specific tools.
 *
 * @author	Bob Jacobsen   Copyright 2003, 2008
 * @version     $Revision$
 * @deprecated 2.11.3
 */
@Deprecated
public class Menu extends jmri.jmrix.ecos.swing.EcosMenu {
    public Menu(EcosSystemConnectionMemo memo) {
        super(memo);
    }
}
