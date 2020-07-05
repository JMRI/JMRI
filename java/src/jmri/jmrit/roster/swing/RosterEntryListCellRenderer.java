package jmri.jmrit.roster.swing;

import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import jmri.InstanceManager;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.RosterIconFactory;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Render RosterEntry objects or Strings as cells in a JComboBox.
 *
 * @see jmri.jmrit.roster.swing.RosterEntryComboBox
 */
@API(status = MAINTAINED)
public class RosterEntryListCellRenderer extends JLabel implements ListCellRenderer<Object> {

    public RosterEntryListCellRenderer() {
        super();
        setOpaque(true);
    }

    // FIXME: JList needs typed
    @Override
    public Component getListCellRendererComponent(@SuppressWarnings("rawtypes") JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        if (value != null) {
            if (value instanceof RosterEntry) {
                String rosterEntryTitle = ((RosterEntry) value).titleString();

                ImageIcon icon = InstanceManager.getDefault(RosterIconFactory.class).getIcon(rosterEntryTitle);
                if (icon != null) {
                    icon.setImageObserver(list);
                }
                setIcon(icon);
                setText(rosterEntryTitle);
            } else {
                setText(value.toString());
                setIcon(null);
            }
        }
        return this;
    }
}
