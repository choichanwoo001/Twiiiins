package com.twiiiins.service;

import com.twiiiins.entity.ContactInfo;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.ContactInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {
    
    private final ContactInfoRepository contactInfoRepository;
    
    public List<ContactInfo> getAllContactInfo() {
        return contactInfoRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public ContactInfo getContactInfoById(@NonNull Long id) {
        return contactInfoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContactInfo not found with id: " + id));
    }
    
    public ContactInfo createContactInfo(@NonNull ContactInfo contactInfo) {
        return contactInfoRepository.save(Objects.requireNonNull(contactInfo, "ContactInfo must not be null"));
    }
    
    public ContactInfo updateContactInfo(@NonNull Long id, @NonNull ContactInfo contactDetails) {
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
        
        return contactInfoRepository.save(Objects.requireNonNull(contactInfo, "ContactInfo must not be null"));
    }
    
    public void deleteContactInfo(@NonNull Long id) {
        ContactInfo contactInfo = getContactInfoById(id);
        contactInfoRepository.delete(Objects.requireNonNull(contactInfo, "ContactInfo must not be null"));
    }
}

