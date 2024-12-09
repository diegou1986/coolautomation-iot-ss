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
    private static final String CERT_DIR = "D:\\Certificados\\"; // Ruta de los certificados
    private static final String CERT_PASSWORD = "Maracaibo1!"; // Contraseña que definiste al exportar el .p12

    private AWSIotMqttClient mqttClient;
    private CustomAWSIotTopic commandTopic;

    public AWSIoTBridge() throws Exception {
        // Cargar certificados en un KeyStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(CERT_DIR + "device-cert.p12"), CERT_PASSWORD.toCharArray());

        // Inicializar el cliente MQTT con el KeyStore
        mqttClient = new AWSIotMqttClient(CLIENT_ENDPOINT, CLIENT_ID, keyStore, CERT_PASSWORD);
        
        
        mqttClient.setKeepAliveInterval(60);
        mqttClient.setConnectionTimeout(30); 
        
        
        
        System.out.println("Cliente MQTT inicializado con KeyStore.");

        // Inicializar el tópico de comandos
        commandTopic = new CustomAWSIotTopic("raspberrypi/commands", AWSIotQos.QOS1);
    }

    public void connect() throws Exception {
        mqttClient.connect();
        System.out.println("Conectado a AWS IoT Core.");

        // Asegurarse de volver a suscribirse al reconectar
        mqttClient.subscribe(commandTopic);
        System.out.println("Suscrito al topic: raspberrypi/commands");
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

    private static class CustomAWSIotTopic extends AWSIotTopic {
        public CustomAWSIotTopic(String topic, AWSIotQos qos) {
            super(topic, qos);
        }

        @Override
        public void onMessage(AWSIotMessage message) {
            System.out.println("Comando recibido: " + message.getStringPayload());
            processCommand(message.getStringPayload());
        }

        private void processCommand(String command) {
            // Procesa el comando recibido
            switch (command.toLowerCase()) {
                case "apagar":
                    System.out.println("Apagando el equipo...");
                    // Lógica para apagar el equipo
                    break;
                case "encender":
                    System.out.println("Encendiendo el equipo...");
                    // Lógica para encender el equipo
                    break;
                default:
                    System.out.println("Comando desconocido: " + command);
            }
        }
    }
}

