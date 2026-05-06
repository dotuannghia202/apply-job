package com.dtn.apply_job.service;

import com.dtn.apply_job.common.response.ResultPaginationDTO;
import com.dtn.apply_job.domain.Company;
import com.dtn.apply_job.domain.User;
import com.dtn.apply_job.domain.request.company.ReqCreateCompanyDTO;
import com.dtn.apply_job.domain.response.company.ResCreateCompanyDTO;
import com.dtn.apply_job.repository.CompanyRepository;
import com.dtn.apply_job.repository.UserRepository;
import com.dtn.apply_job.security.SecurityUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Setter
@Getter
@Service
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(CompanyRepository companyRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResCreateCompanyDTO handleCreateCompany(ReqCreateCompanyDTO reqDTO) {
        // 1. Lưu company vào DB
        Company company = new Company();
        company.setName(reqDTO.getName());
        company.setDescription(reqDTO.getDescription());
        company.setAddress(reqDTO.getAddress());
        company.setLogo(reqDTO.getLogo());

        Company savedCompany = this.companyRepository.save(company);

        // 2. Lấy User đang đăng nhập (qua email trong SecurityContext)
        String currentUserEmail = SecurityUtil.getCurrentUser().orElse("");
        if (!currentUserEmail.isBlank()) {
            User currentUser = this.userRepository.findByEmail(currentUserEmail);
            if (currentUser != null) {
                // 3. Gán company vừa tạo cho user hiện tại
                currentUser.setCompany(savedCompany);
                // 4. Lưu user vào DB
                this.userRepository.save(currentUser);
            }
        }

        // 5. Map sang DTO trả về
        ResCreateCompanyDTO res = new ResCreateCompanyDTO();
        res.setId(savedCompany.getId());
        res.setName(savedCompany.getName());
        res.setDescription(savedCompany.getDescription());
        res.setAddress(savedCompany.getAddress());
        res.setLogo(savedCompany.getLogo());
        res.setCreatedAt(savedCompany.getCreatedAt());
        res.setCreatedBy(savedCompany.getCreatedBy());
        return res;
    }

    public List<Company> handleCreateCompanies(List<Company> companies) {
        if (companies == null || companies.isEmpty()) {
            return List.of();
        }
        return this.companyRepository.saveAll(companies);
    }

    public ResultPaginationDTO handleGetAllCompany(Specification spec, Pageable pageable) {

        Page<Company> companyPage = this.companyRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(companyPage.getNumber() + 1);
        meta.setPageSize(companyPage.getSize());
        meta.setPages(companyPage.getTotalPages());
        meta.setTotal(companyPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(companyPage.getContent());

        return resultPaginationDTO;
    }

    public Company handleUpdateCompany(long id, Company company) {
        Optional<Company> optionalCompany = this.companyRepository.findById(id);
        if (optionalCompany.isPresent()) {
            Company updatedCompany = optionalCompany.get();
            updatedCompany.setName(company.getName());
            updatedCompany.setAddress(company.getAddress());
            updatedCompany.setLogo(company.getLogo());
            updatedCompany.setDescription(company.getDescription());
            return this.companyRepository.save(updatedCompany);
        }
        return null;
    }

    public void handleDeleteCompany(long id) {
        Optional<Company> optionalCompany = this.companyRepository.findById(id);
        if (optionalCompany.isPresent()) {
            Company company = optionalCompany.get();
            Optional<List<User>> optionalUsers = this.userRepository.findByCompany(company);
            if (optionalUsers.isPresent()) {
                List<User> users = optionalUsers.get();
                this.userRepository.deleteAll(users);
            }
        }
        this.companyRepository.deleteById(id);
    }

    public Optional<Company> handleGetCompanyById(long id) {
        return this.companyRepository.findById(id);
    }
}
