// ConnectionLabel.java
package jmri.swing;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;

/**
 * A JLabel that listens to a system connection and reports its status
 *
 * @author rhwood
 */
public final class ConnectionLabel extends JLabel implements PropertyChangeListener {

    /**
     *
     */
    private static final long serialVersionUID = -4404932240707919099L;
    ConnectionConfig connection;

    public ConnectionLabel(ConnectionConfig connection) {
        super();
        this.connection = connection;
        if (connection.name().equals(JmrixConfigPane.NONE)) {
            this.setText("");
        } else {
            ConnectionStatus.instance().addConnection(connection.name(), connection.getInfo());
            this.update();
        }
        ConnectionStatus.instance().addPropertyChangeListener(this);
    }

    protected void update() {
        if (this.connection.getDisabled()) {
            return;
        }
        String name = this.connection.getConnectionName();
        if (name == null) {
            name = this.connection.getManufacturer();
        }
        if (ConnectionStatus.instance().isConnectionOk(this.connection.getInfo())) {
            this.setForeground(Color.BLACK);
            this.setText(Bundle.getMessage("ConnectionSucceeded",
                    name, this.connection.name(), this.connection.getInfo()));
        } else {
            this.setForeground(Color.RED);
            this.setText(Bundle.getMessage("ConnectionFailed",
                    name, this.connection.name(), this.connection.getInfo()));
        }
        this.revalidate();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        this.update();
    }

}
