package co.za.gmapssolutions.beatraffic.restClient;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class KafkaConsumerRestClient implements Runnable {
    private final RestClient restClient;
    private final  Handler handler;
    private final Bundle bundle = new Bundle();

    public KafkaConsumerRestClient(RestClient restClient, Handler handler){
        this.restClient = restClient;
        this.handler = handler;
    }
    @Override
    public void run(){
        try {
            int response = restClient.get();
            Message msg = handler.obtainMessage();
            if(response == HttpURLConnection.HTTP_OK){
                bundle.putString("traffic-forecast",restClient.getData());
            }
            bundle.putInt("traffic-response", response);
            msg.setData(bundle);
            handler.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
