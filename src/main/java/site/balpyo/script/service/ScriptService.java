package site.balpyo.script.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import site.balpyo.ai.dto.AIGenerateRequest;
import site.balpyo.ai.entity.AIGenerateLogEntity;
import site.balpyo.ai.entity.GPTInfoEntity;
import site.balpyo.ai.repository.AIGenerateLogRepository;
import site.balpyo.ai.repository.GPTInfoRepository;
import site.balpyo.auth.entity.User;
import site.balpyo.auth.service.AuthenticationService;
import site.balpyo.common.dto.CommonResponse;
import site.balpyo.common.dto.ErrorEnum;
import site.balpyo.guest.entity.GuestEntity;
import site.balpyo.guest.repository.GuestRepository;
import site.balpyo.script.dto.ScriptRequest;
import site.balpyo.script.dto.ScriptResponse;
import site.balpyo.script.entity.ScriptEntity;
import site.balpyo.script.repository.ScriptRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final GuestRepository guestRepository;
    private final AIGenerateLogRepository aiGenerateLogRepository;
    private final GPTInfoRepository gptInfoRepository;
    private final AuthenticationService authenticationService;


    public ResponseEntity<CommonResponse> saveEmptyScript(ScriptRequest scriptRequest) {

        User user = authenticationService.authenticationToUser();

        ScriptEntity scriptEntity = ScriptEntity.builder()
                .title(scriptRequest.getTitle())
                .script(scriptRequest.getScript())
                .secTime(scriptRequest.getSecTime())
                .user(user)
                .isGenerating(scriptRequest.isUseAi())
                .build();


        ScriptEntity insertedScriptEntity = scriptRepository.save(scriptEntity);
        ScriptResponse scriptResponse = ScriptResponse.builder()
                .scriptId(insertedScriptEntity.getScriptId())
                .isGenerating(insertedScriptEntity.getIsGenerating())
                .build();

        return CommonResponse.success(scriptResponse);
    }

    public ResponseEntity<CommonResponse> getAllScript(String uid) {
        User user = authenticationService.authenticationToUser();

        List<ScriptEntity> scriptEntities = scriptRepository.findAllByUserAndIsGeneratingIsFalse(user);


        List<ScriptResponse> scriptResponses = new ArrayList<>();

        for(ScriptEntity scriptEntity: scriptEntities){
            ScriptResponse scriptResponse = ScriptResponse.builder()
                    .scriptId(scriptEntity.getScriptId())
                    .uid(uid)
                    .title(scriptEntity.getTitle())
                    .secTime(scriptEntity.getSecTime())
                    .voiceFilePath(scriptEntity.getVoiceFilePath())
                    .isGenerating(scriptEntity.getIsGenerating())
                    .script(scriptEntity.getScript())
                    .build();

            scriptResponses.add(scriptResponse);
        }

        return CommonResponse.success(scriptResponses);

    }


    public ResponseEntity<CommonResponse> patchScript(ScriptRequest scriptRequest,Long scriptId) {
        User user = authenticationService.authenticationToUser();

        Optional<ScriptEntity> optionalScriptEntity = scriptRepository.findByUserAndScriptId(user, scriptId);

        if(optionalScriptEntity.isEmpty())return CommonResponse.error(ErrorEnum.SCRIPT_DETAIL_NOT_FOUND);

        ScriptEntity scriptEntity = optionalScriptEntity.get();


        scriptEntity.setScript(scriptRequest.getScript());
        scriptEntity.setTitle(scriptRequest.getTitle());
        scriptEntity.setSecTime(scriptRequest.getSecTime());



        if (scriptRequest.getVoiceFilePath() != null && !scriptRequest.getVoiceFilePath().isEmpty()) {
            scriptEntity.setVoiceFilePath(scriptRequest.getVoiceFilePath());
        }

        scriptRepository.save(scriptEntity);

        return CommonResponse.success("");


    }


    public ResponseEntity<CommonResponse> deleteScript(String uid, Long scriptId) {

        User user = authenticationService.authenticationToUser();

        Optional<ScriptEntity> optionalScriptEntity = scriptRepository.findByUserAndScriptId(user, scriptId);

        if(optionalScriptEntity.isEmpty())return CommonResponse.error(ErrorEnum.SCRIPT_DETAIL_NOT_FOUND);

        ScriptEntity scriptEntity = optionalScriptEntity.get();

        scriptRepository.delete(scriptEntity);

        return CommonResponse.success("");

    }
}