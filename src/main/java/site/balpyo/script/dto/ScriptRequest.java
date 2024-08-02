package site.balpyo.script.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptRequest {
    private String script;
    private String gptId;
    private String uid;
    private String title;
    private Integer secTime;
    private String voiceFilePath;
    private boolean useAi;
    private List<String> tag;
}
