package jmri.jmrix.ecos.networkdriver.configurexml;

import java.util.List;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import jmri.jmrix.ecos.EcosPreferences;
import jmri.jmrix.ecos.networkdriver.ConnectionConfig;
import jmri.jmrix.ecos.networkdriver.NetworkDriverAdapter;
import org.jdom2.Element;

/**
 * Handle XML persistance of layout connections by persistening the
 * NetworkDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the NetworkDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright (c) 2003, 208
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    @Override
    protected void getInstance() {
        adapter = new NetworkDriverAdapter();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void extendElement(Element e) {
        Element ecosPrefElem = new Element("commandStationPreferences");
        EcosPreferences p = ((jmri.jmrix.ecos.EcosSystemConnectionMemo) adapter.getSystemConnectionMemo()).getPreferenceManager();

        if (p.getAddTurnoutsToEcos() == 0x01) {
            ecosPrefElem.setAttribute("addTurnoutToCS", "no");
        } else if (p.getAddTurnoutsToEcos() == 0x02) {
            ecosPrefElem.setAttribute("addTurnoutToCS", "yes");
        }

        if (p.getRemoveTurnoutsFromEcos() == 0x01) {
            ecosPrefElem.setAttribute("removeTurnoutFromCS", "no");
        } else if (p.getRemoveTurnoutsFromEcos() == 0x02) {
            ecosPrefElem.setAttribute("removeTurnoutFromCS", "yes");
        }

        if (p.getAddTurnoutsToJMRI() == 0x01) {
            ecosPrefElem.setAttribute("addTurnoutToJMRI", "no");
        } else if (p.getAddTurnoutsToJMRI() == 0x02) {
            ecosPrefElem.setAttribute("addTurnoutToJMRI", "yes");
        }

        if (p.getRemoveTurnoutsFromJMRI() == 0x01) {
            ecosPrefElem.setAttribute("removeTurnoutFromJMRI", "no");
        } else if (p.getRemoveTurnoutsFromJMRI() == 0x02) {
            ecosPrefElem.setAttribute("removeTurnoutFromJMRI", "yes");
        }

        if (p.getLocoMaster() > 0x00) {
            ecosPrefElem.setAttribute("locoMaster", p.getLocoMasterAsString());
        }

        if (p.getAddLocoToEcos() == 0x01) {
            ecosPrefElem.setAttribute("addLocoToCS", "no");
        }
        if (p.getAddLocoToEcos() == 0x02) {
            ecosPrefElem.setAttribute("addLocoToCS", "yes");
        }

        if (p.getRemoveLocoFromEcos() == 0x01) {
            ecosPrefElem.setAttribute("removeLocoFromCS", "no");
        } else if (p.getRemoveLocoFromEcos() == 0x02) {
            ecosPrefElem.setAttribute("removeLocoFromCS", "yes");
        }

        if (p.getAddLocoToJMRI() == 0x01) {
            ecosPrefElem.setAttribute("addLocoToJMRI", "no");
        } else if (p.getAddLocoToJMRI() == 0x02) {
            ecosPrefElem.setAttribute("addLocoToJMRI", "yes");
        }

        if (p.getRemoveLocoFromJMRI() == 0x01) {
            ecosPrefElem.setAttribute("removeLocoFromJMRI", "no");
        } else if (p.getRemoveLocoFromJMRI() == 0x01) {
            ecosPrefElem.setAttribute("removeLocoFromJMRI", "yes");
        }

        if (p.getAdhocLocoFromEcos() == 0x01) {
            ecosPrefElem.setAttribute("removeAdhocLocoFromCS", "no");
        } else if (p.getAdhocLocoFromEcos() == 0x02) {
            ecosPrefElem.setAttribute("removeAdhocLocoFromCS", "yes");
        }

        if (p.getForceControlFromEcos() == 0x01) {
            ecosPrefElem.setAttribute("forceControlFromCS", "no");
        } else if (p.getForceControlFromEcos() == 0x02) {
            ecosPrefElem.setAttribute("forceControlCS", "yes");
        }

        //if(!p.getDefaultEcosProtocol().equals("DCC128")) ecosPrefElem.setAttribute("defaultCSProtocol", p.getDefaultEcosProtocol());
        if (p.getEcosLocoDescription() != null) {
            if (!p.getEcosLocoDescription().equals("")) {
                ecosPrefElem.setAttribute("defaultCSLocoDescription", p.getEcosLocoDescription());
            }
        }

        if (p.getLocoControl()) {
            ecosPrefElem.setAttribute("locoControl", "force");
        }

        ecosPrefElem.setAttribute("ecosRosterAttribute", p.getRosterAttribute());
        e.addContent(ecosPrefElem);
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        List<Element> ecosPref = shared.getChildren("commandStationPreferences");
        EcosPreferences p = ((jmri.jmrix.ecos.EcosSystemConnectionMemo) adapter.getSystemConnectionMemo()).getPreferenceManager();
        for (int i = 0; i < ecosPref.size(); i++) {
            if (ecosPref.get(i).getAttribute("addTurnoutToCS") != null) {
                String yesno = ecosPref.get(i).getAttribute("addTurnoutToCS").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setAddTurnoutsToEcos(0x02);
                    } else if (yesno.equals("no")) {
                        p.setAddTurnoutsToEcos(0x01);
                    }
                }
            }
            if (ecosPref.get(i).getAttribute("removeTurnoutFromCS") != null) {
                String yesno = ecosPref.get(i).getAttribute("removeTurnoutFromCS").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setRemoveTurnoutsFromEcos(0x02);
                    } else if (yesno.equals("no")) {
                        p.setRemoveTurnoutsFromEcos(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("addTurnoutToJMRI") != null) {
                String yesno = ecosPref.get(i).getAttribute("addTurnoutToJMRI").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setAddTurnoutsToJMRI(0x02);
                    } else if (yesno.equals("no")) {
                        p.setAddTurnoutsToJMRI(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("removeTurnoutFromJMRI") != null) {
                String yesno = ecosPref.get(i).getAttribute("removeTurnoutFromJMRI").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setRemoveTurnoutsFromJMRI(0x02);
                    } else if (yesno.equals("no")) {
                        p.setRemoveTurnoutsFromJMRI(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("addLocoToCS") != null) {
                String yesno = ecosPref.get(i).getAttribute("addLocoToCS").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setAddLocoToEcos(0x02);
                    } else if (yesno.equals("no")) {
                        p.setAddLocoToEcos(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("removeLocoFromCS") != null) {
                String yesno = ecosPref.get(i).getAttribute("removeLocoFromCS").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setRemoveLocoFromEcos(0x02);
                    } else if (yesno.equals("no")) {
                        p.setRemoveLocoFromEcos(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("addLocoToJMRI") != null) {
                String yesno = ecosPref.get(i).getAttribute("addLocoToJMRI").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setAddLocoToJMRI(0x02);
                    } else if (yesno.equals("no")) {
                        p.setAddLocoToJMRI(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("removeLocoFromJMRI") != null) {
                String yesno = ecosPref.get(i).getAttribute("removeLocoFromJMRI").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setRemoveLocoFromJMRI(0x02);
                    } else if (yesno.equals("no")) {
                        p.setRemoveLocoFromJMRI(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("removeLocoFromJMRI") != null) {
                String yesno = ecosPref.get(i).getAttribute("removeLocoFromJMRI").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setRemoveLocoFromJMRI(0x02);
                    } else if (yesno.equals("no")) {
                        p.setRemoveLocoFromJMRI(0x01);
                    }
                }
            }

            if (ecosPref.get(i).getAttribute("locoMaster") != null) {
                p.setLocoMaster(ecosPref.get(i).getAttribute("locoMaster").getValue());
            }

            if (ecosPref.get(i).getAttribute("removeAdhocLocoFromCS") != null) {
                String yesno = ecosPref.get(i).getAttribute("removeAdhocLocoFromCS").getValue();
                if ((yesno != null) && (!yesno.equals(""))) {
                    if (yesno.equals("yes")) {
                        p.setAdhocLocoFromEcos(0x02);
                    } else if (yesno.equals("no")) {
                        p.setAdhocLocoFromEcos(0x01);
                    }
                }
            }
            /*if (ecosPref.get(i).getAttribute("defaultCSProtocol") != null){
             p.setDefaultEcosProtocol(ecosPref.get(i).getAttribute("defaultCSProtocol").getValue());
             }*/
            if (ecosPref.get(i).getAttribute("defaultCSLocoDescription") != null) {
                p.setEcosLocoDescription(ecosPref.get(i).getAttribute("defaultCSLocoDescription").getValue());
            }

            if (ecosPref.get(i).getAttribute("ecosRosterAttribute") != null) {
                p.setRosterAttribute(ecosPref.get(i).getAttribute("ecosRosterAttribute").getValue());
            }
            if (ecosPref.get(i).getAttribute("locoControl") != null) {
                p.setLocoControl(true);
            }

            p.resetChangeMade();
        }
        p.setPreferencesLoaded();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
