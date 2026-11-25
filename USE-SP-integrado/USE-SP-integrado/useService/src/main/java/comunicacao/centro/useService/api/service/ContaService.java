package comunicacao.centro.useService.api.service;

import comunicacao.centro.useService.api.model.Conta;
import comunicacao.centro.useService.api.repository.ContaRepository;
import comunicacao.centro.useService.api.repository.CentroRepository;
import comunicacao.centro.useService.api.repository.PagamentoRepository;
import comunicacao.centro.useService.api.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ContaService {

 private final ContaRepository contaRepository;
 private final CentroRepository centroRepository;
 private final PagamentoRepository pagamentoRepository;
 private final UsuarioRepository usuarioRepository;

 public ContaService(
         ContaRepository contaRepository,
         CentroRepository centroRepository,
         PagamentoRepository pagamentoRepository,
         UsuarioRepository usuarioRepository) {

  this.contaRepository = contaRepository;
  this.centroRepository = centroRepository;
  this.pagamentoRepository = pagamentoRepository;
  this.usuarioRepository = usuarioRepository;
 }

 // LISTAR CONTAS

 public List<Conta> listarContas() {
  return contaRepository.findAll();
 }

 // BUSCAR CONTA POR ID

 public Optional<Conta> buscarPorId(String idConta) {
  return contaRepository.findById(idConta);
 }

 // CADASTRAR CONTA

 public Conta cadastrarConta(Conta conta) {

  if (conta.getIdConta() == null ||
          conta.getIdConta().isBlank()) {

   throw new IllegalArgumentException(
           "Informe o ID da conta.");
  }

  if (contaRepository.existsById(conta.getIdConta())) {

   throw new IllegalArgumentException(
           "Já existe uma conta com esse ID.");
  }

  validarConta(conta);

  if (!"PENDENTE".equals(conta.getStatus())) {

   throw new IllegalArgumentException(
           "Uma nova conta deve ser cadastrada como PENDENTE.");
  }

  conta.setDataPagamento(null);
  atualizarStatus(conta);

  return contaRepository.save(conta);
 }

 // ALTERAR CONTA

 @Transactional
 public Optional<Conta> alterarConta(
         String idConta, Conta dados) {

  return contaRepository.findById(idConta).map(conta -> {

   if ("PAGO".equals(conta.getStatus())) {

    throw new IllegalStateException(
            "Não é possível alterar uma conta já paga.");
   }

   conta.setIdCentro(dados.getIdCentro());
   conta.setCodigoPagamento(dados.getCodigoPagamento());
   conta.setValor(dados.getValor());
   conta.setDataVencimento(dados.getDataVencimento());

   if (conta.getIdCentro() == null) {

    throw new IllegalArgumentException(
            "Informe o centro.");
   }

   var centro = centroRepository
           .findById(conta.getIdCentro())
           .orElseThrow(() ->
                   new IllegalArgumentException(
                           "Centro não encontrado."));

   conta.setIdUsuario(centro.getIdUsuario());

   validarConta(conta);
   atualizarStatus(conta);

   return contaRepository.save(conta);
  });
 }

 // EXCLUIR CONTA

 @Transactional
 public boolean excluirConta(String idConta) {

  Optional<Conta> resultado =
          contaRepository.findById(idConta);

  if (resultado.isEmpty()) {
   return false;
  }

  Conta conta = resultado.get();

  if ("PAGO".equals(conta.getStatus())) {

   throw new IllegalStateException(
           "Não é possível excluir uma conta já paga.");
  }

  contaRepository.delete(conta);

  return true;
 }

 // PAGAR CONTA

 @Transactional
 public Conta pagarConta(String idConta) {

  Conta conta = contaRepository.findById(idConta)
          .orElseThrow(() ->
                  new IllegalArgumentException(
                          "Conta não encontrada."));

  if ("PAGO".equals(conta.getStatus())) {

   throw new IllegalArgumentException(
           "Esta conta já foi paga.");
  }

  conta.setStatus("PAGO");
  conta.setDataPagamento(LocalDate.now());

  return contaRepository.save(conta);
 }

 // VERIFICAR VENCIMENTOS

 @Transactional
 public List<Conta> atualizarContasVencidas() {

  List<Conta> contas = contaRepository.findAll();

  for (Conta conta : contas) {

   if (!"PAGO".equals(conta.getStatus()) &&
           conta.getDataVencimento() != null) {

    atualizarStatus(conta);
   }
  }

  return contaRepository.saveAll(contas);
 }

 // ATUALIZAR STATUS AUTOMATICAMENTE

 private void atualizarStatus(Conta conta) {

  if ("PAGO".equals(conta.getStatus())) {
   return;
  }

  if (conta.getDataVencimento() != null &&
          conta.getDataVencimento()
                  .isBefore(LocalDate.now())) {

   conta.setStatus("VENCIDO");

  } else {

   conta.setStatus("PENDENTE");
  }
 }

 // VALIDAR DADOS DA CONTA

 private void validarConta(Conta conta) {

  if (conta.getIdCentro() == null ||
          !centroRepository.existsById(conta.getIdCentro())) {

   throw new IllegalArgumentException(
           "Centro não encontrado.");
  }

  if (conta.getCodigoPagamento() == null ||
          !pagamentoRepository.existsById(
                  conta.getCodigoPagamento())) {

   throw new IllegalArgumentException(
           "Tipo de pagamento não encontrado.");
  }

  if (conta.getIdUsuario() == null ||
          !usuarioRepository.existsById(conta.getIdUsuario())) {

   throw new IllegalArgumentException(
           "Usuário não encontrado.");
  }

  if (conta.getValor() == null ||
          conta.getValor().compareTo(BigDecimal.ZERO) <= 0) {

   throw new IllegalArgumentException(
           "O valor deve ser maior que zero.");
  }

  if (conta.getDataVencimento() == null) {

   throw new IllegalArgumentException(
           "Informe a data de vencimento.");
  }

  if (conta.getDataVencimento().getYear() < 1900 ||
          conta.getDataVencimento().getYear() > 9999) {

   throw new IllegalArgumentException(
           "Informe uma data de vencimento válida.");
  }
 }
}