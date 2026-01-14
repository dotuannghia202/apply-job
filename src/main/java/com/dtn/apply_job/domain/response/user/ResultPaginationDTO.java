package com.dtn.apply_job.domain.response.user;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResultPaginationDTO {

    private Object result;
    private Meta meta;

    @Setter
    @Getter
    public static class Meta {
        private int page;
        private int pageSize;
        private int pages;
        private long total;
    }
}
