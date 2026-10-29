package com.criadorslides.controller;

import com.criadorslides.dto.PagamentoPixRequest;
import com.criadorslides.dto.PagamentoPixResponse;
import com.criadorslides.model.PedidoPagamento;
import com.criadorslides.service.MercadoPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagamento")
@CrossOrigin(origins = "*")
public class PagamentoController {

    private final MercadoPagoService mercadoPagoService;

    public PagamentoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @PostMapping("/pix")
    public ResponseEntity<PagamentoPixResponse> gerarPix(@RequestBody PagamentoPixRequest request) {
        PedidoPagamento pedido = mercadoPagoService.criarCobrancaPix(request);
        return ResponseEntity.ok(toResponse(pedido));
    }

    @GetMapping("/status/{id}")
    public ResponseEntity<PagamentoPixResponse> consultarStatus(@PathVariable String id) {
        PedidoPagamento pedido = mercadoPagoService.verificarStatus(id);
        return ResponseEntity.ok(toResponse(pedido));
    }

    @PostMapping("/simular-aprovacao/{id}")
    public ResponseEntity<PagamentoPixResponse> simularAprovacao(@PathVariable String id) {
        PedidoPagamento pedido = mercadoPagoService.simularAprovacao(id);
        return ResponseEntity.ok(toResponse(pedido));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody(required = false) Map<String, Object> payload) {
        // Recebe notificações IPN do Mercado Pago
        return ResponseEntity.ok().build();
    }

    private PagamentoPixResponse toResponse(PedidoPagamento p) {
        PagamentoPixResponse res = new PagamentoPixResponse();
        res.setPedidoId(p.getId());
        res.setMercadoPagoId(p.getMercadoPagoId());
        res.setStatus(p.getStatus());
        res.setValorTotal(p.getValorTotal());
        res.setQuantidadeSlides(p.getQuantidadeSlides());
        res.setQrCode(p.getQrCode());
        res.setQrCodeBase64(p.getQrCodeBase64());
        res.setApproved("APPROVED".equalsIgnoreCase(p.getStatus()));
        return res;
    }
}

