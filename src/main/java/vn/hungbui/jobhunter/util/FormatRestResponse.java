package vn.hungbui.jobhunter.util;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import jakarta.servlet.http.HttpServletResponse;
import vn.hungbui.jobhunter.domain.RestResponse;
import vn.hungbui.jobhunter.util.annotation.ApiMessage;

//xử lý và thay đổi các response body trước khi trả về client
@ControllerAdvice
//@ControllerAdvice giúp quản lý các vấn đề như ngoại lệ, dữ liệu trả về, và các phản hồi cho toàn bộ các controller trong ứng dụng mà không cần xử lý từng ngoại lệ trong từng controller riêng lẻ.
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    //Phương thức supports quyết định liệu logic beforeBodyWrite có được áp dụng hay không. Trong trường hợp này, supports luôn trả về true, tức là logic sẽ được áp dụng cho mọi response từ controller.
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    //Phương thức beforeBodyWrite cho phép bạn can thiệp vào nội dung của response body trước khi nó được gửi về client.
    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(status);

        if(body instanceof String){
            return body;
        }

        if (status >= 400) {
            return body;
        } else {
            res.setData(body);
            ApiMessage message = returnType.getMethodAnnotation(ApiMessage.class);
            res.setMessage(message != null ? message.value() : "CALL API SUCCESS");
        }

        return res;
    }

}
