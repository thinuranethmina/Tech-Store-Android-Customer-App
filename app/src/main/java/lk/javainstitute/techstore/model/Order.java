package lk.javainstitute.techstore.model;

public class Order {

    private String id;
    private String email;
    private String total;
    private String mobile;
    private String address;
    private String latitude;
    private String longitude;
    private String date_time;
    private int deliver_status = 0;

    public Order() {
    }

    public Order(String id, String email, String total, String mobile, String address, String latitude, String longitude, String date_time, int deliver_status) {
        this.id = id;
        this.email = email;
        this.total = total;
        this.mobile = mobile;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date_time = date_time;
        this.deliver_status = deliver_status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDate_time() {
        return date_time;
    }

    public void setDate_time(String date_time) {
        this.date_time = date_time;
    }

    public int getDeliver_status() {
        return deliver_status;
    }

    public void setDeliver_status(int deliver_status) {
        this.deliver_status = deliver_status;
    }
}
