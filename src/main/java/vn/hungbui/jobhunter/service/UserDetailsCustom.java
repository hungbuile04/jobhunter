package vn.hungbui.jobhunter.service;

import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

//Khi một người dùng cố gắng đăng nhập, Spring Security sẽ gọi phương thức loadUserByUsername() để lấy thông tin về người dùng từ nguồn dữ liệu.
@Component("userDetailsService")
//"userDetailsService" là tên mà Spring sẽ sử dụng để định danh bean này. Đây là điểm khác biệt quan trọng, vì thông thường, khi bạn chỉ dùng @Component, Spring sẽ lấy tên mặc định là tên class với chữ cái đầu viết thường (trong trường hợp này là userDetailCustom). Tuy nhiên, khi bạn cung cấp "userDetailsService", bạn có thể tham chiếu bean này bằng tên cụ thể.
public class UserDetailsCustom implements UserDetailsService {

    private final UserService userService;

    public UserDetailsCustom(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        vn.hungbui.jobhunter.domain.User user = this.userService.handleGetUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("Username/password không hợp lệ");
        }

        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
//Spring Security sử dụng tiền tố "ROLE_" để phân biệt giữa các quyền (authorities) và vai trò (roles). Trong hệ thống bảo mật của Spring:
//Roles (Vai trò): là các quyền cấp cao, đại diện cho một nhóm quyền cụ thể. Vai trò trong Spring Security thường có tiền tố "ROLE_".
//Authorities (Quyền): là các quyền cụ thể hơn. Chúng có thể bao gồm các đặc quyền như "READ_PRIVILEGE" hoặc "WRITE_PRIVILEGE".
//Ví dụ, nếu một người dùng có vai trò ADMIN, thì Spring Security sẽ kỳ vọng vai trò đó được định danh là "ROLE_ADMIN". Khi bạn chỉ định vai trò mà không có tiền tố "ROLE_", Spring Security sẽ không nhận ra nó như một role hợp lệ mà chỉ coi nó là một quyền thông thường.
}
