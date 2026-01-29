package br.com.beca.userservice.domain.model;

import br.com.beca.userservice.application.port.PasswordHasher;

import java.time.LocalDate;
import java.util.UUID;

public class User {
    private UUID id;
    private String cpf;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private boolean active;
    private LocalDate createdAt;
    private LocalDate updatedAt;

    public User() {};

    public User(UUID id, String cpf, String nome, String email, String senha, String telefone, boolean active, LocalDate createdAt, LocalDate updatedAt) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.active = active;
        this.createdAt = updatedAt;
        this.updatedAt = createdAt;

    }

    public User(String cpf, String nome, String email, String senha, String telefone) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.active = true;
    }

    public User(UUID id, String cpf, String nome, String email, String senha, String telefone) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.active = true;
    }

    public void deactivate() {
        if (!this.active) throw new IllegalStateException("Usuário já esta inativo!");
        this.active = false;
        this.updatedAt = LocalDate.now();
    }

    public void activate(){
        if (this.active) throw new IllegalStateException("Usuario já está ativo!");
        this.active = true;
        this.updatedAt = LocalDate.now();
        }


    public void updateProfile(String nome, String email, String telefone) {
        if (nome != null && !nome.isBlank()) {
            this.nome = nome;
        }
        if (email != null && !email.isBlank()){
            this.email = email;
        }
        if (telefone != null && !telefone.isBlank()){
            this.telefone = telefone;
        }

        this.updatedAt = LocalDate.now();
    }

    public void ensurePasswordHashed(PasswordHasher hasher) {
        if (!hasher.isHashed(this.senha)) {
            this.senha = hasher.hash(this.senha);
        }
    }


    public UUID getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getSenha() {
        return senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }
}
