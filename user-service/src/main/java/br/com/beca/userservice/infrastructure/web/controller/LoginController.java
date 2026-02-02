package br.com.beca.userservice.infrastructure.web.controller;

import br.com.beca.userservice.infrastructure.persistence.model.UserJpa;
import br.com.beca.userservice.infrastructure.security.TokenJwtData;
import br.com.beca.userservice.infrastructure.security.TokenService;
import br.com.beca.userservice.infrastructure.web.dto.AuthData;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    @CrossOrigin(origins = "*/*")
    public ResponseEntity efetuarLogin(@RequestBody @Valid AuthData data) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(data.login(), data.senha());
            var authentication = manager.authenticate(authenticationToken);
            var tokenJWT = tokenService.generateToken((UserJpa) authentication.getPrincipal());

            return ResponseEntity.ok(new TokenJwtData(tokenJWT));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }

}

