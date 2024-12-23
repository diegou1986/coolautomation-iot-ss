package proxy;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private Properties properties;

    public ConfigLoader() throws Exception {
        properties = new Properties();
        
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new Exception("No se pudo encontrar el archivo config.properties");
            }
            properties.load(input);
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }
}
