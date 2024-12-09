package proxy;

import java.util.Timer;
import java.util.TimerTask;

public class RaspberryPiPublisher {
	public static void main(String[] args) throws Exception {
		AWSIoTBridge bridge = new AWSIoTBridge();
//        CoolMasterASCIIClient coolMasterClient = new CoolMasterASCIIClient();

		// Conectar al puente AWS IoT
		bridge.connect();

		// Configuración del timer para enviar datos periódicamente
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					// Obtener datos del CoolMaster
//                    String rawResponse = coolMasterClient.sendCommand("status");
					
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
							int roomTemp = Integer.parseInt(parts[3]);
							int setTemp = Integer.parseInt(parts[4]);
							int humidity = Integer.parseInt(parts[5].replace("%", ""));

							// Crear el mensaje JSON
							String message = String.format(
									"{\"deviceId\": \"%s\", \"uid\": \"%s\", \"onOff\": \"%s\", \"mode\": \"%s\", \"roomTemperature\": %d, \"setTemperature\": %d, \"humidity\": %d, \"timestamp\": %d}",
									"raspberrypi22", uid, onOff, mode, roomTemp, setTemp, humidity,
									System.currentTimeMillis());

							// Publicar al tópico de AWS IoT
							bridge.publish("raspberrypi/data", message);
							System.out.println("Datos publicados: " + message);
						}
					} else {
						System.err.println("No se obtuvo respuesta del CoolMaster.");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 0, 60 * 1000); // Publicar cada minuto
	}
}
