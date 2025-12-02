import Bus.SoftwareBus;
import CommandCenter.*;
import CommandCenter.ElevatorPanel;
import ElevatorController.HigherLevel.ElevatorMain;
import PFDGUI.gui;
import Mux.BuildingMultiplexor;
import Mux.ElevatorMultiplexor;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage commandStage;
    private static Stage muxStage;
    private static ElevatorControlSystem elevatorControlSystem;
    private static gui guiMux = new gui();

    public static void main(String[] args) {
        int numElevators = 4;
        int elevator1 = 1;
        int elevator2 = 2;
        int elevator3 = 3;
        int elevator4 = 4;
        SoftwareBus serverBus = new SoftwareBus(true);
        //SoftwareBus clientBus = new SoftwareBus(false);
        elevatorControlSystem =new ElevatorControlSystem(serverBus);



        //TODO will need to change the code in building and elevator
        // multiplexer to be given a single client software bus. These
        // changes will need to be made to the init function and the
        // constructor for both objects.
        BuildingMultiplexor buildingMultiplexor = new Mux.BuildingMultiplexor(serverBus);
        ElevatorMultiplexor[] elevatorMuxes = new ElevatorMultiplexor[4];
        for (int i = 0; i < numElevators; i++) {
            elevatorMuxes[i] = new Mux.ElevatorMultiplexor(i + 1,serverBus);  // Store
            // the reference



        }
        guiMux.initilizeMuxs(elevatorMuxes);
        ElevatorMain em1 = new ElevatorMain(elevator1, serverBus);
        ElevatorMain em2 = new ElevatorMain(elevator2, serverBus);
        ElevatorMain em3 = new ElevatorMain(elevator3, serverBus);
        ElevatorMain em4 = new ElevatorMain(elevator4, serverBus);

        Thread thread1 = new Thread(em1);
        Thread thread2 = new Thread(em2);
        Thread thread3 = new Thread(em3);
        Thread thread4 = new Thread(em4);
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();

        launch(args);

    }

    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     * <p>
     * NOTE: This method is called on the JavaFX Application Thread.
     * </p>
     *
     * @param primaryStage the primary stage for this application, onto which
     *                     the application scene can be set.
     *                     Applications may create other stages, if needed, but they will not be
     *                     primary stages.
     * @throws Exception if something goes wrong
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        commandStage=elevatorControlSystem.getStage();

        commandStage.show();
        Thread.sleep(500);
        System.out.println("ooooooooooo");
        muxStage = guiMux.getStage();
        muxStage.show();

    }
}