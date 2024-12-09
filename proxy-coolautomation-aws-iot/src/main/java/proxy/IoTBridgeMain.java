package proxy;

public class IoTBridgeMain {

    public static void main(String[] args) {
        AWSIoTBridge awsIoTBridge = null;
        CoolMasterAPI coolMasterAPI = new CoolMasterAPI();
        String topicStatus = "raspberrypi/data";
        String topicCommands = "raspberrypi/commands";
        String topicResonse = "raspberrypi/commands/response";

        System.setProperty("com.amazonaws.services.iot.client.debug", "true");
        System.setProperty("com.amazonaws.sdk.enableDefaultMetrics", "true");        
		System.setProperty("https.protocols", "TLSv1.2");

        try {
            // Inicializar el puente
            awsIoTBridge = new AWSIoTBridge();
            awsIoTBridge.connect();

            // Suscribirse al topic de comandos
            awsIoTBridge.subscribeToTopic(topicCommands);

            // Publicar datos periódicamente
            while (true) {
                try {
                    // Obtener datos del CoolMaster
//                    Map<String, Object> deviceData = coolMasterAPI.getDeviceData();
//
//                    if (deviceData != null) {
//                        String message = new ObjectMapper().writeValueAsString(deviceData);
//                        awsIoTBridge.publish(topicStatus, message);
//                        System.out.println("Datos publicados: " + message);
//                    } else {
//                        System.out.println("No se pudieron obtener datos del CoolMaster.");
//                    }
                } catch (Exception e) {
                    System.err.println("Error al publicar datos: " + e.getMessage());
                }

                Thread.sleep(1000); // Esperar 1 segundo antes de la siguiente publicación
            }

        } catch (Exception e) {
            System.err.println("Error en el puente IoT: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (awsIoTBridge != null) {
                try {
                    awsIoTBridge.disconnect();
                } catch (Exception e) {
                    System.err.println("Error al desconectar el cliente: " + e.getMessage());
                }
            }
        }
    }
}


