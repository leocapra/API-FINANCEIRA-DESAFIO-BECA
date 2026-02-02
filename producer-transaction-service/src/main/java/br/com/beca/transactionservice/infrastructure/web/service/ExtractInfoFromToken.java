package br.com.beca.transactionservice.infrastructure.web.service;

import br.com.beca.transactionservice.domain.dto.TokenInfoData;
import br.com.beca.transactionservice.infrastructure.security.SecurityFilter;
import br.com.beca.transactionservice.infrastructure.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class ExtractInfoFromToken {
    private final SecurityFilter securityFilter;
    private final TokenService tokenService;

    public ExtractInfoFromToken(SecurityFilter securityFilter, TokenService tokenService) {
        this.securityFilter = securityFilter;
        this.tokenService = tokenService;
    }

    public TokenInfoData tokenInfo(HttpServletRequest request){
        String userId = this.extractUserId(request);
        String role = this.extractRole(request);

        return new TokenInfoData(userId, role);
    }

    private String extractUserId(HttpServletRequest request){
        var tokenJWT = securityFilter.recoverToken(request);
        return tokenService.extractId(tokenJWT);
    }

    private String extractRole(HttpServletRequest request){
        var tokenJWT = securityFilter.recoverToken(request);
        return tokenService.extractRole(tokenJWT);
    }
}
