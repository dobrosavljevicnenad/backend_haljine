package io.github.dobrosavljevicnenad.dressinventory.dress;

import io.github.dobrosavljevicnenad.dressinventory.common.NotFoundException;
import io.github.dobrosavljevicnenad.dressinventory.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DressController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class DressControllerTest {

    private static final UUID DRESS_ID = UUID.fromString("0b6f6c39-5d0b-4e5e-9a2a-5f0c8d1a7e11");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DressService dresses;

    @Test
    void rejectsRequestsWithoutToken() throws Exception {
        mvc.perform(get("/api/haljine"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.error").value("Token nije prosleđen ili nije važeći"));
    }

    @Test
    void listsDressesUsingTheLegacyJsonContract() throws Exception {
        Dress dress = new Dress("Lana", "https://img.example.com/lana.jpg", new BigDecimal("4500.00"));
        ReflectionTestUtils.setField(dress, "id", DRESS_ID);
        dress.replaceStock(Map.of("Crvena", 3));
        given(dresses.findAll()).willReturn(List.of(dress));

        mvc.perform(get("/api/haljine").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]._id").value(DRESS_ID.toString()))
                .andExpect(jsonPath("$[0].naziv").value("Lana"))
                .andExpect(jsonPath("$[0].slika").value("https://img.example.com/lana.jpg"))
                .andExpect(jsonPath("$[0].cena").value(4500.0))
                .andExpect(jsonPath("$[0].lager[0].boja").value("Crvena"))
                .andExpect(jsonPath("$[0].lager[0].kolicina").value(3));
    }

    @Test
    void createsDressAndReturnsItsId() throws Exception {
        Dress saved = new Dress("Mila", null, new BigDecimal("3900"));
        ReflectionTestUtils.setField(saved, "id", DRESS_ID);
        given(dresses.create(any())).willReturn(saved);

        mvc.perform(post("/api/haljine").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"naziv": "Mila", "slika": "", "cena": 3900,
                                 "lager": [{"boja": "Bela", "kolicina": 4}, {"boja": "", "kolicina": 0}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$._id").value(DRESS_ID.toString()));
    }

    @Test
    void rejectsInvalidDressWithFieldErrors() throws Exception {
        mvc.perform(post("/api/haljine").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"naziv": "", "cena": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Neispravni podaci u zahtevu"))
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
        verifyNoInteractions(dresses);
    }

    @Test
    void returnsProblemDetailsForUnknownDress() throws Exception {
        given(dresses.delete(DRESS_ID)).willThrow(NotFoundException.dress(DRESS_ID));

        mvc.perform(delete("/api/haljine/{id}", DRESS_ID).with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void rejectsMalformedId() throws Exception {
        mvc.perform(delete("/api/haljine/{id}", "not-a-uuid").with(jwt()))
                .andExpect(status().isBadRequest());
    }
}
