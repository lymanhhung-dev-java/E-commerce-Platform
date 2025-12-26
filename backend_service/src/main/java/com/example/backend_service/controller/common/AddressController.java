package com.example.backend_service.controller.common;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend_service.dto.request.account.AddressRequest;
import com.example.backend_service.model.Address;
import com.example.backend_service.service.common.AddressService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Tag(name = "Address Controller", description = "API quản lý địa chỉ")
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<Address>> getMyAddresses() {
        return ResponseEntity.ok(addressService.getMyAddresses());
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(@RequestBody AddressRequest req) {
        return ResponseEntity.ok(addressService.createAddress(req));
    }

    @PutMapping
    public ResponseEntity<Address> updateAddress(@PathVariable Long addressId, @RequestBody AddressRequest req) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, req));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAddress(@PathVariable Long addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok("Xóa địa chỉ thành công");
    }
}
