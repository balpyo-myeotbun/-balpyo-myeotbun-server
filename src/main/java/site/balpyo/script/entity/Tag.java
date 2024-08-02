package site.balpyo.script.entity;

import jakarta.persistence.*;
import lombok.*;
import org.checkerframework.common.aliasing.qual.Unique;
import site.balpyo.auth.entity.ERole;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tags")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, unique = true)
    private ETag tag;

//    @ManyToMany(mappedBy = "tags")
//    private Set<ScriptEntity> scripts = new HashSet<>();

    public Tag(ETag tag) {
        this.tag = tag;
    }

}
