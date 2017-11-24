package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import jmri.NamedBean;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.ReporterIcon;
import jmri.jmrit.picker.PickListModel;
import jmri.util.JmriJFrame;
import jmri.util.swing.DrawSquares;
import jmri.util.swing.ImagePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReporterItemPanel extends TableItemPanel {

    ReporterIcon _reporter;
    private ImagePanel _iconFamilyPanel;
//    static final Color _grayColor = new Color(235, 235, 235);
//    static final Color _darkGrayColor = new Color(150, 150, 150);
//    protected Color[] colorChoice = new Color[] {Color.white, _grayColor, _darkGrayColor}; // panel bg color picked up directly
//    protected Color _currentBackground = _grayColor;
    protected BufferedImage[] _backgrounds; // array of Image backgrounds

    public ReporterItemPanel(JmriJFrame parentFrame, String type, String family, PickListModel<jmri.Reporter> model, Editor editor) {
        super(parentFrame, type, family, model, editor);
    }

    @Override
    public void init() {
        if (!_initialized) {
            super.init();
//            super.hideBgBoxPanel();
        }
    }

    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        blurb.add(new JLabel(Bundle.getMessage("PickRowReporter")));
        blurb.add(new JLabel(Bundle.getMessage("DragReporter")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    @Override
    protected void initIconFamiliesPanel() {
        _iconFamilyPanel = new ImagePanel();
        _iconFamilyPanel.setOpaque(true);
        _iconFamilyPanel.setLayout(new BoxLayout(_iconFamilyPanel, BoxLayout.Y_AXIS));
        if (!_update) {
            _iconFamilyPanel.add(instructions());
        }

        // create array of backgrounds
        // if (_backgrounds == null) { // reduces load but will not redraw for new size
        _backgrounds = new BufferedImage[5];
        _currentBackground = _editor.getTargetPanel().getBackground(); // start using Panel background color
        _backgrounds[0] = DrawSquares.getImage(_iconFamilyPanel, 20, _currentBackground, _currentBackground);
        for (int i = 1; i <= 3; i++) {
            _backgrounds[i] = DrawSquares.getImage(_iconFamilyPanel, 20, colorChoice[i - 1], colorChoice[i - 1]); // choice 0 is not in colorChoice[]
        }
        _backgrounds[4] = DrawSquares.getImage(_iconFamilyPanel, 20, Color.white, _grayColor);

        // _iconPanel = new ImagePanel();
        // _iconPanel.setOpaque(false);
        // _iconPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1),
        //     Bundle.getMessage("PreviewBorderTitle")));
        // _iconFamilyPanel.add(_iconPanel);
        makeDragIconPanel(1);
        makeDndIconPanel(null, null);
    }

    @Override
    protected void makeBottomPanel(ActionListener doneAction) {
        if (doneAction != null) {
            addUpdateButtonToBottom(doneAction);
        }
        initIconFamiliesPanel();
        add(_iconFamilyPanel);
    }
    
    @Override
    protected void makeDndIconPanel(HashMap<String, NamedIcon> iconMap, String displayKey) {
        if (_update) {
            return;
        }
        _reporter = new ReporterIcon(_editor);
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        JPanel comp;
        try {
            comp = getDragger(new DataFlavor(Editor.POSITIONABLE_FLAVOR));
            comp.setOpaque(false);
            comp.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        } catch (java.lang.ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            comp = new JPanel();
        }
        panel.add(comp);
        panel.revalidate();
        int width = Math.max(100, panel.getPreferredSize().width);
        panel.setPreferredSize(new java.awt.Dimension(width, panel.getPreferredSize().height));
        panel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        _dragIconPanel.add(panel);
        _dragIconPanel.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
        _dragIconPanel.invalidate();
    }

    protected JPanel makeItemButtonPanel() {
        return new JPanel();
    }
//
//    /**
//     * Create panel element containing [Set background:] drop down list.
//     * @see jmri.jmrit.catalog.PreviewDialog#setupPanel()
//     * @see DecoratorPanel
//     * @see FamilyItemPanel
//     *
//     * @return a JPanel with label and drop down
//     */
//    private JPanel makeButtonPanel(ImagePanel preview, BufferedImage[] imgArray) {
//        JComboBox<String> bgColorBox = new JComboBox<>();
//        bgColorBox.addItem(Bundle.getMessage("PanelBgColor")); // PanelColor key is specific for CPE, too long for combo
//        bgColorBox.addItem(Bundle.getMessage("White"));
//        bgColorBox.addItem(Bundle.getMessage("LightGray"));
//        bgColorBox.addItem(Bundle.getMessage("DarkGray"));
//        // bgColorBox.addItem(Bundle.getMessage("Checkers")); // checkers option not yet in combobox, under development
//        bgColorBox.setSelectedIndex(0); // panel bg color
//        bgColorBox.addActionListener((ActionEvent e) -> {
//            // load background image
//            preview.setImage(imgArray[bgColorBox.getSelectedIndex()]);
//            log.debug("Catalog setImage called");
//            preview.setOpaque(false);
//            // preview.repaint();
//            preview.invalidate(); // force redraw
//        });
//
//        JPanel backgroundPanel = new JPanel();
//        backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
//        JPanel pp = new JPanel();
//        pp.setLayout(new FlowLayout(FlowLayout.CENTER));
//        pp.add(new JLabel(Bundle.getMessage("setBackground")));
//        pp.add(bgColorBox);
//        backgroundPanel.add(pp);
//        backgroundPanel.setMaximumSize(backgroundPanel.getPreferredSize());
//        return backgroundPanel;
//    }

    /**
     * ListSelectionListener action from table.
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (_table == null) {
            return;
        }
        int row = _table.getSelectedRow();
        log.debug("Table valueChanged: row = {}", row);
        if (row >= 0) {
            if (_updateButton != null) {
                _updateButton.setEnabled(true);
                _updateButton.setToolTipText(null);
            }
            NamedBean bean = getDeviceNamedBean();
            _reporter.setReporter(bean.getDisplayName());
        } else {
            if (_updateButton != null) {
                _updateButton.setEnabled(false);
                _updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
                _reporter = new ReporterIcon(_editor);
            }
        }
        initIconFamiliesPanel();
        validate();
    }

    @Override
    protected void showIcons() {
    }

    @Override
    protected void setEditor(Editor ed) {
        _family = null;
        super.setEditor(ed);
        if (_initialized) {
            remove(_iconFamilyPanel);
            initIconFamiliesPanel();
            add(_iconFamilyPanel, 1);
            validate();
        }
    }

    protected IconDragJComponent getDragger(DataFlavor flavor) {
        return new IconDragJComponent(flavor, _reporter);
    }

    protected class IconDragJComponent extends DragJComponent {

        public IconDragJComponent(DataFlavor flavor, JComponent comp) {
            super(flavor, comp);
        }
        
        @Override
        protected boolean okToDrag() {
            NamedBean bean = getDeviceNamedBean();
            if (bean == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("noRowSelected"),
                        Bundle.getMessage("WarningTitle"), JOptionPane.WARNING_MESSAGE);
                return false;
            }
            return true;
        }


        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            NamedBean bean = getDeviceNamedBean();
            if (bean == null) {
                return null;
            }

            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                ReporterIcon r = new ReporterIcon(_editor);
                r.setReporter(bean.getDisplayName());
                r.setLevel(Editor.REPORTERS);
                return r;                
            } else if (DataFlavor.stringFlavor.equals(flavor)) {
                StringBuilder sb = new StringBuilder(_itemType);
                sb.append(" icon for \"");
                sb.append(bean.getDisplayName());
                sb.append("\"");
                return  sb.toString();
            }
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ReporterItemPanel.class);

}
