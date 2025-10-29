package com.twiiiins.service;

import com.twiiiins.dto.ContactDto;
import com.twiiiins.entity.Contact;
import com.twiiiins.exception.ResourceNotFoundException;
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
    
    public List<ContactDto> getAllContacts() {
        return contactRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public ContactDto getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        return convertToDto(contact);
    }
    
    public ContactDto createContact(ContactDto contactDto) {
        Contact contact = convertToEntity(contactDto);
        Contact savedContact = contactRepository.save(contact);
        return convertToDto(savedContact);
    }
    
    public ContactDto updateContact(Long id, ContactDto contactDto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
        
        contact.setName(contactDto.getName());
        contact.setRole(contactDto.getRole());
        contact.setEmail(contactDto.getEmail());
        contact.setDisplayOrder(contactDto.getDisplayOrder());
        
        Contact savedContact = contactRepository.save(contact);
        return convertToDto(savedContact);
    }
    
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
    
    private ContactDto convertToDto(Contact contact) {
        return new ContactDto(
            contact.getId(),
            contact.getName(),
            contact.getRole(),
            contact.getEmail(),
            contact.getDisplayOrder()
        );
    }
    
    private Contact convertToEntity(ContactDto contactDto) {
        Contact contact = new Contact();
        contact.setName(contactDto.getName());
        contact.setRole(contactDto.getRole());
        contact.setEmail(contactDto.getEmail());
        contact.setDisplayOrder(contactDto.getDisplayOrder());
        return contact;
    }
}
