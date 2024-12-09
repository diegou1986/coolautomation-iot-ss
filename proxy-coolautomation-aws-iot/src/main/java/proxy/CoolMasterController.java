package proxy;

public class CoolMasterController {

    private CoolMasterASCIIClient client;

    public CoolMasterController() {
        client = new CoolMasterASCIIClient();
    }

    public String turnOnUnit(String uid) {
        return client.sendCommand("on " + uid);
    }

    public String turnOffUnit(String uid) {
        return client.sendCommand("off " + uid);
    }

    public String setMode(String uid, String mode) {
        return client.sendCommand(mode + " " + uid);
    }

    public String setTemperature(String uid, String temp) {
        return client.sendCommand("temp " + uid + " " + temp);
    }

    public String setFanSpeed(String uid, String speed) {
        return client.sendCommand("fspeed " + uid + " " + speed);
    }

    public String setSwing(String uid, String swing) {
        return client.sendCommand("swing " + uid + " " + swing);
    }

    public static void main(String[] args) {
        CoolMasterController controller = new CoolMasterController();

        // Ejemplos de uso:
        System.out.println(controller.turnOnUnit("L1.102"));
        System.out.println(controller.setMode("L1.102", "cool"));
        System.out.println(controller.setTemperature("L1.102", "23"));
        System.out.println(controller.setFanSpeed("L1.102", "m"));
        System.out.println(controller.setSwing("L1.102", "h"));
        System.out.println(controller.turnOffUnit("L1.102"));
    }
}

