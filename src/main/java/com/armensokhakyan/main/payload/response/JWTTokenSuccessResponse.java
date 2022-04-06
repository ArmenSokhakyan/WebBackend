package com.armensokhakyan.main.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JWTTokenSuccessResponse {
    private boolean isSuccess;
    private String message;
}
