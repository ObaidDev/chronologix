package com.plutus360.chronologix.exception;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor  @Getter @Setter 
public class InvalideTokenException extends RuntimeException {

    public InvalideTokenException(String message) {
        super(message); 
    }
    
}
