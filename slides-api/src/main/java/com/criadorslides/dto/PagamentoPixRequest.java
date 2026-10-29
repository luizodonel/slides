package com.criadorslides.dto;

public class PagamentoPixRequest {
    private Integer totalSlides;
    private String payerName;
    private String payerEmail;
    private String payerCpf;

    public PagamentoPixRequest() {
    }

    public Integer getTotalSlides() {
        return totalSlides;
    }

    public void setTotalSlides(Integer totalSlides) {
        this.totalSlides = totalSlides;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public String getPayerEmail() {
        return payerEmail;
    }

    public void setPayerEmail(String payerEmail) {
        this.payerEmail = payerEmail;
    }

    public String getPayerCpf() {
        return payerCpf;
    }

    public void setPayerCpf(String payerCpf) {
        this.payerCpf = payerCpf;
    }
}

