package com.twiiiins.service;

import com.twiiiins.dto.EquipmentDto;
import com.twiiiins.dto.request.EquipmentCreateRequest;
import com.twiiiins.dto.request.EquipmentUpdateRequest;
import com.twiiiins.entity.Equipment;
import com.twiiiins.exception.ResourceNotFoundException;
import com.twiiiins.mapper.EquipmentMapper;
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
    private final EquipmentMapper equipmentMapper;
    
    public List<EquipmentDto> getAllEquipment() {
        return equipmentRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(equipmentMapper::toDto)
                .toList();
    }
    
    public List<EquipmentDto> getEquipmentWithFilters(String name) {
        return equipmentRepository.findEquipmentWithFilters(name)
                .stream()
                .map(equipmentMapper::toDto)
                .toList();
    }
    
    public EquipmentDto getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
        return equipmentMapper.toDto(equipment);
    }
    
    public EquipmentDto createEquipment(EquipmentCreateRequest request) {
        Equipment equipment = equipmentMapper.toEntity(request);
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(savedEquipment);
    }
    
    public EquipmentDto updateEquipment(Long id, EquipmentUpdateRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found with id: " + id));
        
        equipmentMapper.updateEntityFromUpdateRequest(request, equipment);
        
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(savedEquipment);
    }
    
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }
}
