package proxy;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RaspberryPiPublisher {
	public static void main(String[] args) throws Exception {
		ConfigLoader configLoader = new ConfigLoader();
        AWSIoTBridge bridge = new AWSIoTBridge(configLoader);
        
//        CoolMasterASCIIClient coolMasterClient = new CoolMasterASCIIClient();

		// Conectar al puente AWS IoT
		bridge.connect();

		// Configuracio del timer para enviar datos periodicamente
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
							
							
//							int roomTemp = Integer.parseInt(parts[3]);
//							int setTemp = Integer.parseInt(parts[4]);
//							int humidity = Integer.parseInt(parts[5].replace("%", ""));
							
							Random random = new Random();

							// Generar numeros aleatorios entre 0 y 4 y sumarlos
							int roomTemp = Integer.parseInt(parts[3]) + random.nextInt(5); // nextInt(5) genera valores entre 0 y 4
							int setTemp = Integer.parseInt(parts[4]) + random.nextInt(5);
							int humidity = Integer.parseInt(parts[5].replace("%", "")) + random.nextInt(5);

							// Crear el mensaje JSON
							String message = String.format(
									"{\"deviceId\": \"%s\", \"uid\": \"%s\", \"onOff\": \"%s\", \"mode\": \"%s\", \"roomTemperature\": %d, \"setTemperature\": %d, \"humidity\": %d, \"timestamp\": %d}",
									"raspberrypi22", uid, onOff, mode, roomTemp, setTemp, humidity,
									System.currentTimeMillis());

							// Publicar al topico de AWS IoT
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
