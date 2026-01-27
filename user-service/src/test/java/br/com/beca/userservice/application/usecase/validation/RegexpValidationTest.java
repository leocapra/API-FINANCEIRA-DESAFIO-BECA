package br.com.beca.userservice.application.usecase.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexpValidationTest {

    // ---------------- CPF ----------------

    @Test
    @DisplayName("cpf() deve retornar true para CPF no formato 000.000.000-00")
    void cpf_valid_format_shouldReturnTrue() {
        assertTrue(RegexpValidation.cpf("123.456.789-10"));
    }

    @Test
    @DisplayName("cpf() deve retornar false para CPF sem pontuação")
    void cpf_without_punctuation_shouldReturnFalse() {
        assertFalse(RegexpValidation.cpf("12345678910"));
    }

    @Test
    @DisplayName("cpf() deve retornar false para CPF com caracteres inválidos")
    void cpf_with_invalid_chars_shouldReturnFalse() {
        assertFalse(RegexpValidation.cpf("123.456.789-AA"));
    }

    @Test
    @DisplayName("cpf() deve retornar false para CPF com tamanho incorreto")
    void cpf_wrong_size_shouldReturnFalse() {
        assertFalse(RegexpValidation.cpf("12.345.678-90"));
        assertFalse(RegexpValidation.cpf("1234.567.890-12"));
    }

    // ---------------- EMAIL ----------------

    @Test
    @DisplayName("email() deve retornar true para email simples válido")
    void email_valid_simple_shouldReturnTrue() {
        assertTrue(RegexpValidation.email("leo.capra+test@gmail.com"));
    }

    @Test
    @DisplayName("email() deve retornar true para subdomínio (ex: a@b.com.br)")
    void email_valid_subdomain_shouldReturnTrue() {
        assertTrue(RegexpValidation.email("a@b.com.br"));
    }

    @Test
    @DisplayName("email() deve retornar false quando não tem @")
    void email_missing_at_shouldReturnFalse() {
        assertFalse(RegexpValidation.email("leocapragmail.com"));
    }

    @Test
    @DisplayName("email() deve retornar false quando não tem usuário antes do @")
    void email_missing_user_shouldReturnFalse() {
        assertFalse(RegexpValidation.email("@gmail.com"));
    }

    @Test
    @DisplayName("email() deve retornar false quando não tem domínio")
    void email_missing_domain_shouldReturnFalse() {
        assertFalse(RegexpValidation.email("leo@"));
    }

    @Test
    @DisplayName("email() BUG: regex atual aceita domínio sem ponto real antes do TLD (por causa do '.' não escapado)")
    void email_bug_regex_allows_missing_dot_before_tld() {
        // Isso deveria ser inválido, mas com o regex atual pode passar:
        // "a@bXcom" -> o '.' no regex casa com 'X'
        boolean result = RegexpValidation.email("a@bXcom");
        // Se isso passar (true), confirma o bug. Se der false, ok (depende do input).
        assertTrue(result, "Com o regex atual, isso tende a passar e revela o bug do '.' não escapado.");
    }

    // ---------------- TELEFONE ----------------

    @Test
    @DisplayName("telefone() deve retornar true para padrão +55 11 91234-5678")
    void telefone_valid_shouldReturnTrue() {
        assertTrue(RegexpValidation.telefone("+55 11 91234-5678"));
    }

    @Test
    @DisplayName("telefone() deve retornar false se faltar o +código do país")
    void telefone_missing_country_code_shouldReturnFalse() {
        assertFalse(RegexpValidation.telefone("55 11 91234-5678"));
    }

    @Test
    @DisplayName("telefone() deve retornar false se não começar com 9 após DDD")
    void telefone_without_9_after_ddd_shouldReturnFalse() {
        assertFalse(RegexpValidation.telefone("+55 11 81234-5678"));
    }

    @Test
    @DisplayName("telefone() deve retornar false para DDD errado (1 dígito)")
    void telefone_invalid_ddd_shouldReturnFalse() {
        assertFalse(RegexpValidation.telefone("+55 1 91234-5678"));
    }

    @Test
    @DisplayName("telefone() deve retornar false se não tiver espaço entre partes")
    void telefone_missing_spaces_shouldReturnFalse() {
        assertFalse(RegexpValidation.telefone("+551191234-5678"));
        assertFalse(RegexpValidation.telefone("+55 1191234-5678"));
    }

    @Test
    @DisplayName("telefone() deve retornar false se o hífen estiver faltando")
    void telefone_missing_hyphen_shouldReturnFalse() {
        assertFalse(RegexpValidation.telefone("+55 11 912345678"));
    }
}
