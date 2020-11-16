package in.co.tsmith.wholesale;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class PayablesActivityAdapter extends ArrayAdapter<String> {

//    List<SODetailPL> lstSODetailPL;
    Context cntext;
    String[] values;
    List<PayablesPL> listPayablesPL;

    public PayablesActivityAdapter(Context context, String[] v1, List<PayablesPL> payablesPLList) {

        super(context, R.layout.activity_payables, v1);
        cntext = context;
        values = v1;
        listPayablesPL = payablesPLList;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) cntext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.item_lv_payables, parent, false);

        TextView tvSlNo = (TextView) rowView.findViewById(R.id.tvSlno);
        TextView tvDocNumber = (TextView) rowView.findViewById(R.id.tvDocNumber);
        TextView tvDocDate = (TextView) rowView.findViewById(R.id.tvDocDate);
        TextView tvOverdueDays = (TextView) rowView.findViewById(R.id.tvOverdueDays);
        TextView tvBillAmount = (TextView) rowView.findViewById(R.id.tvBillAmount);
        TextView tvBalance = (TextView) rowView.findViewById(R.id.tvBalance);


        PayablesPL payablesPLObj = listPayablesPL.get(position);
        tvSlNo.setText(String.valueOf(position + 1));
        tvDocNumber.setText(payablesPLObj.DocNo);
        tvDocDate.setText(payablesPLObj.DocDate);
        tvOverdueDays.setText("" + payablesPLObj.OverDueDays);
//        tvTotAmountPayables.setText("₹ "+String.format("%.2f",+totalAmount));
        tvBillAmount.setText(String.format("%.2f",+payablesPLObj.BillAmt));
        tvBalance.setText(String.format("%.2f",+payablesPLObj.BalanceAmt));
//        tvBalance.setText("" + payablesPLObj.BalanceAmt);
//        tvBillAmount.setText("" + payablesPLObj.BillAmt);
//        tvBalance.setText("" + payablesPLObj.BalanceAmt);
        return rowView;
    }


    @Override
    public int getCount() {
        return listPayablesPL.size();
    }
}
