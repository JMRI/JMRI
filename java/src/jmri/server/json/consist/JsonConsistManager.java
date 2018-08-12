package jmri.server.json.consist;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import jmri.Consist;
import jmri.ConsistListListener;
import jmri.ConsistManager;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.beans.Bean;
import jmri.jmrit.consisttool.ConsistFile;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ConsistManager for the JSON services. This consist manager passes requests
 * for CS consisting to the
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class JsonConsistManager extends Bean implements ConsistManager {

    private ConsistManager manager = null;
    private HashSet<ConsistListListener> listeners = new HashSet<>();
    private final static Logger log = LoggerFactory.getLogger(JsonConsistManager.class);

    public JsonConsistManager() {
        super();
        InstanceManager.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals(InstanceManager.getDefaultsPropertyName(ConsistManager.class))) {
                this.manager = InstanceManager.getDefault(ConsistManager.class);
                this.manager.addConsistListListener(() -> {
                    this.notifyConsistListChanged();
                });
                this.manager.requestUpdateFromLayout();
                try {
                    (new ConsistFile()).readFile();
                } catch (JDOMException | IOException ex) {
                    log.warn("Error reading consist file {} due to {}", ConsistFile.defaultConsistFilename(), ex.getMessage());
                }
            }
        });
        this.manager = InstanceManager.getNullableDefault(ConsistManager.class);
        if (this.manager != null) {
            this.manager.addConsistListListener(() -> {
                this.notifyConsistListChanged();
            });
            this.manager.requestUpdateFromLayout();
            try {
                (new ConsistFile()).readFile();
            } catch (JDOMException | IOException ex) {
                log.warn("Error reading consist file {} due to {}", ConsistFile.defaultConsistFilename(), ex.getMessage());
            }
        }
    }

    @Override
    public Consist getConsist(LocoAddress address) {
        if (this.manager != null) {
            return this.manager.getConsist(address);
        }
        return null;
    }

    @Override
    public void delConsist(LocoAddress address) {
        if (this.manager != null) {
            this.manager.delConsist(address);
        }
    }

    @Override
    public boolean isCommandStationConsistPossible() {
        if (this.manager != null) {
            return this.manager.isCommandStationConsistPossible();
        }
        return false;
    }

    @Override
    public boolean csConsistNeedsSeperateAddress() {
        if (this.manager != null) {
            return this.manager.csConsistNeedsSeperateAddress();
        }
        return false;
    }

    @Override
    public ArrayList<LocoAddress> getConsistList() {
        if (this.manager != null) {
            return this.manager.getConsistList();
        }
        return new ArrayList<>();
    }

    @Override
    public String decodeErrorCode(int errorCode) {
        if (this.manager != null) {
            return this.manager.decodeErrorCode(errorCode);
        }
        return "Unknown Status Code: " + errorCode;
    }

    @Override
    public void requestUpdateFromLayout() {
        if (this.manager != null) {
            this.manager.requestUpdateFromLayout();
        }
    }

    @Override
    public void addConsistListListener(ConsistListListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeConsistListListener(ConsistListListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void notifyConsistListChanged() {
        new HashSet<>(this.listeners).stream().forEach((listener) -> {
            listener.notifyConsistListChanged();
        });
    }

    /**
     * Test if a real ConsistManager is available.
     *
     * @return true if a real consist manager is available, false otherwise.
     */
    public boolean isConsistManager() {
        return this.manager != null;
    }
}
