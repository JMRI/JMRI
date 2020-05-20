package jmri.jmrit.beantable.routetable;

import jmri.*;
import jmri.swing.NamedBeanComboBox;
import jmri.swing.RowSorterUtil;
import jmri.util.AlphanumComparator;
import jmri.util.FileUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class RouteAddFrame extends AbstractRouteAddEditFrame {


    public RouteAddFrame() {
        this(Bundle.getMessage("TitleAddRoute"));
    }

    public RouteAddFrame(String name) {
        this(name,false,true);
    }

    public RouteAddFrame(String name, boolean saveSize, boolean savePosition) {
        super(name, saveSize, savePosition);
        initComponents();
    }


}
