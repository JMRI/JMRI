package jmri.jmrit.display.controlPanelEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import jmri.jmrit.logix.OBlock;
//import jmri.jmrit.logix.Portal;

/**
 * <P>
 * @author  Pete Cressman Copyright: Copyright (c) 2013
 * 
 */

public class EditPortalDirection extends jmri.util.JmriJFrame implements ActionListener{

    private OBlock _homeBlock;
    private CircuitBuilder _parent;
    private PortalIcon _icon;
    private JRadioButton _toButton;
    private JRadioButton _fromButton;
    private JRadioButton _noButton;
    private boolean _regular;		// true when TO_ARROW show entry into ToBlock


    static int STRUT_SIZE = 10;
    static boolean _firstInstance = true;
    static Point _loc = null;
    static Dimension _dim = null;

    public EditPortalDirection(String title, CircuitBuilder parent, OBlock block) {
        _homeBlock = block;
        _parent = parent;
        setTitle(java.text.MessageFormat.format(title, _homeBlock.getDisplayName()));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                closingEvent();
            }
        });
        addHelpMenu("package.jmri.jmrit.display.CircuitBuilder", true);

        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));
        contentPane.add(makePortalPanel());
        contentPane.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel border = new JPanel();
        border.setLayout(new java.awt.BorderLayout(10,10));
        border.add(contentPane);
        setContentPane(border);
        if (_firstInstance) {
            setLocationRelativeTo(_parent._editor);
            setSize(500,500);
            _firstInstance = false;
        } else {
            setLocation(_loc);
            setSize(_dim);
        }
        pack();
        setVisible(true);
    }

    private JPanel makeDoneButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton doneButton = new JButton(Bundle.getMessage("ButtonDone"));
        doneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent a) {
                    closingEvent();
                }
        });
        panel.add(doneButton);
        buttonPanel.add(panel);

        panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(buttonPanel);

        return panel;
    }
    
    private JPanel makeArrowPanel() {   	
    	JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    	panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(java.awt.Color.black),
    			Bundle.getMessage("ArrowIconsTitle")));
        panel.add(Box.createHorizontalStrut(200));
        
        ButtonGroup group = new ButtonGroup();
        _toButton = new JRadioButton(_parent._editor.getPortalIcon(PortalIcon.TO_ARROW));
        _toButton.setActionCommand(PortalIcon.TO_ARROW);
        _toButton.addActionListener(this);
        group.add(_toButton);
        panel.add(_toButton);
        
        _fromButton = new JRadioButton(_parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));
        _fromButton.setActionCommand(PortalIcon.FROM_ARROW);
        _fromButton.addActionListener(this);
        group.add(_fromButton);
        panel.add(_fromButton);
        
        _noButton = new JRadioButton(Bundle.getMessage("noIcon"), _parent._editor.getPortalIcon(PortalIcon.HIDDEN));
        _noButton.setVerticalTextPosition(AbstractButton.CENTER);
        _noButton.setHorizontalTextPosition(AbstractButton.CENTER);
        _noButton.setActionCommand(PortalIcon.HIDDEN);
        _noButton.addActionListener(this);
        group.add(_noButton);
        panel.add(_noButton);
        
    	return panel;
    }

    private JPanel makePortalPanel() {
        JPanel portalPanel = new JPanel();
        portalPanel.setLayout(new BoxLayout(portalPanel, BoxLayout.Y_AXIS));
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel l = new JLabel(Bundle.getMessage("PortalDirection1", _homeBlock.getDisplayName()));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE/2));
        l = new JLabel(Bundle.getMessage("PortalDirection2"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("PortalDirection3"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        panel.add(Box.createVerticalStrut(STRUT_SIZE));
        l = new JLabel(Bundle.getMessage("PortalDirection4"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        l = new JLabel(Bundle.getMessage("PortalDirection5"));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        panel.add(l);
        portalPanel.add(panel);

        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));
        panel = new JPanel();
//        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(makeArrowPanel());
        portalPanel.add(panel);
       
        portalPanel.add(Box.createVerticalStrut(STRUT_SIZE));

        portalPanel.add(makeDoneButtonPanel());
        return portalPanel;
    }

    /************************* end setup **************************/
    public void actionPerformed(ActionEvent e) {
    	if (_icon==null) {
    		return;
    	}
    	if (PortalIcon.TO_ARROW.equals(e.getActionCommand())) {
    		if (_regular) {
            	_icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.TO_ARROW));    			
            	_icon.setIcon(PortalIcon.FROM_ARROW, _parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));    		
    		} else {    			
            	_icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));    		
            	_icon.setIcon(PortalIcon.FROM_ARROW, _parent._editor.getPortalIcon(PortalIcon.TO_ARROW));    		
    		}
        	_icon.setStatus(PortalIcon.TO_ARROW);
    	} else if (PortalIcon.FROM_ARROW.equals(e.getActionCommand())) {
    		if (_regular) {
            	_icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));    			
            	_icon.setIcon(PortalIcon.FROM_ARROW, _parent._editor.getPortalIcon(PortalIcon.TO_ARROW));    		
    		} else {    			
            	_icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.TO_ARROW));    		
            	_icon.setIcon(PortalIcon.FROM_ARROW, _parent._editor.getPortalIcon(PortalIcon.FROM_ARROW));    		
    		}
        	_icon.setStatus(PortalIcon.TO_ARROW);
    	} else if (PortalIcon.HIDDEN.equals(e.getActionCommand())) {
        	_icon.setIcon(PortalIcon.TO_ARROW, _parent._editor.getPortalIcon(PortalIcon.HIDDEN));    		
        	_icon.setStatus(PortalIcon.TO_ARROW);
    	}
    }

    protected void setPortalIcon(PortalIcon icon) {
        _parent._editor.highlight(icon);
        if (_icon!=null) {
    		_icon.setStatus(PortalIcon.VISIBLE);        	
        }
    	if (icon!=null) {
    		if (_homeBlock.equals(icon.getPortal().getToBlock())) {    			
        		icon.setStatus(PortalIcon.TO_ARROW);
        		_regular = true;
    		} else {    			
        		icon.setStatus(PortalIcon.FROM_ARROW);
        		_regular = false;
    		}
    		_toButton.setEnabled(true);
    		_fromButton.setEnabled(true);
    		_noButton.setEnabled(true);
    	} else {
    		_toButton.setEnabled(false);
    		_fromButton.setEnabled(false);
    		_noButton.setEnabled(false);
    	}
    	_icon = icon;
    }
    
    protected void closingEvent() {
        _parent.closePortalDirection(_homeBlock);
        _loc = getLocation(_loc);
        _dim = getSize(_dim);
        dispose();
    }

    protected OBlock getHomeBlock() {
        return _homeBlock;
    }
    
    static Logger log = LoggerFactory.getLogger(EditPortalDirection.class.getName());
}
