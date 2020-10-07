package co.za.gmapssolutions.beatraffic.Roads;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import co.za.gmapssolutions.beatraffic.mHandler;

public class DisplayForecast implements Runnable {
    private static final String TAG = DisplayForecast.class.getSimpleName();

    private Context context;
    private String forecast;
    public DisplayForecast(Context context, String forecast){
        this.context = context;
        this.forecast = forecast;
    }

    @Override
    public void run() {
        Log.i(TAG,"Forecasted: " +forecast);
        Toast.makeText(context,"Forecasted: " +forecast,Toast.LENGTH_LONG).show();
    }
}
