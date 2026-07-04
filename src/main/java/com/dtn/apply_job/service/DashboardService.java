package com.dtn.apply_job.service;

import com.dtn.apply_job.domain.response.admin.IndustryStatProjection;
import com.dtn.apply_job.domain.response.admin.ResAdminDashboardDTO;
import com.dtn.apply_job.repository.ApplicationRepository;
import com.dtn.apply_job.repository.CompanyRepository;
import com.dtn.apply_job.repository.JobRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.util.constant.enums.ERole;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public DashboardService(UserRepository userRepository, CompanyRepository companyRepository,
                            JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    public ResAdminDashboardDTO getAdminDashboardStats() {
        ResAdminDashboardDTO stats = new ResAdminDashboardDTO();

        
        long absoluteTotalUsers = userRepository.count();
        long totalAdmins = userRepository.countByRoles_Name(ERole.ADMIN);
        stats.setTotalUsers(absoluteTotalUsers - totalAdmins);

        
        stats.setTotalCandidates(userRepository.countByRoles_Name(ERole.CANDIDATE));
        stats.setTotalEmployers(userRepository.countByRoles_Name(ERole.EMPLOYER));

        
        stats.setTotalCompanies(companyRepository.count());
        stats.setTotalActiveJobs(jobRepository.countByActiveTrue());

        
        stats.setTotalApplications(applicationRepository.count());

        
        List<IndustryStatProjection> projections = jobRepository.getJobCountByIndustry();

        
        List<ResAdminDashboardDTO.IndustryStat> industryStats = projections.stream()
                .map(p -> new ResAdminDashboardDTO.IndustryStat(p.getIndustryName(), p.getJobCount()))
                .toList();

        stats.setIndustryStats(industryStats);
        return stats;

    }
}