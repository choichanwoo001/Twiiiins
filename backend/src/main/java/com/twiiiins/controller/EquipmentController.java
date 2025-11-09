package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.EquipmentDto;
import com.twiiiins.dto.request.EquipmentCreateRequest;
import com.twiiiins.dto.request.EquipmentUpdateRequest;
import com.twiiiins.service.EquipmentService;
import com.twiiiins.util.ResponseUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media/equipment")
@RequiredArgsConstructor
@Validated
public class EquipmentController {
    
    private final EquipmentService equipmentService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipmentDto>>> getAllEquipment(
            @RequestParam(required = false) @Size(min = 1, max = 100) String name) {
        List<EquipmentDto> equipmentList;
        if (name != null) {
            equipmentList = equipmentService.getEquipmentWithFilters(name);
        } else {
            equipmentList = equipmentService.getAllEquipment();
        }
        return ResponseUtil.listSuccess(equipmentList);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto>> getEquipmentById(@PathVariable @NonNull Long id) {
        return ResponseUtil.success(equipmentService.getEquipmentById(id));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentDto>> createEquipment(@Valid @RequestBody @NonNull EquipmentCreateRequest request) {
        return ResponseUtil.created(equipmentService.createEquipment(request), "장비가 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto>> updateEquipment(
            @PathVariable @NonNull Long id,
            @Valid @RequestBody @NonNull EquipmentUpdateRequest request) {
        return ResponseUtil.success(equipmentService.updateEquipment(id, request), "장비가 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable @NonNull Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseUtil.deleted("장비가 성공적으로 삭제되었습니다.");
    }
}

