package site.balpyo.script.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import site.balpyo.ai.entity.AIGenerateLogEntity;
import site.balpyo.auth.entity.Role;
import site.balpyo.auth.entity.User;
import site.balpyo.voice.entity.VoiceEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "script")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ScriptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scriptId;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String script;

    private String title;

    private Integer secTime;

    private String voiceFilePath;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isGenerating;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tags_scripts",
            joinColumns = @JoinColumn(name = "script_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToOne(mappedBy = "scriptEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private AIGenerateLogEntity aiGenerateLog;

    @OneToMany(mappedBy = "scriptEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoiceEntity> voiceEntities = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
