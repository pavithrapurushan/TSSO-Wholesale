package in.co.tsmith.wholesale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;

//Modified by Pavithra on 11-11-2020

public class SOActivityAdapter extends ArrayAdapter<String> {

//    List<SalesBillDetailPL> lstSalesBillDetailPL;
    List<SODetailPL> lstSODetailPL;
    Context cntext;
    String[] values;

    SharedPreferences prefs;
    TextView tvTotAmount;
    Dialog dialog;

    double remarksDialogWindowHeight;
    double qtyDialogwindowHeight;
    Dialog qtydialog;

    String item_name = "";
    String mrp = "";
    String soh = "";
    int selected_item_id = 0;

    String CurrentQty = "";

    boolean isSaved = false;
    int itemQty = 0;
    int billedQty = 0;

    int currentPos = 0;

    String strGetFreeStock = "";
    EditText etQty;
    ProgressDialog pDialog;

    public SOActivityAdapter(Context context, String[] v1, List<SODetailPL> sODetailPLList,TextView tvBillTotal) {

        super(context, R.layout.activity_so, v1);
        cntext = context;
        values = v1;
        lstSODetailPL = sODetailPLList;
        tvTotAmount = tvBillTotal;

        prefs = PreferenceManager.getDefaultSharedPreferences(cntext);

//        DisplayMetrics displayMetrics = new DisplayMetrics();
        DisplayMetrics displayMetrics = cntext.getResources().getDisplayMetrics();
        int screen_height = displayMetrics.heightPixels;

        remarksDialogWindowHeight = (screen_height * 38) / 100;  //  243/640
//        qtyDialogwindowHeight = (screen_height * 60) / 100;
        qtyDialogwindowHeight = (screen_height * 75) / 100;

    }

    @Override

    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cntext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item_lv_salesorder, parent, false);

        TextView tvSlNo = (TextView) rowView.findViewById(R.id.tvSlNo);
        TextView tvItem = (TextView) rowView.findViewById(R.id.tvProductName);
        TextView tvMrp = (TextView) rowView.findViewById(R.id.tvMRP);
        TextView tvQty = (TextView) rowView.findViewById(R.id.tvQty); //Commented by Pavithra on 03-09-2020
//        EditText tvQty = (EditText) rowView.findViewById(R.id.tvQty);   //added by Pavithra on 03-09-2020
        TextView tvFreeQty = (TextView) rowView.findViewById(R.id.tvFreeQty);
        TextView tvTotal = (TextView) rowView.findViewById(R.id.tvTotal);
        final ImageButton imgBtnRemarksItem = (ImageButton) rowView.findViewById(R.id.imgBtnRemarksItem);
        ImageButton btnDelete = (ImageButton) rowView.findViewById(R.id.btnDeleteItem);

        SODetailPL itemDetailsPLObj = lstSODetailPL.get(position);
        tvSlNo.setText(String.valueOf(position + 1));
        tvItem.setText(itemDetailsPLObj.Name);
//        tvMrp.setText(String.valueOf(itemDetailsPLObj.Mrp));
        tvMrp.setText(String.format("%.2f",+itemDetailsPLObj.Mrp));
        tvMrp.setGravity(Gravity.LEFT);

        tvQty.setText(""+itemDetailsPLObj.Qty);
        tvFreeQty.setText(""+itemDetailsPLObj.FreeQty);
//        tvTotal.setText(""+itemDetailsPLObj.LineAmt);
        tvTotal.setText(String.format("%.2f",+itemDetailsPLObj.LineAmt));
//        tvApproxAmount.setText(String.format("%.2f",+obj.AppAmt));

        isSaved = prefs.getBoolean("IsSavedTssoWS",false);
        if(isSaved){
            imgBtnRemarksItem.setEnabled(false);
            btnDelete.setEnabled(false);
            tvQty.setEnabled(false);
        }else {
            imgBtnRemarksItem.setEnabled(true);
            btnDelete.setEnabled(true);
            tvQty.setEnabled(true);
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeletePopUP(position);
            }
        });

        imgBtnRemarksItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    dialog = new Dialog(cntext);
                    dialog.setContentView(R.layout.add_remarks);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.setTitle("Remarks");
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

                    ImageButton imgBtnCloseRemarksWindow = (ImageButton) dialog.findViewById(R.id.imgBtnCloseRemarksWindow);
                    Button btnOkRemarks_Itemwise = (Button) dialog.findViewById(R.id.btnOkRemarks_Itemwise);
                    Button btnClearRemarks_Itemwise = (Button) dialog.findViewById(R.id.btnClearRemarks_Itemwise);
                    final EditText etAddRemarks_Itemwise = (EditText) dialog.findViewById(R.id.etAddRemarks_Itemwise);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(dialog.getWindow().getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = (int) remarksDialogWindowHeight;
                    lp.gravity = Gravity.CENTER;
                    dialog.getWindow().setAttributes(lp);

//                    etAddRemarks_Itemwise.setText(lstSalesBillDetailPL.get(position).Remarks);

                    imgBtnCloseRemarksWindow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    btnOkRemarks_Itemwise.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

//                            listItemDetails.get(position).Remarks = etAddRemarks_Itemwise.getText().toString();
//
//                            Gson gson = new Gson();
//                            String ListOfItemsAddedStr = gson.toJson(listItemDetails);
//                            prefs = PreferenceManager.getDefaultSharedPreferences(cntext);
//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putString("ListOfItemsAddedAndroidSO", ListOfItemsAddedStr);
//                            editor.commit();

//                            imgBtnRemarksItem.setColorFilter(cntext.getResources().getColor(android.R.color.holo_red_dark), PorterDuff.Mode.SRC_ATOP);


                            // locally store remarks
                            if(etAddRemarks_Itemwise.getText().toString().equalsIgnoreCase("")){
                                imgBtnRemarksItem.setImageResource(R.drawable.ic_remarks_item);
                            }else {
                                imgBtnRemarksItem.setImageResource(R.drawable.ic_remarks_colrchanged);
                            }

                            dialog.dismiss();

                        }
                    });
                    btnClearRemarks_Itemwise.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            etAddRemarks_Itemwise.setText("");

                        }
                    });

                    dialog.show();
                }catch (Exception ex){
                    Toast.makeText(cntext, ""+ex, Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                qtydialog = new Dialog(cntext);
                qtydialog.setContentView(R.layout.quantity_selection_dialogwindow);
                qtydialog.setCanceledOnTouchOutside(false);
                qtydialog.setTitle("Quantity Selection");
                qtydialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


//                tvSelectedItemName = (TextView) qtydialog.findViewById(R.id.tvSelectedItemName); //setting product name
//                tvMrpInQtySelection = (TextView) qtydialog.findViewById(R.id.tvMrpInQtySelection);
//                tvUperpackInQtySelection = (TextView) qtydialog.findViewById(R.id.tvUperpackInQtySelection);
//
//                tvSelectedItemName.setText("" + itemname);
//                tvMrpInQtySelection.setText("MRP: " + mrp);
//                tvUperpackInQtySelection.setText("UPerPack: " + uperpack);

                ImageButton cancel = (ImageButton) qtydialog.findViewById(R.id.imgBtnCloseQtySelection);

                TextView tvSelectedItemName = (TextView) qtydialog.findViewById(R.id.tvSelectedItemName);
                TextView tvMrp = (TextView) qtydialog.findViewById(R.id.tvMrpInQtySelection);
                TextView tvSOH = (TextView) qtydialog.findViewById(R.id.tvSOHInQtySelection);
                TextView tvOfferDesc = (TextView) qtydialog.findViewById(R.id.tvOfferDesc);   //added by Pavithra on 11-11-2020
                ImageButton btnPlus = (ImageButton) qtydialog.findViewById(R.id.imgBtnPlusPack);
                ImageButton btnMinus = (ImageButton) qtydialog.findViewById(R.id.imgBtnMinusPack);
                Button btnAdd = (Button) qtydialog.findViewById(R.id.btnAddItem_qtySelection);
//                final EditText etQty = (EditText) qtydialog.findViewById(R.id.etQty);
                etQty = (EditText) qtydialog.findViewById(R.id.etQty);

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(qtydialog.getWindow().getAttributes());
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//                lp.height = (int)qtyDialogwindowHeight;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                lp.gravity = Gravity.CENTER;
                qtydialog.getWindow().setAttributes(lp);

                item_name = lstSODetailPL.get(position).Name;
                mrp = String.valueOf(lstSODetailPL.get(position).Mrp);
                soh = String.valueOf(lstSODetailPL.get(position).SOH);
                selected_item_id = lstSODetailPL.get(position).ItemId;
                CurrentQty = String.valueOf(lstSODetailPL.get(position).Qty);


//                tvSelectedItemName.setText("" + item_name);
                tvSelectedItemName.setText("" + item_name);
                tvMrp.setText("MRP : " + mrp);
                tvSOH.setText("SOH : " + soh);
                tvOfferDesc.setText(lstSODetailPL.get(position).OfferDesc); //added by Pavithra on 11-11-2020
                etQty.setText(""+CurrentQty);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        qtydialog.dismiss();
                    }
                });


                btnPlus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        try {
                            if (etQty.getText().toString().equals("") || etQty.getText().toString() == null) {
                                etQty.setText("0");
                            }

                            itemQty = Integer.parseInt(etQty.getText().toString());
                            itemQty += 1;
                            etQty.setText("" + itemQty);

                        } catch (Exception ex){
                            Toast.makeText(cntext, ""+ex, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (!etQty.getText().toString().equals("") && etQty.getText().toString() != null) {

                                itemQty = Integer.parseInt(etQty.getText().toString());
                                if (itemQty >= 1)
                                    itemQty -= 1;

                                etQty.setText("" +  itemQty);

                            } else {
                                Toast.makeText(cntext, "Qty field is empty", Toast.LENGTH_SHORT).show();
                            }
                        }catch (Exception ex){
                            Toast.makeText(cntext, ""+ex, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        try {

                            if (Integer.parseInt(etQty.getText().toString()) < 1) {
                                Toast.makeText(cntext, "Qty cannot be less than 1", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            for (int i = 0; i < lstSODetailPL.size(); i++) {
                                if (selected_item_id == lstSODetailPL.get(i).ItemId) {
                                    if (lstSODetailPL.get(i).SchemeExists == 1) {

                                        currentPos = i;
                                        billedQty = Integer.parseInt(etQty.getText().toString());
                                        new GetFreeStockTask().execute();
                                        return;

                                    }
                                }
                            }

                            for (int i = 0; i < lstSODetailPL.size(); i++) {
                                if (selected_item_id == lstSODetailPL.get(i).ItemId) {

                                    lstSODetailPL.get(i).ItemId = selected_item_id;
//                                lstSODetailPL.get(i).Qty = Double.parseDouble(etQty.getText().toString());    //Added by Pavithra on 11-09-2020
                                    lstSODetailPL.get(i).Qty = Integer.parseInt(etQty.getText().toString());    //Added by Pavithra on 11-09-2020

                                    Double qty = Double.valueOf(lstSODetailPL.get(i).Qty);
                                    Double mrp = Double.valueOf(lstSODetailPL.get(i).Mrp);

                                    Double totAmount = qty * mrp;
                                    lstSODetailPL.get(i).LineAmt = totAmount;

                                    //Calculations

                                    lstSODetailPL.get(i).Amount = lstSODetailPL.get(i).Qty * lstSODetailPL.get(i).Rate;

                                    lstSODetailPL.get(i).Discount = lstSODetailPL.get(i).Amount * lstSODetailPL.get(i).DiscountPer / 100;

                                    double taxableAmount = lstSODetailPL.get(i).Amount - lstSODetailPL.get(i).Discount;
                                    double taxAmount = taxableAmount * lstSODetailPL.get(i).TaxPer / 100;
                                    lstSODetailPL.get(i).LineAmt = taxableAmount + taxAmount;


//                                //Calculations
//
//                                soplObj.items.get(0).Amount = soplObj.items.get(0).Qty *soplObj.items.get(0).Rate;
//
//                                soplObj.items.get(0).Discount =  soplObj.items.get(0).Amount* soplObj.items.get(0).DiscountPer/100;
//
//                                double taxableAmount = soplObj.items.get(0).Amount - soplObj.items.get(0).Discount;
//                                double taxAmount  = taxableAmount * soplObj.items.get(0).TaxPer/100;
//                                soplObj.items.get(0).LineAmt = taxableAmount+taxAmount;


                                }
                            }

                            Double totamnt = 0d;

                            String[] arr = new String[lstSODetailPL.size()];
                            for (int j = 0; j < lstSODetailPL.size(); j++) {
                                arr[j] = lstSODetailPL.get(j).Name;
//                                if (j != 0)
                                totamnt = totamnt + Double.valueOf(lstSODetailPL.get(j).LineAmt);
                            }


                            SOActivityAdapter productListActivityAdapter = new SOActivityAdapter(cntext, arr, lstSODetailPL, ((SOActivity) cntext).tvBilltotal);
                            ((SOActivity) cntext).lvProductlist.setAdapter(productListActivityAdapter);
//                                ((SOActivity) cntext).tvTotalAmountValue.setText("Total Amount : " + String.format("%.2f", totamnt));
                            ((SOActivity) cntext).tvBilltotal.setText("₹ " + String.format("%.2f", totamnt));
                            ((SOActivity) cntext).tvBilltotal.setVisibility(View.VISIBLE);


                            qtydialog.dismiss();
                            ((SOActivity) cntext).acvItemSearchSOActivity.setText("");
//                        acvItemSearchSOActivity.setText(""); //Added by Pavithra on 10-10-2020

                        }catch(Exception ex){
                            Toast.makeText(cntext, ""+ex, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                qtydialog.show();

            }
        });

        return rowView;
    }

    @Override
    public int getCount() {
        return lstSODetailPL.size();
    }

    public void showDeletePopUP(final int position) {

        AlertDialog.Builder b = new AlertDialog.Builder(cntext);
        b.setTitle("Confirm Delete");
        b.setMessage("Are you sure to delete " + lstSODetailPL.get(position).Name);  //item name should specify

        b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteItem(position);
                Toast.makeText(cntext, "Item Deleted successfully", Toast.LENGTH_LONG).show();
            }
        });

        b.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        b.show();
    }

    public void deleteItem(int position) {
        try {
            this.lstSODetailPL.remove(position);
            this.notifyDataSetChanged();

            Double totamnt = 0d;
            if (lstSODetailPL.size() > 0) {

                for (int j = 0; j < lstSODetailPL.size(); j++) {
                    totamnt = totamnt + Double.valueOf(lstSODetailPL.get(j).LineAmt);
                }

                tvTotAmount.setText("" + String.format("%.2f", totamnt));
                tvTotAmount.setVisibility(View.VISIBLE);

            } else {
                tvTotAmount.setVisibility(View.GONE);
//            this.listItemDetails.remove(0);
                this.notifyDataSetChanged();
            }

            //Following added by Pavithra on 26-08-2020
            Gson gson = new Gson();
            String ListOfItemsAddedStr = gson.toJson(lstSODetailPL);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("ListOfItemsAddedMiniSO", ListOfItemsAddedStr);
            editor.commit();
        } catch (Exception ex) {
            Toast.makeText(cntext, "" + ex, Toast.LENGTH_SHORT).show();
        }

    }

    private class GetFreeStockTask extends AsyncTask<String,String,String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(cntext);
            pDialog.setMessage("Checking free stock...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            getFreeStock();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }

            if(strGetFreeStock == null || strGetFreeStock.equals("")){
                Toast.makeText(cntext, "No result from web", Toast.LENGTH_SHORT).show();
            }else{
                Gson gson = new Gson();
                SODetailPL soDetailPLObj = new SODetailPL();
                soDetailPLObj = gson.fromJson(strGetFreeStock, SODetailPL.class);

                if(soDetailPLObj.ErrorStatus ==0){

//                    lstSODetailPL.get(currentPos).Qty = Double.parseDouble(etQty.getText().toString());
                    lstSODetailPL.get(currentPos).Qty = Integer.parseInt(etQty.getText().toString());

                    Double totAmount = lstSODetailPL.get(currentPos).Qty * Double.valueOf(lstSODetailPL.get(currentPos).Mrp);
                    lstSODetailPL.get(currentPos).LineAmt = totAmount;
                    lstSODetailPL.get(currentPos).FreeQty = soDetailPLObj.FreeQty;

                    //Calculations

                    lstSODetailPL.get(currentPos).Amount = lstSODetailPL.get(currentPos).Qty *lstSODetailPL.get(currentPos).Rate;

                    lstSODetailPL.get(currentPos).Discount = lstSODetailPL.get(currentPos).Amount* lstSODetailPL.get(currentPos).DiscountPer/100;

                    double taxableAmount = lstSODetailPL.get(currentPos).Amount -lstSODetailPL.get(currentPos).Discount;
                    double taxAmount  = taxableAmount *lstSODetailPL.get(currentPos).TaxPer/100;
                    lstSODetailPL.get(currentPos).LineAmt = taxableAmount+taxAmount;



                    Double totamnt = 0d;

                    String[] arr = new String[lstSODetailPL.size()];
                    for (int j = 0; j < lstSODetailPL.size(); j++) {
                        arr[j] = lstSODetailPL.get(j).Name;
//                                if (j != 0)
                        totamnt = totamnt + Double.valueOf(lstSODetailPL.get(j).LineAmt);
                    }

                    SOActivityAdapter productListActivityAdapter = new SOActivityAdapter(cntext, arr, lstSODetailPL, ((SOActivity) cntext).tvBilltotal);
                    ((SOActivity) cntext).lvProductlist.setAdapter(productListActivityAdapter);
//                    ((SOActivity) cntext).tvBilltotal.setText("" + String.format("%.2f", totamnt));
                    ((SOActivity) cntext).tvBilltotal.setText("₹ " + String.format("%.2f", totamnt));
                    ((SOActivity) cntext).tvBilltotal.setVisibility(View.VISIBLE);

                    if(qtydialog.isShowing()) {
                        qtydialog.dismiss();
                    }

                }else{
//                    tsMessages(""+soDetailPLObj.Message);
                    Toast.makeText(cntext, ""+soDetailPLObj.Message, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getFreeStock(){

        prefs = PreferenceManager.getDefaultSharedPreferences(cntext);


        int CustomerId = 0;
        Gson gson = new Gson();

        String UserPLObjStr = prefs.getString("LoginResultStr", "");
        UserPL userPLObj = gson.fromJson(UserPLObjStr,UserPL.class); //do null checking

        if(userPLObj.UserType.equals("CR CUSTOMER")){

            CustomerId = userPLObj.AcId;

        } else {
            CustomerId = prefs.getInt("CustomerId",0);
        }


//        int cust_id = prefs.getInt("CustomerId", 0);// commented by Pavithra on 12-11-2020
        int store_id = prefs.getInt("StoreId", 0);
        String URL = prefs.getString("MiniSOURL", "");
        if(URL.equals("")) {  //Added by Pavithra on 10-11-2020
            URL = AppConfigSettings.WsUrl;
        }


//        input : (String AuthKey, int ItemId, int CustomerId, int StoreId, int BilledQty)

        try {
            String MethodName = "GetFreeStock";
            SoapObject request = new SoapObject(AppConfigSettings.WSNAMESPACE, MethodName);
            request.addProperty("AuthKey", "");
            request.addProperty("ItemId",lstSODetailPL.get(currentPos).ItemId );
//            request.addProperty("CustomerId", cust_id); //Commenetd by Pavithra on 12-11-2020
            request.addProperty("CustomerId", CustomerId); //Added by Pavithra on 12-11-2020
            request.addProperty("StoreId", store_id);
            request.addProperty("BilledQty", billedQty);

            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

//            String URL = "http://tsmithy.in/tssowholesale/webservice.asmx";

//            HttpTransportSE androidHttpTransport = new HttpTransportSE(AppConfigSettings.WsUrl, AppConfigSettings.WSTimeOutValueMedium);
            HttpTransportSE androidHttpTransport = new HttpTransportSE(URL, AppConfigSettings.WSTimeOutValueMedium);
            androidHttpTransport.call(AppConfigSettings.WSNAMESPACE + "/" + MethodName, envelope);

            Object result = envelope.getResponse();
            String str = result.toString();
            strGetFreeStock = str;


//            {"Id":0,"ItemId":0,"Name":null,"Qty":1.0,"QtyType":0.0,"UperPack":0,"Mrp":0.0,"ItemCode":null,"SOH":0.0,"UOM":null,"Rate":0.0,"FreeQty":0.0,"Tax":0.0,"Discount":0.0,"LineAmt":0.0,"TaxPer":0.0,"DiscountPer":0.0,"Amount":0.0,"OfferDesc":null,"SchemeExists":0,"BuyQtyInPacks":0.0,"FreeQtyInPacks":0.0,"ErrorStatus":0,"Message":null}
        } catch (Exception ex) {
            Log.d("CI", "" + ex);
        }
    }
}
