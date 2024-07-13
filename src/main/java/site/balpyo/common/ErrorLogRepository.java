package site.balpyo.common;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import site.balpyo.common.entity.ErrorLogEntity;

import java.util.List;

@Repository
public interface ErrorLogRepository extends JpaRepository<ErrorLogEntity, Long> {

    @Query(value = "SELECT * FROM error_log_entity ORDER BY created_at DESC LIMIT 100", nativeQuery = true)
    List<ErrorLogEntity> findTop100ByOrderByCreatedAtDesc();
}
