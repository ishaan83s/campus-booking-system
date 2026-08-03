package backend.auth.service;

import backend.auth.dto.*;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

}