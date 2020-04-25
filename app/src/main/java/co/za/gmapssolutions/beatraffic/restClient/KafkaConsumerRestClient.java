package co.za.gmapssolutions.beatraffic.restClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KafkaConsumerRestClient implements Runnable {
    private URL url;
    private HttpURLConnection con;
    public KafkaConsumerRestClient(URL url){
        this.url = url;
    }
    @Override
    public void run(){
        try {
            con = (HttpURLConnection) url.openConnection();
            try(BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
