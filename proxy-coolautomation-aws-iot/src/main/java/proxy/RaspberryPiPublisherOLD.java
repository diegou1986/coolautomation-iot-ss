package proxy;

import java.util.Timer;
import java.util.TimerTask;

public class RaspberryPiPublisherOLD {
    public static void main(String[] args) throws Exception {
        AWSIoTBridge bridge = new AWSIoTBridge();
        bridge.connect();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    String topic = "raspberrypi/data";
                    String message = "{\"deviceId\": \"raspberrypi22\",\"temperature\": 22.5, \"humidity\": 60, \"timestamp\": " 
                    + System.currentTimeMillis() + "}";
                    bridge.publish(topic, message);
                    System.out.println("Datos publicados: " + message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 60 * 1000); // Publicar cada minuto
    }
}

