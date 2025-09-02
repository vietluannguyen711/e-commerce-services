package com.example.e_commerce_services.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_services.domain.Cart;
import com.example.e_commerce_services.domain.CartItem;
import com.example.e_commerce_services.domain.Product;
import com.example.e_commerce_services.domain.ProductImage;
import com.example.e_commerce_services.domain.User;
import com.example.e_commerce_services.domain.Variant;
import com.example.e_commerce_services.dto.CartDto;
import com.example.e_commerce_services.dto.CartItemDto;
import com.example.e_commerce_services.dto.MergeCartRequest;
import com.example.e_commerce_services.exception.NotFoundException;
import com.example.e_commerce_services.exception.OutOfStockException;
import com.example.e_commerce_services.exception.SkuNotFoundException;
import com.example.e_commerce_services.repository.CartItemRepository;
import com.example.e_commerce_services.repository.CartRepository;
import com.example.e_commerce_services.repository.ProductRepository;
import com.example.e_commerce_services.repository.VariantRepository;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final VariantRepository variantRepo;
    private final ProductRepository productRepo;

    public CartService(CartRepository cartRepo, CartItemRepository cartItemRepo,
            VariantRepository variantRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.variantRepo = variantRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public CartDto addItem(Long userId, String sku, int qty) {
        Variant v = variantRepo.findBySku(sku)
                .orElseThrow(() -> new SkuNotFoundException("SKU không tồn tại: " + sku));
        if (qty > v.getStock()) {
            throw new OutOfStockException(v.getSku(), v.getStock(), "Hết hàng cho SKU " + v.getSku());
        }

        Cart cart = getOrCreateCart(userId);

        // Nếu đã có dòng này, cộng dồn qty
        Optional<CartItem> existing = cartItemRepo.findByCart_IdAndVariant_Id(cart.getId(), v.getId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQty() + qty;
            if (newQty > v.getStock()) {
                throw new OutOfStockException(v.getSku(), v.getStock(), "Hết hàng cho SKU " + v.getSku());
            }
            item.setQty(newQty);
            item.setPriceSnapshot(v.getPrice());
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setVariant(v);
            item.setQty(qty);
            item.setPriceSnapshot(v.getPrice());
            cart.getItems().add(item);
        }
        return toDto(cart);
    }

    @Transactional
    public CartDto updateQty(Long userId, Long itemId, int qty) {
        Cart cart = cartRepo.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user " + userId));

        CartItem item = cartItemRepo.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + itemId));

        Variant v = variantRepo.findById(item.getVariant().getId())
                .orElseThrow(() -> new NotFoundException("Variant not found: " + item.getVariant().getId()));

        if (qty <= 0) {
            cartItemRepo.delete(item);
        } else {
            if (qty > v.getStock()) {
                throw new OutOfStockException(v.getSku(), v.getStock(), "Hết hàng cho SKU " + v.getSku());
            }
            item.setQty(qty);
            item.setPriceSnapshot(v.getPrice());
        }
        return toDto(cart);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = cartRepo.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user " + userId));
        CartItem item = cartItemRepo.findByIdAndCart_Id(itemId, cart.getId())
                .orElseThrow(() -> new NotFoundException("Cart item not found: " + itemId));
        cartItemRepo.delete(item);
    }

    @Transactional(readOnly = true)
    public CartDto getCart(Long userId) {
        Cart cart = cartRepo.findByUser_Id(userId).orElseGet(() -> {
            Cart c = new Cart();
            User u = new User();
            u.setId(userId);
            c.setUser(u);
            return c;
        });
        return toDto(cart);
    }

    @Transactional
    public Map<String, Object> mergeCart(Long userId, List<MergeCartRequest.MergeItem> guestItems) {
        Cart cart = getOrCreateCart(userId);

        List<Map<String, Object>> conflicts = new ArrayList<>();
        for (var gi : guestItems) {
            Variant v = variantRepo.findBySku(gi.sku()).orElse(null);
            if (v == null) {
                conflicts.add(Map.of("sku", gi.sku(), "reason", "SKU_NOT_FOUND", "acceptedQty", 0));
                continue;
            }
            int desired = gi.qty();
            int stock = v.getStock();

            CartItem item = cartItemRepo.findByCart_IdAndVariant_Id(cart.getId(), v.getId()).orElse(null);
            int currentQty = (item != null) ? item.getQty() : 0;
            int newQty = Math.min(currentQty + desired, stock);

            if (newQty <= 0) {
                conflicts.add(Map.of("sku", v.getSku(), "reason", "OUT_OF_STOCK", "acceptedQty", 0));
                continue;
            }
            if (item == null) {
                item = new CartItem();
                item.setCart(cart);
                item.setVariant(v);
                item.setQty(newQty);
                item.setPriceSnapshot(v.getPrice());
                cart.getItems().add(item);
            } else {
                item.setQty(newQty);
                item.setPriceSnapshot(v.getPrice());
            }
            if (newQty < currentQty + desired) {
                conflicts.add(Map.of("sku", v.getSku(), "reason", "OUT_OF_STOCK", "acceptedQty", newQty));
            }
        }
        return Map.of("merged", true, "conflicts", conflicts);
    }

    // ==== helpers ====
    private Cart getOrCreateCart(Long userId) {
        return cartRepo.findByUser_Id(userId).orElseGet(() -> {
            Cart c = new Cart();
            User u = new User();
            u.setId(userId);
            c.setUser(u);
            return cartRepo.save(c);
        });
    }

    private CartDto toDto(Cart cart) {
        int subtotal = 0;
        List<CartItemDto> items = new ArrayList<>();
        for (CartItem it : cart.getItems()) {
            Variant v = it.getVariant();
            Product p = v.getProduct();
            String thumbnail = p.getImages().stream()
                    .sorted(Comparator.comparing(ProductImage::getPosition))
                    .map(ProductImage::getUrl)
                    .findFirst().orElse(null);
            int lineTotal = it.getQty() * it.getPriceSnapshot();
            subtotal += lineTotal;

            items.add(new CartItemDto(
                    it.getId(),
                    v.getSku(),
                    p.getSlug(),
                    p.getTitle(),
                    thumbnail,
                    it.getPriceSnapshot(),
                    it.getQty(),
                    lineTotal
            ));
        }
        // MVP: phí ship 0 tại trang Cart
        var summary = new CartDto.Summary(subtotal, 0, subtotal);
        return new CartDto(cart.getId(), items, summary);
    }
}
