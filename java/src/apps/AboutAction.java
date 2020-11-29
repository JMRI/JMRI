package apps;

import javax.swing.Icon;
import jmri.util.swing.WindowInterface;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author Randall Wood
 * @deprecated since 4.17.5 use {@link apps.swing.AboutAction} instead.
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public class AboutAction extends apps.swing.AboutAction {

    public AboutAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public AboutAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    public AboutAction() {
        super();
    }
}
