package com.example.e_commerce_services.service;



import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_services.domain.Cart;
import com.example.e_commerce_services.domain.CartItem;
import com.example.e_commerce_services.domain.Order;
import com.example.e_commerce_services.domain.OrderItem;
import com.example.e_commerce_services.domain.OrderStatus;
import com.example.e_commerce_services.domain.User;
import com.example.e_commerce_services.domain.Variant;
import com.example.e_commerce_services.dto.PlaceOrderRequest;
import com.example.e_commerce_services.dto.PreviewResponse;
import com.example.e_commerce_services.dto.ShippingAddressDto;
import com.example.e_commerce_services.exception.NotFoundException;
import com.example.e_commerce_services.exception.OutOfStockException;
import com.example.e_commerce_services.repository.CartItemRepository;
import com.example.e_commerce_services.repository.CartRepository;
import com.example.e_commerce_services.repository.OrderItemRepository;
import com.example.e_commerce_services.repository.OrderRepository;

@Service
public class CheckoutService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final OrderNumberGenerator numberGen;

    public CheckoutService(CartRepository cartRepo,
                           CartItemRepository cartItemRepo,
                           OrderRepository orderRepo,
                           OrderItemRepository orderItemRepo) {
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.numberGen = new OrderNumberGenerator(orderRepo);
    }

    @Transactional(readOnly = true)
    public PreviewResponse preview(Long userId, ShippingAddressDto addr) {
        Cart cart = cartRepo.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user " + userId));

        int subtotal = 0;
        List<PreviewResponse.PreviewItem> items = new ArrayList<>();

        for (CartItem it : cart.getItems()) {
            Variant v = it.getVariant();
            // Check tồn hiện tại (không trừ ở preview)
            int qty = Math.min(it.getQty(), v.getStock());
            int unit = v.getPrice(); // có thể khác priceSnapshot => hiển thị luôn giá hiện tại
            int line = qty * unit;
            subtotal += line;
            items.add(new PreviewResponse.PreviewItem(v.getSku(), qty, unit, line));
        }

        int shippingFee = calcShippingFee(addr);
        int total = subtotal + shippingFee;
        boolean canPlace = !items.isEmpty() && items.stream().allMatch(i -> i.qty() > 0);

        return new PreviewResponse(items, shippingFee, subtotal, total, canPlace);
    }

    @Transactional
    public Order placeOrder(Long userId, PlaceOrderRequest req) {
        Cart cart = cartRepo.findByUser_Id(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found for user " + userId));
        if (cart.getItems().isEmpty()) {
            throw new NotFoundException("Cart is empty");
        }

        // Validate tồn & tính tiền theo giá hiện tại (snapshot)
        int subtotal = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Variant v = ci.getVariant();

            if (ci.getQty() > v.getStock()) {
                throw new OutOfStockException(v.getSku(), v.getStock(), "Hết hàng cho SKU " + v.getSku());
            }

            int unit = v.getPrice(); // snapshot giá hiện tại
            int line = unit * ci.getQty();

            OrderItem oi = new OrderItem();
            oi.setVariant(v);
            oi.setSku(v.getSku());
            oi.setTitle(v.getProduct().getTitle());
            oi.setQty(ci.getQty());
            oi.setUnitPrice(unit);
            oi.setLineTotal(line);
            orderItems.add(oi);

            subtotal += line;
        }

        int shippingFee = calcShippingFee(req.shippingAddress());
        int total = subtotal + shippingFee;

        // Tạo Order
        Order o = new Order();
        User u = new User(); u.setId(userId);
        o.setUser(u);
        o.setNumber(numberGen.next()); // đảm bảo unique
        o.setStatus(OrderStatus.PLACED);

        // Shipping snapshot
        o.setShipFullName(req.shippingAddress().fullName());
        o.setShipPhone(req.shippingAddress().phone());
        o.setShipLine1(req.shippingAddress().line1());
        o.setShipProvince(req.shippingAddress().province());
        o.setShipDistrict(req.shippingAddress().district());
        o.setShipWard(req.shippingAddress().ward());

        o.setShippingFee(shippingFee);
        o.setSubtotal(subtotal);
        o.setTotal(total);

        // Gắn items
        o.setItems(new ArrayList<>());
        Order saved = orderRepo.save(o);
        for (OrderItem oi : orderItems) {
            oi.setOrder(saved);
            orderItemRepo.save(oi);

            // TRỪ TỒN KHO (MVP: trừ ngay khi order thành công)
            Variant v = oi.getVariant();
            v.setStock(v.getStock() - oi.getQty());
        }

        // Xoá cart items (MVP clear)
        cartItemRepo.deleteAll(cart.getItems());
        cart.getItems().clear();

        return saved;
    }

    private int calcShippingFee(ShippingAddressDto addr) {
        // MVP: phí ship cố định, có thể tuỳ tỉnh thành nếu muốn.
        return 20000;
    }

    // ============ Simple order number generator ============
    static class OrderNumberGenerator {
        private final OrderRepository repo;
        OrderNumberGenerator(OrderRepository repo){ this.repo = repo; }

        String next() {
            String date = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int seq = 1;
            String number;
            do {
                number = "DH-" + date + "-" + String.format("%04d", seq++);
            } while (repo.existsByNumber(number));
            return number;
        }
    }
}

