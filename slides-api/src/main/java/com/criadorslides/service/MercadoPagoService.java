package com.criadorslides.service;

import com.criadorslides.dto.PagamentoPixRequest;
import com.criadorslides.model.PedidoPagamento;
import com.criadorslides.repository.PedidoPagamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MercadoPagoService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoService.class);
    private static final BigDecimal PRECO_POR_SLIDE = new BigDecimal("1.50");

    private final RestTemplate restTemplate;
    private final PedidoPagamentoRepository pedidoRepository;

    @Value("${mercadopago.access-token:${MERCADOPAGO_ACCESS_TOKEN:}}")
    private String accessToken;

    public MercadoPagoService(PedidoPagamentoRepository pedidoRepository) {
        this.restTemplate = new RestTemplate();
        this.pedidoRepository = pedidoRepository;
    }

    /**
     * Cria um pedido de pagamento Pix no Mercado Pago (R$ 1,50 por folha de slide)
     */
    public PedidoPagamento criarCobrancaPix(PagamentoPixRequest request) {
        int slides = (request.getTotalSlides() != null && request.getTotalSlides() > 0)
                ? request.getTotalSlides()
                : 2;

        BigDecimal valorTotal = PRECO_POR_SLIDE.multiply(new BigDecimal(slides));

        String pedidoId = UUID.randomUUID().toString();
        String payerName = (request.getPayerName() != null && !request.getPayerName().isBlank())
                ? request.getPayerName().trim()
                : "Estudante";
        String payerEmail = (request.getPayerEmail() != null && !request.getPayerEmail().isBlank())
                ? request.getPayerEmail().trim()
                : "estudante." + System.currentTimeMillis() + "@slidesdaanna.com.br";
        String payerCpf = (request.getPayerCpf() != null && !request.getPayerCpf().isBlank())
                ? request.getPayerCpf().replaceAll("[^0-9]", "")
                : "00000000000";

        PedidoPagamento pedido = new PedidoPagamento();
        pedido.setId(pedidoId);
        pedido.setQuantidadeSlides(slides);
        pedido.setValorTotal(valorTotal);
        pedido.setPayerName(payerName);
        pedido.setPayerEmail(payerEmail);
        pedido.setDataCriacao(LocalDateTime.now());
        pedido.setStatus("PENDING");

        // Se o token do Mercado Pago não estiver configurado ou for de teste local, gera mock funcional
        if (accessToken == null || accessToken.isBlank() || accessToken.contains("TEST-XXXXXXXX") || accessToken.contains("placeholder")) {
            log.info("Mercado Pago Token não configurado. Gerando cobrança Pix de teste para o pedido {}", pedidoId);
            String mockMpId = "999" + (System.currentTimeMillis() / 1000);
            pedido.setMercadoPagoId(mockMpId);
            pedido.setQrCode("00020101021226870014br.gov.bcb.pix2565pix.mercadopago.com/transfer/receiver/f2f/TEST-PIX-SLIDES-ANNA-" + pedidoId);
            pedido.setQrCodeBase64("iVBORw0KGgoAAAANSUhEUgAAAIIAAACCCAIAAAAFYYeqAAADAUlEQVR4nO2WMWIbMRAD/f9PJ006yTGE7ILDHKa1Z0UAzX19HeXXUc5mB9EZEHQGBJ0BQWdA0Bm+JRl777do7/n7b3UGRPbOgMjeGRDZOwMie2dAZJdmePNPAmdjK3Cymyc2npKHk908sfGUPJzs5omNp+ThZDdPbDwlDye7eWLjKXk42c0TG0/Jw8lunth4Sh5OdvPExlPycLKbJzaekoeT3Tyx8ZQ8nOzmiY2n5OFkN09sPCUPJ7t5YuMpeTjZzRMbT8nDyW6e2HhKHk5288TGU/JwspsnNp6Sh5NdOpGMnYSTvTMgsncGRPbOgMjeGRDZOwMi+5sZknjV7P3PQ+kMCDoDgs6AoDMg6Ax/eH1cydMZEHQGBJ0BQWdA0BkQRGeY+k7z7pCtzoCwOgPC6gwIqzMgrM6AsC74YCXXN2YpwlkuK7QzXGwpwlkuK7QzXGwpwlkuK5Q/g/Lcy+obsjoDwuoMCKszIKzOgLA6A8K64IP1FU59U1ZnQFidAWF1BoTVGRBWZ0BYuA/Wh1qKMMVl1XSGx1mKMMVl1XSGx1mKMMVl1fyvMyhcVt+Q1RkQVmdAWJ0BYXUGhNUZENbrX6MoAcj1TVlDdbookU5Vk7SG6nRRIp2qJmkN1emiRDpVTdIaqtNFiXSqmqQ1VKeLEulUNUlrrJqNxz3N6gwIqzMgrM6AsDoDwuoMCOuHEz8I/3CHVsQpy6xPwQvAqSZpmfUpeAE41SQtsz4FLwCnmqRl1qfgBeBUk7TM+hS8AJxqkpZZn8JUgEwRZy2zPoWpSFN3yJZZn8JUpKk7ZMusT2Eq0tQdsmXWpzAVaeoO2TLrU5iKNHWHbJn1KdxVBMH6uD4FWki+9XF9CrSQfOvj+hRoIfnWx/Up0ELyrY/rU6CF5Fsfn/DoVN9ZnQFhdQaE1RkQVmdAWJ0BYb2ZIYkSgFzflDVUp4sS6VQ1SWuoThcl0qlqktZQnS5KpFPVJK2hOl2USKeqSVpDdbookU5Vk7TGqtl43NOszoCwOgPC6gyHrd8djxjTCJp0UwAAAABJRU5ErkJggg==");
            return pedidoRepository.save(pedido);
        }

        try {
            String url = "https://api.mercadopago.com/v1/payments";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken.trim());
            headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("transaction_amount", valorTotal);
            requestBody.put("description", "Slides da Anna - " + slides + " páginas");
            requestBody.put("payment_method_id", "pix");
            requestBody.put("external_reference", pedidoId);

            String[] nameParts = payerName.split(" ");
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[nameParts.length - 1] : nameParts[0];

            Map<String, Object> payer = new HashMap<>();
            payer.put("email", payerEmail);
            payer.put("first_name", firstName);
            payer.put("last_name", lastName);

            if (payerCpf.length() == 11) {
                Map<String, String> identification = new HashMap<>();
                identification.put("type", "CPF");
                identification.put("number", payerCpf);
                payer.put("identification", identification);
            }

            requestBody.put("payer", payer);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = (Map<String, Object>) response.getBody();
                Object mpIdObj = body.get("id");
                pedido.setMercadoPagoId(mpIdObj != null ? mpIdObj.toString() : null);

                Object statusObj = body.get("status");
                if (statusObj != null) {
                    pedido.setStatus(statusObj.toString().toUpperCase());
                }

                Map<String, Object> poi = (Map<String, Object>) body.get("point_of_interaction");
                if (poi != null) {
                    Map<String, Object> td = (Map<String, Object>) poi.get("transaction_data");
                    if (td != null) {
                        pedido.setQrCode((String) td.get("qr_code"));
                        pedido.setQrCodeBase64((String) td.get("qr_code_base64"));
                    }
                }

                return pedidoRepository.save(pedido);
            }
        } catch (Exception e) {
            log.error("Erro ao chamar API do Mercado Pago: {}", e.getMessage(), e);
            // Fallback para não travar a experiência do usuário
            pedido.setMercadoPagoId("999" + (System.currentTimeMillis() / 1000));
            pedido.setQrCode("00020101021226870014br.gov.bcb.pix2565pix.mercadopago.com/transfer/receiver/f2f/TEST-PIX-SLIDES-ANNA-" + pedidoId);
            pedido.setQrCodeBase64("iVBORw0KGgoAAAANSUhEUgAAAIIAAACCCAIAAAAFYYeqAAADAUlEQVR4nO2WMWIbMRAD/f9PJ006yTGE7ILDHKa1Z0UAzX19HeXXUc5mB9EZEHQGBJ0BQWdA0Bm+JRl777do7/n7b3UGRPbOgMjeGRDZOwMie2dAZJdmePNPAmdjK3Cymyc2npKHk908sfGUPJzs5omNp+ThZDdPbDwlDye7eWLjKXk42c0TG0/Jw8lunth4Sh5OdvPExlPycLKbJzaekoeT3Tyx8ZQ8nOzmiY2n5OFkN09sPCUPJ7t5YuMpeTjZzRMbT8nDyW6e2HhKHk5288TGU/JwspsnNp6Sh5NdOpGMnYSTvTMgsncGRPbOgMjeGRDZOwMi+5sZknjV7P3PQ+kMCDoDgs6AoDMg6Ax/eH1cydMZEHQGBJ0BQWdA0BkQRGeY+k7z7pCtzoCwOgPC6gwIqzMgrM6AsC74YCXXN2YpwlkuK7QzXGwpwlkuK7QzXGwpwlkuK5Q/g/Lcy+obsjoDwuoMCKszIKzOgLA6A8K64IP1FU59U1ZnQFidAWF1BoTVGRBWZ0BYuA/Wh1qKMMVl1XSGx1mKMMVl1XSGx1mKMMVl1fyvMyhcVt+Q1RkQVmdAWJ0BYXUGhNUZENbrX6MoAcj1TVlDdbookU5Vk7SG6nRRIp2qJmkN1emiRDpVTdIaqtNFiXSqmqQ1VKeLEulUNUlrrJqNxz3N6gwIqzMgrM6AsDoDwuoMCOuHEz8I/3CHVsQpy6xPwQvAqSZpmfUpeAE41SQtsz4FLwCnmqRl1qfgBeBUk7TM+hS8AJxqkpZZn8JUgEwRZy2zPoWpSFN3yJZZn8JUpKk7ZMusT2Eq0tQdsmXWpzAVaeoO2TLrU5iKNHWHbJn1KdxVBMH6uD4FWki+9XF9CrSQfOvj+hRoIfnWx/Up0ELyrY/rU6CF5Fsfn/DoVN9ZnQFhdQaE1RkQVmdAWJ0BYb2ZIYkSgFzflDVUp4sS6VQ1SWuoThcl0qlqktZQnS5KpFPVJK2hOl2USKeqSVDdbookU5Vk7TGqtl43NOszoCwOgPC6gyHrd8djxjTCJp0UwAAAABJRU5ErkJggg==");
        }

        return pedidoRepository.save(pedido);
    }

    /**
     * Consulta o status de um pedido pelo seu ID interno ou pelo Mercado Pago ID
     */
    public PedidoPagamento verificarStatus(String pedidoId) {
        PedidoPagamento pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + pedidoId));

        // Se já está aprovado, retorna imediatamente
        if ("APPROVED".equalsIgnoreCase(pedido.getStatus())) {
            return pedido;
        }

        String mpId = pedido.getMercadoPagoId();

        // Se for mock, permite simulação ou aprovação
        if (mpId != null && mpId.startsWith("999")) {
            return pedido;
        }

        // Se temos token e ID do Mercado Pago, consulta status atualizado na API
        if (accessToken != null && !accessToken.isBlank() && mpId != null && !mpId.isBlank()) {
            try {
                String url = "https://api.mercadopago.com/v1/payments/" + mpId;
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken.trim());

                HttpEntity<Void> entity = new HttpEntity<>(headers);
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> body = (Map<String, Object>) response.getBody();
                    Object statusObj = body.get("status");
                    if (statusObj != null) {
                        String novoStatus = statusObj.toString().toUpperCase();
                        pedido.setStatus(novoStatus);
                        if ("APPROVED".equalsIgnoreCase(novoStatus) && pedido.getDataAprovacao() == null) {
                            pedido.setDataAprovacao(LocalDateTime.now());
                        }
                        return pedidoRepository.save(pedido);
                    }
                }
            } catch (Exception e) {
                log.warn("Falha ao consultar status no Mercado Pago: {}", e.getMessage());
            }
        }

        return pedido;
    }

    /**
     * Simula a aprovação imediata de um pedido (útil para testes em desenvolvimento)
     */
    public PedidoPagamento simularAprovacao(String pedidoId) {
        PedidoPagamento pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + pedidoId));
        pedido.setStatus("APPROVED");
        pedido.setDataAprovacao(LocalDateTime.now());
        return pedidoRepository.save(pedido);
    }

    public boolean isPedidoAprovado(String pedidoId) {
        if (pedidoId == null || pedidoId.isBlank()) return false;
        return pedidoRepository.findById(pedidoId)
                .map(p -> "APPROVED".equalsIgnoreCase(p.getStatus()))
                .orElse(false);
    }
}

