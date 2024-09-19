package site.balpyo.auth.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import site.balpyo.auth.EmailConfig;
import site.balpyo.auth.dto.request.LoginRequest;
import site.balpyo.auth.dto.request.SignupRequest;
import site.balpyo.auth.dto.response.JwtResponse;
import site.balpyo.auth.dto.response.MessageResponse;
import site.balpyo.auth.entity.ERole;
import site.balpyo.auth.entity.LoginType;
import site.balpyo.auth.entity.Role;
import site.balpyo.auth.entity.User;
import site.balpyo.auth.repository.RoleRepository;
import site.balpyo.auth.repository.UserRepository;
import site.balpyo.auth.security.jwt.JwtUtils;
import site.balpyo.auth.service.EmailService;
import site.balpyo.auth.service.RandomAdjectiveAnimalGenerator;
import site.balpyo.auth.service.UserDetailsImpl;
import site.balpyo.common.dto.CommonResponse;
import site.balpyo.common.dto.ErrorEnum;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;
    @Autowired
    EmailService emailService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    EmailConfig emailConfig;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {

        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        //새로운 유저 생성
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));


        Set<Role> roles = new HashSet<>();

            Role userRole = roleRepository.findByName(ERole.ROLE_UNVERIFIED_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);

        user.setRoles(roles);
        user.setCoin(3); // TODO : 최초 가입시 제공받는 코인 갯수 차후 확장성있게 수정가능하도록 구현할 예쩡
        user.setLoginType(LoginType.LOCAL);

        if(signUpRequest.getUsername()==null){
            user.setUsername(new RandomAdjectiveAnimalGenerator().generateNickname());
        }else{
            user.setUsername(signUpRequest.getUsername());
        }

        System.out.println("user :"+user.toString());
        User insertedUser = userRepository.save(user);

        // // 서버의 IP 주소를 가져옵니다.
        // String serverIpAddress = "";
        // try {
        //     InetAddress inetAddress = InetAddress.getLocalHost();
        //     serverIpAddress = inetAddress.getHostAddress();
        // } catch (UnknownHostException e) {
        //     e.printStackTrace();
        // }

        String verificationUrl = "https://balpyo.site" + "/api/verify?uid=" + insertedUser.getVerifyCode(); // 포트 번호를 적절히 수정하십시오.

        emailService.sendEmail(signUpRequest.getEmail(), emailConfig.getBalpyoTitle(), emailConfig.getBalpyoBody(verificationUrl));

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/verify")
    public ResponseEntity<CommonResponse> checkUserVerify(@RequestParam("uid") String uid) {
        Optional<User> optionalUser = userRepository.findByVerifyCode(uid);
        if(optionalUser.isPresent()){
            Set<Role> roles = new HashSet<>();
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);

            User user = optionalUser.get();
            user.setRoles(roles);

            userRepository.save(user);

            return CommonResponse.success("");
        }else{
            return CommonResponse.error(ErrorEnum.GUEST_NOT_FOUND);
        }
    }

    @GetMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        // HTTP 요청을 통해 사용자 정보를 조회
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);


        if (response.getStatusCode() == HttpStatus.OK) {
            String userInfo = response.getBody();
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestParam String accessToken) {
        // 구글 사용자 정보 API URL
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        // 액세스 토큰을 헤더에 포함하여 요청
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 사용자 정보 요청
        ResponseEntity<String> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, entity, String.class);

        // 사용자 정보 처리
        if (response.getStatusCode() == HttpStatus.OK) {
            String userInfo = response.getBody();
            // 응답을 처리하거나 사용자를 등록
            return ResponseEntity.ok(userInfo); // 사용자 정보를 반환하거나, 응답 처리
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }







}