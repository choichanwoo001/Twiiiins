package com.twiiiins.service;

import com.twiiiins.dto.ContactDto;
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
    
    public ContactDto createContact(ContactDto contactDto) {
        Contact contact = contactMapper.toEntity(contactDto);
        Contact savedContact = contactRepository.save(contact);
        return contactMapper.toDto(savedContact);
    }
    
    public ContactDto updateContact(Long id, ContactDto contactDto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        
        contactMapper.updateEntityFromDto(contactDto, contact);
        
        Contact savedContact = contactRepository.save(contact);
        return contactMapper.toDto(savedContact);
    }
    
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}
