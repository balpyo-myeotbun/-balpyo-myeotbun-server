package site.balpyo.script.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import site.balpyo.common.dto.CommonResponse;
import site.balpyo.common.dto.ErrorEnum;
import site.balpyo.common.util.CommonUtils;
import site.balpyo.script.dto.ScriptRequest;
import site.balpyo.script.service.ScriptService;
import site.balpyo.script.service.ScriptServiceDeprecated;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping("/every/manage")
public class EveryScriptController {

    private final ScriptServiceDeprecated scriptServiceDeprecated;
    private final ScriptService scriptService;


    @PostMapping("/script")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponse> saveScript(@RequestBody ScriptRequest scriptRequest,
                                                     @RequestHeader(value = "UID", required = false) String uid){

        return scriptService.saveEmptyScript(scriptRequest);
    }

    @GetMapping("/script/all")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponse> getAllScript(@RequestHeader(value = "UID", required = false) String uid) {

       return scriptService.getAllScript(uid);
    }



    @GetMapping("/script/detail/{scriptId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponse> getDetailScript(@RequestHeader(value = "UID", required = false) String uid,
    @PathVariable Long scriptId) {
        return scriptServiceDeprecated.getDetailScript(scriptId);
    }

    @PatchMapping("/script/detail/{scriptId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponse> patchDetailScript(@RequestBody ScriptRequest scriptRequest,
                                                     @RequestHeader(value = "UID", required = false) String uid,
                                                            @PathVariable Long scriptId){

       return scriptServiceDeprecated.patchScript(scriptRequest,scriptId);
    }

    @DeleteMapping("/script/detail/{scriptId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<CommonResponse> deleteDetailScript(@PathVariable Long scriptId){
        return scriptServiceDeprecated.deleteScript(scriptId);
    }

}
