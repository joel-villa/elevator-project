package CommandCenter;

import CommandCenter.ElevatorPanel;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import Bus.*;

public class ElevatorControlSystem{

    private ElevatorPanel2[] elevators;
    private CommandCenter commandCenter;
    private CommandPanel commandPanel;

    private SoftwareBus softwareBus;


    public ElevatorControlSystem(SoftwareBus softwareBus){
        this.softwareBus=softwareBus;
        commandCenter=new CommandCenter(softwareBus);

        commandPanel=new CommandPanel(commandCenter);

        elevators = new ElevatorPanel2[4];
        for (int i = 0; i < 4; i++) {
            elevators[i] = new ElevatorPanel2(i + 1, commandCenter); //Changed by team 6,7

        }

    }

    /**
     * This has been changed to just use java fx, any logic surrounding the
     * software bust or starting logic has been moved to the constructor
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     */


    public Stage getStage() {
        Stage primaryStage=new Stage();
        primaryStage.setTitle("Command Center");

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #333333;");

        Label title = new Label("Command Center");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        root.setTop(title);
        BorderPane.setMargin(title, new Insets(10));

        HBox elevatorContainer = new HBox(15);
        elevatorContainer.setAlignment(Pos.TOP_CENTER);
        elevatorContainer.setPadding(new Insets(10));

        for (int i = 0; i < 4; i++) {
            elevatorContainer.getChildren().add(elevators[i]);
        }
        root.setCenter(elevatorContainer);


        root.setRight(commandPanel);

        Scene scene = new Scene(root, 800, 660);
        primaryStage.setScene(scene);
        return primaryStage;

    }


}