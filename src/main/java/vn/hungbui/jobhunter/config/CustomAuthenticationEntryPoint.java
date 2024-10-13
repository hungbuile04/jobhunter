package vn.hungbui.jobhunter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import vn.hungbui.jobhunter.domain.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;


//xử lý khi xảy ra lỗi xác thực (authentication error), ví dụ khi người dùng gửi yêu cầu nhưng không có token hoặc token không hợp lệ.
//được gọi bất cứ khi nào có lỗi xác thực trong ứng dụng
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthenticationEntryPoint delegate = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper mapper;

    public CustomAuthenticationEntryPoint(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    // @Override
    // public void commence(HttpServletRequest request, HttpServletResponse response,
    //         AuthenticationException authException) throws IOException, ServletException {
    //     this.delegate.commence(request, response, authException);
    //     response.setContentType("application/json;charset=UTF-8");

    //     RestResponse<Object> res = new RestResponse<Object>();
    //     res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
    //     res.setError(authException.getCause().getMessage());
    //     res.setMessage("Token không hợp lệ (hết hạn, không đúng định dạng, hoặc không truyền JWT ở header)...");

    //     mapper.writeValue(response.getWriter(), res);
    // }
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        this.delegate.commence(request, response, authException);
        response.setContentType("application/json;charset=UTF-8");

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());

        String errorMessage = Optional.ofNullable(authException.getCause()) // NULL
                .map(Throwable::getMessage) // nếu có Cause thì lấy (authException.getCause().getMessage()).
                .orElse(authException.getMessage()); // nếu không có Cause thì lấy trực tiếp authException.getMessage()
        res.setError(errorMessage);

        res.setMessage("Token không hợp lệ (hết hạn, không đúng định dạng, hoặc không truyền JWT ở header)...");

        mapper.writeValue(response.getWriter(), res);
    }
}