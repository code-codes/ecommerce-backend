package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.payload.CartDTO;
import com.ecommerce.ecommerce.payload.CartItemDTO;
import jakarta.transaction.Transactional;

import java.util.List;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);


    List<CartDTO> getCarts();

    CartDTO getCart(String emailId, Long cartId);

    @Transactional
    CartDTO updateProductQuantityInCart(Long productId, int quantity);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);

    String createOrUpdateCartWithItems(List<CartItemDTO> cartItems);
}
