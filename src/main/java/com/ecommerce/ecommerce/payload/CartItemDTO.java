package com.ecommerce.ecommerce.payload;


import com.ecommerce.ecommerce.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long productId;
    private Integer quantity;
}
