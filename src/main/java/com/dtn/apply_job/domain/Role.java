package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.ERole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tên Role phải là duy nhất (UNIQUE) và không được để trống
    @Enumerated(EnumType.STRING)
    @Column(length = 50, unique = true, nullable = false)
    private ERole name;

    // Ràng buộc: Không cần khai báo list User ở đây để tránh vòng lặp JSON vô tận
}
