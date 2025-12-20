package com.ecommerce.ecommerce.controller;

import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.payload.CartDTO;
import com.ecommerce.ecommerce.payload.CartItemDTO;
import com.ecommerce.ecommerce.repositories.CartRepository;
import com.ecommerce.ecommerce.service.CartService;
import com.ecommerce.ecommerce.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {
    @Autowired
    CartService cartService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    CartRepository cartRepository;

    @PostMapping("/cart/create")
    public ResponseEntity<String> createOrUpdateCart(@RequestBody List<CartItemDTO> cartItems){
        String response = cartService.createOrUpdateCartWithItems(cartItems);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity){
        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);
        return new ResponseEntity<CartDTO>(cartDTO,HttpStatus.CREATED);
    }

    @GetMapping("/carts")
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOs = cartService.getCarts();
        return new ResponseEntity<List<CartDTO>>(cartDTOs, HttpStatus.FOUND);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getCartByUserId() {
        String emailId = authUtil.loggedInUserEmail();
        Cart cart = cartRepository.findCartByEmail(emailId);
        Long cartId = cart.getCartId();
        CartDTO cartDTO = cartService.getCart(emailId, cartId);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);
    }

    @PutMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId, @PathVariable String operation) {
        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete") ? -1 : 1);
        return new ResponseEntity<CartDTO>(cartDTO, HttpStatus.OK);

    }

    @DeleteMapping("/carts/{cartId}/products/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId,
                                                        @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId, productId);
        return new ResponseEntity<String>(status, HttpStatus.OK);
    }

}
