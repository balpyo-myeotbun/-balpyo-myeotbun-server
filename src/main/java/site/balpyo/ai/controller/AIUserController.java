package site.balpyo.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.balpyo.ai.dto.AIGenerateRequest;
import site.balpyo.ai.service.AIGenerateService;
import site.balpyo.common.dto.CommonResponse;

import reactor.core.publisher.Mono;




@CrossOrigin
@RestController
@RequestMapping("/user/ai")
@RequiredArgsConstructor
@Slf4j
public class AIUserController {

    private final AIGenerateService aiGenerateService;

    @Value("${secrets.BALPYO_API_KEY}") //TODO :: 임시 api 시크릿 키 구현 (차후 로그인 연동시 삭제예정)
    public String BALPYO_API_KEY;

    @PostMapping("/script")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public Mono<ResponseEntity<CommonResponse>> generateScript(@Valid @RequestBody AIGenerateRequest aiGenerateRequest){

        log.info("-------------------- Requested Generate Script");
        log.info("-------------------- Request Topic : " + aiGenerateRequest.getTopic());
        log.info("-------------------- Request Keywords : " + aiGenerateRequest.getKeywords());

        return aiGenerateService.generateScript(aiGenerateRequest);
    }

}
