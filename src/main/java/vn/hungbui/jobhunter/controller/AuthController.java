package vn.hungbui.jobhunter.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import vn.hungbui.jobhunter.domain.User;
import vn.hungbui.jobhunter.domain.dto.LoginDTO;
import vn.hungbui.jobhunter.domain.dto.ResLoginDTO;
import vn.hungbui.jobhunter.service.UserService;
import vn.hungbui.jobhunter.util.SecurityUtil;

@RestController
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil, UserService userService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    //Phải truyền class mới là ResLoginDTO do nếu truyền String thì bị lỗi ở FormatRestResponse vì String không convert sang json được
    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDto) {
        // Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginDto.getUsername(), loginDto.getPassword());

        // xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // nạp thông tin vào securitycontext nếu đăng nhập thành công. thực ra spring tự làm rồi nhưng để sau này cấu hình lại ở đây
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDTO res = new ResLoginDTO();
        User currentUserDB = this.userService.handleGetUserByUsername(loginDto.getUsername());
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName());
            res.setUser(userLogin);
        }

        // create a token
        String access_token = this.securityUtil.createToken(authentication);

        // create refresh token
        String refresh_token = this.securityUtil.createRefreshToken(loginDto.getUsername(), res);

        // update user
        this.userService.updateUserToken(refresh_token, loginDto.getUsername());

        res.setAccessToken(access_token);
        return ResponseEntity.ok().body(res);
    }
}
