package proxy;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class CoolMasterAPI {

    private static final String COOLMASTER_URL = "http://<IP_LOCAL_COOLMASTER>/v2.0/device/ls";

    /**
     * Realiza una solicitud GET a la API REST del CoolMaster y devuelve los datos como un Map.
     *
     * @return Un Map con los datos de los dispositivos o null en caso de error.
     */
    public Map<String, Object> getDeviceData() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            // Configurar la URL y abrir la conexión
            URL url = new URL(COOLMASTER_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            // Leer la respuesta
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parsear la respuesta JSON a un Map
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(response.toString(), Map.class);

        } catch (Exception e) {
            System.err.println("Error al obtener datos del CoolMaster: " + e.getMessage());
            return null;

        } finally {
            // Cerrar recursos
            try {
                if (reader != null) reader.close();
                if (connection != null) connection.disconnect();
            } catch (Exception ex) {
                System.err.println("Error al cerrar recursos: " + ex.getMessage());
            }
        }
    }
}

