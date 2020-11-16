package in.co.tsmith.wholesale;

import java.util.ArrayList;
import java.util.List;

public class SOPL extends CommonPL {

    public SOPL() {
        Source = "WEBSO";
        BillType = "W";
        TransactionFormType = "Local";
        TransactionFormId = 5;
        SaleType = "LOCAL";
        PartyName = "";
        Address = "";
        Phone = "";
        Email = "";
        RefNo = "";
        Remarks = "";
        items = new ArrayList<>();

    }

    public int Id;
    public String Source;
    public String PartyName;
    public int PartyAcId;
    public String Address;
    public String Phone;
    public String Email;
    public Double APPTotalMRP;
    public int StoreId;
    public int UserId;

    public int CustTypeId;

    public String RefNo;

    /// <summary>
    /// Expects W
    /// </summary>
    public String BillType;

    public String TransactionFormType;
    public int TransactionFormId;

    /// <summary>
    /// LOCAL ,INTERSTATE
    /// </summary>
    public String SaleType;
    public int UserDocTypeId;

    public List<SODetailPL> items;

    //Added By najeela on 13102020
    public String State;
    public String Pincode;
    public String Area;

    public int StateId;
    public String Remarks;

}
