package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role handleCreateRole(Role role) {
        if (role.getName() != null && this.roleRepository.existsByName(role.getName())) {
            throw new NameExistedException("Role name is unique!");
        }
        return this.roleRepository.save(role);
    }

    public Role handleUpdateRole(long id, Role role) throws IdInvalidException {
        Role currentRole = this.roleRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Role id not found"));

        if (role.getName() != null) {
            boolean isNameChanged = !role.getName().equals(currentRole.getName());
            if (isNameChanged && this.roleRepository.existsByName(role.getName())) {
                throw new NameExistedException(role.getName().toString() + " is existed!");
            }
            currentRole.setName(role.getName());
        }

        return this.roleRepository.save(currentRole);
    }

    public Role handleGetRoleById(long id) throws IdInvalidException {
        return this.roleRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Role id not found!"));
    }

    public ResultPaginationDTO handleGetAllRoles(Specification<Role> spec, Pageable pageable) {
        Page<Role> rolePage = this.roleRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(rolePage.getNumber() + 1);
        meta.setPageSize(rolePage.getSize());
        meta.setPages(rolePage.getTotalPages());
        meta.setTotal(rolePage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(rolePage.getContent());

        return resultPaginationDTO;
    }

    public void handleDeleteRole(long id) throws IdInvalidException {
        Role currentRole = this.roleRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Role id not found!"));
        this.roleRepository.delete(currentRole);
    }
}
