package com.twiiiins.controller;

import com.twiiiins.dto.ApiResponse;
import com.twiiiins.dto.EquipmentDto;
import com.twiiiins.service.EquipmentService;
import com.twiiiins.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/media/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    
    private final EquipmentService equipmentService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<EquipmentDto>>> getAllEquipment() {
        return ResponseUtil.listSuccess(equipmentService.getAllEquipment());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto>> getEquipmentById(@PathVariable Long id) {
        return ResponseUtil.success(equipmentService.getEquipmentById(id));
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<EquipmentDto>> createEquipment(@RequestBody EquipmentDto equipmentDto) {
        return ResponseUtil.created(equipmentService.createEquipment(equipmentDto), "장비가 성공적으로 생성되었습니다.");
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDto>> updateEquipment(
            @PathVariable Long id,
            @RequestBody EquipmentDto equipmentDto) {
        return ResponseUtil.success(equipmentService.updateEquipment(id, equipmentDto), "장비가 성공적으로 수정되었습니다.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseUtil.deleted("장비가 성공적으로 삭제되었습니다.");
    }
}
