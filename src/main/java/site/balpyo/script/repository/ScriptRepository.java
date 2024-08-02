package site.balpyo.script.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import site.balpyo.auth.entity.User;
import site.balpyo.script.entity.ScriptEntity;

import java.util.List;
import java.util.Optional;

public interface ScriptRepository extends JpaRepository<ScriptEntity, Long> {

    List<ScriptEntity> findAllByUserAndIsGeneratingIsFalse(User user);
    List<ScriptEntity> findAllByUserId(Long id);

    Optional<ScriptEntity> findByUserAndScriptId(User user,Long scriptId);

}
