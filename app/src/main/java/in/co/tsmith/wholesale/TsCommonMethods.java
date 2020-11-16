package in.co.tsmith.wholesale;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class TsCommonMethods {

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;
    private static final int MY_PERMISSIONS_REQUEST_INTERNET = 4;
    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 6;
    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 8;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_STORAGE = 10;

    Context CurrentContext;
    TelephonyManager mTelephony;
    boolean AllPermissionFlag ;


    public TsCommonMethods(Context cntxt) {
        CurrentContext = cntxt;
        AllPermissionFlag = false;
    }

    public boolean isNetworkConnected() {

        try {
            ConnectivityManager cm = (ConnectivityManager) CurrentContext.getSystemService(CurrentContext.CONNECTIVITY_SERVICE);

            NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiNetwork != null && wifiNetwork.isConnected()) {
                return true;
            }

            NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mobileNetwork != null && mobileNetwork.isConnected()) {
                return true;
            }

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.isConnected()) {
                return true;
            }
        }catch (Exception ex) {
            Toast.makeText(CurrentContext, "" + ex, Toast.LENGTH_SHORT).show();
            return false;//Added by Pavithra on 11-09-2020
        }

        return false;
    }

    public void allowPermissionsDynamically(){
        if (ActivityCompat.checkSelfPermission(CurrentContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) CurrentContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_STORAGE);

        }
        if (ActivityCompat.checkSelfPermission(CurrentContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions((Activity) CurrentContext, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_STORAGE);

        }
    }
}
