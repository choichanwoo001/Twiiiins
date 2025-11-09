package com.twiiiins.service;

import com.twiiiins.dto.ContactDto;
import com.twiiiins.dto.request.ContactCreateRequest;
import com.twiiiins.dto.request.ContactUpdateRequest;
import com.twiiiins.entity.Contact;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.ContactMapper;
import com.twiiiins.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactEntityService {
    
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    
    public List<ContactDto> getAllContacts() {
        return contactRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(contactMapper::toDto)
                .toList();
    }
    
    public ContactDto getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        return contactMapper.toDto(contact);
    }
    
    public ContactDto createContact(ContactCreateRequest request) {
        Contact contact = contactMapper.toEntity(request);
        Contact savedContact = contactRepository.save(contact);
        return contactMapper.toDto(savedContact);
    }
    
    public ContactDto updateContact(Long id, ContactUpdateRequest request) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        
        contactMapper.updateEntityFromUpdateRequest(request, contact);
        
        Contact savedContact = contactRepository.save(contact);
        return contactMapper.toDto(savedContact);
    }
    
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}
