package com.dtn.apply_job.domain;

import com.dtn.apply_job.util.constant.enums.ERole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    // Tên Role phải là duy nhất (UNIQUE) và không được để trống
    @Enumerated(EnumType.STRING)
    @Column(length = 50, unique = true, nullable = false)
    ERole name;

    // Ràng buộc: Không cần khai báo list User ở đây để tránh vòng lặp JSON vô tận
}
