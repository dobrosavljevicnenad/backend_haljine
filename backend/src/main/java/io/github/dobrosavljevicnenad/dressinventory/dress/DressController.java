package io.github.dobrosavljevicnenad.dressinventory.dress;

import io.github.dobrosavljevicnenad.dressinventory.common.MessageResponse;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.ColorRequest;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.CreatedResponse;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.DressRequest;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.DressResponse;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.DressUpdateRequest;
import io.github.dobrosavljevicnenad.dressinventory.dress.DressDtos.StockChangeRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Dress catalog and stock management. Paths are kept identical to the legacy Node.js API.
 */
@RestController
@RequestMapping("/api")
class DressController {

    private final DressService dresses;

    DressController(DressService dresses) {
        this.dresses = dresses;
    }

    @GetMapping("/haljine")
    List<DressResponse> list() {
        return dresses.findAll().stream().map(DressResponse::from).toList();
    }

    @PostMapping("/haljine")
    @ResponseStatus(HttpStatus.CREATED)
    CreatedResponse create(@Valid @RequestBody DressRequest request) {
        Dress dress = dresses.create(request);
        return new CreatedResponse("OK", dress.getId());
    }

    @PutMapping("/haljine/{id}")
    MessageResponse update(@PathVariable UUID id, @Valid @RequestBody DressUpdateRequest request) {
        dresses.update(id, request);
        return new MessageResponse("Izmenjeno");
    }

    @DeleteMapping("/haljine/{id}")
    MessageResponse delete(@PathVariable UUID id) {
        dresses.delete(id);
        return new MessageResponse("Haljina je obrisana.");
    }

    @PostMapping("/dodajKolicinu/{id}")
    MessageResponse addQuantity(@PathVariable UUID id, @Valid @RequestBody StockChangeRequest request) {
        dresses.addQuantity(id, request.color(), request.quantity());
        return new MessageResponse("Količina ažurirana");
    }

    @PostMapping("/obrisi-boju/{id}")
    MessageResponse removeColor(@PathVariable UUID id, @Valid @RequestBody ColorRequest request) {
        dresses.removeColor(id, request.color());
        return new MessageResponse("Boja obrisana");
    }
}
