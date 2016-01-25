package models;

import java.util.List;

/**
 * Created by Owner on 12/2/2015.
 */
public class Transaction {
    private String tid;
    private String tpid;
    private String tcustid;
    private String tuid;
    private String quantity;
    private String amount;
    private String tstatus;
    private String ttime;


    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTpid() {
        return tpid;
    }

    public void setTpid(String tpid) {
        this.tpid = tpid;
    }

    public String getTcustid() {
        return tcustid;
    }

    public void setTcustid(String tcustid) {
        this.tcustid = tcustid;
    }

    public String getTuid() {
        return tuid;
    }

    public void setTuid(String tuid) {
        this.tuid = tuid;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTstatus() {
        return tstatus;
    }

    public void setTstatus(String tstatus) {
        this.tstatus = tstatus;
    }

    public String getTtime() {
        return ttime;
    }

    public void setTtime(String ttime) {
        this.ttime = ttime;
    }
}
