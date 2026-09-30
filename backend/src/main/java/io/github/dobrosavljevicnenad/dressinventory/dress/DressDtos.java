package io.github.dobrosavljevicnenad.dressinventory.dress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request and response bodies of the dress API. JSON property names follow the
 * existing (Serbian) contract of the legacy API, so the frontend keeps working unchanged,
 * while the Java code uses English names.
 */
public final class DressDtos {

    private DressDtos() {
    }

    public record DressResponse(
            @JsonProperty("_id") UUID id,
            @JsonProperty("naziv") String name,
            @JsonProperty("slika") String imageUrl,
            @JsonProperty("cena") BigDecimal price,
            @JsonProperty("lager") List<StockItemResponse> stock) {

        public static DressResponse from(Dress dress) {
            return new DressResponse(dress.getId(), dress.getName(), dress.getImageUrl(), dress.getPrice(),
                    dress.getStock().stream().map(StockItemResponse::from).toList());
        }
    }

    public record StockItemResponse(
            @JsonProperty("_id") UUID id,
            @JsonProperty("boja") String color,
            @JsonProperty("kolicina") int quantity) {

        static StockItemResponse from(StockItem item) {
            return new StockItemResponse(item.getId(), item.getColor(), item.getQuantity());
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DressRequest(
            @JsonProperty("naziv") @NotBlank @Size(max = 200) String name,
            @JsonProperty("slika") @Size(max = 1000) String imageUrl,
            @JsonProperty("cena") @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price,
            @JsonProperty("lager") List<@Valid StockItemRequest> stock) {
    }

    /** Partial update: only the fields that are present are changed. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DressUpdateRequest(
            @JsonProperty("naziv") @Size(min = 1, max = 200) String name,
            @JsonProperty("slika") @Size(max = 1000) String imageUrl,
            @JsonProperty("cena") @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal price,
            @JsonProperty("lager") List<@Valid StockItemRequest> stock) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StockItemRequest(
            @JsonProperty("boja") @Size(max = 100) String color,
            @JsonProperty("kolicina") @PositiveOrZero int quantity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StockChangeRequest(
            @JsonProperty("boja") @NotBlank @Size(max = 100) String color,
            @JsonProperty("kolicina") @Positive int quantity) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ColorRequest(
            @JsonProperty("boja") @NotBlank @Size(max = 100) String color) {
    }

    public record CreatedResponse(
            String message,
            @JsonProperty("_id") UUID id) {
    }
}
