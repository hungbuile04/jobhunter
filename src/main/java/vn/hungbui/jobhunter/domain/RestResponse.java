package vn.hungbui.jobhunter.domain;

//Lớp RestResponse<T> được định nghĩa để đóng gói phản hồi (response) từ API trong các ứng dụng web, thường là RESTful APIs. Nó giúp chuẩn hóa cách dữ liệu và thông tin về trạng thái (status) được trả về cho client.
public class RestResponse<T> {
    private int statusCode;
    private String error;

    // message có thể là string, hoặc arrayList
    private Object message;
    private T data;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
