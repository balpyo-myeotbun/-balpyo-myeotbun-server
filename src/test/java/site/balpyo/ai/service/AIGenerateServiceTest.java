package site.balpyo.ai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.HttpBody;
import com.google.api.HttpBodyOrBuilder;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import site.balpyo.ai.dto.AIGenerateRequest;
import site.balpyo.ai.dto.AudioDTO;
import site.balpyo.ai.dto.PollyDTO;
import site.balpyo.auth.dto.request.LoginRequest;
import site.balpyo.auth.dto.response.JwtResponse;
import site.balpyo.auth.entity.User;
import site.balpyo.auth.repository.UserRepository;
import site.balpyo.common.dto.CommonResponse;
import site.balpyo.script.dto.ScriptRequest;
import site.balpyo.script.dto.ScriptResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AIGenerateServiceTest {
    private static String jwtToken;
    private static String userVerifyUid;

    private static Long scriptId;
    @Autowired
    private TestRestTemplate restTemplate;



    @BeforeAll
    public static void testSignup(@Autowired TestRestTemplate restTemplate,
                                  @Autowired UserRepository userRepository) {

        LoginRequest request = new LoginRequest();
        request.setEmail("testuser@example.com");
        request.setPassword("password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/auth/signin", entity, String.class);
        System.out.println("response--------------------");
        System.out.println("response : " + response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JwtResponse jwtResponse = objectMapper.readValue(response.getBody(), JwtResponse.class);
            jwtToken = jwtResponse.getToken();
            Optional<User> optionalUser = userRepository.findByEmail("testuser@example.com");
            userVerifyUid = optionalUser.get().getVerifyCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    @Order(1)
    public void testJwtExist() {
        System.out.println("jwtToken :" + jwtToken);
        assertNotNull(jwtToken);
    }

    @Test
    @Order(2)
    public void testVerifyCodeExist() {
        assertNotNull(userVerifyUid);
    }
//
//    @Test
//    @Order(3)
//    public void testVerify() {
//
//        HttpHeaders headers = new HttpHeaders();
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<CommonResponse> response = restTemplate.exchange("/api/auth/verify?uid="+userVerifyUid, HttpMethod.GET, entity, CommonResponse.class);
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//
//    }

    @Test
    @Order(4)
    public void testSaveScript() {
        ScriptRequest request = new ScriptRequest();
        request.setTitle("Test Script");
        request.setScript("This is a test script.");
        request.setGptId("gpt-test-id");
        request.setSecTime(120);
        request.setVoiceFilePath("path/to/voice/file");
        request.setUseAi(false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);

        HttpEntity<ScriptRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<CommonResponse> response = restTemplate.postForEntity("/every/manage/script", entity, CommonResponse.class);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object result = response.getBody().getResult();
            ScriptResponse scriptResponse = objectMapper.convertValue(result, ScriptResponse.class);
            scriptId = scriptResponse.getScriptId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(response.getBody()+"response.getBody()");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
//    @Test
//    @Order(5)
//    public void generateAi() {
//        AIGenerateRequest request = new AIGenerateRequest();
//        request.setTopic("발표준비");
//        request.setTest(true);
//        request.setKeywords("발표에서 최적화하는 방법");
//        request.setSecTime(100);
//        request.setScriptId(scriptId);
//        request.setBalpyoAPIKey("1234");
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.set("Authorization", "Bearer " + jwtToken);
//
//        HttpEntity<AIGenerateRequest> entity = new HttpEntity<>(request, headers);
//
//        ResponseEntity<CommonResponse> response = restTemplate.exchange("/user/ai/script", HttpMethod.POST, entity, CommonResponse.class);
//        System.out.println(response.toString());
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertNotNull(response.getBody());
//
//    }


    @Test
    @Order(6)
    public void testGetAllScripts() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CommonResponse> beforeResponse = restTemplate.exchange("/every/manage/script/all", HttpMethod.GET, entity, CommonResponse.class);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<ScriptResponse> beforeScriptResponses = objectMapper.convertValue(
                    beforeResponse.getBody().getResult(), new TypeReference<List<ScriptResponse>>() {}
            );

            Integer beforeSize = beforeScriptResponses.size();
            System.out.println("Before size: " + beforeSize);

            testSaveScript();

            ResponseEntity<CommonResponse> afterResponse = restTemplate.exchange("/every/manage/script/all", HttpMethod.GET, entity, CommonResponse.class);
            List<ScriptResponse> afterScriptResponses = objectMapper.convertValue(
                    afterResponse.getBody().getResult(), new TypeReference<List<ScriptResponse>>() {}
            );
            Integer afterSize = afterScriptResponses.size();
            System.out.println("After size: " + afterSize);

            assertEquals(1, afterSize - beforeSize);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred: " + e.getMessage());
        }

    }


    @Test
    @Order(7)
    public void testGetScriptDetail() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CommonResponse> response = restTemplate.exchange("/every/manage/script/detail/" + scriptId, HttpMethod.GET, entity, CommonResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(8)
    public void testPatchScriptDetail() {

        testSaveScript();

        ScriptRequest request = new ScriptRequest();

        String randomString = UUID.randomUUID().toString();
        request.setTitle(randomString + "title");
        request.setScript(randomString + "script");
        request.setGptId(randomString + "gptId");
        request.setUid(randomString + "uid");
        request.setSecTime(9999);
        request.setVoiceFilePath(randomString + "path");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<ScriptRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<CommonResponse> patchResponse = restTemplate.exchange("/every/manage/script/detail/" + scriptId, HttpMethod.PATCH, entity, CommonResponse.class);

        assertEquals(HttpStatus.OK, patchResponse.getStatusCode());
        assertNotNull(patchResponse.getBody());
        assertEquals("0000", patchResponse.getBody().getCode());
        assertEquals("success", patchResponse.getBody().getMessage());

        HttpEntity<Void> entity2 = new HttpEntity<>(headers);

        ResponseEntity<CommonResponse> getResponse = restTemplate.exchange("/every/manage/script/detail/" + scriptId, HttpMethod.GET, entity2, CommonResponse.class);

        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertNotNull(getResponse.getBody());
        assertEquals("0000", getResponse.getBody().getCode());
        assertEquals("success", getResponse.getBody().getMessage());
        assertNotNull(getResponse.getBody().getResult());

        ScriptResponse scriptResponse = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Object result = getResponse.getBody().getResult();
            scriptResponse = objectMapper.convertValue(result, ScriptResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception occurred while parsing the response body");
        }

        assertNotNull(scriptResponse);
        assertEquals(scriptResponse.getTitle(), randomString + "title");
        assertEquals(scriptResponse.getScript(), randomString + "script");


        assertEquals(scriptResponse.getSecTime(), 9999);
        assertEquals(scriptResponse.getVoiceFilePath(), randomString + "path");

    }


    @Test
    @Order(9)
    public void testDeleteScriptDetail() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<CommonResponse> response = restTemplate.exchange("/every/manage/script/detail/" + scriptId, HttpMethod.DELETE, entity, CommonResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }


    @Test
    @Order(10)
    public void testGenerateAudio() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        PollyDTO request = new PollyDTO();
        request.setText("NOW TESTING!");
        request.setBalpyoAPIKey("1234");
        HttpEntity<PollyDTO> entity = new HttpEntity<>(request,headers);

        ResponseEntity<AudioDTO> response = restTemplate.exchange("/polly/uploadSpeech", HttpMethod.POST, entity, AudioDTO.class);

        String profileUrl = response.getBody().getProfileUrl();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(profileUrl);
    }


}