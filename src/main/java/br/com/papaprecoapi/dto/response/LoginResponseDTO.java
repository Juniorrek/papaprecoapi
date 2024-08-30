package br.com.papaprecoapi.dto.response;

public record LoginResponseDTO(String accessToken, LoginUsuarioResponseDTO usuario) {
    
}
