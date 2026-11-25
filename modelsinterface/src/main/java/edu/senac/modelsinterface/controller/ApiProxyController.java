package edu.senac.modelsinterface.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api")
public class ApiProxyController {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${api.base-url}")
    private String baseUrl;

    // CONSULTAR DADOS
    @GetMapping({
            "/centros",
            "/centros/{id}",
            "/contas",
            "/contas/{id}",
            "/recebimentos",
            "/pagamentos",
            "/usuarios/responsaveis",
            "/historico"
    })
    public ResponseEntity<byte[]> get(
            HttpServletRequest request) {

        return forward("GET", obterCaminho(request), null);
    }

    // CADASTRAR DADOS
    @PostMapping({
            "/centros",
            "/contas",
            "/recebimentos",
            "/pagamentos"
    })
    public ResponseEntity<byte[]> post(
            HttpServletRequest request,
            @RequestBody String body) {

        return forward("POST", obterCaminho(request), body);
    }

    // PAGAR CONTAS E VERIFICAR VENCIMENTOS
    @PutMapping({
            "/contas/{id}/pagar",
            "/contas/verificar-vencimentos"
    })
    public ResponseEntity<byte[]> put(
            HttpServletRequest request) {

        return forward("PUT", obterCaminho(request), null);
    }

    // ALTERAR CENTROS E CONTAS
    @PutMapping({
            "/centros/{id}",
            "/contas/{id}"
    })
    public ResponseEntity<byte[]> alterar(
            HttpServletRequest request,
            @RequestBody String body) {

        return forward("PUT", obterCaminho(request), body);
    }

    // EXCLUIR CENTROS E CONTAS
    @DeleteMapping({
            "/centros/{id}",
            "/contas/{id}"
    })
    public ResponseEntity<byte[]> excluir(
            HttpServletRequest request) {

        return forward("DELETE", obterCaminho(request), null);
    }

    // OBTER CAMINHO DA REQUISIÇÃO
    private String obterCaminho(HttpServletRequest request) {

        String caminho = request.getRequestURI()
                .substring(request.getContextPath().length() + 4);

        if (request.getQueryString() != null) {

            caminho += "?" + request.getQueryString();
        }

        return caminho;
    }

    // ENCAMINHAR REQUISIÇÕES PARA A API
    private ResponseEntity<byte[]> forward(
            String method,
            String path,
            String body) {

        try {

            var request = HttpRequest.newBuilder(
                            URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .method(method, body == null
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 204) {

                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.status(response.statusCode())
                    .header("Content-Type",
                            response.headers()
                                    .firstValue("Content-Type")
                                    .orElse("application/json"))
                    .body(response.body());

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return unavailable();

        } catch (Exception e) {

            return unavailable();
        }
    }

    // TRATAR API INDISPONÍVEL
    private ResponseEntity<byte[]> unavailable() {

        return ResponseEntity.status(502)
                .contentType(MediaType.TEXT_PLAIN)
                .body("API indisponível. Verifique o useService."
                        .getBytes(StandardCharsets.UTF_8));
    }
}