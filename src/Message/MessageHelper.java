package Message;

import Bus.SoftwareBus;

/**
 * This class is meant to hold methods that we will use on messages in all lower level objects
 * (Could go in processUtil)
 */
public class MessageHelper {

    public static Message pullAllMessages(SoftwareBus softwareBus, int elevatorID, int topic) {
        Message message = softwareBus.get(topic, elevatorID);
        Message temp = message;
        while(temp != null) {
            message = temp;
            temp = softwareBus.get(topic, elevatorID);
        }
        return message;
    }
}
