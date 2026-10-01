package io.github.dobrosavljevicnenad.dressinventory.order;

import io.github.dobrosavljevicnenad.dressinventory.common.BadRequestException;
import io.github.dobrosavljevicnenad.dressinventory.common.MessageResponse;
import io.github.dobrosavljevicnenad.dressinventory.order.OrderDtos.BulkOrderRequest;
import io.github.dobrosavljevicnenad.dressinventory.order.OrderDtos.OrderResponse;
import io.github.dobrosavljevicnenad.dressinventory.order.OrderDtos.SingleOrderRequest;
import io.github.dobrosavljevicnenad.dressinventory.order.OrderService.OrderLine;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
class OrderController {

    private final OrderService orders;

    OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping("/naruci/{id}")
    MessageResponse orderOne(@PathVariable UUID id, @Valid @RequestBody SingleOrderRequest request) {
        orders.placeOrder(id, request.color(), request.quantity(), request.paymentScreenshotUrl());
        return new MessageResponse("Naručeno");
    }

    @PostMapping("/naruci-vise")
    MessageResponse orderMany(@Valid @RequestBody BulkOrderRequest request) {
        List<OrderLine> lines = request.lines().stream()
                .map(line -> new OrderLine(line.dressId(), line.color(), line.quantity()))
                .toList();
        String screenshot = request.buyer() == null ? null : request.buyer().paymentScreenshotUrl();
        orders.placeOrders(lines, screenshot);
        return new MessageResponse("Uspešno naručeno više haljina");
    }

    @GetMapping("/narudzbine")
    List<OrderResponse> list() {
        return orders.findAll().stream().map(OrderResponse::from).toList();
    }

    /** Missing in the legacy API although the frontend already called it. */
    @DeleteMapping("/narudzbine/{id}")
    MessageResponse delete(@PathVariable UUID id) {
        orders.delete(id);
        return new MessageResponse("Narudžbina obrisana");
    }

    /**
     * The screenshot URL is passed as a query parameter: the legacy route put the encoded URL
     * into the path, which Tomcat and Spring Security reject by default (encoded slashes).
     */
    @DeleteMapping("/narudzbine/grupa")
    MessageResponse deleteGroup(@RequestParam("screenshot") String screenshotUrl) {
        if (screenshotUrl.isBlank()) {
            throw new BadRequestException("Screenshot nije prosleđen");
        }
        long deleted = orders.deleteGroup(screenshotUrl);
        return new MessageResponse("Obrisano narudžbina: " + deleted);
    }
}
