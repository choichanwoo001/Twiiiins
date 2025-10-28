package com.twiiiins.service;

import com.twiiiins.entity.ContactInfo;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.ContactInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {
    
    private final ContactInfoRepository contactInfoRepository;
    
    public List<ContactInfo> getAllContactInfo() {
        return contactInfoRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public ContactInfo getContactInfoById(Long id) {
        return contactInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContactInfo not found with id: " + id));
    }
    
    public ContactInfo createContactInfo(ContactInfo contactInfo) {
        return contactInfoRepository.save(contactInfo);
    }
    
    public ContactInfo updateContactInfo(Long id, ContactInfo contactDetails) {
        ContactInfo contactInfo = getContactInfoById(id);
        
        contactInfo.setName(contactDetails.getName());
        contactInfo.setRole(contactDetails.getRole());
        contactInfo.setEmail(contactDetails.getEmail());
        contactInfo.setDisplayOrder(contactDetails.getDisplayOrder());
        contactInfo.setIntroText(contactDetails.getIntroText());
        contactInfo.setPortfolioEnUrl(contactDetails.getPortfolioEnUrl());
        contactInfo.setPortfolioDeUrl(contactDetails.getPortfolioDeUrl());
        contactInfo.setPortfolioKrUrl(contactDetails.getPortfolioKrUrl());
        contactInfo.setStageRiderUrl(contactDetails.getStageRiderUrl());
        contactInfo.setCvUrl(contactDetails.getCvUrl());
        
        return contactInfoRepository.save(contactInfo);
    }
    
    public void deleteContactInfo(Long id) {
        ContactInfo contactInfo = getContactInfoById(id);
        contactInfoRepository.delete(contactInfo);
    }
}

