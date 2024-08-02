package site.balpyo.script.service;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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
public class ScriptServiceDeprecated {

    private final ScriptRepository scriptRepository;
    private final GuestRepository guestRepository;
    private final AIGenerateLogRepository aiGenerateLogRepository;
    private final GPTInfoRepository gptInfoRepository;

    private final AuthenticationService authenticationService;


    public ResponseEntity<CommonResponse> saveScript(ScriptRequest scriptRequest) {

        GuestEntity guestEntity = null;
        User user = authenticationService.authenticationToUser();

        GPTInfoEntity gptInfoEntity = null;
        if (scriptRequest.getGptId() != null) {
            gptInfoEntity = gptInfoRepository.findById(scriptRequest.getGptId()).orElse(null);
        }

        AIGenerateLogEntity aiGenerateLogEntity = null;
        if (gptInfoEntity != null) {
            Optional<AIGenerateLogEntity> aiGenerateLogEntityOptional = aiGenerateLogRepository.findByGptInfoEntity(gptInfoEntity);
            aiGenerateLogEntity = aiGenerateLogEntityOptional.orElse(null);
            System.out.println(aiGenerateLogEntity.getTopic());
        }

        ScriptEntity scriptEntity = ScriptEntity.builder()
                .title(scriptRequest.getTitle())
                .script(scriptRequest.getScript())
                .secTime(scriptRequest.getSecTime())
                .user(user)
                .aiGenerateLog(aiGenerateLogEntity)
                .voiceFilePath(scriptRequest.getVoiceFilePath())
                .build();


        ScriptEntity insertedScript = scriptRepository.save(scriptEntity);


        return CommonResponse.success(insertedScript);
    }
/*
    public ResponseEntity<CommonResponse> getAllScript(String uid) {
        Optional<GuestEntity> guestEntity = guestRepository.findById(uid);

        if(guestEntity.isEmpty())return CommonResponse.error(ErrorEnum.GUEST_NOT_FOUND);
        List<ScriptResponse> scriptResponses = new ArrayList<>();
        if(guestEntity.get().getScriptEntities().isEmpty())return CommonResponse.success(scriptResponses);

        List<ScriptEntity> scriptEntities = guestEntity.get().getScriptEntities();


        for(ScriptEntity scriptEntity: scriptEntities){
              ScriptResponse scriptResponse = ScriptResponse.builder()
                .scriptId(scriptEntity.getScript_id())
                .uid(uid)
                .title(scriptEntity.getTitle())
                .secTime(scriptEntity.getSecTime())
                      .voiceFilePath(scriptEntity.getVoiceFilePath())
                      .isGenerating(scriptEntity.getIsGenerating())
                .build();

        scriptResponses.add(scriptResponse);
    }

        return CommonResponse.success(scriptResponses);

    }
    */


    public ResponseEntity<CommonResponse> getDetailScript(Long scriptId) {

        User user = authenticationService.authenticationToUser();

        Optional<ScriptEntity> optionalScriptEntity = scriptRepository.findByUserAndScriptId(user,scriptId);

        if(optionalScriptEntity.isEmpty())return CommonResponse.error(ErrorEnum.SCRIPT_DETAIL_NOT_FOUND);

        ScriptEntity scriptEntity = optionalScriptEntity.get();

        ScriptResponse scriptResponse = ScriptResponse
                .builder()
                .scriptId(scriptEntity.getScriptId())
                .secTime(scriptEntity.getSecTime())
                .title(scriptEntity.getTitle())
                .script(scriptEntity.getScript())
                .voiceFilePath(scriptEntity.getVoiceFilePath())
                .build();

        return CommonResponse.success(scriptResponse);


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


    public ResponseEntity<CommonResponse> deleteScript(Long scriptId) {
        User user = authenticationService.authenticationToUser();

        Optional<ScriptEntity> optionalScriptEntity = scriptRepository.findByUserAndScriptId(user, scriptId);

        if(optionalScriptEntity.isEmpty())return CommonResponse.error(ErrorEnum.SCRIPT_DETAIL_NOT_FOUND);

        ScriptEntity scriptEntity = optionalScriptEntity.get();

        scriptRepository.delete(scriptEntity);

        return CommonResponse.success("");

    }


}
