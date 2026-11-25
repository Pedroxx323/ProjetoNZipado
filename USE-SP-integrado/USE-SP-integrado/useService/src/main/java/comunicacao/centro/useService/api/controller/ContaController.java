package comunicacao.centro.useService.api.controller;

import comunicacao.centro.useService.api.model.Conta;
import comunicacao.centro.useService.api.service.ContaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    // LISTAR CONTAS
    @GetMapping
    public List<Conta> listarContas() {
        return contaService.listarContas();
    }

    // BUSCAR CONTA POR ID
    @GetMapping("/{idConta}")
    public ResponseEntity<Conta> buscarPorId(
            @PathVariable("idConta") String idConta) {

        return ResponseEntity.of(
                contaService.buscarPorId(idConta));
    }

    // CADASTRAR CONTA
    @PostMapping
    public ResponseEntity<?> cadastrarConta(
            @RequestBody Conta conta) {

        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(contaService.cadastrarConta(conta));

        } catch (IllegalArgumentException erro) {

            return ResponseEntity.badRequest()
                    .body(erro.getMessage());
        }
    }

    // ALTERAR CONTA
    @PutMapping("/{idConta}")
    public ResponseEntity<?> alterarConta(
            @PathVariable("idConta") String idConta,
            @RequestBody Conta dados) {

        try {

            return ResponseEntity.of(
                    contaService.alterarConta(idConta, dados));

        } catch (IllegalArgumentException erro) {

            return ResponseEntity.badRequest()
                    .body(erro.getMessage());

        } catch (IllegalStateException erro) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(erro.getMessage());
        }
    }

    // EXCLUIR CONTA
    @DeleteMapping("/{idConta}")
    public ResponseEntity<?> excluirConta(
            @PathVariable("idConta") String idConta) {

        try {

            if (contaService.excluirConta(idConta)) {

                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.notFound().build();

        } catch (IllegalStateException erro) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(erro.getMessage());

        } catch (DataIntegrityViolationException erro) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Esta conta possui registros vinculados "
                            + "e não pode ser excluída.");
        }
    }

    // PAGAR CONTA
    @PutMapping("/{idConta}/pagar")
    public ResponseEntity<?> pagarConta(
            @PathVariable("idConta") String idConta) {

        try {

            return ResponseEntity.ok(
                    contaService.pagarConta(idConta));

        } catch (IllegalArgumentException erro) {

            return ResponseEntity.badRequest()
                    .body(erro.getMessage());
        }
    }

    // VERIFICAR VENCIMENTOS
    @PutMapping("/verificar-vencimentos")
    public ResponseEntity<List<Conta>> verificarVencimentos() {

        return ResponseEntity.ok(
                contaService.atualizarContasVencidas());
    }
}