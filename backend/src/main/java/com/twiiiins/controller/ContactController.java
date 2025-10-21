package com.twiiiins.controller;

import com.twiiiins.entity.ContactInfo;
import com.twiiiins.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {
    
    private final ContactService contactService;
    
    @GetMapping
    public ResponseEntity<List<ContactInfo>> getAllContactInfo() {
        return ResponseEntity.ok(contactService.getAllContactInfo());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ContactInfo> getContactInfoById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContactInfoById(id));
    }
    
    @PostMapping
    public ResponseEntity<ContactInfo> createContactInfo(@RequestBody ContactInfo contactInfo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactService.createContactInfo(contactInfo));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ContactInfo> updateContactInfo(
            @PathVariable Long id,
            @RequestBody ContactInfo contactInfo) {
        return ResponseEntity.ok(contactService.updateContactInfo(id, contactInfo));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContactInfo(@PathVariable Long id) {
        contactService.deleteContactInfo(id);
        return ResponseEntity.noContent().build();
    }
}

