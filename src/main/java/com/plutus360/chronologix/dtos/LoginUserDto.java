package com.plutus360.chronologix.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserDto {


    @NotBlank
    private String email;
    
    @NotBlank
    private String password;
    
}