package jmri.jmrix.jmriclient.json.configurexml;

import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.jmriclient.json.JsonClientSystemConnectionMemo;
import jmri.jmrix.jmriclient.json.JsonNetworkConnectionConfig;
import jmri.jmrix.jmriclient.json.JsonNetworkPortController;
import jmri.util.node.NodeIdentity;
import org.jdom2.Element;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonNetworkConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public final static String TRANSMIT_PREFIX = "transmitPrefix"; // NOI18N
    public final static String NODE_IDENTITY = "nodeIdentity"; // NOI18N

    public JsonNetworkConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (this.adapter == null) {
            this.adapter = new JsonNetworkPortController();
        }
    }

    @Override
    protected void getInstance(Object object) {
        this.adapter = ((JsonNetworkConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new JsonNetworkConnectionConfig(this.adapter));
    }

    @Override
    protected void extendElement(Element e) {
        if (this.adapter.getSystemConnectionMemo() != null) {
            e.setAttribute(TRANSMIT_PREFIX, ((JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo()).getTransmitPrefix());
            if (!((JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo()).getNodeIdentity().equals(NodeIdentity.identity())) {
                // Don't store our own identity
                e.setAttribute(NODE_IDENTITY, ((JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo()).getNodeIdentity());
            }
        }
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        if (shared.getAttribute(TRANSMIT_PREFIX) != null) {
            ((JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo()).setTransmitPrefix(shared.getAttribute(TRANSMIT_PREFIX).getValue());
        }
        if (shared.getAttribute(NODE_IDENTITY) != null) {
            ((JsonClientSystemConnectionMemo) this.adapter.getSystemConnectionMemo()).setNodeIdentity(shared.getAttribute(NODE_IDENTITY).getValue());
        }
    }
}
