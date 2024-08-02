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
import site.balpyo.script.entity.ETag;
import site.balpyo.script.entity.ScriptEntity;
import site.balpyo.script.entity.Tag;
import site.balpyo.script.repository.ScriptRepository;
import site.balpyo.script.repository.TagRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScriptService {

    private final ScriptRepository scriptRepository;
    private final GuestRepository guestRepository;
    private final AIGenerateLogRepository aiGenerateLogRepository;
    private final GPTInfoRepository gptInfoRepository;
    private final AuthenticationService authenticationService;
    private final TagRepository tagRepository;

    public ResponseEntity<CommonResponse> saveEmptyScript(ScriptRequest scriptRequest) {

        User user = authenticationService.authenticationToUser();

        Set<Tag> tags = new HashSet<>();

        List<String> reqTags = scriptRequest.getTag() != null ? scriptRequest.getTag() : new ArrayList<>();
        for (String tag : reqTags) {
            ETag eTag = ETag.valueOf(tag);
            switch (eTag) {
                case NOTE:
                    tags.add(tagRepository.findByTag(ETag.NOTE).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.NOTE)));
                    break;
                case TIME:
                    tags.add(tagRepository.findByTag(ETag.TIME).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.TIME)));
                    break;
                case SCRIPT:
                    tags.add(tagRepository.findByTag(ETag.SCRIPT).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.SCRIPT)));
                    break;
                case FLOW:
                    tags.add(tagRepository.findByTag(ETag.FLOW).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.FLOW)));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tag: " + tag);
            }
        }



        ScriptEntity scriptEntity = ScriptEntity.builder()
                .title(scriptRequest.getTitle())
                .script(scriptRequest.getScript())
                .secTime(scriptRequest.getSecTime())
                .user(user)
                .isGenerating(scriptRequest.isUseAi())
                .tags(tags)
                .build();


        ScriptEntity insertedScriptEntity = scriptRepository.save(scriptEntity);


        ScriptResponse scriptResponse = ScriptResponse.builder()
                .scriptId(insertedScriptEntity.getScriptId())
                .isGenerating(insertedScriptEntity.getIsGenerating())
                .tag(insertedScriptEntity.getTags().stream()
                        .map(tag -> tag.getTag().toString())
                        .collect(Collectors.toSet()))
                .build();

        System.out.println(insertedScriptEntity.getTags().toString());

        return CommonResponse.success(scriptResponse);
    }

    public ResponseEntity<CommonResponse> getAllScript(String uid) {
        User user = authenticationService.authenticationToUser();

        List<ScriptEntity> scriptEntities = scriptRepository.findAllByUserAndIsGeneratingIsFalse(user);


        List<ScriptResponse> scriptResponses = new ArrayList<>();



        for(ScriptEntity scriptEntity: scriptEntities){

            System.out.println("TAGS : "+scriptEntity.getTags());
            ScriptResponse scriptResponse = ScriptResponse.builder()
                    .scriptId(scriptEntity.getScriptId())
                    .uid(uid)
                    .title(scriptEntity.getTitle())
                    .secTime(scriptEntity.getSecTime())
                    .voiceFilePath(scriptEntity.getVoiceFilePath())
                    .isGenerating(scriptEntity.getIsGenerating())
                    .script(scriptEntity.getScript())
                    .tag(scriptEntity.getTags().stream()
                            .map(tag -> tag.getTag().toString())
                            .collect(Collectors.toSet())
                    )
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

        Set<Tag> tags = new HashSet<>();

        for (String tag : scriptRequest.getTag()) {
            ETag eTag = ETag.valueOf(tag);
            switch (eTag) {
                case NOTE:
                    tags.add(tagRepository.findByTag(ETag.NOTE).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.NOTE)));
                    break;
                case TIME:
                    tags.add(tagRepository.findByTag(ETag.TIME).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.TIME)));
                    break;
                case SCRIPT:
                    tags.add(tagRepository.findByTag(ETag.SCRIPT).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.SCRIPT)));
                    break;
                case FLOW:
                    tags.add(tagRepository.findByTag(ETag.FLOW).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + ETag.FLOW)));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown tag: " + tag);
            }
        }
        scriptEntity.setScript(scriptRequest.getScript());
        scriptEntity.setTitle(scriptRequest.getTitle());
        scriptEntity.setSecTime(scriptRequest.getSecTime());

        Set<Tag> existedtags = scriptEntity.getTags();

        for(Tag tag : existedtags){
            tags.add(tag);
        }

        scriptEntity.setTags(tags);


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