package com.dtn.apply_job.util.constant.enums;

public enum CompanyStatus {
    PENDING,    // Chờ phê duyệt (Mặc định khi vừa tạo)
    APPROVED,
    REJECTED,// Đã duyệt (Được phép đăng Job)
    SUSPENDED   // Bị đình chỉ (Nếu vi phạm sau khi đã duyệt)
}