package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.resume.ReqCreateResumeDTO;
import com.dtn.apply_job.domain.request.resume.ReqUpdateResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResUpdateResumeDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.mapper.ResumeMapper;
import com.dtn.apply_job.repository.ResumeRepository;
import com.dtn.apply_job.repository.SkillRepository;
import com.dtn.apply_job.repository.SpecializationRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SpecializationRepository specializationRepository;
    private final AiPythonService aiPythonService;
    private final ResumeMapper resumeMapper;

    @Transactional
    public ResResumeDTO handleCreateResume(ReqCreateResumeDTO req) throws IdInvalidException {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please login to create a CV!"));
        User currentUser = userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new IdInvalidException("Tài khoản không tồn tại!");
        }

        Resume resume = resumeMapper.toResume(req);
        resume.setCandidate(currentUser);

        if (req.getSkillIds() != null && !req.getSkillIds().isEmpty()) {
            List<Skill> skills = this.skillRepository.findByIdIn(req.getSkillIds());
            if (skills.size() != req.getSkillIds().size()) {
                throw new IdInvalidException("Một số mã kỹ năng không hợp lệ");
            }
            resume.setSkills(skills);
        } else {
            resume.setSkills(Collections.emptyList());
        }

        if (req.getSpecializationId() != null) {
            Specialization specialization = this.specializationRepository.findById(req.getSpecializationId())
                    .orElseThrow(() -> new IdInvalidException("Specialization id not found"));
            resume.setSpecialization(specialization);
        } else {
            resume.setSpecialization(null);
        }

        Resume savedResume = this.resumeRepository.save(resume);
        aiPythonService.processCvTextAsync(savedResume.getId(), savedResume.getFileUrl());
        return resumeMapper.toResResumeDTO(savedResume);
    }

    public ResUpdateResumeDTO handleUpdateResume(UUID id, ReqUpdateResumeDTO reqDTO) throws IdInvalidException {
        Resume currentResume = getResumeAndCheckPermission(id);
        boolean fileUrlChanged = reqDTO.getFileUrl() != null && !reqDTO.getFileUrl().equals(currentResume.getFileUrl());

        resumeMapper.updateResumeFromDTO(reqDTO, currentResume);

        if (fileUrlChanged) {
            aiPythonService.processCvTextAsync(id, reqDTO.getFileUrl());
        }

        if (reqDTO.getSkillIds() != null) {
            if (reqDTO.getSkillIds().isEmpty()) {
                currentResume.setSkills(Collections.emptyList());
            } else {
                List<Skill> skills = this.skillRepository.findByIdIn(reqDTO.getSkillIds());
                if (skills.size() != reqDTO.getSkillIds().size()) {
                    throw new IdInvalidException("Một số mã kỹ năng không hợp lệ");
                }
                currentResume.setSkills(skills);
            }
        }

        if (reqDTO.getSpecializationId() != null) {
            Specialization specialization = this.specializationRepository.findById(reqDTO.getSpecializationId())
                    .orElseThrow(() -> new IdInvalidException("Specialization id not found"));
            currentResume.setSpecialization(specialization);
        }

        currentResume.setUpdatedAt(Instant.now());

        Resume updatedResume = resumeRepository.save(currentResume);
        return resumeMapper.toResUpdateResumeDTO(updatedResume);
    }

    @Transactional
    public void handleSoftDeleteResume(UUID id) throws IdInvalidException {
        Resume currentResume = getResumeAndCheckPermission(id);
        currentResume.setActive(false);
        resumeRepository.save(currentResume);
    }

    public ResResumeDTO handleGetResumeById(UUID id) throws Exception {
        Resume resume = getResumeAndCheckPermission(id);
        return resumeMapper.toResResumeDTO(resume);
    }

    public ResultPaginationDTO handleGetAllResumes(Specification<Resume> spec, Pageable pageable) {
        Page<Resume> resumePage = this.resumeRepository.findAll(spec, pageable);

        List<ResResumeDTO> results = resumePage.getContent().stream()
                .map(resumeMapper::toResResumeDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(resumePage.getNumber() + 1);
        meta.setPageSize(resumePage.getSize());
        meta.setPages(resumePage.getTotalPages());
        meta.setTotal(resumePage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(results);

        return resultPaginationDTO;
    }

    public List<ResResumeDTO> handleGetMyResumes() throws IdInvalidException {
        String email = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to view your CV!"));

        User currentUser = this.userRepository.findByEmail(email);
        if (currentUser == null) {
            throw new IdInvalidException("Tài khoản không tồn tại! Vui lòng đăng ký trước!");
        }

        List<Resume> myResumes = this.resumeRepository.findByCandidateAndActiveTrue(currentUser);
        return myResumes.stream()
                .map(resumeMapper::toResResumeDTO)
                .collect(Collectors.toList());
    }

    private Resume getResumeAndCheckPermission(UUID resumeId) throws IdInvalidException {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IdInvalidException("CV doesn't exist!"));

        String currentUserEmail = SecurityUtil.getCurrentUser()
                .orElseThrow(() -> new IdInvalidException("Please log in to view this CV!"));
        User currentUser = userRepository.findByEmail(currentUserEmail);
        if (currentUser == null) {
            throw new IdInvalidException("Tài khoản không tồn tại! Vui lòng đăng ký trước!");
        }

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().name().equals("ADMIN"));

        if (!isAdmin && !resume.getCandidate().getEmail().equals(currentUserEmail)) {
            throw new IdInvalidException("Bạn không có quyền xem CV này! Chỉ chủ sở hữu mới có quyền xem!");
        }

        return resume;
    }

    @Transactional
    public ResResumeDTO handleSetDefaultResume(UUID targetResumeId) throws IdInvalidException {
        
        Resume targetResume = getResumeAndCheckPermission(targetResumeId);
        User candidate = targetResume.getCandidate();

        
        List<Resume> allMyResumes = resumeRepository.findByCandidate(candidate);

        
        for (Resume r : allMyResumes) {
            if (r.getId().equals(targetResumeId)) {
                r.setDefault(true);  
            } else {
                r.setDefault(false); 
            }
        }

        
        resumeRepository.saveAll(allMyResumes);

        return resumeMapper.toResResumeDTO(targetResume);
    }
}
