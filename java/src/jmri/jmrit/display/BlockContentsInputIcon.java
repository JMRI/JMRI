package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.annotation.Nonnull;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import jmri.InstanceManager;
import jmri.Block;
import jmri.NamedBeanHandle;
import jmri.NamedBean.DisplayOptions;
import jmri.util.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An icon to display and input a Block contents value in a TextField.
 * <p>
 * Handles the case of either a String or an Integer in the Block, preserving
 * what it finds.
 *
 * Cloned from MemoryInputIcon by Pete Cressman
 *
 * @author Dave Sand Copyright (c) 2026
 * @since 5.15.4
 */
public class BlockContentsInputIcon extends PositionableJPanel implements java.beans.PropertyChangeListener {

    JTextField _textBox = new JTextField();
    int _nCols;

    // the associated Block object
    private NamedBeanHandle<Block> namedBlock;

    private final java.awt.event.MouseListener _mouseListener = JmriMouseListener.adapt(this);
    private final java.awt.event.MouseMotionListener _mouseMotionListener = JmriMouseMotionListener.adapt(this);

    public BlockContentsInputIcon(int nCols, Editor editor) {
        super(editor);
        _nCols = nCols;
        setDisplayLevel(Editor.LABELS);

        setLayout(new java.awt.GridBagLayout());
        add(_textBox, new java.awt.GridBagConstraints());
        _textBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int key = e.getKeyCode();
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_TAB) {
                    updateBlock();
                }
            }
        });
        _textBox.setColumns(_nCols);
        _textBox.addMouseMotionListener(_mouseMotionListener);
        _textBox.addMouseListener(_mouseListener);
        setPopupUtility(new PositionablePopupUtil(this, _textBox));
    }

    @Override
    public Positionable deepClone() {
        BlockContentsInputIcon pos = new BlockContentsInputIcon(_nCols, _editor);
        return finishClone(pos);
    }

    protected Positionable finishClone(BlockContentsInputIcon pos) {
        pos.setBlock(namedBlock.getName());
        return super.finishClone(pos);
    }

    @Override
    public JComponent getTextComponent() {
        return _textBox;
    }

    @Override
    public void mouseExited(JmriMouseEvent e) {
        updateBlock();
        super.mouseExited(e);
    }

    /**
     * Attached a named Block to this display item
     *
     * @param pName Used as a system/user name to lookup the Block object
     */
    public void setBlock(String pName) {
        log.debug("setBlock for block = {}", pName);
        if (InstanceManager.getNullableDefault(jmri.BlockManager.class) != null) {
            try {
                Block block = InstanceManager.getDefault(jmri.BlockManager.class).provideBlock(pName);
                setBlock(jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(pName, block));
            } catch (IllegalArgumentException e) {
                log.error("Block '{}' not available, icon won't see changes", pName);
            }
        } else {
            log.error("No BlockManager for this protocol, icon won't see changes");
        }
        updateSize();
    }

    /**
     * Attached a named Block to this display item
     *
     * @param b The Block object
     */
    public void setBlock(NamedBeanHandle<Block> b) {
        if (namedBlock != null) {
            getBlock().removePropertyChangeListener(this);
        }
        namedBlock = b;
        if (namedBlock != null) {
            getBlock().addPropertyChangeListener(this, namedBlock.getName(), "Block Input Icon");
            displayState();
            setName(namedBlock.getName());
        }
    }

    public void setNumColumns(int nCols) {
        _textBox.setColumns(nCols);
        _nCols = nCols;
    }

    public NamedBeanHandle<Block> getNamedBlock() {
        return namedBlock;
    }

    public Block getBlock() {
        if (namedBlock == null) {
            return null;
        }
        return namedBlock.getBean();
    }

    public int getNumColumns() {
        return _nCols;
    }

    // update icon as state of Block changes
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("value")) {
            displayState();
        }
    }

    @Override
    @Nonnull
    public String getTypeString() {
        return Bundle.getMessage("PositionableType_BlockContentsInputIcon");
    }

    @Override
    public String getNameString() {
        String name;
        if (namedBlock == null) {
            name = Bundle.getMessage("NotConnected");
        } else {
            name = getBlock().getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        }
        return name;
    }

    @Override
    public void mouseMoved(JmriMouseEvent e) {
        updateBlock();
    }

    private void updateBlock() {
        if (namedBlock == null) {
            return;
        }
        String str = _textBox.getText();
        getBlock().setValue(str);
    }

    @Override
    public boolean setEditIconMenu(javax.swing.JPopupMenu popup) {
        String txt = java.text.MessageFormat.format(Bundle.getMessage("EditItem"), Bundle.getMessage("BeanNameBlock"));
        popup.add(new javax.swing.AbstractAction(txt) {
            @Override
            public void actionPerformed(ActionEvent e) {
                edit();
            }
        });
        return true;
    }

    /**
     * Popup menu iconEditor's ActionListener
     */
    SpinnerNumberModel _spinModel = new SpinnerNumberModel(3, 1, 100, 1);

    @Override
    protected void edit() {
        _iconEditor = new IconAdder("Block") {
            final JSpinner spinner = new JSpinner(_spinModel);

            @Override
            protected void addAdditionalButtons(JPanel p) {
                ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(2);
                spinner.setMaximumSize(spinner.getPreferredSize());
                spinner.setValue(_textBox.getColumns());
                JPanel p2 = new JPanel();
                //p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
                //p2.setLayout(new FlowLayout(FlowLayout.TRAILING));
                p2.add(new JLabel(Bundle.getMessage("NumColsLabel")));
                p2.add(spinner);
                p.add(p2);
                p.setVisible(true);
            }
        };

        makeIconEditorFrame(this, "Block", true, _iconEditor);
        _iconEditor.setPickList(jmri.jmrit.picker.PickListModel.blockPickModelInstance());
        ActionListener addIconAction = a -> editBlock();
        _iconEditor.makeIconPanel(false);
        _iconEditor.complete(addIconAction, false, true, true);
        _iconEditor.setSelection(getBlock());
    }

    void editBlock() {
        setBlock(_iconEditor.getTableSelection().getDisplayName());
        _nCols = _spinModel.getNumber().intValue();
        _textBox.setColumns(_nCols);
        setSize(getPreferredSize().width + 1, getPreferredSize().height);
        _iconEditorFrame.dispose();
        _iconEditorFrame = null;
        _iconEditor = null;
        validate();
    }

    /**
     * Drive the current state of the display from the state of the Block.
     */
    public void displayState() {
        log.debug("displayState");
        if (namedBlock == null) {  // leave alone if not connected yet
            return;
        }
        Object show = getBlock().getValue();
        if (show != null) {
            _textBox.setText(show.toString());
        } else {
            _textBox.setText("");
        }
    }

    @Override
    void cleanup() {
        if (namedBlock != null) {
            getBlock().removePropertyChangeListener(this);
        }
        if (_textBox != null) {
            _textBox.removeMouseMotionListener(_mouseMotionListener);
            _textBox.removeMouseListener(_mouseListener);
        }
        namedBlock = null;
    }

    private final static Logger log = LoggerFactory.getLogger(BlockContentsInputIcon.class);
}
