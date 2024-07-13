package site.balpyo.common.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "error_log_entity")
@Builder
@AllArgsConstructor
public class ErrorLogEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Lob
        @Column(columnDefinition = "LONGTEXT")
        private String errorText;

        private String className;
        private String methodName;
        private Integer lineNumber;

        @CreationTimestamp
        private LocalDateTime createdAt;

}
