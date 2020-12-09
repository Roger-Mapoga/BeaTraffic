package co.za.gmapssolutions.beatraffic.restClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class RestClient {
    private final String TAG = RestClient.class.getSimpleName();
    private final HttpURLConnection con;
    private final StringBuilder response = new StringBuilder();

    public RestClient(HttpURLConnection con){
        this.con = con;
        this.con.setRequestProperty("Content-Type", "application/json; utf-8");
        this.con.setRequestProperty("Accept", "application/json");
    }
    public int post(String data) throws IOException {
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        try(OutputStream os = con.getOutputStream()) {
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
       // Log.i(TAG,"Response: "+ con.getResponseCode());
        return con.getResponseCode();
    }
    public int get() throws IOException {
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
//        System.out.println(con.getResponseCode());
        return con.getResponseCode();
    }
    public String getData(){
        return response.toString();
    }



//    //        Log.i(TAG,"Response: "+ responseCode);
//        System.out.println(responseCode);
//        if (responseCode == HttpURLConnection.HTTP_OK){

//    }else {
//        System.out.println("GET request not successful");
//    }
}
