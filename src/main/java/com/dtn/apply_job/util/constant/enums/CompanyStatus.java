package com.dtn.apply_job.util.constant.enums;

public enum CompanyStatus {
    PENDING,    // Chờ phê duyệt (Mặc định khi vừa tạo)
    APPROVED,   // Đã duyệt (Được phép đăng Job)
    REJECTED,   // Bị từ chối
    SUSPENDED   // Bị đình chỉ (Nếu vi phạm sau khi đã duyệt)
}