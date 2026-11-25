package comunicacao.centro.useService.api.controller;
import comunicacao.centro.useService.api.model.Centro;
import comunicacao.centro.useService.api.service.CentroService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/centros")
public class CentroController {
    private final CentroService centroService;
    public CentroController(CentroService centroService) { this.centroService = centroService; }
    @GetMapping public List<Centro> listarCentros() { return centroService.listarCentros(); }
    @GetMapping("/{idCentro}")
    public ResponseEntity<Centro> buscarPorId(@PathVariable("idCentro") String idCentro) {
        return ResponseEntity.of(centroService.buscarPorId(idCentro));
    }
    @PostMapping public ResponseEntity<?> cadastrarCentro(@RequestBody Centro centro) {
        try { return ResponseEntity.status(HttpStatus.CREATED).body(centroService.cadastrarCentro(centro)); }
        catch (IllegalArgumentException erro) { return ResponseEntity.badRequest().body(erro.getMessage()); }
    }
    @PutMapping("/{idCentro}")
    public ResponseEntity<?> alterarCentro(@PathVariable("idCentro") String idCentro, @RequestBody Centro dados) {
        try { return ResponseEntity.of(centroService.alterarCentro(idCentro, dados)); }
        catch (IllegalArgumentException erro) { return ResponseEntity.badRequest().body(erro.getMessage()); }
    }
    @DeleteMapping("/{idCentro}")
    public ResponseEntity<?> excluirCentro(@PathVariable("idCentro") String idCentro) {
        try {
            return centroService.excluirCentro(idCentro) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalStateException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(erro.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException erro) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Centro possui registros vinculados e não pode ser excluído.");
        }
    }
}
