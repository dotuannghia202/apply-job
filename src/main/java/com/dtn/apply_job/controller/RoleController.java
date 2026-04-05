package com.dtn.apply_job.controller;

import com.dtn.apply_job.common.annotation.ApiMessage;
import com.dtn.apply_job.domain.Role;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.exception.NameExistedException;
import com.dtn.apply_job.service.RoleService;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @ApiMessage("Create role")
    public ResponseEntity<Role> createRole(@RequestBody Role role) throws NameExistedException {
        Role newRole = this.roleService.handleCreateRole(role);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRole);
    }

    @PutMapping("/roles/{id}")
    @ApiMessage("Update role")
    public ResponseEntity<Role> updateRole(@PathVariable long id, @RequestBody Role role) throws IdInvalidException {
        Role updatedRole = this.roleService.handleUpdateRole(id, role);
        return ResponseEntity.status(HttpStatus.OK).body(updatedRole);
    }

    @GetMapping("/roles/{id}")
    @ApiMessage("Fetch role by Id")
    public ResponseEntity<Role> getRole(@PathVariable long id) throws IdInvalidException {
        Role role = this.roleService.handleGetRoleById(id);
        return ResponseEntity.status(HttpStatus.OK).body(role);
    }

    @GetMapping("/roles")
    @ApiMessage("Fetch all roles")
    public ResponseEntity<ResultPaginationDTO> getAllRoles(
            @Filter Specification<Role> spec,
            Pageable pageable
    ) {
        ResultPaginationDTO result = this.roleService.handleGetAllRoles(spec, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @DeleteMapping("/roles/{id}")
    @ApiMessage("Delete role")
    public ResponseEntity<Void> deleteRole(@PathVariable long id) throws IdInvalidException {
        this.roleService.handleDeleteRole(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
