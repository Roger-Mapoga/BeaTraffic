package co.za.gmapssolutions.beatraffic.popup;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

public class Popup extends AlertDialog.Builder implements DialogInterface.OnClickListener {
    private int response = 0;
    private Context context;
    public Popup(Context context,String message,String btnYesText,String btnNoText) {
        super(context);
        this.context = context;
        setMessage(message)
        .setTitle("Alert")
        .setPositiveButton(btnYesText, this)
        .setNegativeButton(btnNoText, this);
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {

        switch (i){
            case DialogInterface.BUTTON_POSITIVE:
                //Yes button clicked
               // Toast.makeText(context,"testing" + DialogInterface.BUTTON_POSITIVE +" : "+ i,Toast.LENGTH_LONG).show();
                response = DialogInterface.BUTTON_POSITIVE;
                break;

            case DialogInterface.BUTTON_NEGATIVE:
                //No button clicked
                //Toast.makeText(context,"testing" + DialogInterface.BUTTON_POSITIVE +" : "+ i,Toast.LENGTH_LONG).show();
                response = DialogInterface.BUTTON_NEGATIVE;
                break;
            default:

        }
    }


    public int getResponse() {
        return response;
    }
}
