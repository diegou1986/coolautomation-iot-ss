package proxy;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

import java.io.FileInputStream;
import java.security.KeyStore;


public class AWSIoTBridge {

    private static final String CLIENT_ENDPOINT = "az7hwirb9l7ni-ats.iot.us-east-1.amazonaws.com"; // Endpoint de AWS IoT Core
    private static final String CLIENT_ID = "pc_oficina"; // ID del dispositivo
//    private static final String CERT_DIR = "C:\\Users\\Diego\\Documents\\Certificados\\"; // Ruta de los certificados PC DIEGO
    private static final String CERT_DIR = "D:\\Certificados\\"; // Ruta de los certificados
    private static final String CERT_PASSWORD = "Maracaibo1!"; // Contraseña que definiste al exportar el .p12

    private AWSIotMqttClient mqttClient;

    public AWSIoTBridge() throws Exception {
        // Cargar certificados en un KeyStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
//        keyStore.load(new FileInputStream(CERT_DIR + "device-cert.p12"), CERT_PASSWORD.toCharArray());
        
        try (FileInputStream fis = new FileInputStream(CERT_DIR + "device-cert.p12")) {
            keyStore.load(fis, CERT_PASSWORD.toCharArray());
            System.out.println("Certificado cargado correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            throw e; // Para depurar más detalles
        }

        // Inicializar el cliente MQTT con el KeyStore
        mqttClient = new AWSIotMqttClient(CLIENT_ENDPOINT, CLIENT_ID, keyStore, CERT_PASSWORD);
        System.out.println("Cliente MQTT inicializado con KeyStore.");
    }

    public void connect() throws Exception {
        mqttClient.connect();
        System.out.println("Conectado a AWS IoT Core.");
    }

    public void disconnect() throws Exception {
        if (mqttClient != null && mqttClient.getConnectionStatus() == AWSIotConnectionStatus.CONNECTED) {
            mqttClient.disconnect();
            System.out.println("Desconectado de AWS IoT Core.");
        }
    }

    public void publish(String topic, String message) throws Exception {
        mqttClient.publish(topic, message);
        System.out.println("Mensaje publicado en el topic " + topic + ": " + message);
    }

    public void subscribe(String topic) throws Exception {
        CustomAWSIotTopic topicListener = new CustomAWSIotTopic(topic, AWSIotQos.QOS1);
        mqttClient.subscribe(topicListener);
        System.out.println("Suscrito al topic " + topic);
    }
    
    public void subscribeToTopic(String topic) throws Exception {
        mqttClient.subscribe(new AWSIotTopic(topic, AWSIotQos.QOS1) {
            @Override
            public void onMessage(AWSIotMessage message) {
                System.out.println("Comando recibido: " + message.getStringPayload());
                processCommand(message.getStringPayload());
            }
        });
    }

    private static class CustomAWSIotTopic extends AWSIotTopic {
        public CustomAWSIotTopic(String topic, AWSIotQos qos) {
            super(topic, qos);
        }

        @Override
        public void onMessage(com.amazonaws.services.iot.client.AWSIotMessage message) {
            System.out.println("Mensaje recibido: " + message.getStringPayload());
        }
    }
    
    private void processCommand(String command) {
        String responseTopic = "raspberrypi/commands/response";
        String responseMessage;

        try {
            switch (command.toLowerCase()) {
                case "apagar":
                    System.out.println("Apagando el equipo...");
                    // Lógica para apagar el equipo
                    CoolMasterASCIIClient client = new CoolMasterASCIIClient();

                    // Prueba el comando "line" para interactuar con CoolMaster
                    String response = client.sendCommand("line");
                    if (response != null) {
                        System.out.println("Respuesta del CoolMaster: " + response);
                        responseMessage = "{\"status\": \"success\", \"command\": \"apagar\", \"message\": \"Equipo apagado correctamente.\"}";
                    } else {
                        responseMessage = "{\"status\": \"error\", \"command\": \"apagar\", \"message\": \"No se pudo apagar el equipo.\"}";
                    }
                    break;

                case "encender":
                    System.out.println("Encendiendo el equipo...");
                    // Lógica para encender el equipo
                    responseMessage = "{\"status\": \"success\", \"command\": \"encender\", \"message\": \"Equipo encendido correctamente.\"}";
                    break;

                default:
                    System.out.println("Comando desconocido: " + command);
                    responseMessage = "{\"status\": \"error\", \"command\": \"" + command + "\", \"message\": \"Comando desconocido.\"}";
                    break;
            }
        } catch (Exception e) {
            responseMessage = "{\"status\": \"error\", \"command\": \"" + command + "\", \"message\": \"Error procesando el comando: " + e.getMessage() + "\"}";
        }

        try {
            mqttClient.publish(responseTopic, responseMessage);
            System.out.println("Respuesta enviada al topic: " + responseTopic + " - Mensaje: " + responseMessage);
        } catch (Exception e) {
            System.err.println("Error al publicar la respuesta: " + e.getMessage());
        }
    }

}


