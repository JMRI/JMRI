package jmri.jmrit.display.layoutEditor;

/**
 * An icon to display a status of a Memory.<P>
 */
import java.awt.event.ActionListener;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This is the same name as display.MemoryIcon, but a very
// separate class. That's not good. Unfortunately, it's too 
// hard to disentangle that now because it's resident in the
// panel file that have been written out, so we just annote 
// the fact.
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS")
public class MemoryIcon extends jmri.jmrit.display.MemoryIcon {

    /**
     *
     */
    private static final long serialVersionUID = 446165214555001212L;
    String defaultText = " ";

    public MemoryIcon(String s, LayoutEditor panel) {
        super(s, panel);
        log.debug("MemoryIcon ctor= " + MemoryIcon.class.getName());
    }

    public void setText(String text) {
        if (text == null || text.length() == 0) {
            super.setText(defaultText);
        } else {
            super.setText(text);
        }
    }

    LayoutBlock lBlock = null;

    public LayoutBlock getLayoutBlock() {
        return lBlock;
    }

    public void setLayoutBlock(LayoutBlock lb) {
        lBlock = lb;
    }

    public void displayState() {
        log.debug("displayState");
        if (getMemory() == null) {  // use default if not connected yet
            setText(defaultText);
            updateSize();
            return;
        }
        if (re != null) {
            jmri.InstanceManager.throttleManagerInstance().removeListener(re.getDccLocoAddress(), this);
            re = null;
        }
        Object key = getMemory().getValue();
        if (key != null) {
            java.util.HashMap<String, NamedIcon> map = getMap();
            if (map == null) {
                // no map, attempt to show object directly
                Object val = key;
                if (val instanceof jmri.jmrit.roster.RosterEntry) {
                    jmri.jmrit.roster.RosterEntry roster = (jmri.jmrit.roster.RosterEntry) val;
                    val = updateIconFromRosterVal(roster);
                    flipRosterIcon = false;
                    if (val == null) {
                        return;
                    }
                }
                if (val instanceof String) {
                    if (val.equals("")) {
                        setText(defaultText);
                    } else {
                        setText((String) val);
                    }
                    setIcon(null);
                    _text = true;
                    _icon = false;
                    updateSize();
                    return;
                } else if (val instanceof javax.swing.ImageIcon) {
                    setIcon((javax.swing.ImageIcon) val);
                    setText(null);
                    _text = false;
                    _icon = true;
                    updateSize();
                    return;
                } else if (val instanceof Number) {
                    setText(val.toString());
                    setIcon(null);
                    _text = true;
                    _icon = false;
                    updateSize();
                    return;
                } else {
                    log.warn("can't display current value of " + getNamedMemory().getName()
                            + ", val= " + val + " of Class " + val.getClass().getName());
                }
            } else {
                // map exists, use it
                NamedIcon newicon = map.get(key.toString());
                if (newicon != null) {

                    setText(null);
                    super.setIcon(newicon);
                    _text = false;
                    _icon = true;
                    updateSize();
                    return;
                } else {
                    // no match, use default
                    setIcon(getDefaultIcon());

                    setText(null);
                    _text = false;
                    _icon = true;
                    updateSize();
                }
            }
        } else {
            setIcon(null);
            setText(defaultText);
            _text = true;
            _icon = false;
            updateSize();
        }
    }

    JCheckBoxMenuItem updateBlockItem = new JCheckBoxMenuItem("Update Block Details");

    @Override
    public boolean showPopUp(JPopupMenu popup) {
        if (isEditable()) {
            popup.add(updateBlockItem);
            updateBlockItem.setSelected(updateBlockValueOnChange());
            updateBlockItem.addActionListener(new ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    updateBlockValueOnChange(updateBlockItem.isSelected());
                }
            });
        }  // end of selectable
        return super.showPopUp(popup);
    }

    public void setMemory(String pName) {
        super.setMemory(pName);
        lBlock = jmri.InstanceManager.getDefault(LayoutBlockManager.class).getBlockWithMemoryAssigned(getMemory());
    }

    @Override
    protected void setValue(Object obj) {
        if (updateBlockValue && lBlock != null) {
            lBlock.getBlock().setValue(obj);
        } else {
            getMemory().setValue(obj);
            updateSize();
        }
    }

    protected void addRosterToIcon(RosterEntry roster) {
        if (!jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled() || lBlock == null) {
            super.addRosterToIcon(roster);
            return;
        }

        int paths = lBlock.getNumberOfThroughPaths();
        jmri.Block srcBlock = null;
        jmri.Block desBlock = null;
        for (int i = 0; i < paths; i++) {
            if (lBlock.isThroughPathActive(i)) {
                srcBlock = lBlock.getThroughPathSource(i);
                desBlock = lBlock.getThroughPathDestination(i);
                break;
            }
        }
        int dirA;
        int dirB;
        if (srcBlock != null && desBlock != null) {
            dirA = lBlock.getNeighbourDirection(srcBlock);
            dirB = lBlock.getNeighbourDirection(desBlock);
        } else {
            dirA = jmri.Path.EAST;
            dirB = jmri.Path.WEST;
        }

        Object[] options = {"Facing " + jmri.Path.decodeDirection(dirB),
            "Facing " + jmri.Path.decodeDirection(dirA),
            "Do Not Add"};
        int n = JOptionPane.showOptionDialog(this,
                "Would you like to assign loco "
                + roster.titleString() + " to this location",
                "Assign Loco",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[2]);
        if (n == 2) {
            return;
        }
        if (n == 0) {
            flipRosterIcon = true;
            if (updateBlockValue) {
                lBlock.getBlock().setDirection(dirB);
            }
        } else {
            flipRosterIcon = false;
            if (updateBlockValue) {
                lBlock.getBlock().setDirection(dirA);
            }
        }
        if (getMemory().getValue() == roster) {
            //No change in the loco but a change in direction facing might have occured
            updateIconFromRosterVal(roster);
        } else {
            setValue(roster);
        }
    }

    protected boolean updateBlockValue = false;

    public void updateBlockValueOnChange(boolean boo) {
        updateBlockValue = boo;
    }

    public boolean updateBlockValueOnChange() {
        return updateBlockValue;
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryIcon.class.getName());
}
