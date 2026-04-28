package hubhds.bpo.controller.login;

import hubhds.bpo.dto.login.LoginRequest;
import hubhds.bpo.dto.login.LoginResponse;
import hubhds.bpo.service.login.LoginService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins="*")
@RestController
@RequestMapping
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return loginService.login(loginRequest);
    }
}