package io.github.dobrosavljevicnenad.dressinventory.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Order API bodies, using the JSON names of the legacy contract.
 * Name, image and price sent by the client are ignored on purpose:
 * the order snapshot is always taken from the catalog.
 */
public final class OrderDtos {

    private OrderDtos() {
    }

    public record OrderResponse(
            @JsonProperty("_id") UUID id,
            @JsonProperty("haljinaId") UUID dressId,
            @JsonProperty("naziv") String dressName,
            @JsonProperty("slika") String imageUrl,
            @JsonProperty("cena") BigDecimal unitPrice,
            @JsonProperty("boja") String color,
            @JsonProperty("kolicina") int quantity,
            @JsonProperty("screenshot") String paymentScreenshotUrl,
            @JsonProperty("vreme") @JsonFormat(shape = JsonFormat.Shape.STRING) Instant createdAt) {

        static OrderResponse from(CustomerOrder order) {
            return new OrderResponse(order.getId(), order.getDressId(), order.getDressName(), order.getImageUrl(),
                    order.getUnitPrice(), order.getColor(), order.getQuantity(), order.getPaymentScreenshotUrl(),
                    order.getCreatedAt());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SingleOrderRequest(
            @JsonProperty("boja") @NotBlank @Size(max = 100) String color,
            @JsonProperty("kolicina") @Positive @Max(1000) int quantity,
            @JsonProperty("screenshot") @Size(max = 1000) String paymentScreenshotUrl) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BulkOrderRequest(
            @JsonProperty("proizvodi") @NotEmpty @Size(max = 50) List<@NotNull @Valid OrderLineRequest> lines,
            @JsonProperty("kupac") @Valid BuyerRequest buyer) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OrderLineRequest(
            @JsonProperty("_id") @NotNull UUID dressId,
            @JsonProperty("boja") @NotBlank @Size(max = 100) String color,
            @JsonProperty("kolicina") @Positive @Max(1000) int quantity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BuyerRequest(
            @JsonProperty("screenshot") @Size(max = 1000) String paymentScreenshotUrl) {
    }
}
