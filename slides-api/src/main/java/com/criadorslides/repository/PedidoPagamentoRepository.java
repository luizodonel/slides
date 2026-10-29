package com.criadorslides.repository;

import com.criadorslides.model.PedidoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PedidoPagamentoRepository extends JpaRepository<PedidoPagamento, String> {
    Optional<PedidoPagamento> findByMercadoPagoId(String mercadoPagoId);
}

