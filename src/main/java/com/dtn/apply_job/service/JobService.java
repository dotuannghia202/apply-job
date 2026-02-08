package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.Job;
import com.dtn.apply_job.domain.Skill;
import com.dtn.apply_job.domain.request.job.ReqUpdateJobDTO;
import com.dtn.apply_job.domain.response.job.ResCreateJobDTO;
import com.dtn.apply_job.domain.response.job.ResJobDTO;
import com.dtn.apply_job.domain.response.job.ResUpdateJobDTO;
import com.dtn.apply_job.domain.response.user.ResultPaginationDTO;
import com.dtn.apply_job.exception.IdInvalidException;
import com.dtn.apply_job.repository.JobRepository;
import com.dtn.apply_job.repository.SkillRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Service
public class JobService {
    private JobRepository jobRepository;
    private SkillRepository skillRepository;

    public JobService(JobRepository jobRepository, SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
    }

    public ResCreateJobDTO handleCreateJob(Job job) {
        if (job.getSkills() != null) {
            List<Long> reqSkills = job.getSkills()
                    .stream().map(skill -> skill.getId())
                    .collect(Collectors.toList());

            List<Skill> dbSkills = this.skillRepository.findByIdIn(reqSkills);
            job.setSkills(dbSkills);
        }

        Job currentJob = this.jobRepository.save(job);

        ResCreateJobDTO dto = new ResCreateJobDTO();
        dto.setId(currentJob.getId());
        dto.setName(currentJob.getName());
        dto.setLocation(currentJob.getLocation());
        dto.setSalary(currentJob.getSalary());
        dto.setLevel(currentJob.getLevel());
        dto.setStartDate(currentJob.getStartDate());
        dto.setEndDate(currentJob.getEndDate());
        dto.setActive(currentJob.getActive());
        dto.setCreatedAt(currentJob.getCreatedAt());
        dto.setCreatedBy(currentJob.getCreatedBy());

        if (currentJob.getSkills() != null) {
            dto.setSkills(currentJob.getSkills()
                    .stream().map(skill -> skill
                            .getName().toString())
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public ResUpdateJobDTO handleUpdateJob(long id, ReqUpdateJobDTO req) throws IdInvalidException {
        Job job = jobRepository.findById(id)
                .orElseThrow(() ->
                        new IdInvalidException("Job id = " + id + " not found"));

        job.setName(req.getName());
        job.setLocation(req.getLocation());
        job.setSalary(req.getSalary());
        job.setLevel(req.getLevel());
        job.setDescription(req.getDescription());
        job.setStartDate(req.getStartDate());
        job.setEndDate(req.getEndDate());
        job.setActive(req.getActive());

        List<Skill> skills = this.skillRepository.findByIdIn(req.getSkillIds());
        job.setSkills(skills);

        this.jobRepository.save(job);

        ResUpdateJobDTO dto = new ResUpdateJobDTO();
        dto.setId(job.getId());
        dto.setName(job.getName());
        dto.setLocation(job.getLocation());
        dto.setSalary(job.getSalary());
        dto.setLevel(job.getLevel());
        dto.setDescription(job.getDescription());
        dto.setStartDate(job.getStartDate());
        dto.setEndDate(job.getEndDate());
        dto.setActive(job.getActive());
        dto.setUpdatedAt(job.getUpdatedAt());
        dto.setUpdatedBy(job.getUpdatedBy());

        if (job.getSkills() != null) {
            dto.setSkills(job.getSkills().stream()
                    .map(s -> s.getName()
                            .toString()).collect(Collectors.toList()));
        }

        return dto;
    }

    public ResultPaginationDTO handleGetAllJobs(Specification<Job> spec, Pageable pageable) {
        Page<Job> pageJob = this.jobRepository.findAll(spec, pageable);

        List<ResJobDTO> results = new ArrayList<>(pageJob.getContent().size());
        for (Job job : pageJob.getContent()) {
            ResJobDTO jobDTO = new ResJobDTO();
            jobDTO.setId(job.getId());
            jobDTO.setName(job.getName());
            jobDTO.setLocation(job.getLocation());
            jobDTO.setSalary(job.getSalary());
            jobDTO.setQuantity(job.getQuantity());
            jobDTO.setLevel(job.getLevel());
            jobDTO.setStartDate(job.getStartDate());
            jobDTO.setEndDate(job.getEndDate());
            jobDTO.setActive(job.getActive());
            jobDTO.setDescription(job.getDescription());
            jobDTO.setCreatedAt(job.getCreatedAt());
            jobDTO.setUpdatedAt(job.getUpdatedAt());
            jobDTO.setUpdatedBy(job.getUpdatedBy());
            jobDTO.setCreatedAt(job.getCreatedAt());
            jobDTO.setCreatedBy(job.getCreatedBy());

            if (job.getSkills() != null) {
                jobDTO.setSkills(job.getSkills().stream()
                        .map(skill -> skill.getName().toString())
                        .collect(Collectors.toList()));

            }

            results.add(jobDTO);

        }

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();

        meta.setPage(pageJob.getNumber() + 1);
        meta.setPageSize(pageJob.getSize());
        meta.setPages(pageJob.getTotalPages());
        meta.setTotal(pageJob.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(results);

        return resultPaginationDTO;
    }

    public ResJobDTO handleGetJobById(long id) throws IdInvalidException {
        Job job = this.jobRepository.findById(id)
                .orElseThrow(() ->
                        new IdInvalidException("Job id = " + id + " not found"));
        ResJobDTO jobDTO = new ResJobDTO();
        jobDTO.setId(job.getId());
        jobDTO.setName(job.getName());
        jobDTO.setLocation(job.getLocation());
        jobDTO.setSalary(job.getSalary());
        jobDTO.setQuantity(job.getQuantity());
        jobDTO.setLevel(job.getLevel());
        jobDTO.setStartDate(job.getStartDate());
        jobDTO.setEndDate(job.getEndDate());
        jobDTO.setActive(job.getActive());
        jobDTO.setDescription(job.getDescription());
        jobDTO.setCreatedAt(job.getCreatedAt());
        jobDTO.setUpdatedAt(job.getUpdatedAt());
        jobDTO.setUpdatedBy(job.getUpdatedBy());
        jobDTO.setCreatedAt(job.getCreatedAt());
        jobDTO.setCreatedBy(job.getCreatedBy());

        if (job.getSkills() != null) {
            jobDTO.setSkills(job.getSkills().stream()
                    .map(skill -> skill.getName().toString())
                    .collect(Collectors.toList()));

        }
        return jobDTO;
    }

    public void handleDeleteJob(long id) {
        this.jobRepository.deleteById(id);
        return;
    }
}
