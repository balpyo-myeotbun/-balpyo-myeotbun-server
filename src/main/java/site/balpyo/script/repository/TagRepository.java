package site.balpyo.script.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.balpyo.auth.entity.ERole;
import site.balpyo.auth.entity.Role;
import site.balpyo.script.entity.ETag;
import site.balpyo.script.entity.ScriptEntity;
import site.balpyo.script.entity.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTag(ETag tag);
}
