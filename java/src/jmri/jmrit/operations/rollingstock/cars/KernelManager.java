package jmri.jmrit.operations.rollingstock.cars;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.operations.rollingstock.RollingStockGroupManager;

/**
 * Manages Kernels.
 *
 * @author Daniel Boudreau Copyright (C) 2021
 */
public class KernelManager extends RollingStockGroupManager implements InstanceManagerAutoDefault {

    public KernelManager() {
    }

    /**
     * Create a new Kernel
     *
     * @param name string name for this Kernel
     *
     * @return Kernel
     */
    public Kernel newKernel(String name) {
        Kernel kernel = getKernelByName(name);
        if (kernel == null && !name.equals(NONE)) {
            kernel = new Kernel(name);
            int oldSize = _groupHashTable.size();
            _groupHashTable.put(name, kernel);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, _groupHashTable
                    .size());
        }
        return kernel;
    }

    /**
     * Delete a Kernel by name
     *
     * @param name string name for the Kernel
     *
     */
    public void deleteKernel(String name) {
        Kernel kernel = getKernelByName(name);
        if (kernel != null) {
            kernel.dispose();
            Integer oldSize = Integer.valueOf(_groupHashTable.size());
            _groupHashTable.remove(name);
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize, Integer.valueOf(_groupHashTable
                    .size()));
        }
    }

    /**
     * Get a Kernel by name
     *
     * @param name string name for the Kernel
     *
     * @return named Kernel
     */
    public Kernel getKernelByName(String name) {
        return (Kernel) _groupHashTable.get(name);
    }

    public void replaceKernelName(String oldName, String newName) {
        Kernel oldKernel = getKernelByName(oldName);
        if (oldKernel != null) {
            Kernel newKernel = newKernel(newName);
            // keep the lead car
            Car leadCar = oldKernel.getLead();
            if (leadCar != null) {
                leadCar.setKernel(newKernel);
            }
            for (Car car : oldKernel.getCars()) {
                car.setKernel(newKernel);
            }
        }
    }

 
    public void load(Element root) {
        // new format using elements starting version 3.3.1
        if (root.getChild(Xml.NEW_KERNELS) != null) {
            List<Element> eKernels = root.getChild(Xml.NEW_KERNELS).getChildren(Xml.KERNEL);
            log.debug("Kernel manager sees {} kernels", eKernels.size());
            Attribute a;
            for (Element eKernel : eKernels) {
                if ((a = eKernel.getAttribute(Xml.NAME)) != null) {
                    newKernel(a.getValue());
                }
            }
        } // old format
        else if (root.getChild(Xml.KERNELS) != null) {
            String names = root.getChildText(Xml.KERNELS);
            if (!names.isEmpty()) {
                String[] kernelNames = names.split("%%"); // NOI18N
                log.debug("kernels: {}", names);
                for (String name : kernelNames) {
                    newKernel(name);
                }
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-cars.dtd.
     *
     * @param root The common Element for operations-cars.dtd.
     */
    public void store(Element root) {
        List<String> names = getNameList();
        Element kernels = new Element(Xml.NEW_KERNELS);
        for (String name : names) {
            Element kernel = new Element(Xml.KERNEL);
            kernel.setAttribute(new Attribute(Xml.NAME, name));
            kernels.addContent(kernel);
        }
        root.addContent(kernels);
    }

    protected void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        // Set dirty
        InstanceManager.getDefault(CarManagerXml.class).setDirty(true);
        super.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(KernelManager.class);
}
