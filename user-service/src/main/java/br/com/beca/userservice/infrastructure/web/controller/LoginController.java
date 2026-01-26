package br.com.beca.userservice.infrastructure.web.controller;

import br.com.beca.userservice.infrastructure.persistence.model.UserJpa;
import br.com.beca.userservice.infrastructure.security.TokenJwtData;
import br.com.beca.userservice.infrastructure.security.TokenService;
import br.com.beca.userservice.infrastructure.web.dto.AuthData;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private AuthenticationManager manager;

    @Autowired
    private TokenService tokenService;

    @PostMapping
    public ResponseEntity efetuarLogin(@RequestBody @Valid AuthData data) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(data.login(), data.senha());
        var authentication = manager.authenticate(authenticationToken);
        var tokenJWT = tokenService.gerarToken((UserJpa) authentication.getPrincipal());

        return ResponseEntity.ok(new TokenJwtData(tokenJWT));
    }

}

