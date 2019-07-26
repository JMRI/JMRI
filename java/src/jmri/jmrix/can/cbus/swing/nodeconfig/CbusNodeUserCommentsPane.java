package jmri.jmrix.can.cbus.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.border.Border;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
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
public class CbusNodeUserCommentsPane extends JPanel implements KeyListener {
    
    private JScrollPane eventScroll;
    private CbusNode nodeOfInterest;
    private ActionListener setNameListener;
    private ActionListener resetCommentListener;
    private JTextArea textFieldName;
    private JButton saveTextButton;
    private JButton resetTextButton;

    /**
     * Create a new instance of CbusNodeSetupPane.
     */
    protected CbusNodeUserCommentsPane( NodeConfigToolPane main ) {
        super();
    }

    public void initComponents() {
        
        saveTextButton = new JButton(Bundle.getMessage("SaveCommentsButton"));
        saveTextButton.setEnabled(false);
        JPanel evMenuPane = new JPanel();
        evMenuPane.add(saveTextButton);
        
        resetTextButton = new JButton(Bundle.getMessage("ResetCommentButton"));
        resetTextButton.setEnabled(false);
        evMenuPane.add(resetTextButton);

        textFieldName = new JTextArea();
        textFieldName.setMargin( new java.awt.Insets(10,10,10,10) );
        textFieldName.addKeyListener(this);
        
        setLayout(new BorderLayout() );
        
        eventScroll = new JScrollPane(textFieldName);
        
        this.add(evMenuPane, BorderLayout.PAGE_START);
        this.add(eventScroll, BorderLayout.CENTER);
        
        validate();
        repaint();
        
        setNameListener = ae -> {
            saveComments();
        };
        saveTextButton.addActionListener(setNameListener);
        
        resetCommentListener = ae -> {
            restoreComments();
        };
        resetTextButton.addActionListener(resetCommentListener);
        
    }
    
    public void setNode(CbusNode node){
        
        if (node == nodeOfInterest){
            return;
        }
        nodeOfInterest = node;
        
        if (nodeOfInterest==null) {
            eventScroll.setVisible(false);
            resetCommentButtons();
            return;
        }
        eventScroll.setVisible(true);
        textFieldName.setText( nodeOfInterest.getUserComment() );
        resetCommentButtons();
    }
    
    public void saveComments() {
        nodeOfInterest.setUserComment(textFieldName.getText());
        resetCommentButtons();
    }
    
    //typingArea.setFocusTraversalKeysEnabled(false);

    /** Handle the key typed event from the text field. */
    @Override
    public void keyTyped(KeyEvent e) {
        // resetCommentButtons();
    }

    /** Handle the key-pressed event from the text field. */
    @Override
    public void keyPressed(KeyEvent e) {
        // resetCommentButtons();
    }

    /** Handle the key-released event from the text field. */
    @Override
    public void keyReleased(KeyEvent e) {
        resetCommentButtons();
    }
    
    private void resetCommentButtons() {
        
        if ( nodeOfInterest==null){
            saveTextButton.setEnabled(false);
            resetTextButton.setEnabled(false);
        }
        else if ( nodeOfInterest.getUserComment().equals(textFieldName.getText()) ){
          //  log.info("clean");
            saveTextButton.setEnabled(false);
            resetTextButton.setEnabled(false);
        }
        else {
          //  log.info("dirty");
            saveTextButton.setEnabled(true);
            resetTextButton.setEnabled(true);
        }
    }
    
    protected String getNodeString(){
        return " " + nodeOfInterest.toString();
    }
    
    public boolean areCommentsDirty(){
        if (saveTextButton!=null) {
            return saveTextButton.isEnabled();
        }
        return false;
    }
    
    protected void restoreComments(){
        textFieldName.setText( nodeOfInterest.getUserComment() );
        resetCommentButtons();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusNodeUserCommentsPane.class);
    
}
