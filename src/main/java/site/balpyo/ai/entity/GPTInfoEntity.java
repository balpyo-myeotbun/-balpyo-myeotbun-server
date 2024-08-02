package site.balpyo.ai.entity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import site.balpyo.ai.dto.GPTResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "gpt_info_entity")
@Builder
@AllArgsConstructor
public class GPTInfoEntity {

    @Id
    private String gptInfoId;

    private String gptObject;

    private String gptModel;

    private Integer gptCreatedAt;

    private Integer promptToken;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String gptGeneratedScript;

    private Integer completionToken;

    private Integer totalToken;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "gptInfoEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AIGenerateLogEntity> aiGenerateLogs = new ArrayList<>();

    public GPTInfoEntity ResponseBodyToGPTInfoEntity(Object resultBody){

        ObjectMapper mapper = new ObjectMapper();
        GPTResponse response = mapper.convertValue(resultBody, GPTResponse.class);

        return GPTInfoEntity.builder()
                .gptInfoId(response.getGptInfoId())
                .gptObject(response.getGptObject())
                .gptModel(response.getGptModel())
                .gptCreatedAt(response.getGptCreatedAt())
                .promptToken(response.getUsage().getPromptToken())
                .gptGeneratedScript(response.getGptGeneratedScript().get(0).getMessage().getContent().toString())
                .completionToken(response.getUsage().getCompletionToken())
                .totalToken(response.getUsage().getTotalToken())
                .build();
    }
}
