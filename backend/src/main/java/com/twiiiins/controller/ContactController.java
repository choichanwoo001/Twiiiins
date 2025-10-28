package com.twiiiins.controller;

import com.twiiiins.dto.ContactDto;
import com.twiiiins.service.ContactEntityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {
    
    private final ContactEntityService contactEntityService;
    
    @GetMapping
    public ResponseEntity<List<ContactDto>> getAllContacts() {
        return ResponseEntity.ok(contactEntityService.getAllContacts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ContactDto> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(contactEntityService.getContactById(id));
    }
    
    @PostMapping
    public ResponseEntity<ContactDto> createContact(@RequestBody ContactDto contactDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contactEntityService.createContact(contactDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ContactDto> updateContact(
            @PathVariable Long id,
            @RequestBody ContactDto contactDto) {
        return ResponseEntity.ok(contactEntityService.updateContact(id, contactDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactEntityService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }
}