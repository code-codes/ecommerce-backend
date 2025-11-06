package com.ecommerce.ecommerce.payload;


import com.ecommerce.ecommerce.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartItemId;
    private ProductDTO product;
    private Integer quantity;
    private double discount;
    private double productPrice;
    private CartDTO cart;
}
