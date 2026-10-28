package com.criadorslides.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "apresentacoes")
public class Apresentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String ideiaOriginal;

    @Column(nullable = false, length = 80)
    private String temaDisciplina;

    @Column(nullable = false, length = 80)
    private String modeloVisual;

    @Column(nullable = false, length = 20)
    private String corPrimaria;

    @Column(nullable = false, length = 20)
    private String corSecundaria;

    @Column(nullable = false)
    private Boolean coresAutomaticas;

    @Column(nullable = false)
    private Integer totalPaginas;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String slidesJson;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getIdeiaOriginal() {
        return ideiaOriginal;
    }

    public void setIdeiaOriginal(String ideiaOriginal) {
        this.ideiaOriginal = ideiaOriginal;
    }

    public String getTemaDisciplina() {
        return temaDisciplina;
    }

    public void setTemaDisciplina(String temaDisciplina) {
        this.temaDisciplina = temaDisciplina;
    }

    public String getModeloVisual() {
        return modeloVisual;
    }

    public void setModeloVisual(String modeloVisual) {
        this.modeloVisual = modeloVisual;
    }

    public String getCorPrimaria() {
        return corPrimaria;
    }

    public void setCorPrimaria(String corPrimaria) {
        this.corPrimaria = corPrimaria;
    }

    public String getCorSecundaria() {
        return corSecundaria;
    }

    public void setCorSecundaria(String corSecundaria) {
        this.corSecundaria = corSecundaria;
    }

    public Boolean getCoresAutomaticas() {
        return coresAutomaticas;
    }

    public void setCoresAutomaticas(Boolean coresAutomaticas) {
        this.coresAutomaticas = coresAutomaticas;
    }

    public Integer getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(Integer totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public String getSlidesJson() {
        return slidesJson;
    }

    public void setSlidesJson(String slidesJson) {
        this.slidesJson = slidesJson;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}

