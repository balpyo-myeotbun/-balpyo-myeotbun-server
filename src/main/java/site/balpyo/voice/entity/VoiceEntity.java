package site.balpyo.voice.entity;

import jakarta.persistence.*;
import lombok.*;
import site.balpyo.script.entity.ScriptEntity;

@Getter
@Setter
@Entity
@Table(name = "voice")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long voiceId;

    @Column
    private String filePath;

    @Column
    private Integer playTime;

    @Column
    private String speechMark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "script_id")
    private ScriptEntity scriptEntity;
}
