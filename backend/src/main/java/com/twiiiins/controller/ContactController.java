package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.ContactDto;
import com.twiiiins.service.ContactEntityService;
import com.twiiiins.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media/contacts")
@RequiredArgsConstructor
public class ContactController {
    
    private final ContactEntityService contactEntityService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContactDto>>> getAllContacts() {
        return ResponseUtil.listSuccess(contactEntityService.getAllContacts());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactDto>> getContactById(@PathVariable Long id) {
        return ResponseUtil.success(contactEntityService.getContactById(id));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<ContactDto>> createContact(@RequestBody ContactDto contactDto) {
        return ResponseUtil.created(contactEntityService.createContact(contactDto), "연락처가 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ContactDto>> updateContact(
            @PathVariable Long id,
            @RequestBody ContactDto contactDto) {
        return ResponseUtil.success(contactEntityService.updateContact(id, contactDto), "연락처가 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContact(@PathVariable Long id) {
        contactEntityService.deleteContact(id);
        return ResponseUtil.deleted("연락처가 성공적으로 삭제되었습니다.");
    }
}