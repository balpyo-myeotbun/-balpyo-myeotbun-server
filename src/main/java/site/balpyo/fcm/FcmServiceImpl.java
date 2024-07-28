package site.balpyo.fcm;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import site.balpyo.fcm.dto.FcmMessageDTO;
import site.balpyo.fcm.dto.FcmSendDTO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class FcmServiceImpl implements FcmService {

    private final ObjectMapper objectMapper;

    @Override
    public int sendMessageTo(FcmSendDTO fcmSendDTO) throws IOException {

        String message = makeMessage(fcmSendDTO);
        RestTemplate restTemplate = new RestTemplate();
       /**
         * 추가된 사항 : RestTemplate 이용중 클라이언트의 한글 깨짐 증상에 대한 수정
         * @refernece : https://stackoverflow.com/questions/29392422/how-can-i-tell-resttemplate-to-post-with-utf-8-encoding
         */
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAccessToken());

        HttpEntity entity = new HttpEntity<>(message, headers);

        String API_URL = "https://fcm.googleapis.com/v1/projects/balpyo-myeotbun/messages:send";
        ResponseEntity response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

        System.out.println(response.getStatusCode());

        log.info("---------------------------");
        log.info(API_URL, response);
        System.out.println(response.toString());

        return response.getStatusCode() == HttpStatusCode.valueOf(200) ? 1 : 0;
    }

    private String makeMessage(FcmSendDTO fcmSendDTO) throws JsonParseException, JsonProcessingException {
        FcmMessageDTO fcmMessageDTO = FcmMessageDTO.builder()
                .message(FcmMessageDTO.Message.builder()
                        .token(fcmSendDTO.getToken())
                        .notification(FcmMessageDTO.Notification.builder()
                                .title(fcmSendDTO.getTitle())
                                .body(fcmSendDTO.getBody())
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessageDTO);
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/balpyo-myeotbun-firebase-adminsdk-7vz0s-c1873c08c3.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}