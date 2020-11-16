package in.co.tsmith.wholesale;

//Created by Pavithra on 13-10-2020

public class UserPL  extends CommonPL{

    public UserPL() {
        UserType = "";
        Username = "";
        DisplayName = "";
        StoreId = 7;
    }

    public int Id ;
    public int UserId ;
    public String UserType ;
    public String Username ;
    public String DisplayName;
    public String Password ;
    public String PasswordEncrypted ;
    public String OldPassword;

    public String PasswordChangeOTP ;
    public String Email ;

    public int Active ;
    public int StoreId ;

    public String Mobile ;

    public String Address ;
    public String GSTIN ;

    public String Name ;
    public int AcId ;

    public String UserEmail;
    public int UserDocTypeId ;

    public String Code ;

    public int EmpId ;

    public String Pincode ;
    public String State ;
    public String Area ;
}
