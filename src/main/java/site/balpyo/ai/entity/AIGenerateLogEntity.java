package site.balpyo.ai.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import site.balpyo.ai.dto.AIGenerateRequest;
import site.balpyo.auth.entity.User;
import site.balpyo.guest.entity.GuestEntity;
import site.balpyo.script.entity.ScriptEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ai_generate_log")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIGenerateLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiLogId;

    private Integer secTime;

    private String topic;

    private String keywords;

    private double secPerLetter;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "gpt_info_id")
    private GPTInfoEntity gptInfoEntity;


    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "script_id")
    private ScriptEntity scriptEntity;

    public AIGenerateLogEntity convertToEntity(AIGenerateRequest aiGenerateRequest, GPTInfoEntity gptInfoEntity,ScriptEntity scriptEntity){
        return AIGenerateLogEntity.builder()
                .scriptEntity(scriptEntity)
                .secTime(aiGenerateRequest.getSecTime())
                .topic(aiGenerateRequest.getTopic())
                .keywords(aiGenerateRequest.getKeywords())
                .secPerLetter(0) // TODO :: 차후 0 값 변경
                .gptInfoEntity(gptInfoEntity)
                .build();
    }
}
