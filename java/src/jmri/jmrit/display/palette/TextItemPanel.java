package jmri.jmrit.display.palette;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.DisplayFrame;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LinkingLabel;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.display.PositionablePopupUtil;
import jmri.jmrit.display.PreviewPanel;
import jmri.jmrit.display.SensorIcon;
import jmri.util.swing.ImagePanel;
import jmri.util.swing.JmriColorChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ItemPanel for text labels.
 * @see ItemPanel palette class diagram
 * @author Pete Cressman Copyright (c) 2010, 2011, 2020
 */
public class TextItemPanel extends ItemPanel {

    private final ImagePanel _samplePanel;
    
    private PositionablePopupUtil _util;
    private final HashMap<String, PositionableLabel> _samples;

    private String _selectedState;
    private boolean _isPositionableLabel;
    private JComponent _textEditComponent;

    private JTabbedPane _tabPane;

    private final ButtonGroup _buttonGroup = new ButtonGroup();
    private AJRadioButton _fontButton;
    private AJRadioButton _borderButton;
    private AJRadioButton _backgroundButton;
    protected int _selectedButton;  // allow access for testing
    protected JColorChooser _chooser;   // allow access for testing

    protected AJSpinner _borderSpin;    // allow testing access
    protected AJSpinner _marginSpin;
    private AJSpinner _widthSpin;
    private AJSpinner _heightSpin;

    public static final int BORDER = 1;
    public static final int MARGIN = 2;
    public static final int FWIDTH = 3;
    public static final int FHEIGHT = 4;

    static final int FOREGROUND_BUTTON = 1;
    static final int BACKGROUND_BUTTON = 2;
    static final int TRANSPARENT_BUTTON = 3;
    static final int BORDERCOLOR_BUTTON = 4;


    /**
     * Constructor for Text Labels.
     *
     * @param parentFrame ItemPalette instance
     * @param type        identifier of the ItemPanel type, should be "Text"
     */
    public TextItemPanel(DisplayFrame parentFrame, String type) {
        super(parentFrame, type);
        setToolTipText(Bundle.getMessage("ToolTipDragText"));
        _samples = new HashMap<>();
        _samplePanel = new ImagePanel();
//        _samplePanel.add(Box.createVerticalStrut(50));
        _samplePanel.setBorder(BorderFactory.createLineBorder(Color.black));
        _samplePanel.setName("SamplePanel");    // to find component for testing
        _samplePanel.setImage(_frame.getPreviewBackground()); 
    }
    JPanel dragger;

    @Override
    public void init() {
        if (!_initialized) {
            add(instructions());
            PositionableLabel label = new PositionableLabel(Bundle.getMessage("sample"), _frame.getEditor());
            label.setLevel(Editor.LABELS);
            JPanel dragger;
            try {
               dragger = new LabelDragJComponent(new DataFlavor(Editor.POSITIONABLE_FLAVOR), label);
            } catch (java.lang.ClassNotFoundException cnfe) {
                log.error("Unable to find class supporting {}", Editor.POSITIONABLE_FLAVOR, cnfe);
                dragger = new JPanel();
            }
            _util = label.getPopupUtility();
            _samples.put("Text", label);
            _selectedState = "Text";
            _isPositionableLabel = true;
            _textEditComponent = makeTextPanel("Text", label, true);
            _samplePanel.add(dragger);
            _previewPanel = new PreviewPanel(_frame, _samplePanel, null, true);
            sampleBgColorChange();
            log.debug("LabelDragJComponent size {} | panel size {}", label.getPreferredSize(), _samplePanel.getPreferredSize());
            finishInit(false);
              dragger.setOpaque(false);
              dragger.setToolTipText(Bundle.getMessage("ToolTipDragIcon"));
            if (log.isDebugEnabled()) {
                log.debug("end init: TextItemPanel size {}", getPreferredSize());
            }
            super.init();
        }
    }

    public void init(ActionListener doneAction, Positionable pos) {
        _update = true;
        _doneAction = doneAction;
        Positionable item = pos.deepClone(); // need copy of PositionableJPanel for PopupUtility
        _util = item.getPopupUtility();
        item.remove();      // don't need copy any more. Removes ghost image of PositionableJPanels
        _isPositionableLabel = (pos instanceof PositionableLabel);
        
        _previewPanel = new PreviewPanel(_frame, _samplePanel, null, false);
        sampleBgColorChange();

        boolean hasTextStates;
        if (pos instanceof SensorIcon && !((SensorIcon)pos).isIcon()) {
            SensorIcon si = (SensorIcon) pos;
            hasTextStates = true;
            if (!si.isIcon() && si.isText()) {
                _tabPane = new JTabbedPane();
                _tabPane.addChangeListener(c -> {
                    TextPanel panel = (TextPanel)_tabPane.getSelectedComponent();
                    _selectedState = panel.getState();
                    log.debug("StateChange");
                    setChooserColor();
                });
                PositionableLabel sample = new PositionableLabel(si.getActiveText(), _frame.getEditor());
                sample.setForeground(si.getTextActive());
                sample.setBackground(si.getBackgroundActive());
                JPanel panel  = doPopupUtility("SensorStateActive", sample, true); // NOI18N
                _tabPane.add(panel, Bundle.getMessage("SensorStateActive"));

                sample = new PositionableLabel(si.getInactiveText(), _frame.getEditor());
                sample.setForeground(si.getTextInActive());
                sample.setBackground(si.getBackgroundInActive());
                panel  = doPopupUtility("SensorStateInactive", sample, true); // NOI18N
                _tabPane.add(panel, Bundle.getMessage("SensorStateInactive"));

                sample = new PositionableLabel(si.getUnknownText(), _frame.getEditor());
                sample.setForeground(si.getTextUnknown());
                sample.setBackground(si.getBackgroundUnknown());
                panel  = doPopupUtility("BeanStateUnknown", sample, true); // NOI18N
                _tabPane.add(panel, Bundle.getMessage("BeanStateUnknown"));

                sample = new PositionableLabel(si.getInconsistentText(), _frame.getEditor());
                sample.setForeground(si.getTextInconsistent());
                sample.setBackground(si.getBackgroundInconsistent());
                panel  = doPopupUtility("BeanStateInconsistent", sample, true); // NOI18N
                _tabPane.add(panel, Bundle.getMessage("BeanStateInconsistent"));
                /*      to be used later!!!
                for (String state : si.getStateNameCollection()) {
                    PositionableLabel sample = new PositionableLabel(si.getActiveText(), _editor);
                    sample.setForeground(si.getTextActive());
                    sample.setBackground(si.getBackgroundActive());
                    JPanel panel  = doPopupUtility(state, sample, true); // NOI18N
                    _tabPane.add(panel, Bundle.getMessage(state));
                }
                */
                _textEditComponent = _tabPane;
            }   // else a non-text SensorIcon cannot be decorated.
        } else { // not a SensorIcon
            hasTextStates = false;
            PositionableLabel sample = new PositionableLabel("", _frame.getEditor());
            sample.setForeground(_util.getForeground());
            sample.setBackground(_util.getBackground());
            PositionablePopupUtil util = sample.getPopupUtility();
            util.setHasBackground(_util.hasBackground());
            boolean addtextField;
            if (pos instanceof PositionableLabel) {
                sample.setText(((PositionableLabel)pos).getUnRotatedText());
                addtextField = !(pos instanceof jmri.jmrit.display.MemoryIcon);
            } else {
                // To display PositionableJPanel types as PositionableLabels, set fixed sizes.
                util.setFixedWidth(pos.getWidth() - 2*_util.getBorderSize());
                util.setFixedHeight(pos.getHeight() - 2*_util.getBorderSize());
                if (pos instanceof jmri.jmrit.display.MemoryInputIcon) {
                    JTextField field = (JTextField) pos.getTextComponent();
                    sample.setText(field.getText());
                    addtextField = false;
                } else if (pos instanceof jmri.jmrit.display.MemoryComboIcon) {
                    JComboBox<String> box = ((jmri.jmrit.display.MemoryComboIcon)pos).getTextComponent();
                    sample.setText(box.getSelectedItem() != null ? box.getSelectedItem().toString() : "");
                    addtextField = false;
                } else if (pos instanceof jmri.jmrit.display.MemorySpinnerIcon) {
                    JTextField field = (JTextField) pos.getTextComponent();
                    sample.setText(field.getText());
                    addtextField = false;
                } else {
                    addtextField = true;
                    log.error("Unknown Postionable Type {}", pos.getClass().getName());
                }
            }
            _textEditComponent = doPopupUtility("Text", sample, addtextField);
            if (log.isDebugEnabled()) {
                log.debug("util width= {} height= {} POS width= {} height= {}",
                        util.getFixedWidth(), util.getFixedHeight(), pos.getWidth(), pos.getHeight());
            }
        }
        finishInit(hasTextStates);
        _bottomPanel = new JPanel();
        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(a -> cancel());
        _bottomPanel.add(cancelButton);
        _bottomPanel.add(makeUpdateButton(_doneAction));
        add(_bottomPanel);
    }

    protected void cancel() {
        _frame.dispose();
    }

    @Override
    protected JPanel instructions() {
        JPanel blurb = new JPanel();
        blurb.setLayout(new BoxLayout(blurb, BoxLayout.Y_AXIS));
        blurb.add(new JLabel(Bundle.getMessage("addTextAndAttrs")));
        blurb.add(new JLabel(Bundle.getMessage("ToolTipDragText")));
        blurb.add(Box.createVerticalStrut(ItemPalette.STRUT_SIZE));
        JPanel panel = new JPanel();
        panel.add(blurb);
        return panel;
    }

    @Override
    protected void previewColorChange() {
        _previewPanel.setBackgroundSelection(_frame.getPreviewBg());
    }

    protected JPanel makeDoneButtonPanel(ActionListener doneAction) {
        JPanel panel = new JPanel();
        JButton updateButton = new JButton(Bundle.getMessage("updateButton")); // custom update label
        updateButton.addActionListener(doneAction);
        updateButton.setToolTipText(Bundle.getMessage("ToolTipPickFromTable"));
        panel.add(updateButton);

        JButton cancelButton = new JButton(Bundle.getMessage("ButtonCancel"));
        cancelButton.addActionListener(a -> closeDialogs());
        panel.add(cancelButton);
        return panel;
    }

    public void updateAttributes(PositionableLabel l) {
        setAttributes(l);
        PositionablePopupUtil util = getPositionablePopupUtil();
        l.setPopupUtility(util.clone(l, l.getTextComponent()));
        l.setFont(util.getFont().deriveFont(util.getFontStyle()));
        if (util.hasBackground()) { // unrotated
            l.setOpaque(true);
        }
    }

    @Override
    protected JPanel makeSpecialBottomPanel(boolean update) {return null;}
    @Override
    protected JPanel makeItemButtonPanel() {return null;}
    @Override
    protected JPanel makeIconDisplayPanel(String k, HashMap<String, NamedIcon> m, boolean d) {return null;}
    @Override
    protected void initIconFamiliesPanel() {}
    @Override
    protected void hideIcons() {}
    @Override
    protected void makeFamiliesPanel() {}


    protected class LabelDragJComponent extends DragJComponent {

        public LabelDragJComponent(DataFlavor flavor, JComponent comp) {
            super(flavor, comp);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }

            if (flavor.isMimeTypeEqual(Editor.POSITIONABLE_FLAVOR)) {
                String link = _linkName.getText().trim();
                PositionableLabel sample = (PositionableLabel)getThing();
                PositionableLabel label;
                if (link.length() == 0) {
                    label = new PositionableLabel(sample.getText(), sample.getEditor());
                } else {
                    label = new LinkingLabel(sample.getText(), sample.getEditor(), link);
                }
                updateAttributes(label);
                label.setLevel(sample.getDisplayLevel());
                return label;
            }
            return null;
        }
    }

    /*************************************************************************/
    static class AJSpinner extends JSpinner {
        int _which;

        AJSpinner(SpinnerModel model, int which) {
            super(model);
            _which = which;
        }
    }

    static class AJRadioButton extends JRadioButton {
        int _which;

        AJRadioButton(String text, int which) {
            super(text);
            _which = which;
        }
    }

    static class TextPanel extends JPanel {
        String _state;
        TextPanel(String state) {
            _state = state;
        }
        String getState() {
            return _state;
        }
    }


    private void finishInit(boolean addCaption) {
        _selectedButton = FOREGROUND_BUTTON;
        if (addCaption) {
            _selectedState = "SensorStateActive";
        } else {
            _selectedState = "Text";
        }
        JPanel colorButtons = makeColorPanel(addCaption);
        makeColorChooser();
        JPanel panel = makeFontPanel();
        if (addCaption) {
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("StateTextBlurb1")));
            panel.add(p);
        }
        this.add(panel);
        if (addCaption) {
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("StateTextBlurb3")));
            this.add(p);
        }
        JPanel textEditPanel = new JPanel();
        textEditPanel.setLayout(new BoxLayout(textEditPanel, BoxLayout.Y_AXIS));
        textEditPanel.add(_textEditComponent);
        this.add(textEditPanel);
        this.add(_previewPanel);
        this.add(Box.createVerticalGlue());
        this.add(colorButtons);
        this.add(_chooser);
    }

    private JPanel doPopupUtility(String type, PositionableLabel sample, boolean editText) {
        PositionablePopupUtil util = sample.getPopupUtility();
        util.setJustification(_util.getJustification());
        util.setHorizontalAlignment(_util.getJustification());
        if (_isPositionableLabel) {
            int size = _util.getFixedWidth();
            util.setFixedWidth(size);
            size = _util.getFixedHeight();
            util.setFixedHeight(size);
        }
        util.setMargin(_util.getMargin());
        util.setBorderSize(_util.getBorderSize());
        util.setBorderColor(_util.getBorderColor());
        util.setFont(util.getFont().deriveFont(_util.getFontStyle()));
        util.setFontSize(_util.getFontSize());
        util.setFontStyle(_util.getFontStyle());
        util.setOrientation(_util.getOrientation());
        boolean back = (sample.getBackground() != null);
        util.setHasBackground(back);
        sample.setOpaque(back);
        sample.updateSize();

        _samples.put(type, sample);
        _selectedState = type;
        JPanel panel = makeTextPanel(type, sample, editText);
        _samplePanel.add(sample);
        return panel;
    }

    private void makeColorChooser() {
        Color panelBackground = _frame.getEditor().getTargetPanel().getBackground(); // start using Panel background color
        _chooser = JmriColorChooser.extendColorChooser(new JColorChooser(panelBackground));
        _chooser.getSelectionModel().addChangeListener(c -> chooserColorChange());
        _chooser.setPreviewPanel(new JPanel());
        JmriColorChooser.suppressAddRecentColor(true);
    }

    private void fontChange() {
        log.debug("fontChange");
        if (_selectedButton != FOREGROUND_BUTTON) {
            _selectedButton = FOREGROUND_BUTTON;
            _fontButton.setSelected(true);  // will change chooser color, if needed
        }
        updateSamples();
     }

    private class SpinnerChangeListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            Object obj = e.getSource();
            int num = ((Number) ((AJSpinner) obj).getValue()).intValue();
            int which = ((AJSpinner) obj)._which;
            switch (which) {
                case BORDER:
                    _util.setBorderSize(num);
                    _borderButton.setSelected(true);
                    _selectedButton = BORDERCOLOR_BUTTON;
                    break;
                case MARGIN:
                    _util.setMargin(num);
                    _selectedButton = BACKGROUND_BUTTON;
                    _backgroundButton.setSelected(true);
                    break;
                case FWIDTH:
                    _util.setFixedWidth(num);
                    _selectedButton = BACKGROUND_BUTTON;
                    _backgroundButton.setSelected(true);
                    break;
                case FHEIGHT:
                    _util.setFixedHeight(num);
                    _selectedButton = BACKGROUND_BUTTON;
                    _backgroundButton.setSelected(true);
                    break;
                default:
                    log.warn("Unexpected _which {}  in stateChanged", ((AJSpinner) obj)._which);
                    break;
            }
            updateSamples();
            log.debug("SpinnerChange which= {}, num= {}", which, num);
        }
    }

    private JPanel makeFontPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("FontDisplaySettings")));
        
        ActionListener fontAction = ((ActionEvent event) -> {
            fontChange(); // callback
        });
        FontPanel fontPanel = new FontPanel(_util, fontAction);
        panel.add(fontPanel);
        fontPanel.setFontSelections();

        ChangeListener listener = new SpinnerChangeListener();
        JPanel sizePanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(_util.getBorderSize(), 0, 100, 1);
        _borderSpin = new AJSpinner(model, BORDER);
        sizePanel.add(makeSpinPanel("borderSize", _borderSpin, listener));
        model = new SpinnerNumberModel(_util.getMargin(), 0, 100, 1);
        _marginSpin = new AJSpinner(model, MARGIN);
        sizePanel.add(makeSpinPanel("marginSize", _marginSpin, listener));
        if (_isPositionableLabel) {
            model = new SpinnerNumberModel(_util.getFixedWidth(), 0, 1000, 1);
            _widthSpin = new AJSpinner(model, FWIDTH);
            sizePanel.add(makeSpinPanel("fixedWidth", _widthSpin, listener));
            model = new SpinnerNumberModel(_util.getFixedHeight(), 0, 1000, 1);
            _heightSpin = new AJSpinner(model, FHEIGHT);
            sizePanel.add(makeSpinPanel("fixedHeight", _heightSpin, listener));
        }
        panel.add(sizePanel);
        return panel;
    }

    public static JPanel makeSpinPanel(String caption, JSpinner spin, ChangeListener listener) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(Bundle.getMessage(caption)));
        spin.addChangeListener(listener);
        panel.add(spin);
        return panel;
    }

    private JPanel makeTextPanel(String beanState, JLabel sample, boolean addTextField) {
        JPanel panel = new TextPanel(beanState);
        // use NamedBeanBundle property for captions
        // sample is the preview JLable
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage(beanState)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        if (addTextField) {
            JTextField textField = new JTextField(sample.getText(), 25);
            textField.addKeyListener(new KeyListener() {
                JLabel sample;

                KeyListener init(JLabel s) {
                    sample = s;
                    return this;
                }

                @Override
                public void keyTyped(KeyEvent evt) {
                }

                @Override
                public void keyPressed(KeyEvent evt) {
                }

                @Override
                public void keyReleased(KeyEvent evt) {
                    JTextField tmp = (JTextField) evt.getSource();
                    sample.setText(tmp.getText());
                    updateSamples();
                }
            }.init(sample));
            p.add(textField);
            panel.add(p);
        }
        return panel;
    }

    private JPanel makeColorPanel(boolean addCaption) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.black, 1),
                Bundle.getMessage("ColorDisplaySettings")));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        _fontButton = makeColorRadioButton("FontColor", FOREGROUND_BUTTON);
        buttonPanel.add(_fontButton);
        
        _backgroundButton = makeColorRadioButton("FontBackgroundColor", BACKGROUND_BUTTON);
        buttonPanel.add(_backgroundButton);
        
        AJRadioButton button = makeColorRadioButton("transparentBack", TRANSPARENT_BUTTON);
        buttonPanel.add(button);

        _borderButton = makeColorRadioButton("borderColor", BORDERCOLOR_BUTTON);
        buttonPanel.add(_borderButton);        

        panel.add(buttonPanel);
        if (addCaption) {
            JPanel p = new JPanel();
            p.add(new JLabel(Bundle.getMessage("StateTextBlurb2")));
            panel.add(p);
        }
        _fontButton.setSelected(true);
        _selectedButton = FOREGROUND_BUTTON;
        return panel;
    }

    private AJRadioButton makeColorRadioButton(String caption, int which) {
        AJRadioButton button = new AJRadioButton(Bundle.getMessage(caption), which);
        button.addActionListener(a -> {
            if (button.isSelected()) {
                _selectedButton = button._which;
                log.debug("Button #{} selected.", which);
                setChooserColor();
            }
        });
        _buttonGroup.add(button);            
        return button;
    }

    private void chooserColorChange() {
        PositionableLabel pos =_samples.get(_selectedState);
        PositionablePopupUtil util = pos.getPopupUtility();
        switch (_selectedButton) {
            case FOREGROUND_BUTTON:
                util.setForeground(_chooser.getColor());
                break;
            case BACKGROUND_BUTTON:
                pos.setOpaque(true);
                util.setBackgroundColor(_chooser.getColor());
                break;
            case TRANSPARENT_BUTTON:
//                util.setBackgroundColor(null);
                pos.setOpaque(false);
                break;
            case BORDERCOLOR_BUTTON:
                _util.setBorderColor(_chooser.getColor());      // will remove later!!!
                util.setBorderColor(_chooser.getColor());
                break;
            default:
                log.warn("Unexpected color change for state {}, button# {}", _selectedState, _selectedButton);
                break;
        }
        log.debug("chooserColorChange opaque= {} _selectedState= {} _selectedButton= {} color= {}",
                pos.isOpaque(), _selectedState, _selectedButton, _chooser.getColor().toString());
        updateSamples();
    }

    private void setChooserColor() {
        PositionableLabel pos =_samples.get(_selectedState);
        PositionablePopupUtil util = pos.getPopupUtility();
        Color c = null;
        switch (_selectedButton) {
            case FOREGROUND_BUTTON:
                c = util.getForeground();
                break;
            case BACKGROUND_BUTTON:
                c = util.getBackground();
                break;
            case BORDERCOLOR_BUTTON:
                c = util.getBorderColor();
                break;
            case TRANSPARENT_BUTTON:
                util.setBackgroundColor(null);
                updateSamples();
                break;
            default:
                return;
        }
        _chooser.setColor(c);
    }

    protected void updateSamples() {
        boolean isPalette = (_frame instanceof ItemPalette);
        Dimension totalDim = _frame.getSize();
        Dimension oldDim = getSize();

        int mar = _util.getMargin();
        int bor = _util.getBorderSize();
        Border outlineBorder;
        Font font = _util.getFont();
        int just = _util.getJustification();

        for (PositionableLabel sam : _samples.values()) {
            PositionablePopupUtil util = sam.getPopupUtility();
            sam.setFont(font);
            if (_isPositionableLabel) {
                util.setFixedWidth(_util.getFixedWidth());
                util.setFixedHeight(_util.getFixedHeight());
            } else {
                util.setFixedWidth(util.getFixedWidth() + 2 * (mar - util.getMargin()) /*+ bor - util.getBorderSize()*/);
                util.setFixedHeight(util.getFixedHeight() + 2 * (mar - util.getMargin()) /*+ bor - util.getBorderSize()*/);
            }
            util.setMargin(mar);
            util.setBorderSize(bor);
            Border borderMargin;
            if (sam.isOpaque()) {
                borderMargin = new LineBorder(sam.getBackground(), mar);
            } else {
                borderMargin = BorderFactory.createEmptyBorder(mar, mar, mar, mar);
            }
            if (bor == 0) {
                outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
            } else {
                outlineBorder = new LineBorder(_util.getBorderColor(), bor);      // will remove later!!!
//                outlineBorder = new LineBorder(util.getBorderColor(), bor);   // will use this
            }
            sam.setBorder(new CompoundBorder(outlineBorder, borderMargin));

            switch (just) {
                case PositionablePopupUtil.LEFT:
                    sam.setHorizontalAlignment(JLabel.LEFT);
                    break;
                case PositionablePopupUtil.RIGHT:
                    sam.setHorizontalAlignment(JLabel.RIGHT);
                    break;
                default:
                    sam.setHorizontalAlignment(JLabel.CENTER);
            }
            sam.updateSize();
            sam.setPreferredSize(sam.getSize());
            sam.invalidate();
            if (log.isTraceEnabled()) {
                log.debug("util width= {} height= {} SAM width= {} height= {}", util.getFixedWidth(), util.getFixedHeight(), sam.getWidth(), sam.getHeight());
                log.debug("margin = {}, border = {}, opaque= {}", util.getMargin(), util.getBorderSize(), sam.isOpaque());
            }
        }
        _previewPanel.invalidate();
        _textEditComponent.invalidate();
        if (dragger != null) {
            dragger.invalidate();
        }
        _samplePanel.invalidate();
        reSizeDisplay(isPalette, oldDim, totalDim);
    }

    // called when editor changed
    protected void sampleBgColorChange() {
        _previewPanel.setBackgroundSelection(_frame.getPreviewBg());
    }

    public PositionablePopupUtil getPositionablePopupUtil() {
        PositionableLabel pos =_samples.get(_selectedState);
        PositionablePopupUtil util = pos.getPopupUtility();
        _util.setForeground(util.getForeground());
        _util.setBackgroundColor(util.getBackground());
        _util.setBorderColor(util.getBorderColor());
        return _util;
    }

    public void setAttributes(Positionable pos) {
        if (pos instanceof SensorIcon  && !((SensorIcon)pos).isIcon()) {
            SensorIcon icon = (SensorIcon) pos;
            PositionableLabel sample = _samples.get("SensorStateActive");
            if (sample.isOpaque()) {
                icon.setBackgroundActive(sample.getBackground());
            } else {
                icon.setBackgroundActive(null);
            }
            icon.setTextActive(sample.getForeground());
            icon.setActiveText(sample.getText());

            sample = _samples.get("SensorStateInactive");
            icon.setInactiveText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundInActive(sample.getBackground());
            } else {
                icon.setBackgroundInActive(null);
            }
            icon.setTextInActive(sample.getForeground());

            sample = _samples.get("BeanStateUnknown");
            icon.setUnknownText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundUnknown(sample.getBackground());
            } else {
                icon.setBackgroundUnknown(null);
            }
            icon.setTextUnknown(sample.getForeground());

            sample = _samples.get("BeanStateInconsistent");
            icon.setInconsistentText(sample.getText());
            if (sample.isOpaque()) {
                icon.setBackgroundInconsistent(sample.getBackground());
            } else {
                icon.setBackgroundInconsistent(null);
            }
            icon.setTextInconsistent(sample.getForeground());
        } else {
            PositionableLabel sample = _samples.get("Text");
            if ( pos instanceof PositionableLabel &&
                !(pos instanceof jmri.jmrit.display.MemoryIcon)) {
                ((PositionableLabel) pos).setText(sample.getText());
            }
            PositionablePopupUtil posUtil = pos.getPopupUtility();
            PositionablePopupUtil samUtil = sample.getPopupUtility();
            if (sample.isOpaque()) {
                posUtil.setBackgroundColor(samUtil.getBackground());
            } else {
                posUtil.setBackgroundColor(null);
            }
            posUtil.setHasBackground(samUtil.hasBackground());
            posUtil.setForeground(samUtil.getForeground());
            posUtil.setBorderColor(samUtil.getBorderColor());
            posUtil.setFont(samUtil.getFont());
            posUtil.setFixedWidth(samUtil.getFixedWidth());
            posUtil.setFixedHeight(samUtil.getFixedHeight());
            posUtil.setMargin(samUtil.getMargin());
            posUtil.setBorderSize(samUtil.getBorderSize());
        }
        pos.invalidate();
    }

    public void close() {
        JmriColorChooser.suppressAddRecentColor(false);
    }

    private final static Logger log = LoggerFactory.getLogger(TextItemPanel.class);

}
