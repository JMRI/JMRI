package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainPrintUtilities;
import jmri.jmrit.operations.trains.TrainUtilities;
import jmri.util.swing.JmriJOptionPane;

/**
 * Creates a routing report for the selected car.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2023
 */
public class CarRoutingReportAction extends AbstractAction {

    CarSetFrame _csFrame;
    boolean _isPreview;

    public CarRoutingReportAction(CarSetFrame frame, boolean isPreview) {
        super(isPreview ? Bundle.getMessage("MenuPreviewRoutingReport") : Bundle.getMessage("MenuPrintRoutingReport"));
        _csFrame = frame;
        _isPreview = isPreview;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        createCarRoutingReport(_csFrame._car);
    }

    private void createCarRoutingReport(Car car) {
        if (car != null && car.getLocation() != null && car.getFinalDestination() != null) {
            PrintWriter printWriter = getCarRouterBuildReportPrintWriter(car);
            Router router = InstanceManager.getDefault(Router.class);
            router.isCarRouteable(car, null, car.getFinalDestination(), car.getFinalDestinationTrack(), printWriter);
            showCarRoutingReport(car);
        } else {
            JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("WarnMsgNoLocOrFD"),
                    Bundle.getMessage("WarnMsgNoLocOrFD"), JmriJOptionPane.WARNING_MESSAGE);
        }
    }

    private PrintWriter getCarRouterBuildReportPrintWriter(Car car) {
        // create car router report print writer
        PrintWriter printWriter = null;
        File file = InstanceManager.getDefault(CarManagerXml.class).createRawCarRouterReportFile(car.toString());
        try {
            printWriter = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)),
                    true);
        } catch (IOException e) {
            log.error("Can not open build report file: {}", e.getLocalizedMessage());
        }
        return printWriter;
    }

    private void showCarRoutingReport(Car car) {
        CarManagerXml carManagerXml = InstanceManager.getDefault(CarManagerXml.class);
        TrainPrintUtilities.editReport(carManagerXml.getRawCarRouterReportFile(car.toString()),
                carManagerXml.createCarRouterReportFile(car.toString()));
        File file = carManagerXml.getCarRouterReportFile(car.toString());
        if (_isPreview && Setup.isBuildReportEditorEnabled()) {
            TrainUtilities.openDesktop(file);
        } else {
            TrainPrintUtilities.printReport(file, Bundle.getMessage("RoutingReportCar", car.toString()), _isPreview,
                    Car.NONE, false, Car.NONE, Car.NONE, Setup.PORTRAIT, Setup.getBuildReportFontSize(), true);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CarRoutingReportAction.class);
}
