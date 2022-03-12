package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import jmri.jmrix.can.cbus.node.CbusNode;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 *
 * @author Steve Young Copyright (C) 2019
 */
public class CbusNodeUserCommentsPane extends CbusNodeConfigTab implements KeyListener {
    
    private JScrollPane eventScroll;
    private ActionListener setNameListener;
    private ActionListener resetCommentListener;
    private JTextArea textFieldName;
    private JButton saveTextButton;
    private JButton resetTextButton;

    /**
     * Create a new instance of CbusNodeSetupPane.
     * @param main the main NodeConfigToolPane this is a pane of.
     */
    protected CbusNodeUserCommentsPane( NodeConfigToolPane main ) {
        super(main);
        initPane();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(){
        return Bundle.getMessage("NodeComments"); // NOI18N
    }

    private void initPane() {
        
        initButtons();
        
        JPanel evMenuPane = new JPanel();
        evMenuPane.add(saveTextButton);
        evMenuPane.add(resetTextButton);

        textFieldName = new JTextArea();
        textFieldName.setMargin( new java.awt.Insets(10,10,10,10) );
        textFieldName.addKeyListener(this);
        eventScroll = new JScrollPane(textFieldName);
        
        add(evMenuPane, BorderLayout.PAGE_START);
        add(eventScroll, BorderLayout.CENTER);
        
    }
    
    private void initButtons(){
    
        saveTextButton = new JButton(Bundle.getMessage("SaveCommentsButton")); // NOI18N
        saveTextButton.setEnabled(false);
        
        resetTextButton = new JButton(Bundle.getMessage("ResetCommentButton")); // NOI18N
        resetTextButton.setEnabled(false);
        
        setNameListener = ae -> {
            saveOption();
        };
        saveTextButton.addActionListener(setNameListener);
        
        resetCommentListener = ae -> {
            cancelOption();
        };
        resetTextButton.addActionListener(resetCommentListener);
    
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void changedNode(CbusNode node){
        textFieldName.setText( nodeOfInterest.getUserComment() );
        resetCommentButtons();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void saveOption() {
        nodeOfInterest.setUserComment(textFieldName.getText());
        resetCommentButtons();
    }

    /** 
     * Handle the key typed event from the text field.
     * {@inheritDoc}
     */
    @Override
    public void keyTyped(KeyEvent e) {
        // resetCommentButtons();
    }

    /** 
     * Handle the key-pressed event from the text field.
     * {@inheritDoc}
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // resetCommentButtons();
    }

    /** 
     * Handle the key-released event from the text field.
     * {@inheritDoc}
     */
    @Override
    public void keyReleased(KeyEvent e) {
        resetCommentButtons();
    }
    
    private void resetCommentButtons() {
        saveTextButton.setEnabled(!(nodeOfInterest.getUserComment().equals(textFieldName.getText())));
        resetTextButton.setEnabled(!(nodeOfInterest.getUserComment().equals(textFieldName.getText())));
    }
    
    public boolean areCommentsDirty(){
        if (saveTextButton!=null) {
            return saveTextButton.isEnabled();
        }
        return false;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void cancelOption() {
        textFieldName.setText( nodeOfInterest.getUserComment() );
        resetCommentButtons();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean getVetoBeingChanged(){
        if (areCommentsDirty()) {
            return getCancelSaveEditDialog(Bundle.getMessage("CommentsEditUnsaved"));
        }
        return false;
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusNodeUserCommentsPane.class);
    
}
