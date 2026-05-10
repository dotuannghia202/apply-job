    @GetMapping("/skills")
    @ApiMessage("Fetch all skills")
    public ResponseEntity<ResultPaginationDTO> getAllSkills(
            @Filter Specification<Skill> spec,
            Pageable pageable,
            @RequestParam(required = false) String name
    ) {
        ResultPaginationDTO result = this.skillService.handleGetAllSkills(spec, pageable, name);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
