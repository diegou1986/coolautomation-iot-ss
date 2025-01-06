package proxy;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.iot.client.AWSIotConnectionStatus;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;

public class AWSIoTBridge {

	private static final String CLIENT_ENDPOINT = "az7hwirb9l7ni-ats.iot.us-east-1.amazonaws.com"; // Endpoint de AWS
																									// IoT Core
//	private static final String CLIENT_ID = "pc_oficina"; // ID del dispositivo
//	private static final String CERT_DIR = "C:\\Users\\Diego\\Documents\\Certificados\\"; // Ruta de los certificados PC
	// DIEGO
//    private static final String CERT_DIR = "D:\\Certificados\\"; // Ruta de los certificados
//	private static final String CERT_DIR = "./"; // La misma ubicaci�n que el JAR
//	private static final String CERT_PASSWORD = "Maracaibo1!"; // Contrasena que definiste al exportar el .p12

	private AWSIotMqttClient mqttClient;

	private final ExecutorService executorService = Executors.newFixedThreadPool(10);

	private static final Logger logger = LoggerFactory.getLogger(AWSIoTBridge.class);

	private String deviceID;
	private String clientEndpoint;
	private String clientID;
	private String certDir;
	private String certPassword;

	private String topicCommands;
	private String topicResponse;
	private String topicStatus;
	
	private String scriptPython;

	public AWSIoTBridge(ConfigLoader configLoader) throws Exception {

		// Primero leo las properties

		deviceID = configLoader.getProperty("device.id");
		clientID = configLoader.getProperty("mqtt.client.id");

		topicCommands = configLoader.getProperty("mqtt.topic.commands") + deviceID;
		topicResponse = configLoader.getProperty("mqtt.topic.commands.response") + deviceID;
		topicStatus = configLoader.getProperty("mqtt.topic.status") + deviceID;

		certDir = configLoader.getProperty("mqtt.cert.dir");
		certPassword = configLoader.getProperty("mqtt.cert.pass");
		
		scriptPython = configLoader.getProperty("comando.script.python");

		// Cargar certificados en un KeyStore
		KeyStore keyStore = KeyStore.getInstance("PKCS12");

		try (FileInputStream fis = new FileInputStream(certDir + "device-cert.p12")) {
			keyStore.load(fis, certPassword.toCharArray());
			logger.info("Certificado cargado correctamente.");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		// Inicializar el cliente MQTT con el KeyStore
		mqttClient = new AWSIotMqttClient(CLIENT_ENDPOINT, clientID, keyStore, certPassword);
		mqttClient.setKeepAliveInterval(30000); // Enviar un ping cada 60 segundos
		mqttClient.setNumOfClientThreads(10);
		mqttClient.setMaxOfflineQueueSize(10000); // Cola de hasta 1000 mensajes mientras está desconectado
	
//		//Prueba de más intentos de reconexion
		mqttClient.setMaxConnectionRetries(10000);
		mqttClient.setMaxRetryDelay(60000); // 1min

		logger.info(deviceID + ": Cliente MQTT inicializado con configuración de reconexión extendida.");

		// Programar el monitoreo del estado de conexión
		monitorConnectionStatus();

	}

	public void connect() throws Exception {
		mqttClient.connect();
		logger.info("Conectado a AWS IoT Core. Dispositivo: " + deviceID);
	}

	public void disconnect() throws Exception {
		if (mqttClient != null && mqttClient.getConnectionStatus() == AWSIotConnectionStatus.CONNECTED) {
			mqttClient.disconnect();
			logger.info("Desconectado de AWS IoT Core. Dispositivo: " + deviceID);
		}
	}

	public void publish(String topic, String message) throws Exception {
		mqttClient.publish(topic, message);
		logger.info("Mensaje publicado en el topic " + topic + ": " + message);
	}

	public void subscribeToTopic(String topic) throws Exception {
		mqttClient.subscribe(new AWSIotTopic(topic, AWSIotQos.QOS1) {
			@Override
			public void onMessage(AWSIotMessage message) {
				logger.info("Comando recibido: " + message.getStringPayload());
				processCommand(message.getStringPayload());
			}
		});
	}

	private void processCommand(String command) {
		executorService.submit(() -> {
			String responseTopic = topicResponse;
			String responseMessage;

			try {
				switch (command.toLowerCase()) {
				case "apagar":

					// Lpgica para apagar el equipo
					CoolMasterASCIIClient client = new CoolMasterASCIIClient();

					// Prueba el comando "line" para interactuar con CoolMaster
					responseMessage = client.sendCommand("line");

					logger.info("Apagando el equipo...");
					mqttClient.publish(responseTopic, responseMessage);
					System.out.println(
							"Respuesta enviada al topic: " + responseTopic + " - Mensaje: \n" + responseMessage);
					break;

				case "encender":
					System.out.println("Encendiendo el equipo...");
					responseMessage = "{\"status\": \"success\", \"command\": \"encender\", \"message\": \"Equipo encendido correctamente.\"}";
					break;

				case "heartbeat":
					logger.info("Pulso...");
					break;

				default:
					System.out.println("Comando desconocido: " + command);
					responseMessage = "{\"status\": \"error\", \"command\": \"" + command
							+ "\", \"message\": \"Comando desconocido.\"}";
					break;
				}

			} catch (Exception e) {
				System.err.println("Error al procesar el comando: " + e.getMessage());
			}
		});
	}

	
	private void monitorConnectionStatus() {
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				logger.info("Estado de conexión: " + mqttClient.getConnectionStatus());

				String heartbeatMessage = "heartbeat";
				try {
					mqttClient.publish(topicCommands, heartbeatMessage);

					
//					publicarDatosTempHumCoolMaster();

//					publicarDatosTempHumCoolRaspberry();
					
					publicarDatosTempHumCoolRaspberryVariosSensores();
				} catch (AWSIotException e) {
					logger.error("Error al publicar datos al topico: " + topicStatus);
				}
				logger.info("Mensaje heartbeat enviado.");
			}
		}, 0, 60000); // Cada 60 segundos
	}

	private void publicarDatosTempHumCoolMaster() throws AWSIotException {

		String rawResponse = """
				UID         ON/OFF  MODE    RT     ST     HUM    FSPEED
				L1.102      ON      COOL    25     22     55%    MED
				L2.201      OFF     HEAT    24     22     50%    LOW
				L3.301      ON      FAN     23     23     60%    HIGH
				""";

		if (rawResponse != null) {
			// Procesar la respuesta y convertirla en JSON
			String[] lines = rawResponse.split("\n");
			for (String line : lines) {
				if (line.trim().isEmpty() || line.startsWith("UID"))
					continue;

				String[] parts = line.split("\\s+");
				String uid = parts[0];
				String onOff = parts[1];
				String mode = parts[2];

//				int roomTemp = Integer.parseInt(parts[3]);
//				int setTemp = Integer.parseInt(parts[4]);
//				int humidity = Integer.parseInt(parts[5].replace("%", ""));

				// PRUEBA
				Random random = new Random();

				// Generar numeros aleatorios entre 0 y 4 y sumarlos
				int roomTemp = Integer.parseInt(parts[3]) + random.nextInt(5); // nextInt(5) genera valores entre 0 y 4
				int setTemp = Integer.parseInt(parts[4]);
				int humidity = Integer.parseInt(parts[5].replace("%", "")) + random.nextInt(5);

				// Crear el mensaje JSON
				String message = String.format(
						"{\"deviceId\": \"%s\", \"uid\": \"%s\", \"onOff\": \"%s\", \"mode\": \"%s\", \"roomTemperature\": %d, \"setTemperature\": %d, \"humidity\": %d, \"timestamp\": %d}",
						deviceID, uid, onOff, mode, roomTemp, setTemp, humidity, System.currentTimeMillis());

				// Publicar al topico de AWS IoT
				mqttClient.publish(topicStatus, message);
				logger.info("Datos publicados de " + deviceID + ": " + message);

			}
		}

	}

	private void publicarDatosTempHumCoolRaspberry() throws AWSIotException {

		try {

			String command = "sudo myenv/bin/python3 /home/admin/dht22.py";

			// Ejecutar el comando
            Process process = Runtime.getRuntime().exec(command);

            // Leer la salida estándar del script
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

            // Leer la salida de error del script
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            StringBuilder jsonOutput = new StringBuilder();
            String line;

            // Capturar la salida estándar
            while ((line = stdInput.readLine()) != null) {
                jsonOutput.append(line);
            }

            // Capturar errores, si los hay
            System.out.println("Salida de error del script:");
            while ((line = stdError.readLine()) != null) {
            	logger.error(line);
            }

            // Esperar a que el proceso termine
            int exitCode = process.waitFor();
            logger.info("El script terminó con código: " + exitCode);

            // Parsear la salida JSON del script
            JSONObject jsonObject = new JSONObject(jsonOutput.toString());
            if (jsonObject.has("temperature") && jsonObject.has("humidity")) {
                double temp = jsonObject.getDouble("temperature");
                double hum = jsonObject.getDouble("humidity");

                // Crear el mensaje JSON para publicar en MQTT
                String message = String.format(
                	    "{\"deviceId\": \"%s\", \"uid\": \"%s\", \"onOff\": \"%s\", \"mode\": \"%s\", \"roomTemperature\": %s, \"setTemperature\": -1, \"humidity\": %s, \"timestamp\": %d}",
                	    deviceID, deviceID, "ON", "MONITOR", temp, hum, System.currentTimeMillis());

                // Publicar al topico de AWS IoT
                logger.info("Datos publicados de " + deviceID + ": " + message);
                // Aquí llamarías al cliente MQTT para publicar el mensaje
                 mqttClient.publish(topicStatus, message);
            } else if (jsonObject.has("error")) {
            	logger.error("Error del script Python: " + jsonObject.getString("error"));
            }

		} catch (Exception e) {
			logger.error("Error al publicar datos al topico: " + topicStatus + ", Error: " + e);
			e.printStackTrace();
		}

	}
	
	private void publicarDatosTempHumCoolRaspberryVariosSensores() throws AWSIotException {
	    try {
	        String command = scriptPython; // Script que maneja los 2 sensores

	        // Ejecutar el comando
	        Process process = Runtime.getRuntime().exec(command);

	        // Leer la salida estándar del script
	        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

	        // Leer la salida de error del script
	        BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

	        StringBuilder jsonOutput = new StringBuilder();
	        String line;

	        // Capturar la salida estándar
	        while ((line = stdInput.readLine()) != null) {
	            jsonOutput.append(line);
	        }

	        // Capturar errores, si los hay
	        System.out.println("Salida de error del script:");
	        while ((line = stdError.readLine()) != null) {
	            logger.error(line);
	        }

	        // Esperar a que el proceso termine
	        int exitCode = process.waitFor();
	        logger.info("El script terminó con código: " + exitCode);

	        // Parsear la salida JSON del script
	        JSONObject jsonObject = new JSONObject(jsonOutput.toString());
	        if (jsonObject.has("sensors")) {
	            JSONArray sensorsArray = jsonObject.getJSONArray("sensors");

	            // Iterar sobre cada sensor en la respuesta
	            for (int i = 0; i < sensorsArray.length(); i++) {
	                JSONObject sensorData = sensorsArray.getJSONObject(i);

	                if (sensorData.has("temperature") && sensorData.has("humidity") && sensorData.has("alarmThreshold")) {
	                    String sensorName = sensorData.getString("sensor");
	                    double temp = sensorData.getDouble("temperature");
	                    double hum = sensorData.getDouble("humidity");
	                    JSONObject thresholds = sensorData.getJSONObject("alarmThreshold");
	                    double highThreshold = thresholds.optDouble("high", 40);
	                    double lowThreshold = thresholds.optDouble("low", 10);

	                    // Crear el mensaje JSON para publicar en MQTT
	                    String message = String.format(
	                        "{\"deviceId\": \"%s\", \"sensor\": \"%s\", \"uid\": \"%s\", \"onOff\": \"%s\", \"mode\": \"%s\", \"roomTemperature\": %s, \"setTemperature\": -1, \"humidity\": %s, \"alarmThreshold\": {\"high\": %s, \"low\": %s}, \"timestamp\": %d}",
	                        deviceID + "_" + sensorName, sensorName, deviceID + "_" + sensorName, "ON", "SENSOR", temp, hum, highThreshold, lowThreshold, System.currentTimeMillis());

	                    String topicForSensor = topicStatus + "_" + sensorName.replace(" ", "_").toLowerCase();
	                    logger.info("Datos publicados de " + topicForSensor + ": " + message);
	                    mqttClient.publish(topicForSensor, message); // Publica los datos del sensor

	                } else if (sensorData.has("error")) {
	                    logger.error("Error en " + sensorData.getString("sensor") + ": " + sensorData.getString("error"));
	                }
	            }
	        } else if (jsonObject.has("error")) {
	            logger.error("Error del script Python: " + jsonObject.getString("error"));
	        }

	    } catch (Exception e) {
	        logger.error("Error al publicar datos al tópico: " + topicStatus + ", Error: " + e);
	        e.printStackTrace();
	    }
	}



	// GETTERS Y SETTERS
	public AWSIotMqttClient getMqttClient() {
		return mqttClient;
	}

	public void setMqttClient(AWSIotMqttClient mqttClient) {
		this.mqttClient = mqttClient;
	}

	public String getClientEndpoint() {
		return clientEndpoint;
	}

	public void setClientEndpoint(String clientEndpoint) {
		this.clientEndpoint = clientEndpoint;
	}

	public String getClientID() {
		return clientID;
	}

	public void setClientID(String clientID) {
		this.clientID = clientID;
	}

	public String getCertDir() {
		return certDir;
	}

	public void setCertDir(String certDir) {
		this.certDir = certDir;
	}

	public String getCertPassword() {
		return certPassword;
	}

	public void setCertPassword(String certPassword) {
		this.certPassword = certPassword;
	}

	public String getTopicCommands() {
		return topicCommands;
	}

	public void setTopicCommands(String topicCommands) {
		this.topicCommands = topicCommands;
	}

	public String getTopicResonse() {
		return topicResponse;
	}

	public void setTopicResonse(String topicResonse) {
		this.topicResponse = topicResonse;
	}

	public String getTopicStatus() {
		return topicStatus;
	}

	public void setTopicStatus(String topicStatus) {
		this.topicStatus = topicStatus;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

}
