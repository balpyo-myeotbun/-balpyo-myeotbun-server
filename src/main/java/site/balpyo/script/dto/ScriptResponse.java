package site.balpyo.script.dto;

import lombok.*;
import site.balpyo.script.entity.Tag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScriptResponse {

        private Long scriptId;
        private String script;
        private String gptId;
        private String uid;
        private String title;
        private Integer secTime;
        private String voiceFilePath;
        private boolean isGenerating;
        private boolean useAi;
        private Set<String> tag;

}
