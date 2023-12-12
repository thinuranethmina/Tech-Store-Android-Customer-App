package lk.javainstitute.techstore.model;

import androidx.annotation.Nullable;

public class Product {
    private String id;
    private String title;
    private String category;
    private String price;
    private String qty;
    private String description;
    private String image1;
    private String image2;
    private String image3;
    private String status;

    public Product() {
    }

    public Product(String id, String title, String category, String price, String qty, String description, String image1,@Nullable String image2,@Nullable String image3, String status) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.price = price;
        this.qty = qty;
        this.description = description;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.status = status;
    }

    public Product(String id, String title, String image1, String qty, String price) {
        this.id = id;
        this.title = title;
        this.price = price;
        this.qty = qty;
        this.image1 = image1;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getImage2() {
        return image2;
    }

    public void setImage2(String image2) {
        this.image2 = image2;
    }

    public String getImage3() {
        return image3;
    }

    public void setImage3(String image3) {
        this.image3 = image3;
    }
}
