package proxy;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IoTBridgeMain {

    public static void main(String[] args) {
        AWSIoTBridge awsIoTBridge = null;
        CoolMasterAPI coolMasterAPI = new CoolMasterAPI();
        String topicStatus = "raspberrypi/data";
        String topicCommands = "raspberrypi/commands";

        System.setProperty("com.amazonaws.services.iot.client.debug", "true");

        try {
            // Inicializar el puente
            awsIoTBridge = new AWSIoTBridge();
            awsIoTBridge.connect();

            // Suscribirse al topic de comandos
//            awsIoTBridge.subscribeToCommands();

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

                Thread.sleep(30000); // Esperar 30 segundos antes de la siguiente publicación
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


