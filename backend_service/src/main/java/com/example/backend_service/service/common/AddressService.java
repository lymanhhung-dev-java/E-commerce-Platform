package com.example.backend_service.service.common;

import java.util.List;

import com.example.backend_service.dto.request.account.AddressRequest;
import com.example.backend_service.model.Address;

public interface AddressService {
    List<Address> getMyAddresses();
    Address createAddress( AddressRequest req);
    Address updateAddress( Long addressId, AddressRequest req);
    void deleteAddress( Long addressId);
}
