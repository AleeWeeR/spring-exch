package uz.fido.pfexchange.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uz.fido.pfexchange.config.properties.JwtProperties;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {
    
    private final JwtProperties properties;

    private SecretKey symmetricKey;
    private RSAPrivateKey rsaPrivateKey;
    private RSAPublicKey rsaPublicKey;

    @PostConstruct
    public void init() {
        initSymmetricKey();
        if (properties.getAsymmetric().isEnabled()) {
            initAsymmetricKeys();
        }
    }

    private void initSymmetricKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecretKey());
        this.symmetricKey = Keys.hmacShaKeyFor(keyBytes);
        log.info("Symmetric JWT key initialized");
    }

    private void initAsymmetricKeys() {
        try {
            String resolvedPrivateKey = resolveKey(properties.getAsymmetric().getPrivateKeyPem(), properties.getAsymmetric().getPrivateKeyBase64(), "private");
            String resolvedPublicKey = resolveKey(properties.getAsymmetric().getPublicKeyPem(), properties.getAsymmetric().getPublicKeyBase64(), "public");

            this.rsaPrivateKey = parsePrivateKey(resolvedPrivateKey);
            this.rsaPublicKey = parsePublicKey(resolvedPublicKey);

            log.info("Asymmetric JWT keys initialized (keyId: {}, source: {})",
                    properties.getAsymmetric().getKeyId(),
                    isNotBlank(properties.getAsymmetric().getPrivateKeyBase64()) ? "base64 env" : "direct PEM");

        } catch (Exception e) {
            log.error("Failed to initialize asymmetric keys", e);
            throw new IllegalStateException("Cannot initialize RSA keys", e);
        }
    }

    private String resolveKey(String directPem, String base64Encoded, String keyType) {
        if (isNotBlank(base64Encoded)) {
            log.debug("Loading {} key from base64", keyType);
            return new String(Base64.getDecoder().decode(base64Encoded));
        }

        if (isNotBlank(directPem)) {
            log.debug("Loading {} key from direct PEM", keyType);
            return directPem;
        }

        throw new IllegalStateException(
                String.format("No %s key provided. Set jwt.asymmetric.%s-key or jwt.asymmetric.%s-key-base64",
                        keyType, keyType, keyType)
        );
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return generateAccessToken(new HashMap<>(), userDetails);
    }

    public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        long now = System.currentTimeMillis();
        long expiration = now + 1000L * 60 * 60 * properties.getExpireHours();

        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(symmetricKey, Jwts.SIG.HS512)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (Objects.equals(username, userDetails.getUsername()) && isTokenNotExpired(token));
    }

    public boolean isTokenNotExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(symmetricKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String generateAsymmetricToken() {
        if (!properties.getAsymmetric().isEnabled()) {
            throw new IllegalStateException("Asymmetric JWT is not enabled");
        }

        long now = System.currentTimeMillis();
        long expiration = now + 1000L * 60 * properties.getAsymmetric().getExpireMinutes();

        return Jwts.builder()
                .header()
                    .keyId(properties.getAsymmetric().getKeyId())
                    .and()
                .subject(properties.getAsymmetric().getSystemName())
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(rsaPrivateKey, Jwts.SIG.RS512)
                .compact();
    }

    public Claims validateAsymmetricToken(String token) {
        return Jwts.parser()
                .verifyWith(rsaPublicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private RSAPrivateKey parsePrivateKey(String pem) throws Exception {
        String content = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(content);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    private RSAPublicKey parsePublicKey(String pem) throws Exception {
        String content = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(content);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    public RSAPublicKey getRsaPublicKey() {
        return rsaPublicKey;
    }

    public RSAPrivateKey getRsaPrivateKey() {
        return rsaPrivateKey;
    }

    public String getKeyId() {
        return properties.getAsymmetric().getKeyId();
    }

    public boolean isAsymmetricEnabled() {
        return properties.getAsymmetric().isEnabled();
    }
}