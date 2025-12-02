package Motion.GUI;

import Motion.Hardware.*;
import Motion.Util.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class SensorFX implements Observer {
    private Circle sen;

    public SensorFX(Circle sen){
        this.sen = sen;
    }
    @Override
    public void update(Observable viewee) {
        if(viewee instanceof Sensor){
            if(((Sensor) viewee).is_triggered()){
                sen.setFill(Color.GREEN);
            }else {
                sen.setFill(Color.RED);
            }
        }
    }
}
