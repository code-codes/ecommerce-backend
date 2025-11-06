package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.payload.CartDTO;

public interface CartService {
    CartDTO addProductToCart(Long productId, Integer quantity);


}
