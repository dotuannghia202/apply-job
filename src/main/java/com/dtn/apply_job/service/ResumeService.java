package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Resume;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.Specialization;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.resume.ReqCreateResumeDTO;
import com.dtn.apply_job.domain.request.resume.ReqUpdateResumeDTO;
import com.dtn.apply_job.domain.response.resume.ResResumeDTO;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.ResumeRepository;
import com.dtn.apply_job.repository.SkillRepository;
import com.dtn.apply_job.repository.SpecializationRepository;
import com.dtn.apply_job.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final SpecializationRepository specializationRepository;

    public ResResumeDTO handleCreateResume(ReqCreateResumeDTO req) throws IdInvalidException {
        Resume resume = new Resume();
        applyRequestToEntity(resume, req.getCandidateId(), req.getFileName(), req.getFileUrl(), req.getParsedText(),
                req.getActive(), req.getSkillIds(), req.getSpecializationId());
        resume.setCreatedAt(LocalDateTime.now());

        Resume savedResume = this.resumeRepository.save(resume);
        return convertToResResumeDTO(savedResume);
    }

    public ResResumeDTO handleUpdateResume(long id, ReqUpdateResumeDTO req) throws IdInvalidException {
        Resume currentResume = this.resumeRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Resume id not found"));

        applyRequestToEntity(currentResume, req.getCandidateId(), req.getFileName(), req.getFileUrl(), req.getParsedText(),
                req.getActive(), req.getSkillIds(), req.getSpecializationId());
        currentResume.setUpdatedAt(LocalDateTime.now());

        Resume updatedResume = this.resumeRepository.save(currentResume);
        return convertToResResumeDTO(updatedResume);
    }

    public ResResumeDTO handleGetResumeById(long id) throws IdInvalidException {
        Resume resume = this.resumeRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Resume id not found!"));
        return convertToResResumeDTO(resume);
    }

    public ResultPaginationDTO handleGetAllResumes(Specification<Resume> spec, Pageable pageable) {
        Page<Resume> resumePage = this.resumeRepository.findAll(spec, pageable);

        List<ResResumeDTO> results = resumePage.getContent().stream()
                .map(this::convertToResResumeDTO)
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

    public void handleDeleteResume(long id) throws IdInvalidException {
        Resume currentResume = this.resumeRepository.findById(id)
                .orElseThrow(() -> new IdInvalidException("Resume id not found!"));
        this.resumeRepository.delete(currentResume);
    }

    private void applyRequestToEntity(
            Resume resume,
            Long candidateId,
            String fileName,
            String fileUrl,
            String parsedText,
            Boolean active,
            List<Long> skillIds,
            Long specializationId
    ) throws IdInvalidException {
        User candidate = this.userRepository.findById(candidateId)
                .orElseThrow(() -> new IdInvalidException("Candidate id not found"));

        resume.setCandidate(candidate);
        resume.setFileName(fileName);
        resume.setFileUrl(fileUrl);
        resume.setParsedText(parsedText);
        resume.setActive(Boolean.TRUE.equals(active));

        if (skillIds != null && !skillIds.isEmpty()) {
            List<Skill> skills = this.skillRepository.findByIdIn(skillIds);
            if (skills.size() != skillIds.size()) {
                throw new IdInvalidException("Some skill ids are invalid");
            }
            resume.setSkills(skills);
        } else {
            resume.setSkills(Collections.emptyList());
        }

        if (specializationId != null) {
            Specialization specialization = this.specializationRepository.findById(specializationId)
                    .orElseThrow(() -> new IdInvalidException("Specialization id not found"));
            resume.setSpecialization(specialization);
        } else {
            resume.setSpecialization(null);
        }
    }

    private ResResumeDTO convertToResResumeDTO(Resume resume) {
        ResResumeDTO dto = new ResResumeDTO();
        dto.setId(resume.getId());
        dto.setCandidateId(resume.getCandidate() != null ? resume.getCandidate().getId() : null);
        dto.setFileName(resume.getFileName());
        dto.setFileUrl(resume.getFileUrl());
        dto.setParsedText(resume.getParsedText());
        dto.setActive(resume.isActive());

        List<Long> skillIds = new ArrayList<>();
        if (resume.getSkills() != null) {
            skillIds = resume.getSkills().stream().map(Skill::getId).collect(Collectors.toList());
        }
        dto.setSkillIds(skillIds);

        dto.setSpecializationId(resume.getSpecialization() != null ? resume.getSpecialization().getId() : null);
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setUpdatedAt(resume.getUpdatedAt());
        return dto;
    }
}

