package com.criadorslides.repository;

import com.criadorslides.model.Apresentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApresentacaoRepository extends JpaRepository<Apresentacao, Long> {
    List<Apresentacao> findTop20ByOrderByCriadoEmDesc();
}

