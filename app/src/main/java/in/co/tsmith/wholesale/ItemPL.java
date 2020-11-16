package in.co.tsmith.wholesale;

public class ItemPL{

    public ItemPL() {
        ItemName = ""; ItemCode = ""; OfferDesc = "";
    }

    public int ItemId;
    public String ItemName ;
    public String ItemCode;
    public Double SOH ;
    public Double Mrp ;

    public String OfferDesc;

    public int SchemeExists ;
    public Double BuyQtyInPacks ;
    public Double FreeQtyInPacks ;

}
