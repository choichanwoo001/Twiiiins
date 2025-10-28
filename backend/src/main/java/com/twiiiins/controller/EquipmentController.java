package com.twiiiins.controller;

import com.twiiiins.dto.EquipmentDto;
import com.twiiiins.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    
    private final EquipmentService equipmentService;
    
    @GetMapping
    public ResponseEntity<List<EquipmentDto>> getAllEquipment() {
        return ResponseEntity.ok(equipmentService.getAllEquipment());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<EquipmentDto> getEquipmentById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getEquipmentById(id));
    }
    
    @PostMapping
    public ResponseEntity<EquipmentDto> createEquipment(@RequestBody EquipmentDto equipmentDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(equipmentService.createEquipment(equipmentDto));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<EquipmentDto> updateEquipment(
            @PathVariable Long id,
            @RequestBody EquipmentDto equipmentDto) {
        return ResponseEntity.ok(equipmentService.updateEquipment(id, equipmentDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
