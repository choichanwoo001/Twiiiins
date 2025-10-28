package com.twiiiins.service;

import com.twiiiins.entity.Equipment;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {
    
    private final EquipmentRepository equipmentRepository;
    
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    public Equipment getEquipmentById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
    }
    
    public Equipment createEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }
    
    public Equipment updateEquipment(Long id, Equipment equipmentDetails) {
        Equipment equipment = getEquipmentById(id);
        equipment.setName(equipmentDetails.getName());
        equipment.setImageUrl(equipmentDetails.getImageUrl());
        equipment.setDisplayOrder(equipmentDetails.getDisplayOrder());
        return equipmentRepository.save(equipment);
    }
    
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }
}
