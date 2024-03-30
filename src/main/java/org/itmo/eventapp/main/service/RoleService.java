package org.itmo.eventapp.main.service;

import lombok.RequiredArgsConstructor;
import org.itmo.eventapp.main.exception.NotFoundException;
import org.itmo.eventapp.main.model.dto.response.RoleResponse;
import org.itmo.eventapp.main.model.entity.Role;
import org.itmo.eventapp.main.model.entity.enums.RoleType;
import org.itmo.eventapp.main.repository.RoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public void deleteRole(Integer id) {
        if (!roleRepository.existsById(id))
            throw new NotFoundException(String.format("Role with id %d doesn't exist", id));
        // TODO: Add checks
        roleRepository.deleteById(id);
    }

    public List<RoleResponse> getAll() {
        var roles = roleRepository.findAll();
        return roles.stream().map(role -> RoleResponse.builder()
                .name(role.getName())
                .description(role.getDescription())
                .id(role.getId())
                .build()).toList();
    }

    public List<RoleResponse> getOrganizational() {
        return roleRepository.findAllByType(RoleType.EVENT);
    }

    public List<RoleResponse> searchByName(String name) {
        return roleRepository.findByNameContainingIgnoreCase(name);
    }

    private Role findByName(String name) {
        return roleRepository.findByName(name);
    }

    public Role getReaderRole() {
        return findByName("Читатель");
    }

    public Role getAdminRole() {
        return findByName("Администратор");
    }

    public Role getAssistantRole() {
        return findByName("Помощник");
    }

    public Role getOrganizerRole() {
        return findByName("Организатор");
    }
}
