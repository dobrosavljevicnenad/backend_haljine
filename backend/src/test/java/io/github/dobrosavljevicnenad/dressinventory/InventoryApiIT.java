package io.github.dobrosavljevicnenad.dressinventory;

import com.jayway.jsonpath.JsonPath;
import io.github.dobrosavljevicnenad.dressinventory.media.ImageStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API tests: real security filter chain, real JWT, real PostgreSQL schema from Flyway.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@Testcontainers(disabledWithoutDocker = true)
class InventoryApiIT {

    private static final String SCREENSHOT = "https://res.cloudinary.com/demo/image/upload/v1/haljine/uplata-1.png";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcClient jdbc;

    @MockitoBean
    private ImageStorage imageStorage;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        jdbc.sql("truncate table customer_order, stock_item, dress").update();
        token = login();
    }

    @Test
    void checkoutFlowKeepsStockAndOrderHistoryConsistent() throws Exception {
        String dressId = createDress("""
                {"naziv": "Lana", "slika": "", "cena": 4500, "lager": [{"boja": "Crvena", "kolicina": 2}]}
                """);

        // restock an existing color and add a new one (atomic upsert)
        postJson("/api/dodajKolicinu/" + dressId, """
                {"boja": "Crvena", "kolicina": 3}
                """).andExpect(status().isOk());
        postJson("/api/dodajKolicinu/" + dressId, """
                {"boja": "Plava", "kolicina": 1}
                """).andExpect(status().isOk());

        // the client sends a manipulated price - it must be ignored
        postJson("/api/naruci-vise", """
                {"proizvodi": [{"_id": "%s", "naziv": "Lana", "cena": 1, "boja": "Crvena", "kolicina": 5},
                               {"_id": "%s", "boja": "Plava", "kolicina": 1}],
                 "kupac": {"screenshot": "%s"}}
                """.formatted(dressId, dressId, SCREENSHOT)).andExpect(status().isOk());

        // sold-out variants disappear from the catalog
        mvc.perform(get("/api/haljine").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].naziv").value("Lana"))
                .andExpect(jsonPath("$[0].lager").isEmpty());

        mvc.perform(get("/api/narudzbine").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cena").value(4500.0))
                .andExpect(jsonPath("$[1].cena").value(4500.0))
                .andExpect(jsonPath("$[0].screenshot").value(SCREENSHOT))
                .andExpect(jsonPath("$[0].vreme").isString());

        // delete the whole checkout; its screenshot is removed from storage after commit
        mvc.perform(delete("/api/narudzbine/grupa").param("screenshot", SCREENSHOT)
                        .header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
        assertThat(count("customer_order")).isZero();
        verify(imageStorage).delete(SCREENSHOT);
    }

    @Test
    void failingLineRollsBackTheWholeCheckout() throws Exception {
        String dressId = createDress("""
                {"naziv": "Mila", "cena": 3900,
                 "lager": [{"boja": "Bela", "kolicina": 2}, {"boja": "Crna", "kolicina": 1}]}
                """);

        postJson("/api/naruci-vise", """
                {"proizvodi": [{"_id": "%s", "boja": "Bela", "kolicina": 2},
                               {"_id": "%s", "boja": "Crna", "kolicina": 5}],
                 "kupac": {"screenshot": "%s"}}
                """.formatted(dressId, dressId, SCREENSHOT))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Nema dovoljno na lageru za Mila (Crna)"));

        assertThat(count("customer_order")).isZero();
        assertThat(quantity(dressId, "Bela")).isEqualTo(2);
        assertThat(quantity(dressId, "Crna")).isEqualTo(1);
    }

    @Test
    void singleOrderCanBeDeletedById() throws Exception {
        String dressId = createDress("""
                {"naziv": "Sara", "cena": 5200, "lager": [{"boja": "Zelena", "kolicina": 3}]}
                """);
        postJson("/api/naruci/" + dressId, """
                {"boja": "Zelena", "kolicina": 1, "screenshot": ""}
                """).andExpect(status().isOk());

        String orders = mvc.perform(get("/api/narudzbine").header(HttpHeaders.AUTHORIZATION, bearer()))
                .andReturn().getResponse().getContentAsString();
        String orderId = JsonPath.read(orders, "$[0]._id");

        mvc.perform(delete("/api/narudzbine/{id}", orderId).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());
        assertThat(count("customer_order")).isZero();
        assertThat(quantity(dressId, "Zelena")).isEqualTo(2);
    }

    @Test
    void deletingDressRemovesItsImageAfterCommit() throws Exception {
        String imageUrl = "https://res.cloudinary.com/demo/image/upload/v1/haljine/sara.jpg";
        String dressId = createDress("""
                {"naziv": "Sara", "slika": "%s", "cena": 5200, "lager": []}
                """.formatted(imageUrl));

        mvc.perform(delete("/api/haljine/{id}", dressId).header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk());

        assertThat(count("dress")).isZero();
        verify(imageStorage).delete(imageUrl);
    }

    @Test
    void uploadsOnlyImages() throws Exception {
        given(imageStorage.upload(any())).willReturn("https://res.cloudinary.com/demo/image/upload/v1/haljine/new.png");

        mvc.perform(multipart("/api/upload")
                        .file(new MockMultipartFile("slika", "dress.png", "image/png", new byte[]{1, 2, 3}))
                        .header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://res.cloudinary.com/demo/image/upload/v1/haljine/new.png"));

        mvc.perform(multipart("/api/upload")
                        .file(new MockMultipartFile("slika", "notes.txt", "text/plain", new byte[]{1}))
                        .header(HttpHeaders.AUTHORIZATION, bearer()))
                .andExpect(status().isBadRequest());
    }

    private String login() throws Exception {
        String body = mvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "admin", "password": "test-password"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String createDress(String json) throws Exception {
        String body = postJson("/api/haljine", json)
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$._id");
    }

    private ResultActions postJson(String path, String json) throws Exception {
        return mvc.perform(post(path)
                .header(HttpHeaders.AUTHORIZATION, bearer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private String bearer() {
        return "Bearer " + token;
    }

    private long count(String table) {
        return jdbc.sql("select count(*) from " + table).query(Long.class).single();
    }

    private int quantity(String dressId, String color) {
        return jdbc.sql("select quantity from stock_item where dress_id = ? and color = ?")
                .params(UUID.fromString(dressId), color)
                .query(Integer.class)
                .single();
    }
}
