package lk.javainstitute.techstore.model;

public class User {
    private String id;
    private String image;
    private String name;
    private String address;
    private String email;
    private String mobile;
    private String password;

    public User() {
    }

    public User(String id, String image, String name, String address, String email, String mobile, String password) {
        this.id = id;
        this.image = image;
        this.name = name;
        this.address = address;
        this.email = email;
        this.mobile = mobile;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
