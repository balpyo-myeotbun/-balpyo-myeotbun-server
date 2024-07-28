package site.balpyo.fcm;

import java.io.IOException;

import org.springframework.stereotype.Service;

import site.balpyo.fcm.dto.FcmSendDTO;

@Service
public interface FcmService {

    int sendMessageTo(FcmSendDTO fcmSendDTO) throws IOException;
    
}
