package br.com.premiumpriceapi.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class NominatimService {
    private final HttpClient httpClient;

    public NominatimService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public HttpResponse<String> getReverseGeocoding(Double latitude, Double longitude) {
        String url = String.format(Locale.US, "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", latitude, longitude);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();


            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response;
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException("Error in Nominatim Service: ", e);
        }
    }

    public String reverseGeocodingShop(Double latitude, Double longitude) {
        HttpResponse<String> response = getReverseGeocoding(latitude, longitude);

        JSONObject json = new JSONObject(response.body());

        // Lógica para extrair dados
        JSONObject address = json.optJSONObject("address");

        if (address != null) {
            String shop = address.optString("shop", null);
            if (shop != null) {
                return shop;
            }

            String road = address.optString("road", null);
            if (road != null) {
                return road;
            }
        }

        if ("shop".equals(json.optString("class", null))) {
            String name = json.optString("name", null);
            if (name != null) {
                return name;
            }
        }

        return json.optString("display_name", null);
    }
}