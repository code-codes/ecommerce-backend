package com.ecommerce.ecommerce.service;

import com.ecommerce.ecommerce.exceptions.APIException;
import com.ecommerce.ecommerce.exceptions.ResourceNotFoundException;
import com.ecommerce.ecommerce.model.Cart;
import com.ecommerce.ecommerce.model.CartItem;
import com.ecommerce.ecommerce.model.Product;
import com.ecommerce.ecommerce.payload.CartDTO;
import com.ecommerce.ecommerce.payload.CartItemDTO;
import com.ecommerce.ecommerce.payload.ProductDTO;
import com.ecommerce.ecommerce.repositories.CartItemRepository;
import com.ecommerce.ecommerce.repositories.CartRepository;
import com.ecommerce.ecommerce.repositories.ProductRepository;
import com.ecommerce.ecommerce.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        //Find existing cart or create new cart if not exists
        Cart cart = createCart();
        //Find product by productId
        //If product not found, throw exception
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //Check if product is already in cart
        //If product is in cart, update quantity

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cart.getCartId());
        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        //If product is not in cart, add new cart item with product and quantity
        //check if quantity is available in stock

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }


        CartItem cartItemToAdd = new CartItem();
        cartItemToAdd.setCart(cart);
        cartItemToAdd.setProduct(product);
        cartItemToAdd.setQuantity(quantity);
        cartItemToAdd.setProductPrice(product.getSpecialPrice());
        cartItemToAdd.setDiscount(product.getDiscount()); // Assuming no discount for simplicity
        cartItemRepository.save(cartItemToAdd);

        cart.getCartItems().add(cartItemToAdd);
        //Calculate total price and discounts
        //Save cart
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + product.getSpecialPrice() * quantity);
        cartRepository.save(cart);

        //Convert cart entity to CartDTO
        //Return CartDTO
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInUserEmail());
        if(userCart != null) {
            return userCart;
        }
        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.getLoggedInUser());
        return cartRepository.save(cart);
    }

    @Override
    public List<CartDTO> getCarts() {
        List<Cart> carts = cartRepository.findAll();
        if(carts.isEmpty()) {
            throw new APIException("Cart is empty");
        }
        List<CartDTO> cartDTOS = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity());
                return productDTO;
            }).collect(Collectors.toList());

            cartDTO.setProducts(products);
            return cartDTO;
        }).collect(Collectors.toList());
        return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);

        if(cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        if(!cart.getUser().getEmail().equals(emailId)) {
            throw new APIException("You are not authorized to view this cart");
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
            ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
            productDTO.setQuantity(cartItem.getQuantity());
            return productDTO;
        }).collect(Collectors.toList());

        cartDTO.setProducts(products);
        return cartDTO;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantityInCart(Long productId, int quantity) {
        String emilId = authUtil.loggedInUserEmail();
        Cart usercart = cartRepository.findCartByEmail(emilId);
        Long cartId = usercart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if(product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if(cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " does not exist in the cart");
        }

        int newQuantity = cartItem.getQuantity() + quantity;
        if(newQuantity < 0) {
            throw new APIException("Quantity cannot be negative");
        }
        if(newQuantity == 0) {
            deleteProductFromCart(productId, cartId);
        } else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + product.getSpecialPrice() * quantity);
            cartRepository.save(cart);
        }

        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }
        CartDTO cartDTO = modelMapper.map(updatedItem, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });
        cartDTO.setProducts(productStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if(cartItem == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(productId, cartId);
        return "Product" + cartItem.getProduct().getProductName() + " removed from cart successfully.";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(productId, cartId);
        if(cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " does not exist in the cart");
        }
        double oldPrice = cartItem.getProductPrice() * cartItem.getQuantity();
        double newPrice = product.getSpecialPrice() * cartItem.getQuantity();
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cart.getTotalPrice() - oldPrice + newPrice);
        cartItem = cartItemRepository.save(cartItem);
    }

    @Transactional
    @Override
    public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {
        // Get user's email
        String emailId = authUtil.loggedInUserEmail();

        // Check if an existing cart is available or create a new one
        Cart existingCart = cartRepository.findCartByEmail(emailId);
        if (existingCart == null) {
            existingCart = new Cart();
            existingCart.setTotalPrice(0.00);
            existingCart.setUser(authUtil.getLoggedInUser());
            existingCart = cartRepository.save(existingCart);
        } else {
            // Clear all current items in the existing cart
            cartItemRepository.deleteAllByCartId(existingCart.getCartId());
        }

        double totalPrice = 0.00;

        // Process each item in the request to add to the cart
        for (CartItemDTO cartItemDTO : cartItems) {
            Long productId = cartItemDTO.getProductId();
            Integer quantity = cartItemDTO.getQuantity();

            // Find the product by ID
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

            // Directly update product stock and total price
            // product.setQuantity(product.getQuantity() - quantity);
            totalPrice += product.getSpecialPrice() * quantity;

            // Create and save cart item
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(existingCart);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItemRepository.save(cartItem);
        }

        // Update the cart's total price and save
        existingCart.setTotalPrice(totalPrice);
        cartRepository.save(existingCart);
        return "Cart created/updated with the new items successfully";
    }
}
