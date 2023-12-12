package lk.javainstitute.techstore.utill;

import java.text.DecimalFormat;

public class Format {
    private Double price;

    // Constructor to initialize the productName and price
    public Format(String price) {
        this.price = Double.valueOf(price);
    }
    public String formatPrice() {
        return String.valueOf(new DecimalFormat("#,##0.00").format(price));
    }

}
