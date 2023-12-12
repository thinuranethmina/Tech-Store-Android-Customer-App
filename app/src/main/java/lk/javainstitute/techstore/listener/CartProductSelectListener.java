package lk.javainstitute.techstore.listener;

import lk.javainstitute.techstore.model.CartProduct;

public interface CartProductSelectListener {
    void viewProduct(CartProduct product);
    void productAddQty(CartProduct product);
    void productRemoveQty(CartProduct product);
    void removeProduct(CartProduct product);
}
