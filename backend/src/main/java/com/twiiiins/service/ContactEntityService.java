package com.twiiiins.service;

import com.twiiiins.dto.ContactDto;
import com.twiiiins.dto.request.ContactCreateRequest;
import com.twiiiins.dto.request.ContactUpdateRequest;
import com.twiiiins.entity.Contact;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.ContactMapper;
import com.twiiiins.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactEntityService {
    
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    
    public List<ContactDto> getAllContacts() {
        return contactRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(contactMapper::toDto)
                .toList();
    }
    
    public ContactDto getContactById(@NonNull Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        return contactMapper.toDto(contact);
    }
    
    @Transactional
    public ContactDto createContact(@NonNull ContactCreateRequest request) {
        Contact contact = Objects.requireNonNull(
                contactMapper.toEntity(request),
                "ContactMapper.toEntity returned null"
        );
        Contact savedContact = contactRepository.save(Objects.requireNonNull(contact, "Contact must not be null"));
        return contactMapper.toDto(savedContact);
    }
    
    @Transactional
    public ContactDto updateContact(@NonNull Long id, @NonNull ContactUpdateRequest request) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        
        contactMapper.updateEntityFromUpdateRequest(request, contact);
        
        Contact savedContact = contactRepository.save(Objects.requireNonNull(contact, "Contact must not be null"));
        return contactMapper.toDto(savedContact);
    }
    
    @Transactional
    public void deleteContact(@NonNull Long id) {
        contactRepository.deleteById(id);
    }
}
