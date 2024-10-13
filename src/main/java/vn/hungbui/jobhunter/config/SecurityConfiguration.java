package vn.hungbui.jobhunter.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWT;

import vn.hungbui.jobhunter.util.SecurityUtil;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {
    //biến môi trường tự khai báo để khởi tạo jwt
    @Value("${hungbui.jwt.base64-secret}")
    private String jwtKey;
    @Value("${hungbui.jwt.token-validity-in-seconds}")
    private long jwtExpiration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        http
                .csrf(c -> c.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers("/","/login").permitAll()
                                .anyRequest().authenticated())
                                //.anyRequest().permitAll())
                //cấu hình oauth2. khi này sẽ thêm Bearer Token. người dùng gửi kèm bearer token trong header lên để hệ thống xác thực
                //Bearer Token: Đây là một thuật ngữ chung để chỉ một token mà người dùng gửi trong tiêu đề Authorization để xác thực.
                //Token này có thể là bất kỳ loại nào, chẳng hạn như JWT, opaque tokens (token không có thông tin rõ ràng), hoặc thậm chí là session IDs.
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())// Truyền Customizer.withDefaults() là truyền đối tượng rỗng {} (tức là cấu hình mặc định)
                //Xử lý ngoại lệ ở tầng Filter
                .authenticationEntryPoint(customAuthenticationEntryPoint))
                //default exception
                //  .exceptionHandling(
                //         exceptions -> exceptions
                //                 .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) //401
                //                 .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) //403
                .formLogin(f -> f.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
    //Người dùng gửi request từ post/login -> hệ thống xác thực và trả về jwt bằng jwtEncoder -> Trong các yêu cầu tiếp theo, client sẽ gửi JWT kèm theo yêu cầu trong header Authorization. -> Spring Security sử dụng JwtDecoder để giải mã và xác thực JWT cho các yêu cầu này.
    //Phương thức này trả về một đối tượng JwtEncoder, chịu trách nhiệm mã hóa và tạo các JWT trong ứng dụng. NimbusJwtEncoder là một triển khai của JwtEncoder, sử dụng thư viện Nimbus JOSE+JWT để mã hóa và ký JWT. Nó yêu cầu một khóa bí mật để mã hóa và ký mã thông báo.
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }
    //Phương thức này tạo ra và trả về một đối tượng SecretKey, là khóa bí mật được sử dụng để mã hóa JWT. Khóa này được tạo từ chuỗi jwtKey (khóa bí mật dưới dạng Base64).
    private SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }
    //Bean để giải mã các JWT
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
        try {
            return jwtDecoder.decode(token);
        } catch (Exception e) {
            System.out.println(">>> JWT error: " + e.getMessage());
            throw e;
        }
        };
    }
    //khi decode thành công. có tác dụng chuyển đổi JWT thành đối tượng Authentication trong Spring Security. 
    //Cụ thể, nó giúp xác định các quyền (authorities) mà người dùng có dựa trên thông tin được chứa trong JWT.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("hungbui");
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }
    //jwtDecoder để giải mã token và JwtAuthenticationConverter để lấy các quyền từ token đã giải mã đó

}

