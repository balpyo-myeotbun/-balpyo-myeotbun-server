package site.balpyo.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import site.balpyo.ai.dto.AIGenerateRequest;
import site.balpyo.ai.service.AIGenerateService;
import site.balpyo.common.dto.CommonResponse;
import site.balpyo.common.dto.ErrorEnum;
import site.balpyo.common.util.CommonUtils;



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
    public ResponseEntity<CommonResponse> generateScript(@Valid @RequestBody AIGenerateRequest aiGenerateRequest){

        log.info("-------------------- 스크립트 생성 요청");
        log.info("-------------------- 요청 내용 ");
        log.info("--------------------" + aiGenerateRequest);

        return aiGenerateService.generateScript(aiGenerateRequest);
    }

}
