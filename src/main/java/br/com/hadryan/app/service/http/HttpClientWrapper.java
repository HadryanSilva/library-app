package br.com.hadryan.app.service.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Wrapper para requisições HTTP com suporte a cache e timeouts configuráveis.
 *
 * @author Hadryan Silva
 * @since 23-03-2025
 */
public class HttpClientWrapper {
    private static final Logger LOGGER = Logger.getLogger(HttpClientWrapper.class.getName());

    private final Map<String, String> cacheRequisicoes = new HashMap<>();
    private int connectTimeout = 5000;
    private int readTimeout = 5000;
    private boolean useCache = true;

    /**
     * Construtor padrão
     */
    public HttpClientWrapper() {
    }

    /**
     * Configura os timeouts
     */
    public void setTimeouts(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Habilita ou desabilita o cache de requisições
     */
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    /**
     * Faz uma requisição GET para a URL especificada
     */
    public String fazerRequisicaoGet(String urlStr) throws IOException {
        if (useCache && cacheRequisicoes.containsKey(urlStr)) {
            LOGGER.log(Level.FINE, "Utilizando resposta em cache para: " + urlStr);
            return cacheRequisicoes.get(urlStr);
        }

        LOGGER.log(Level.FINE, "Fazendo requisição HTTP para: " + urlStr);

        HttpURLConnection connection = null;
        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                String responseStr = response.toString();

                if (useCache && !responseStr.isEmpty()) {
                    LOGGER.log(Level.FINE, "Adicionando resposta ao cache para: " + urlStr);
                    cacheRequisicoes.put(urlStr, responseStr);
                }

                return responseStr;
            } else {
                LOGGER.log(Level.WARNING, "Resposta não-OK da API: " + responseCode + " para URL: " + urlStr);
                return null;
            }

        } finally {
            if (reader != null) {
                reader.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
