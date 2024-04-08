package org.itmo.eventapp.main.service;

import lombok.RequiredArgsConstructor;
import org.itmo.eventapp.main.exceptionhandling.ExceptionConst;
import org.itmo.eventapp.main.model.entity.RegistrationRequest;
import org.itmo.eventapp.main.repository.RegistrationRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class RegistrationRequestService {

    private final RegistrationRequestRepository registrationRequestRepository;

    public RegistrationRequest findById(Integer id) {
        return registrationRequestRepository.findRegistrationRequestById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        ExceptionConst.REGISTRATION_REQUEST_NOT_FOUND_MESSAGE));
    }

    public boolean existsByEmail(String email) {
        return registrationRequestRepository.existsByEmail(email);
    }

    public void save(RegistrationRequest registrationRequest) {
        registrationRequestRepository.save(registrationRequest);
    }
}
