    public ResultPaginationDTO handleGetAllSkills(Specification<Skill> spec, Pageable pageable, String name) {

        // Thêm điều kiện lọc theo name nếu có
        if (name != null && !name.trim().isEmpty()) {
            Specification<Skill> nameSpec = (root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
            spec = spec != null ? spec.and(nameSpec) : nameSpec;
        }

        Page<Skill> skillPage = this.skillRepository.findAll(spec, pageable);

        ResultPaginationDTO resultPaginationDTO = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(skillPage.getNumber() + 1);
        meta.setPageSize(skillPage.getSize());
        meta.setPages(skillPage.getTotalPages());
        meta.setTotal(skillPage.getTotalElements());

        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(skillPage.getContent());

        return resultPaginationDTO;

    }
