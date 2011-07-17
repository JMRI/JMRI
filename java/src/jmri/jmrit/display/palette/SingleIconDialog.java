// SingleIconDialog.java
package jmri.jmrit.display.palette;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Plain icons have a single family but like MultiSensorIcons,
 * icons can be added and deleted from a family
 * @author Pete Cressman  Copyright (c) 2010
 */

public class SingleIconDialog extends IconDialog implements MouseListener {

    /**
    * Constructor for existing family to change icons, add/delete icons, or to delete the family
    */
    public SingleIconDialog(String type, String family, ItemPanel parent) {
        super(type, family, parent);
        _familyName.setText("");
        if (_iconPanel!=null) {
            _iconPanel.addMouseListener(this);
        }
    }

    public void dispose() {
        if (_iconPanel!=null) {
            _iconPanel.removeMouseListener(this);
        }
        super.dispose();
    }

    protected JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        makeAddIconButtonPanel(buttonPanel, "ToolTipAddIcon", "ToolTipDeleteIcon");
        makeDoneButtonPanel(buttonPanel);
        return buttonPanel;
    }

    /**
    * add/delete icon.
    */
    protected void makeAddIconButtonPanel(JPanel buttonPanel, String addTip, String deleteTip) {
        JPanel panel2 = new JPanel();
        panel2.setLayout(new FlowLayout());
        JButton addIcon = new JButton(ItemPalette.rbp.getString("addIcon"));
        addIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    addNewIcon(_familyName.getText());
                }
        });
        addIcon.setToolTipText(ItemPalette.rbp.getString(addTip));
        panel2.add(addIcon);

        JButton deleteIcon = new JButton(ItemPalette.rbp.getString("deleteIcon"));
        deleteIcon.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    if (deleteIcon()) {
                        dispose();
                    }
                }
        });
        deleteIcon.setToolTipText(ItemPalette.rbp.getString(deleteTip));
        panel2.add(deleteIcon);
        buttonPanel.add(panel2);
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean addNewIcon(String name) {
        if (log.isDebugEnabled()) log.debug("addNewIcon Action: iconMap.size()= "+_iconMap.size());
        if (name==null || name.length()==0) {
            JOptionPane.showMessageDialog(_parent._paletteFrame, ItemPalette.rbp.getString("NoIconName"),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (_iconMap.get(name)!=null) {
            JOptionPane.showMessageDialog(_parent._paletteFrame,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("DuplicateIconName"), name),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        String fileName = "resources/icons/misc/X-red.gif";
        NamedIcon icon = new jmri.jmrit.catalog.NamedIcon(fileName, fileName);
        _iconMap.put(name, icon);
        java.awt.Container con = getContentPane();
        con.remove(_iconPanel);
        _iconPanel = makeIconPanel(_iconMap);
        con.add(_iconPanel, 1);
        pack();
        return true;
    }

    /**
    * Action item for makeAddIconButtonPanel
    */
    protected boolean deleteIcon() {
        if (log.isDebugEnabled()) log.debug("deleteIcon Action: iconMap.size()= "+_iconMap.size());
        // for simple icons, _family is icon's name
        String iconName = _familyName.getText();
        if (_iconMap.remove(iconName)==null) {
            JOptionPane.showMessageDialog(_parent._paletteFrame,
                    java.text.MessageFormat.format(ItemPalette.rbp.getString("IconNotFound"), iconName),
                    ItemPalette.rb.getString("warnTitle"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        doDoneAction();
        return true;
    }
        
    /**
    * Action item for makeDoneButtonPanel
    */
    protected boolean doDoneAction() {
        //check text
        if (_family!=null) {
            ItemPalette.removeIconMap(_type, _family);
        }
        if (addFamily(_family, _iconMap)) {
            //_parent.addIconsToPanel(_iconMap);
            _parent.updateFamiliesPanel();
            return true;
        }
        return false;
    }


    public void mousePressed(MouseEvent event) {}

    public void mouseReleased(MouseEvent event) {
        if (log.isDebugEnabled()) log.debug("mouseReleased at ("+event.getX()+", "+event.getY()+")");
        java.awt.Component[] comp = _iconPanel.getComponents();
        for (int i=0; i<comp.length; i++) {
            if (comp[i] instanceof JPanel) {
                JPanel p = (JPanel)comp[i];
                java.awt.Component[] com = p.getComponents();
                for (int k=0; k<com.length; k++) {
                    if (com[k] instanceof JPanel) {
                        JPanel panel = (JPanel)com[k];
                        java.awt.Component[] c = panel.getComponents();
                        for (int j=0; j<c.length; j++) {
                            if (c[j] instanceof DropJLabel) {
                                JLabel icon = (JLabel)c[j];
                                java.awt.Rectangle r = p.getBounds();
                                //if (log.isDebugEnabled()) log.debug("Name= "+icon.getName()+" at ("+r.x+", "+r.y+
                                //                                    ") w= "+r.width+", h="+r.height);
                                if (r.contains(event.getX(), event.getY())) {
                                    _familyName.setText(icon.getName());
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        _familyName.setText("");
    }

    public void mouseClicked(MouseEvent event) {}
    public void mouseEntered(MouseEvent event) {}
    public void mouseExited(MouseEvent event) {}
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleIconDialog.class.getName());
}

