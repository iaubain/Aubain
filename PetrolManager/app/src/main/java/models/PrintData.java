package models;

/**
 * Created by Owner on 12/18/2015.
 */
public class PrintData {

    private String tId;
    private String bName;
    private String pName;
    private String amount;
    private String quantity;
    private String pMode;
    private String tel;
    private String time;
    private String pStatus;
    private String imei;

    public String gettId() {
        return tId;
    }

    public void settId(String tId) {
        this.tId = tId;
    }

    public String getbName() {
        return bName;
    }

    public void setbName(String bName) {
        this.bName = bName;
    }

    public String getpName() {
        return pName;
    }

    public void setpName(String pName) {
        this.pName = pName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getpMode() {
        return pMode;
    }

    public void setpMode(String pMode) {
        this.pMode = pMode;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getpStatus() {
        return pStatus;
    }

    public void setpStatus(String pStatus) {
        this.pStatus = pStatus;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

//    {
//        "validation": "valid",
//            "traId": "4",
//            "pStatus": "success",
//            "pTime": "Fri Dec 18 15:01:55 CAT 2015",
//            "pName": "petrol",
//            "amount": "10000.0",
//            "quantity": "5",
//            "pMode": "cash",
//            "custTel": "768686",
//            "uId": "1",
//            "bId": "1",
//            "bName": "engen_kicukiro",
//            "imei": "2"
//    }
}
