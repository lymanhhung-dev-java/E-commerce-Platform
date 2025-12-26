package com.example.backend_service.dto.request.account;

import lombok.Data;

@Data
public class AddressRequest {
    private String street;
    private String city;
    private String district;
    private String ward;
    private String phoneNumber;
    private Boolean isDefault;
}
