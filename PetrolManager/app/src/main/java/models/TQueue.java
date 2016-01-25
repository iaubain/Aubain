package models;

/**
 * Created by Owner on 12/22/2015.
 */
public class TQueue {

    private String tId;
    private String pMode;
    private String request;
    private String sum;
    private String qTime;
    private String qId;

    public String gettId() {
        return tId;
    }

    public void settId(String tId) {
        this.tId = tId;
    }

    public String getpMode() {
        return pMode;
    }

    public void setpMode(String pMode) {
        this.pMode = pMode;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }

    public String getqTime() {
        return qTime;
    }

    public void setqTime(String qTime) {
        this.qTime = qTime;
    }

    public String getqId() {
        return qId;
    }

    public void setqId(String qId) {
        this.qId = qId;
    }
}
