package backend.auth.service;
import backend.auth.dto.AuthResponse;
import backend.auth.dto.LoginRequest;
import backend.auth.dto.RegisterRequest;
import backend.auth.dto.UserResponse;
public interface AuthService { UserResponse register(RegisterRequest request); AuthResponse login(LoginRequest request); UserResponse getCurrentUser(Long userId); }
