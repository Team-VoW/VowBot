package me.kmaxi.wynnvp;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthApiClient {

    private final String baseUrl = "http://129.151.214.102:8080/auth"; // Replace with your API base URL
    private final String apiKey = "test"; // Replace with your actual API key


    public ServerRespons checkAuthentication(String key) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/check?key=" + key))
                    .header("Authorization", apiKey)
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

            return ServerRespons.mapResponse(response.statusCode());
        } catch (ConnectException e) {
            return ServerRespons.SERVER_DOWN;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ServerRespons.SERVER_ERROR;
        }
    }

    public ServerRespons addToAuthentication(String key) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/add"))
                    .header("Authorization", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(key))
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            return ServerRespons.mapResponse(response.statusCode());
        } catch (ConnectException e) {
            return ServerRespons.SERVER_DOWN; // Service Unavailable (Connection Error)
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ServerRespons.SERVER_ERROR; // Internal Server Error
        }
    }


    public String getAllAuthenticationEntries() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/getAll"))
                .header("Authorization", apiKey)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public static void main(String[] args) {
        AuthApiClient apiClient = new AuthApiClient();
/*
        // Test checkAuthentication
        ServerRespons result1 = apiClient.checkAuthentication("new_key_to_add");
        System.out.println("checkAuthentication Result: " + result1);


 */

        // Test addToAuthentication
        ServerRespons result2 = apiClient.addToAuthentication("1gC831ae2fJ4fEZM");
        System.out.println("addToAuthentication Result: " + result2);
/*
            // Test getAllAuthenticationEntries
            String result3 = apiClient.getAllAuthenticationEntries();
            System.out.println("getAllAuthenticationEntries Result: " + result3);*/
    }
}
