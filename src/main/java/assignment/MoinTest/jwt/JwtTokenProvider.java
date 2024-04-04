//package assignment.MoinTest.jwt;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.io.Decoders;
//import io.jsonwebtoken.security.Keys;
//import lombok.Value;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.stereotype.Component;
//
//import java.security.Key;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Date;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Component
//public class JwtTokenProvider {
//    private final Key key;
//
//    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey){
//        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
//        this.key = Keys.hmacShaKeyFor(keyBytes);
//    }
//
//    //user 정보를 가지고 AccessToken, RefreshToken을 생성
//    public JwtToken generateToken(Authentication authentication){
//        //권환 가져오기
//        String authorities = authentication.getAuthorities().stream()
//                .map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));
//
//        long now = (new Date()).getTime();
//
//        //AccessToken 생성
//        Date accessTokenExpiresIn = new Date(now + 1800000);
//        String accessToken = Jwts.builder()
//                .setSubject(authentication.getName())
//                .claim("auth", authorities)
//                .setExpiration(accessTokenExpiresIn)
//                .signWith(key, SignatureAlgorithm.ES256)
//                .compact();
//
//        //RefreshToken 생성
//        String refreshToken = Jwts.builder()
//                .setExpiration(new Date(now + 180000))
//                .signWith(key, SignatureAlgorithm.ES256)
//                .compact();
//
//        return JwtToken.builder()
//                .grantType("bearer")
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//    }
//
//    // Jwt 토큰을 복호화하여 토큰 내 정보를 꺼내는 메서드
//    public Authentication getAuthentication(String accessToken){
//
//        Claims claims = parseClaims(accessToken);
//
//        if(claims.get("auth") == null){
//            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
//        }
//
//        Collection<? extends GrantedAuthority> authorities =
//                Arrays.stream(claims.get("auth").toString().split(","))
//                        .map(SimpleGrantedAuthority::new)
//                        .collect(Collectors.toList());
//
//        UserDetails principal = new User(claims.getSubject(), "", authorities);
//        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
//    }
//
//    // accessToken
//    private Claims parseClaims(String accessToken) {
//        try {
//            return Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(accessToken)
//                    .getBody();
//        } catch (ExpiredJwtException e) {
//            return e.getClaims();
//        }
//    }
//
//}
