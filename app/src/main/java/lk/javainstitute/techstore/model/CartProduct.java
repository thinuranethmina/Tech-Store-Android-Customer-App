package lk.javainstitute.techstore.model;

public class CartProduct {
    private String id;
    private String title;
    private String image1;
    private String qty;
    private String price;

    public CartProduct(String id, String title, String image1, String qty, String price) {
        this.id = id;
        this.title = title;
        this.image1 = image1;
        this.qty = qty;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
