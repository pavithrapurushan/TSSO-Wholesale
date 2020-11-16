package in.co.tsmith.wholesale;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

//Created by Pavithra on 21-10-2020

public class PayablesActivity extends AppCompatActivity {

    String URL = "";
    String strGetPayables = "";
    SharedPreferences prefs;
    int customer_id = 0;
    int store_id = 0;

    LinearLayout llBottomBar;
    double llCustInfoToolbarHeight;
    double llBottomHeight;
    LinearLayout llSOToolbar;
    TextView tvCustomerName;
    ListView lvPayables;

    TextView tvTotAmountPayables;
    TextView tvBalancePayables;

    ImageButton imgBtnBackPayables;

    double tsMsgDialogWindowHeight;

    ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payables);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_height = displayMetrics.heightPixels;
        int screen_width = displayMetrics.widthPixels;

        tsMsgDialogWindowHeight = (screen_height * 38) / 100;

        llCustInfoToolbarHeight = (screen_height * 8.75) / 100;  //    56/640

        llBottomHeight = (screen_height * 16) / 100;  //    56/640
        llSOToolbar = (LinearLayout) findViewById(R.id.llSOToolbar);
        llBottomBar = (LinearLayout) findViewById(R.id.llbottom);
        tvCustomerName = (TextView) findViewById(R.id.tvCustomerNamePayables);
        tvTotAmountPayables = (TextView) findViewById(R.id.tvTotAmountPayables);
        tvBalancePayables = (TextView) findViewById(R.id.tvBalancePayables);
        lvPayables = (ListView) findViewById(R.id.lvPayablesList);

        LinearLayout.LayoutParams paramsllHeader = (LinearLayout.LayoutParams) llSOToolbar.getLayoutParams();
        paramsllHeader.height = (int) llCustInfoToolbarHeight;
        paramsllHeader.width = LinearLayout.LayoutParams.MATCH_PARENT;
        llSOToolbar.setLayoutParams(paramsllHeader);

        LinearLayout.LayoutParams paramsllHeader1 = (LinearLayout.LayoutParams) llBottomBar.getLayoutParams();
        paramsllHeader1.height = (int) llBottomHeight;
        paramsllHeader1.width = LinearLayout.LayoutParams.MATCH_PARENT;
        llBottomBar.setLayoutParams(paramsllHeader1);

        imgBtnBackPayables = (ImageButton) findViewById(R.id.imgBtnBackPayables);


        imgBtnBackPayables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intnt = new Intent(PayablesActivity.this,CustomerInfoActivity.class);
                startActivity(intnt);
                PayablesActivity.this.finish();
            }
        });


        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String customerName = prefs.getString("CustomerNameWS","");
        URL = prefs.getString("MiniSOURL", "");

        if(URL.equals("")) {  //Added by Pavithra on 10-11-2020
            URL = AppConfigSettings.WsUrl;
        }

        tvCustomerName.setText(""+customerName);


        new GetPayablesTask().execute();
    }

    private class GetPayablesTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(PayablesActivity.this);
//            pDialog.setMessage("Loading payables...");
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            getPayables();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if(strGetPayables == null || strGetPayables.equals("")){
                Toast.makeText(PayablesActivity.this, "No result from web", Toast.LENGTH_SHORT).show();
            }else{
                ListPayablesPL listPayablesPL = new ListPayablesPL();

                Gson gson = new Gson();
                listPayablesPL = gson.fromJson(strGetPayables, ListPayablesPL.class);

                Double totalAmount = 0d;
                Double balanceAmount = 0d;
                if(listPayablesPL.ErrorStatus == 0){
                    String[] arrslno = new String[listPayablesPL.list.size()];
                    for(int i = 0; i < listPayablesPL.list.size(); i++){
                        arrslno[i] = listPayablesPL.list.get(i).Name;
                        totalAmount = totalAmount + listPayablesPL.list.get(i).BillAmt;
                        balanceAmount = balanceAmount + listPayablesPL.list.get(i).BalanceAmt;
                    }

                    PayablesActivityAdapter OBJArrayAdSO = new PayablesActivityAdapter(PayablesActivity.this, arrslno,  listPayablesPL.list);
                    lvPayables.setAdapter(OBJArrayAdSO);

//                    tvTotAmt.setText("Total Amount: ₹ "+String.format("%.2f",+TotAmt));
                    tvTotAmountPayables.setText("₹ "+String.format("%.2f",+totalAmount));
                    tvBalancePayables.setText("₹ "+String.format("%.2f",+balanceAmount));
//                    tvTotAmountPayables.setText(""+totalAmount);
//                    tvBalancePayables.setText(""+balanceAmount);


                } else{
                    tsMessages(""+listPayablesPL.Message);
                }
            }
        }
    }

    private void getPayables(){


        Gson gson = new Gson();

        String UserPLObjStr = prefs.getString("LoginResultStr", "");
        UserPL userPLObj = gson.fromJson(UserPLObjStr,UserPL.class); //do null checking

        if(userPLObj.UserType.equals("CR CUSTOMER")){

            customer_id = userPLObj.AcId;

        }else{
            customer_id = prefs.getInt("CustomerId",0);
        }

        store_id = prefs.getInt("StoreId",0);

        try {
            String MethodName = "GetPayables";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("StoreId", store_id);
            request.addProperty("AccId", customer_id);
//            request.addProperty("UserType", "CR CUSTOMER");
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetPayables = str;

        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }

    }

    private void tsMessages(String msg) {

        try {
            final Dialog dialog = new Dialog(PayablesActivity.this);
            dialog.setContentView(R.layout.tsmessage_dialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setTitle("Save");
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            ImageButton imgBtnCloseSaveWindow = (ImageButton) dialog.findViewById(R.id.imgBtnClosetsMsgWindow);
            TextView tvSaveStatus = (TextView) dialog.findViewById(R.id.tvTsMessageDisplay);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = (int) tsMsgDialogWindowHeight;
            lp.gravity = Gravity.CENTER;
            dialog.getWindow().setAttributes(lp);

            imgBtnCloseSaveWindow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
//                    if (dialogItemLookup != null)
//                        dialogItemLookup.dismiss();
                }
            });
            tvSaveStatus.setText("" + msg);
            dialog.show();
        } catch (Exception ex) {
            Toast.makeText(this, "" + ex, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {

        Intent intnt =  new Intent(PayablesActivity.this,CustomerInfoActivity.class);
        startActivity(intnt);
        PayablesActivity.this.finish();

    }


}
