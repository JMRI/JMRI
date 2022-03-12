package jmri.jmrix.can.cbus.swing;

import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.dnd.DragSourceDragEvent;
import java.util.TimerTask;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.jmrix.can.cbus.CbusNameService;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.node.CbusNodeEventTableDataModel;
import jmri.util.TimerUtil;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Drag and drop handler for CBUS Events being dragged from a table.
 * @author Steve Young Copyright (C) 2019
 */
public class CbusTableRowEventDnDHandler extends TransferHandler implements DragSourceMotionListener {

    private int nn,en;
    private JLabel tempLabel;
    private JFrame frame;
    private final CbusNameService nameService;
    private TimerTask showFrameTimerTask;
    private DragSource source;
    
    public CbusTableRowEventDnDHandler(CanSystemConnectionMemo memo, JTable table){
        super();
        nameService = new CbusNameService(memo);
        initFrame();

    }
    
    final void initFrame(){
        tempLabel = new JLabel();
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.add(tempLabel);
        frame.setFocusableWindowState(false);
        
        source = DragSource.getDefaultDragSource();
        source.addDragSourceMotionListener(this);
    
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dragMouseMoved(DragSourceDragEvent e){
        mouseMoved(e.getX(),e.getY());
    }
        
    protected void mouseMoved(int x, int y){
        clearFrameTimeout();
        if (nn<0 || en<0){
            return;
        }
        tempLabel.setText(nameService.getEventNodeString(nn, en));
        frame.repaint();
        frame.pack();

        frame.setLocation(x+20, y+15);
        frame.setVisible(true);
        frame.toFront();
        
        setFrameTimeout();
    
    }
    
    private void clearFrameTimeout(){
        if (showFrameTimerTask != null ) {
            showFrameTimerTask.cancel();
            showFrameTimerTask = null;
            frame.setVisible(false);
        }
    }
    
    private void setFrameTimeout(){
        showFrameTimerTask = new TimerTask() {
            @Override
            public void run() {
                showFrameTimerTask = null;
                frame.setVisible(false);
            }
        };
        TimerUtil.schedule(showFrameTimerTask, ( 650 ) );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transferable createTransferable(JComponent c) {
        nn = -1;
        en = -1;
        if (!(c instanceof JTable )){
            return null;
        }
        
        setNnEn((JTable) c);
        if (nn<0 || en<0){
            return null;
        }
        
        return new StringSelection( CbusEvent.getJmriString(nn, en) );
    }
    
    private void setNnEn(JTable table){
    
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        
        if ( table.getName().equals(CbusEventTableDataModel.class.getName())
            || table.getName().equals(CbusNodeEventTableDataModel.class.getName())) {
            
            nn = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), CbusNodeEventTableDataModel.NODE_NUMBER_COLUMN); // node number
            en = (Integer) table.getModel().getValueAt(table.convertRowIndexToModel(row), CbusNodeEventTableDataModel.EVENT_NUMBER_COLUMN); // event number
        }
    }
    
    public void dispose(){
        source.removeDragSourceMotionListener(this);
        clearFrameTimeout();
        frame.dispose();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusTableRowEventDnDHandler.class);

}
