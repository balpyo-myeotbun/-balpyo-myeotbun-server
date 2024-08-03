package site.balpyo.ai.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import site.balpyo.ai.dto.AIGenerateRequest;
import site.balpyo.ai.dto.AIGenerateResponse;
import site.balpyo.ai.entity.AIGenerateLogEntity;
import site.balpyo.ai.entity.GPTInfoEntity;
import site.balpyo.ai.repository.AIGenerateLogRepository;
import site.balpyo.auth.repository.UserRepository;
import site.balpyo.auth.service.AuthenticationService;
import site.balpyo.common.dto.CommonResponse;
import site.balpyo.common.dto.ErrorEnum;
import site.balpyo.common.util.CommonUtils;
import site.balpyo.fcm.FcmService;
import site.balpyo.fcm.dto.FcmSendDTO;
import site.balpyo.guest.entity.GuestEntity;
import site.balpyo.guest.repository.GuestRepository;
import site.balpyo.script.entity.ScriptEntity;
import site.balpyo.script.repository.ScriptRepository;
import site.balpyo.auth.entity.User;

import site.balpyo.auth.service.UserDetailsImpl;



import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import java.util.Objects;


@Service
@Slf4j  
@RequiredArgsConstructor
public class AIGenerateService {

    private final AIGenerateUtils aiGenerateUtils;

    private final AIGenerateLogRepository aiGenerateLogRepository;

    private final GuestRepository guestRepository;

    private final ScriptRepository scriptRepository;

    private final FcmService fcmService;

    private final UserRepository userRepository;

    private final AuthenticationService authenticationService;
    @Value("${secrets.GPT_API_KEY}")
    public String GPT_API_KEY;

    @Transactional
    public Mono<ResponseEntity<CommonResponse>> generateScript(AIGenerateRequest request){
        User user = authenticationService.authenticationToUser();
        // Test인 경우 테스트 값 반환
        if (request.isTest()) {
            return Mono.just(CommonResponse.success(new GPTTestObject().getGPTTestObject()));
        }

        // API Key가 없는 경우 에러 반환
        if (CommonUtils.isAnyParameterNullOrBlank(GPT_API_KEY)) {
            return Mono.just(CommonResponse.error(ErrorEnum.GPT_API_KEY_MISSING));
        }

        // 1. 주제, 소주제, 시간을 기반으로 프롬프트 생성
        String currentPromptString = aiGenerateUtils.createPromptString(request.getTopic(), request.getKeywords(), request.getSecTime());

        // 2. 작성된 프롬프트를 기반으로 GPT에게 대본 작성 요청
        Mono<Map> generatedScriptMono = aiGenerateUtils.requestScriptGeneration(currentPromptString, 0.5f, 100000, GPT_API_KEY);

        // 3. GPT 응답을 기반으로 대본 추출 및 대본이 없다면 대본 생성 실패 에러 반환
        return generatedScriptMono.flatMap(generatedScriptObject -> {
            Object resultScript = generatedScriptObject.get("choices");
            if (CommonUtils.isAnyParameterNullOrBlank(resultScript)) {
                log.info("[-] GPT 응답에서 대본을 추출하는 데 실패했습니다.");
                return Mono.just(CommonResponse.error(ErrorEnum.GPT_GENERATION_ERROR));
            }

            // 4. GPT 응답에서 Body 추출
            Object resultBody = generatedScriptObject;

            log.info("-------------------- resultBody");
            log.info(resultBody.toString());

            // 5. GPT 응답에서 GPTInfoEntity 추출 및 jpa로 저장할 수 있도록 GPTInfoEntity로 변환
            GPTInfoEntity gptInfoData = new GPTInfoEntity().ResponseBodyToGPTInfoEntity(resultBody);

            log.info("-------------------- GPT Info Data");
            log.info(gptInfoData.getGptGeneratedScript());
            

            // 6. 대본 저장
            Optional<ScriptEntity> optionalScriptEntity = scriptRepository.findByUserAndScriptId(user, request.getScriptId());

            if (optionalScriptEntity.isEmpty()) {
                log.info("[-] 대본을 찾을 수 없습니다.");
                return Mono.just(CommonResponse.error(ErrorEnum.SCRIPT_DETAIL_NOT_FOUND));
            }

            ScriptEntity scriptEntity = optionalScriptEntity.get();
            scriptEntity.setScript(gptInfoData.getGptGeneratedScript());
            scriptEntity.setIsGenerating(false);

            log.info("------------------");
            log.info(scriptEntity.toString());

            
            ScriptEntity newScirpt = scriptRepository.save(scriptEntity);

 

            AIGenerateLogEntity aiGenerateLog = new AIGenerateLogEntity().convertToEntity(request, gptInfoData, optionalScriptEntity.get());

            
            // 7. AI 사용기록 저장
            aiGenerateLogRepository.save(aiGenerateLog);
            String GPTId = aiGenerateLog.getGptInfoEntity().getGptInfoId();
            String newScriptId = newScirpt.getScriptId().toString();

            log.info("-------------------- AI Generate Log");
            log.info(newScriptId);
        

            // FCM 푸쉬 알림 요청
            FcmSendDTO fcmSendDTO = new FcmSendDTO(request.getFcmtoken(), "대본 생성 알림", "요청하신 대본이 완성되었어요!", newScriptId);
            log.info(fcmSendDTO.toString());

            try {
                fcmService.sendMessageTo(fcmSendDTO).subscribe();
            } catch (IOException e) {
                log.error("[-] IOException occurred: " + e.getMessage());
                return Mono.just(CommonResponse.error(ErrorEnum.INTERNAL_SERVER_ERROR));                
            }

            return Mono.just(CommonResponse.success(new AIGenerateResponse(resultScript, GPTId)));

            });
        }
    }