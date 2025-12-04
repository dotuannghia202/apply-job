package com.dtn.apply_job.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class SecurityUtil {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    private SecretKey secretKey;

    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Value("${jwt.secret}")
    private String jwtKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;


    public String createToken(Authentication authentication) {

        //Cấu hình thời gian token
        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtExpiration, ChronoUnit.MILLIS);

        //Cấu hình payload
        JwtClaimsSet claims = JwtClaimsSet.builder()
                        .issuedAt(now)
                                .expiresAt(validity)
                                        .subject(authentication.getName())
                                                .claim("devgay", authentication)
                                                        .build();
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
