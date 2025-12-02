import Bus.SoftwareBus;
import Bus.SoftwareBusCodes;
import ElevatorController.LowerLevel.DoorAssembly;
import Message.Message;
import Mux.ElevatorMultiplexor;

public class DoorAssemblyTest {
    public static void main(String[] args) {
        //SoftwareBus softwareBus=new SoftwareBus(true);
        SoftwareBus softwareBusClient =new SoftwareBus(true);
        DoorAssembly doorAssembly =new DoorAssembly(1,softwareBusClient);
        softwareBusClient.subscribe(SoftwareBusCodes.doorControl,1);

        while(true){
           doorAssembly.close();
            //softwareBusClient.publish(new Message(SoftwareBusCodes.doorControl,1,100));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Message message= softwareBusClient.get(SoftwareBusCodes.doorControl,0);
            System.out.println(message);



        }
    }
}
