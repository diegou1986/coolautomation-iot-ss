package proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IoTBridgeMain {
	
	private static final Logger logger = LoggerFactory.getLogger(IoTBridgeMain.class);


    public static void main(String[] args) {
        AWSIoTBridge awsIoTBridge = null;
//        String topicStatus = "raspberrypi/data";
//        String topicCommands = "raspberrypi/commands";
//        String topicResonse = "raspberrypi/commands/response";

        System.setProperty("com.amazonaws.services.iot.client.debug", "true");
        System.setProperty("com.amazonaws.sdk.enableDefaultMetrics", "true");        
		System.setProperty("https.protocols", "TLSv1.2");

        try {
        	
        	ConfigLoader configLoader = new ConfigLoader();
        	
        	String deviceID = configLoader.getProperty("device.id");
        	String topicCommands = configLoader.getProperty("mqtt.topic.commands") + deviceID;
            // Inicializar el puente
            awsIoTBridge = new AWSIoTBridge(configLoader);
            
            awsIoTBridge.connect();

            // Suscribirse al topic de comandos
            awsIoTBridge.subscribeToTopic(topicCommands);

            //Dejo abierto el programa
            while (true) {
              
            }

        } catch (Exception e) {
            logger.error("Error en el puente IoT: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (awsIoTBridge != null) {
                try {
                    awsIoTBridge.disconnect();
                } catch (Exception e) {
                    logger.error("Error al desconectar el cliente: " + e.getMessage());
                }
            }
        }
    }
}


