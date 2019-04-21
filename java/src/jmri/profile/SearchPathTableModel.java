package jmri.profile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A tabular data model for the search paths used to find
 * {@link jmri.profile.Profile}s.
 *
 * @author Randall Wood
 */
class SearchPathTableModel extends AbstractTableModel implements PropertyChangeListener {

    private static final Logger log = LoggerFactory.getLogger(SearchPathTableModel.class);

    @SuppressWarnings("LeakingThisInConstructor")
    public SearchPathTableModel() {
        ProfileManager.getDefault().addPropertyChangeListener(this);
    }

    @Override
    public int getRowCount() {
        return ProfileManager.getDefault().getSearchPaths().length;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        File p = ProfileManager.getDefault().getSearchPaths(rowIndex);
        switch (columnIndex) {
            case -1: // tooltip
                return Bundle.getMessage("SearchPathTableModel.tooltip", ProfileManager.getDefault().getDefaultSearchPath().getPath());
            case 0:
                return p;
            case 1:
                return (ProfileManager.getDefault().getDefaultSearchPath().equals(p));
            default:
                return null;
        }

    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Bundle.getMessage("SearchPathTableModel.searchPath"); // NOI18N
            case 1:
                return Bundle.getMessage("SearchPathTableModel.isDefault"); // NOI18N
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return File.class;
            case 1:
                return Boolean.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 1:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 1:
                try {
                    ProfileManager.getDefault().setDefaultSearchPath((File) this.getValueAt(rowIndex, 0));
                } catch (IOException ex) {
                    log.warn("Unable to write profiles while setting default search path", ex);
                }
                break;
            default:
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.fireTableDataChanged();
    }

}
