package publisher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;

import com.amazonaws.services.iot.client.AWSIotMqttClient;

public class IoTWebPublisher {
    private static final String CLIENT_ENDPOINT = "az7hwirb9l7ni-ats.iot.us-east-1.amazonaws.com"; // Endpoint
    private static final String CLIENT_ID = "web-app-system-service"; // Client ID único
//    private static final String CLIENT_ID = "pc_oficina"; // ID del dispositivo
    private static final String CERT_RESOURCE_PATH = "/certificados/device-cert.p12"; // Ruta en resources
//    private static final String CERT_RESOURCE_PATH = "/certificados/device-cert.p12"; // Ruta en resources
    private static final String CERT_PASSWORD = "Maracaibo1!"; // Contraseña del .p12
    
//    private static final String CERT_RESOURCE_PATH = "D:\\Certificados\\web app\\app-web-system-service-cert.p12"; // Ruta de los certificados
                                                     
    private static final String CERT_DIR = "D:\\Certificados\\"; // Ruta de los certificados
    
    private AWSIotMqttClient mqttClient;

    public IoTWebPublisher() throws Exception {
        // Extrae el archivo .p12 de resources al sistema de archivos
        File tempCertFile = extractCertFile();
    	
    	 // Cargar certificados en un KeyStore
//        KeyStore keyStore = KeyStore.getInstance("PKCS12");
//        keyStore.load(new FileInputStream(CERT_DIR + "device-cert.p12"), CERT_PASSWORD.toCharArray());

        // Inicializar el cliente MQTT con el KeyStore
//        mqttClient = new AWSIotMqttClient(CLIENT_ENDPOINT, CLIENT_ID, keyStore, CERT_PASSWORD);
//        System.out.println("Cliente MQTT inicializado con KeyStore.");

        // Cargar el archivo .p12 en un KeyStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(tempCertFile), CERT_PASSWORD.toCharArray());

        // Inicializar el cliente MQTT usando el KeyStore
        mqttClient = new AWSIotMqttClient(CLIENT_ENDPOINT, CLIENT_ID, keyStore, CERT_PASSWORD);
        mqttClient.setKeepAliveInterval(30); 
        mqttClient.connect();

        // Limpia el archivo temporal
        tempCertFile.delete();
    }

    public void publishCommand(String topic, String message) throws Exception {
        mqttClient.publish(topic, message);
        System.out.println("Mensaje publicado: " + message);
    }

    public void disconnect() throws Exception {
        if (mqttClient != null) {
            mqttClient.disconnect();
            System.out.println("Desconectado de AWS IoT Core.");
        }
    }

    private File extractCertFile() throws Exception {
        // Obtiene el archivo del classpath
        InputStream certStream = getClass().getResourceAsStream(CERT_RESOURCE_PATH);
        if (certStream == null) {
            throw new Exception("Certificado no encontrado en el classpath: " + CERT_RESOURCE_PATH);
        }

        // Crea un archivo temporal
        File tempCertFile = File.createTempFile("app-web-system-service-cert", ".p12");
        try (FileOutputStream outStream = new FileOutputStream(tempCertFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = certStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
        }

        return tempCertFile;
    }
}
