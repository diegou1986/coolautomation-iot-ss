package proxy;

import java.io.*;
import java.net.Socket;

public class CoolMasterASCIIClient {

    private static final String COOLMASTER_IP = "192.168.1.26";
    private static final int COOLMASTER_PORT = 10102;

    public String sendCommand(String command) {
        try (Socket socket = new Socket(COOLMASTER_IP, COOLMASTER_PORT);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Enviar el comando
            writer.println(command);

            // Leer la respuesta
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append("\n");
                if (line.equals("OK")) { // Salir al final de la respuesta
                    break;
                }
            }

            return response.toString();

        } catch (IOException e) {
            System.err.println("Error al comunicarse con CoolMasterNet: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        CoolMasterASCIIClient client = new CoolMasterASCIIClient();

        // Prueba el comando "ls" para listar dispositivos
        String response = client.sendCommand("status");
        if (response != null) {
            System.out.println("Respuesta del CoolMaster:");
            System.out.println(response);
        } else {
            System.out.println("No se pudo obtener respuesta del CoolMaster.");
        }

        // Habilitar REST
        String restResponse = client.sendCommand("rest enable");
        System.out.println("Respuesta al comando 'rest enable': " + restResponse);
    }
}

