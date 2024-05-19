package me.falu.exero;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExeroHttp {
    private static final Logger LOGGER = LogManager.getLogger();

    public static String get(URL uri) {
        try {
            HttpURLConnection connection = (HttpURLConnection) uri.openConnection();
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return response.toString();
            } else {
                LOGGER.warn("GET request returned non-ok response code: {}", code);
            }
            connection.disconnect();
        } catch (IOException e) {
            LOGGER.error("Error during GET request", e);
        }
        return null;
    }
}
