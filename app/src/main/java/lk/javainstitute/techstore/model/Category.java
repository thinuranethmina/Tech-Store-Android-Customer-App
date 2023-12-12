package lk.javainstitute.techstore.model;

public class Category {
private String name;

    private String id;
    private String imageId;

    public Category() {
    }

    public Category(String id, String name, String imageId) {
        this.id = id;
        this.name = name;
        this.imageId = imageId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }
}
