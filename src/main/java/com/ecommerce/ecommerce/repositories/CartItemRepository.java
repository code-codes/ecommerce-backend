package com.ecommerce.ecommerce.repositories;

import com.ecommerce.ecommerce.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    @Query("SELECT ci FROM CartItem ci WHERE ci.product.id = ?1 AND ci.cart.id = ?2")
    CartItem findCartItemByProductIdAndCartId(Long productId, Long cartId);
}
