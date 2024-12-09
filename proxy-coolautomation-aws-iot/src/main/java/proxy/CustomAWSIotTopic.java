package proxy;

import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

public class CustomAWSIotTopic extends AWSIotTopic {

    public CustomAWSIotTopic(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    @Override
    public void onMessage(com.amazonaws.services.iot.client.AWSIotMessage message) {
        System.out.println("Mensaje recibido: " + message.getStringPayload());
    }
}

