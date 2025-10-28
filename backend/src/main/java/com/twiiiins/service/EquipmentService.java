package com.twiiiins.service;

import com.twiiiins.dto.EquipmentDto;
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
    
    public List<EquipmentDto> getAllEquipment() {
        return equipmentRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    public EquipmentDto getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
        return convertToDto(equipment);
    }
    
    public EquipmentDto createEquipment(EquipmentDto equipmentDto) {
        Equipment equipment = convertToEntity(equipmentDto);
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return convertToDto(savedEquipment);
    }
    
    public EquipmentDto updateEquipment(Long id, EquipmentDto equipmentDto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
        
        equipment.setName(equipmentDto.getName());
        equipment.setImageUrl(equipmentDto.getImageUrl());
        equipment.setDisplayOrder(equipmentDto.getDisplayOrder());
        
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return convertToDto(savedEquipment);
    }
    
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }
    
    private EquipmentDto convertToDto(Equipment equipment) {
        return new EquipmentDto(
            equipment.getId(),
            equipment.getName(),
            equipment.getImageUrl(),
            equipment.getDisplayOrder()
        );
    }
    
    private Equipment convertToEntity(EquipmentDto equipmentDto) {
        Equipment equipment = new Equipment();
        equipment.setName(equipmentDto.getName());
        equipment.setImageUrl(equipmentDto.getImageUrl());
        equipment.setDisplayOrder(equipmentDto.getDisplayOrder());
        return equipment;
    }
}
