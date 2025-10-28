package com.twiiiins.service;

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
    
    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found with id: " + id));
    }
    
    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }
    
    public Contact updateContact(Long id, Contact contactDetails) {
        Contact contact = getContactById(id);
        contact.setName(contactDetails.getName());
        contact.setRole(contactDetails.getRole());
        contact.setEmail(contactDetails.getEmail());
        contact.setDisplayOrder(contactDetails.getDisplayOrder());
        return contactRepository.save(contact);
    }
    
    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }
}
