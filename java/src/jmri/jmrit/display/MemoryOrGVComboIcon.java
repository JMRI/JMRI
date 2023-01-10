package jmri.jmrit.display;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * An icon to display and input a Memory value in a TextField.
 * <p>
 * Handles the case of either a String or an Integer in the Memory, preserving
 * what it finds.
 *
 * @author Pete Cressman    Copyright (c) 2012
 * @author Daniel Bergqvist Copyright (C) 2022
 * @since 5.3.1
 */
public abstract class MemoryOrGVComboIcon extends PositionableJPanel {

    public MemoryOrGVComboIcon(Editor editor) {
        super(editor);
    }

    @Override
    public abstract JComboBox<String> getTextComponent();

    protected abstract ComboModel getComboModel();

    protected abstract void update();


    protected class ComboModel extends DefaultComboBoxModel<String> {

        ComboModel() {
            super();
        }

        ComboModel(String[] l) {
            super(l);
        }

        @Override
        public void addElement(String obj) {
            if (getIndexOf(obj) >= 0) {
                return;
            }
            super.addElement(obj);
            update();
        }

        @Override
        public void insertElementAt(String obj, int idx) {
            if (getIndexOf(obj) >= 0) {
                return;
            }
            super.insertElementAt(obj, idx);
            update();
        }
    }

}
