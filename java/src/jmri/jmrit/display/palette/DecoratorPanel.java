package jmri.jmrit.display.palette;

import org.apache.log4j.Logger;
import java.awt.Color;
import java.awt.Font;
//import java.awt.Dimension;

//import java.awt.event.ActionListener;
//import java.awt.event.ActionEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import java.awt.datatransfer.Transferable; 
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;
//import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.*;
import javax.swing.border.Border;

import jmri.jmrit.display.*;
//import jmri.jmrit.display.PositionablePropertiesUtil.TextDetails;

/**
*  ItemPanel for text labels
*/
public class DecoratorPanel extends JPanel implements ChangeListener, ItemListener {

    static final String[] JUSTIFICATION = {ItemPalette.rbp.getString("left"), 
                                            ItemPalette.rbp.getString("center"),
                                            ItemPalette.rbp.getString("right")};

    static final String[] STYLES = {ItemPalette.rbp.getString("plain"), 
                                    ItemPalette.rbp.getString("bold"),
                                    ItemPalette.rbp.getString("italic"),
                                    ItemPalette.rbp.getString("bold/italic")};

    static final String[] FONTSIZE = {"6", "8", "10", "11", "12", "14", "16",
                                        "20", "24", "28", "32", "36"};
    public static final int SIZE = 1;
    public static final int STYLE = 2;
    public static final int JUST = 3;

    AJComboBox _fontSizeBox;
    AJComboBox _fontStyleBox;
    AJComboBox _fontJustBox;
    
    public static final int STRUT = 10;

    public static final int BORDER = 1;
    public static final int MARGIN = 2;
    public static final int FWIDTH = 3;
    public static final int FHEIGHT = 4;

    public static final int TEXT_FONT = 10;
    public static final int ACTIVE_FONT = 11;
    public static final int INACTIVE_FONT = 12;
    public static final int UNKOWN_FONT = 13;
    public static final int INCONSISTENT_FONT = 14;
    public static final int TEXT_BACKGROUND = 20;
    public static final int ACTIVE_BACKGROUND = 21;
    public static final int INACTIVE_BACKGROUND = 22;
    public static final int UNKOWN_BACKGROUND = 23;
    public static final int INCONSISTENT_BACKGROUND = 24;
    public static final int TRANSPARENT_COLOR =31;
    public static final int BORDER_COLOR =32;
    
    AJSpinner _borderSpin;
    AJSpinner _marginSpin; 
    AJSpinner _widthSpin;
    AJSpinner _heightSpin;

    JColorChooser _chooser;
    private PositionablePopupUtil _util;
    boolean _isOpaque;			// transfer opaqueness from decorator label here to panel label being edited
    private Hashtable <String, PositionableLabel> _sample = null;
    private ButtonGroup _buttonGroup;
    private int _selectedButton;
 
    Editor _editor;
    
    public DecoratorPanel(Editor editor) {
        _editor = editor;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    static class AJComboBox extends JComboBox {
        int _which;
        AJComboBox(String[] items, int which) {
            super(items);
            _which = which;
        }
    }

    private JPanel makeBoxPanel(String caption, JComboBox box) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(ItemPalette.rbp.getString(caption)));
        box.addItemListener(this);
        panel.add(box);
        return panel;
    }

    static class AJSpinner extends JSpinner {
        int _which;

        AJSpinner(SpinnerModel model, int which) {
            super(model);
            _which = which;
        }
    }

    static class AJRadioButton extends JRadioButton {
        int which;

        AJRadioButton(String text, int w) {
            super(text);
            which = w;
        }
    }

    private JPanel makeSpinPanel(String caption, JSpinner spin) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(ItemPalette.rbp.getString(caption)));
        spin.addChangeListener(this);
        panel.add(spin);
        return panel;
    }

    public void initDecoratorPanel(Positionable pos) {
        _chooser = new JColorChooser(_editor.getTargetPanel().getBackground());
    	JPanel samplePanel = new JPanel();
    	JPanel preview = new JPanel();
    	preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));
    	preview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 1)));
    	preview.add(Box.createVerticalStrut(STRUT));
    	preview.add(samplePanel);
    	preview.add(Box.createVerticalStrut(STRUT));
    	preview.setBackground(_editor.getTargetPanel().getBackground());
        samplePanel.setBackground(_editor.getTargetPanel().getBackground());
        String text = ItemPalette.rbp.getString("sample");
       _sample = new Hashtable <String, PositionableLabel>();       
       _buttonGroup = new ButtonGroup();
       Positionable item;		// copy of PositionableLabel being edited
        if (pos==null) {
        	item = new PositionableLabel("crap", null);
        	_util = item.getPopupUtility();
        } else {
    		item = pos.deepClone();
        	if (pos instanceof PositionableLabel) {
//        		PositionableLabel item = (PositionableLabel)pos.deepClone();
                _isOpaque = item.getSaveOpaque();
            	item.rotate(0);
            	PositionablePopupUtil u = item.getPopupUtility();
                text = ((PositionableLabel)item).getUnRotatedText();
                _util = item.getPopupUtility();
        		
                PositionableLabel p = (PositionableLabel)pos;
        		if (p.isRotated()) {
            		_isOpaque = pos.getSaveOpaque();     			
        		} else {
            		_isOpaque = pos.isOpaque();
        		}
        	} else {
        		_isOpaque = pos.isOpaque();        		
        		//_util = pos.getPopupUtility().clone(pos);
                _util = item.getPopupUtility();
        	}
         }
        samplePanel.add(Box.createHorizontalStrut(STRUT));
        if (pos instanceof SensorIcon) {
        	SensorIcon si = (SensorIcon)pos;
        	if (!si.isIcon() && si.isText()) {
                PositionableLabel sample = new PositionableLabel(si.getActiveText(), _editor);
                sample.setForeground(si.getTextActive());
                sample.setBackground(si.getBackgroundActive());
                sample.setPopupUtility(_util);
                _sample.put("Active", sample);
                samplePanel.add(sample);
                samplePanel.add(Box.createHorizontalStrut(STRUT));
                this.add(makeTextPanel("Active", sample, ACTIVE_FONT));
                
                sample = new PositionableLabel(si.getInactiveText(), _editor);
                sample.setForeground(si.getTextInActive());
                sample.setBackground(si.getBackgroundInActive());
                sample.setPopupUtility(_util);
                _sample.put("InActive", sample);
                samplePanel.add(sample);
                samplePanel.add(Box.createHorizontalStrut(STRUT));
                this.add(makeTextPanel("InActive", sample, INACTIVE_FONT));
                
                sample = new PositionableLabel(si.getUnknownText(), _editor);
                sample.setForeground(si.getTextUnknown());
                sample.setBackground(si.getBackgroundUnknown());
                sample.setPopupUtility(_util);
                _sample.put("Unknown", sample);
                samplePanel.add(sample);
                samplePanel.add(Box.createHorizontalStrut(STRUT));
                this.add(makeTextPanel("Unknown", sample, UNKOWN_FONT));
                
                sample = new PositionableLabel(si.getInconsistentText(), _editor);
                sample.setForeground(si.getTextInconsistent());
                sample.setBackground(si.getBackgroundInconsistent());
                sample.setPopupUtility(_util);
                _sample.put("Inconsistent", sample);
                samplePanel.add(sample);
                this.add(makeTextPanel("Inconsistent", sample, INCONSISTENT_FONT));
        	} 
        } else {
        	PositionableLabel sample;
        	if (pos==null) {
        		sample = new DragDecoratorLabel(text, _editor);
        		sample.setDisplayLevel(Editor.LABELS);
                sample.setPopupUtility(_util);
        	} else {
        		sample = new PositionableLabel(text, _editor);
                sample.setPopupUtility(_util);
                sample.setForeground(pos.getForeground());
                sample.setBackground(pos.getBackground());
                _util.setBackgroundColor(pos.getBackground());
                sample.setOpaque(true);
            }
            _sample.put("Text", sample);
            samplePanel.add(sample);
            this.add(makeTextPanel("Text", sample, TEXT_FONT));
        }
        samplePanel.add(Box.createHorizontalStrut(STRUT));
        
        JPanel fontPanel = new JPanel();
        _fontSizeBox = new AJComboBox(FONTSIZE, SIZE);
        fontPanel.add(makeBoxPanel("fontSize", _fontSizeBox));
        int row = 4;
        for (int i=0; i<FONTSIZE.length; i++) {
            if (_util.getFontSize()==Integer.parseInt(FONTSIZE[i])) {
                row = i;
                break;
            }
        }
        _fontSizeBox.setSelectedIndex(row);

        _fontStyleBox = new AJComboBox(STYLES, STYLE);
        fontPanel.add(makeBoxPanel("fontStyle", _fontStyleBox));
        _fontStyleBox.setSelectedIndex(_util.getFont().getStyle());

        _fontJustBox = new AJComboBox(JUSTIFICATION, JUST);
        fontPanel.add(makeBoxPanel("justification", _fontJustBox));
        switch (_util.getJustification()){
            case PositionablePopupUtil.LEFT:     row = 0;
                            break;
            case PositionablePopupUtil.RIGHT:    row = 2;
                            break;
            case PositionablePopupUtil.CENTRE:   row = 1;
                            break;
            default     :   row = 2;
        }
        _fontJustBox.setSelectedIndex(row);
        this.add(fontPanel);
        

        JPanel sizePanel = new JPanel();
        SpinnerNumberModel model = new SpinnerNumberModel(_util.getBorderSize(),0,100,1);
        _borderSpin = new AJSpinner(model, BORDER);
        sizePanel.add(makeSpinPanel("borderSize", _borderSpin));
        model = new SpinnerNumberModel(_util.getMargin(),0,100,1);
        _marginSpin = new AJSpinner(model, MARGIN);
        sizePanel.add(makeSpinPanel("marginSize", _marginSpin));
        model = new SpinnerNumberModel(_util.getFixedWidth(),0,1000,1);
        _widthSpin = new AJSpinner(model, FWIDTH);
        sizePanel.add(makeSpinPanel("fixedWidth", _widthSpin));
        model = new SpinnerNumberModel(_util.getFixedHeight(),0,1000,1);
        _heightSpin = new AJSpinner(model, FHEIGHT);
        sizePanel.add(makeSpinPanel("fixedHeight", _heightSpin));
        this.add(sizePanel);

        JPanel colorPanel = new JPanel();
        colorPanel.add(makeButton(new AJRadioButton(ItemPalette.rbp.getString("borderColor"), BORDER_COLOR)));
        //colorPanel.add(makeButton(new AJRadioButton(ItemPalette.rbp.getString("transparentBack"), TRANSPARENT_COLOR)));
        JRadioButton button = new AJRadioButton(ItemPalette.rbp.getString("transparentBack"), TRANSPARENT_COLOR);
        button.addActionListener(new ActionListener() {
        	JRadioButton button ;
            public void actionPerformed(ActionEvent a) {
             	if (button.isSelected()) {
                	_isOpaque = false;
            		_util.setBackgroundColor(null);
                	updateSamples();
                }
            }
            ActionListener init(JRadioButton b) {
            	button = b;
            	return this;
            }
        }.init(button));       
        _buttonGroup.add(button);
        colorPanel.add(button);
        this.add(colorPanel);

        _chooser.getSelectionModel().addChangeListener(this);
        _chooser.setPreviewPanel(preview);
        this.add(_chooser);
        updateSamples();
    	item.setVisible(false);		// otherwise leaves traces for PositionableJPanels
    }
    
    private JPanel makeTextPanel(String caption, JLabel sample, int state) {
    	JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(ItemPalette.rbp.getString(caption)));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel p = new JPanel();
        JTextField textField = new JTextField(sample.getText(), 25);
        textField.addKeyListener(new KeyListener() {
        	JLabel sample;
        	KeyListener init(JLabel s) {
        		sample = s;
        		return this;
        	}
            public void keyTyped(KeyEvent E) { }
            public void keyPressed(KeyEvent E){ }
            public void keyReleased(KeyEvent E) { 
              JTextField tmp = (JTextField) E.getSource();
              sample.setText(tmp.getText());
            }
          }.init(sample));
        p.add(textField);
        panel.add(p);
        
        p = new JPanel();
        p.add(makeButton(new AJRadioButton(ItemPalette.rbp.getString("fontColor"), state)));
        p.add(makeButton(new AJRadioButton(ItemPalette.rbp.getString("backColor"), state+10)));
        panel.add(p);
       
    	return panel;
    }
    
    private AJRadioButton makeButton(AJRadioButton button) {
        button.addActionListener(new ActionListener() {
        	AJRadioButton button ;
            public void actionPerformed(ActionEvent a) {
             	if (button.isSelected()) {
            		_selectedButton = button.which;
                }
            }
            ActionListener init(AJRadioButton b) {
            	button = b;
            	return this;
            }
        }.init(button));       
        _buttonGroup.add(button);
    	return button;
    }
    
    private void updateSamples() {
        PositionablePopupUtil util = getPositionablePopupUtil();
        int mar = util.getMargin();
        int bor = util.getBorderSize();
        Border outlineBorder;
        if (bor==0) {
        	outlineBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
        }else {
        	outlineBorder = new LineBorder(util.getBorderColor(), bor);           	
        }
        Font font = _util.getFont();
        int just = util.getJustification();
		Iterator<PositionableLabel> it = _sample.values().iterator();
		while (it.hasNext()) {
			PositionableLabel sam = it.next();
			sam.setOpaque(_isOpaque);
			sam.setFont(font);
	        Border borderMargin;
	        if (_isOpaque){
	            borderMargin = new LineBorder(sam.getBackground(),mar);    	                
	        } else{
	            borderMargin = BorderFactory.createEmptyBorder(mar, mar, mar, mar);
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
		}
    }

    public void stateChanged(ChangeEvent e) {
        PositionablePopupUtil util = getPositionablePopupUtil();
        Object obj = e.getSource();
        if (obj instanceof AJSpinner) {
            int num = ((Number)((AJSpinner)obj).getValue()).intValue();
            switch (((AJSpinner)obj)._which) {
                case BORDER:
                    util.setBorderSize(num); 
                    break;
                case MARGIN:
                    util.setMargin(num); 
                    break;
                case FWIDTH:
                    util.setFixedWidth(num); 
                    break;
                case FHEIGHT:
                    util.setFixedHeight(num); 
                    break;
            }
        } else {
        	switch (_selectedButton) {
        		case TEXT_FONT:
        			_sample.get("Text").setForeground(_chooser.getColor());
        			util.setForeground(_chooser.getColor());
        			break;
        		case ACTIVE_FONT:
        			_sample.get("Active").setForeground(_chooser.getColor());
        			break;
        		case INACTIVE_FONT:
        			_sample.get("InActive").setForeground(_chooser.getColor());
        			break;
        		case UNKOWN_FONT:
        			_sample.get("Unknown").setForeground(_chooser.getColor());
        			break;
        		case INCONSISTENT_FONT:
        			_sample.get("Inconsistent").setForeground(_chooser.getColor());
        			break;
        		case TEXT_BACKGROUND:
        			_sample.get("Text").setBackground(_chooser.getColor());
        			util.setBackgroundColor(_chooser.getColor());
                	_isOpaque = true;
        			break;
        		case ACTIVE_BACKGROUND:
        			_sample.get("Active").setBackground(_chooser.getColor());
        			_sample.get("Active").setOpaque(true);
                	_isOpaque = true;
        			break;
        		case INACTIVE_BACKGROUND:
           			_sample.get("InActive").setBackground(_chooser.getColor());
        			_sample.get("InActive").setOpaque(true);
                	_isOpaque = true;
        			break;
        		case UNKOWN_BACKGROUND:
           			_sample.get("Unknown").setBackground(_chooser.getColor());
        			_sample.get("Unknown").setOpaque(true);
                	_isOpaque = true;
        			break;
        		case INCONSISTENT_BACKGROUND:
           			_sample.get("Inconsistent").setBackground(_chooser.getColor());
        			_sample.get("Inconsistent").setOpaque(true);
                	_isOpaque = true;
        			break;
        		case TRANSPARENT_COLOR:
            		util.setBackgroundColor(null);
                	_isOpaque = false;
        			break;
        		case BORDER_COLOR:
        			util.setBorderColor(_chooser.getColor());
        			break;
        	}
        }
         updateSamples();
    }

    public PositionablePopupUtil getPositionablePopupUtil() {
        return _util;
    }

    public void getText(Positionable pos) {
    	if (pos instanceof SensorIcon && ((SensorIcon)pos).isText()) {
    		SensorIcon icon = (SensorIcon)pos;
    		PositionableLabel sample = _sample.get("Active");
    		icon.setActiveText(sample.getText());   		
    		icon.setBackgroundActive(sample.getBackground());
    		icon.setTextActive(sample.getForeground());
    		
    		sample = _sample.get("InActive");
    		icon.setInactiveText(sample.getText());   		
    		icon.setBackgroundInActive(sample.getBackground());
    		icon.setTextInActive(sample.getForeground());
    		
    		sample = _sample.get("Unknown");
    		icon.setUnknownText(sample.getText());   		
    		icon.setBackgroundUnknown(sample.getBackground());
    		icon.setTextUnknown(sample.getForeground());
    		
    		sample = _sample.get("Inconsistent");
    		icon.setInconsistentText(sample.getText());   		
    		icon.setBackgroundInconsistent(sample.getBackground());
    		icon.setTextInconsistent(sample.getForeground());
    	} else {
    		PositionableLabel sample = _sample.get("Text");
    		pos.setBackground(sample.getBackground());
    		pos.setForeground(sample.getForeground());
    		if (pos instanceof PositionableLabel) {
        		((PositionableLabel)pos).setText(_sample.get("Text").getText());    			
    		}
    	}
		pos.setPopupUtility(_util);
    }
    
    public boolean isOpaque() {
    	return _isOpaque;
    }

    public void itemStateChanged(ItemEvent e) {   
        PositionablePopupUtil util = getPositionablePopupUtil();
        Object obj = e.getSource();
        if (obj instanceof AJComboBox) {
            switch (((AJComboBox)obj)._which) {
                case SIZE:
                    String size = (String)((AJComboBox)obj).getSelectedItem();
                    util.setFontSize(Float.valueOf(size));
                    break;
                case STYLE:
                    int style = 0;
                    switch (((AJComboBox)obj).getSelectedIndex()) {
                        case 0:
                            style = Font.PLAIN;
                            break;
                        case 1:
                            style = Font.BOLD;
                            break;
                        case 2:
                            style = Font.ITALIC;
                            break;
                        case 3:
                            style = (Font.BOLD | Font.ITALIC);
                            break;
                    }
                    util.setFontStyle(style);
                    break;
                case JUST:
                    int just = 0;
                    switch (((AJComboBox)obj).getSelectedIndex()) {
                        case 0:
                            just = PositionablePopupUtil.LEFT;
                            break;
                        case 1:
                            just = PositionablePopupUtil.CENTRE;
                            break;
                        case 2:
                            just = PositionablePopupUtil.RIGHT;
                            break;
                    }
                    util.setJustification(just); 
                    break;
            }
            updateSamples();
        }
    }

    /**
    * Export a Positionable item from panel 
    */
    class DragDecoratorLabel extends PositionableLabel implements DragGestureListener, DragSourceListener, Transferable {    

        DataFlavor dataFlavor;

        public DragDecoratorLabel(String s, Editor editor) {
            super(s, editor);
            DragSource dragSource = DragSource.getDefaultDragSource();
            dragSource.createDefaultDragGestureRecognizer(this,
                        DnDConstants.ACTION_COPY, this);
            try {
                dataFlavor = new DataFlavor(Editor.POSITIONABLE_FLAVOR);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
        /**************** DragGestureListener ***************/
        public void dragGestureRecognized(DragGestureEvent e) {
            if (log.isDebugEnabled()) log.debug("DragPositionable.dragGestureRecognized ");
            //Transferable t = getTransferable(this);
            e.startDrag(DragSource.DefaultCopyDrop, this, this); 
        }
        /**************** DragSourceListener ************/
        public void dragDropEnd(DragSourceDropEvent e) {
            }
        public void dragEnter(DragSourceDragEvent e) {
            }
        public void dragExit(DragSourceEvent e) {
            }
        public void dragOver(DragSourceDragEvent e) {
            }
        public void dropActionChanged(DragSourceDragEvent e) {
            }
        /*************** Transferable *********************/
        public DataFlavor[] getTransferDataFlavors() {
            //if (log.isDebugEnabled()) log.debug("DragPositionable.getTransferDataFlavors ");
            return new DataFlavor[] { dataFlavor };
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            //if (log.isDebugEnabled()) log.debug("DragPositionable.isDataFlavorSupported ");
            return dataFlavor.equals(flavor);
        }
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException,IOException {
            if (!isDataFlavorSupported(flavor)) {
                return null;
            }
            return _sample.get("Text").deepClone();
        }
    }

    static Logger log = Logger.getLogger(DecoratorPanel.class.getName());
}
