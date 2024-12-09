package publisher;

public class IoTPublisherMain {

	public static void main(String[] args) {
		// Inicializa las clases necesarias
		String topicStatus = "raspberrypi/status";
		String topicCommands = "raspberrypi/commands";

		System.setProperty("com.amazonaws.sdk.enableDefaultMetrics", "true");

		System.setProperty("https.protocols", "TLSv1.2");

		try {
			IoTWebPublisher publisher = new IoTWebPublisher();
			String topic = "commands/control";
//	            String command = "{\"command\": \"apagar\", \"timestamp\": \"2024-11-26T14:30:00Z\"}";
			String command = "{\"temperature\": 22.5, \"humidity\": 60, \"timestamp\": " + System.currentTimeMillis() + "}";

//			String command = "apagar";
			publisher.publishCommand(topic, command);
			
			
			publisher.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void sendCommand() {

	}
}
