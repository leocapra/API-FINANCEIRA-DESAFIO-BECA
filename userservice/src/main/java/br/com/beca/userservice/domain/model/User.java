package br.com.beca.userservice.domain.model;

import br.com.beca.userservice.domain.PasswordHasher;

public class User {
    private Long id;
    private String cpf;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private boolean active;

    public User() {};

    public User(String cpf, String nome, String email, String senha, String telefone) {
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.active = true;
    }

    public User(Long id, String cpf, String nome, String email, String senha, String telefone) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
    }

    public User(Long id, String cpf, String nome, String email, String senha, String telefone, boolean active) {
        this.id = id;
        this.cpf = cpf;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.active = active;
    }

    public void deactivate() {
        if (!this.active) throw new IllegalStateException("Usuário já esta inativo!");
        this.active = false;
    }

    public void activate(){
        if (this.active) throw new IllegalStateException("Usuario já está ativo!");
        this.active = true;
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
    }

    public void ensurePasswordHashed(PasswordHasher hasher) {
        if (!hasher.isHashed(this.senha)) {
            this.senha = hasher.hash(this.senha);
        }
    }


    public Long getId() {
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
}
