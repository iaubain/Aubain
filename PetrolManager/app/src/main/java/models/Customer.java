package models;

/**
 * Created by Owner on 12/2/2015.
 */
public class Customer {

    private String custid;
    private String custtel;
    private String custtin;
    private String custname;
    private String time;

    public String getCustid() {
        return custid;
    }

    public void setCustid(String custid) {
        this.custid = custid;
    }

    public String getCusttel() {
        return custtel;
    }

    public void setCusttel(String custtel) {
        this.custtel = custtel;
    }

    public String getCusttin() {
        return custtin;
    }

    public void setCusttin(String custtin) {
        this.custtin = custtin;
    }

    public String getCustname() {
        return custname;
    }

    public void setCustname(String custname) {
        this.custname = custname;
    }


    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
