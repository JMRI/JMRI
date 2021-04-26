package jmri.jmrix.can.cbus.swing.eventtable;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import javax.annotation.Nonnull;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;
import jmri.NamedBean;
import jmri.jmrix.can.cbus.eventtable.CbusEventBeanData;
import jmri.jmrix.can.cbus.swing.CbusCommonSwing;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Cell Renderer for 
 */ 
public class CbusEventBeanCellRenderer implements TableCellRenderer {

    private final JTextField _filterText;
    private final CbusBeanRenderer _beanImages;
    
    public CbusEventBeanCellRenderer( @Nonnull JTextField filterText, int iconHeight ){
        super();
        _filterText = filterText;
        _beanImages = new CbusBeanRenderer(iconHeight);
    }
    
    private JPanel f;
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, 
        boolean hasFocus, int row, int column) {
        
        f = new JPanel();
        f.setLayout(new BorderLayout());
        
        if (value instanceof CbusEventBeanData && !value.toString().isEmpty()) {
            
            JTextField tf = new JTextField(value.toString());
            CbusCommonSwing.setCellTextHighlighter(_filterText.getText(), value.toString(), tf);
            
            tf.setBorder(null);
            tf.setOpaque(false);
            tf.setHorizontalAlignment(JTextField.CENTER);
            
            f.add(tf,BorderLayout.CENTER);
            
            JPanel yy = new JPanel();
            yy.setLayout(new GridBagLayout());
            yy.setOpaque(false);
        
            ((CbusEventBeanData) value).getActionA().forEach((n) -> appendImgToPanel( yy, n, true) );
            ((CbusEventBeanData) value).getActionB().forEach((n) -> appendImgToPanel( yy, n, false) );
        
            f.add(yy, BorderLayout.LINE_START);
            
        }
        
        CbusCommonSwing.setCellBackground(isSelected, f, table,row);
        CbusCommonSwing.setCellFocus(hasFocus, f, table);
        
        return f;
    }
    
    private void appendImgToPanel(JPanel pnl, NamedBean bean, boolean beanOn){
        JLabel lbl;
         if (bean instanceof jmri.Turnout) {
            lbl = new JLabel(_beanImages.getBeanIcon("T",beanOn? jmri.DigitalIO.ON : jmri.DigitalIO.OFF));
        }
        else if (bean instanceof jmri.Sensor) {
            lbl = new JLabel(_beanImages.getBeanIcon("S",beanOn? jmri.DigitalIO.ON : jmri.DigitalIO.OFF));
        }
        else if (bean instanceof jmri.Light) {
            lbl = new JLabel(_beanImages.getBeanIcon("L",beanOn? jmri.DigitalIO.ON : jmri.DigitalIO.OFF));
        }
        else {
            return;
        }
        lbl.setOpaque(false); 
           
        JPanel tmp = new JPanel();
        tmp.setLayout(new GridBagLayout());
        tmp.setOpaque(false);
        tmp.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
        tmp.add(lbl);
        
        pnl.add(tmp);
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusEventBeanCellRenderer.class);
    
}
