package co.za.gmapssolutions.beatraffic.restClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class RestClient {
    private final String TAG = RestClient.class.getSimpleName();
    private final URL url;
    private final StringBuilder response = new StringBuilder();

    public RestClient(URL url){
        this.url = url;

    }
    public int post(String data) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        setup(con);
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        return con.getResponseCode();
    }
    public int get() throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        setup(con);
        con.setRequestMethod("GET");
        if(con.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
        }
        return con.getResponseCode();
    }
    public String getData(){
        return response.toString();
    }

    private void setup(HttpURLConnection con) {
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
    }
}
