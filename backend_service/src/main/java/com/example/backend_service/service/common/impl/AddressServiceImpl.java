package com.example.backend_service.service.common.impl;

import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.backend_service.dto.request.account.AddressRequest;
import com.example.backend_service.exception.AppException;
import com.example.backend_service.model.Address;
import com.example.backend_service.model.auth.User;
import com.example.backend_service.repository.AddressRepository;
import com.example.backend_service.repository.UserRepository;
import com.example.backend_service.service.common.AddressService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "ADDRESS-SERVICE")
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username);
    }

    @Override
    public List<Address> getMyAddresses() {
        User user = getCurrentUser();
        return addressRepository.findByUser(user);
    }

    @Override
    @Transactional
    public Address createAddress(AddressRequest req) {
        User user = getCurrentUser();
        Address address = new Address();
        address.setUser(user);
        mapRequestToEntity(req, address);

        List<Address> existingAddresses = addressRepository.findByUser(user);
        if (existingAddresses.isEmpty()) {
            address.setIsDefault(true);
        } else if (Boolean.TRUE.equals(req.getIsDefault())) {
            resetDefaultAddress(user, null);
        }
        return addressRepository.save(address);

    }

    @Override
    @Transactional
    public Address updateAddress(Long addressId, AddressRequest req) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException("Địa chỉ không tồn tại"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AppException("Bạn không có quyền cập nhật địa chỉ này");
        }

        mapRequestToEntity(req, address);

        if (Boolean.TRUE.equals(req.getIsDefault())) {
            resetDefaultAddress(user, addressId);
        }
        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        User user = getCurrentUser();
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new AppException("Địa chỉ không tồn tại"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new AppException("Bạn không có quyền xóa địa chỉ này");
        }

        addressRepository.delete(address);
    }

    private void mapRequestToEntity(AddressRequest req, Address address) {
        address.setReceiverName(req.getReceiverName());
        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setDistrict(req.getDistrict());
        address.setWard(req.getWard());
        address.setPhoneNumber(req.getPhoneNumber());
        if (req.getIsDefault() != null) {
            address.setIsDefault(req.getIsDefault());
        }
    }

    private void resetDefaultAddress(User user, Long excludeAddressId) {
        List<Address> oldDefaults = addressRepository.findByUserAndIsDefaultTrue(user);

        for (Address addr : oldDefaults) {
            if (excludeAddressId == null || !addr.getId().equals(excludeAddressId)) {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            }
        }
    }

}
