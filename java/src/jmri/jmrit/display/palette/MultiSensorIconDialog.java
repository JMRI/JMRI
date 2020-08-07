package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import jmri.jmrit.catalog.NamedIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Icons may be added or deleted from a family.
 *
 * @author Pete Cressman Copyright (c) 2010
 */
public class MultiSensorIconDialog extends IconDialog {

    /*
     * Constructor for existing family to change icons, add/delete icons, or to
     * delete the family.
     */
    public MultiSensorIconDialog(String type, String family, FamilyItemPanel parent) {
        super(type, family, parent);
    }

    protected String getIconName() {
        return MultiSensorItemPanel.POSITION[_iconMap.size() - 3];
    }

    /*
     * Add/delete icon. For Multisensor, it adds another sensor position.
     */
    @Override
    protected void makeDoneButtonPanel(JPanel buttonPanel, String text) {
        super.makeDoneButtonPanel(buttonPanel, text);
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JButton addSensor = new JButton(Bundle.getMessage("addIcon"));
        addSensor.addActionListener(a -> {
            if (addNewIcon(getIconName())) {
                finishAddDelete();
            }
        });
        addSensor.setToolTipText(Bundle.getMessage("ToolTipAddPosition"));
        panel2.add(addSensor);

        JButton deleteSensor = new JButton(Bundle.getMessage("deleteIcon"));
        deleteSensor.addActionListener(a -> {
            if (deleteIcon()) {
                finishAddDelete();
            }
        });
        deleteSensor.setToolTipText(Bundle.getMessage("ToolTipDeletePosition"));
        panel2.add(deleteSensor);
        buttonPanel.add(panel2);
    }

    private void finishAddDelete() {
        _iconEditPanel.removeAll();
        _parent.addIconsToPanel(_iconMap, _iconEditPanel, true);
        pack();
    }

    @Override
    protected boolean doDoneAction() {
        MultiSensorItemPanel parent = (MultiSensorItemPanel) _parent;
        
        if (_iconMap.size() != parent.getIconMap().size()) {
            parent.setSelections();
        }
        return super.doDoneAction();
    }

    /**
     * Action item for makeAddIconButtonPanel.
     * @param name icon name
     * @return true if name is OK
     */
    private boolean addNewIcon(String name) {
        if (log.isDebugEnabled()) {
            log.debug("addNewIcon Action: iconMap.size()= {}", _iconMap.size());
        }
        if (name == null || name.length() == 0) {
            JOptionPane.showMessageDialog(_parent._frame, Bundle.getMessage("NoIconName"),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (_iconMap.get(name) != null) {
            JOptionPane.showMessageDialog(_parent._frame,
                    Bundle.getMessage("DuplicateIconName", name),
                    Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String fileName = "resources/icons/misc/X-red.gif";
        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
        _iconMap.put(name, icon);
        return true;
    }

    /**
     * Action item for makeAddIconButtonPanel.
     */
    private boolean deleteIcon() {
        if (log.isDebugEnabled()) {
            log.debug("deleteSensor Action: iconMap.size()= {}", _iconMap.size());
        }
        if (_iconMap.size() < 4) {
            return false;
        }
        String name = MultiSensorItemPanel.POSITION[_iconMap.size() - 4];
        _iconMap.remove(name);
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(MultiSensorIconDialog.class);

}
