package com.skillbox.ru.developerspublics.test;

import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.DevelopersPublicationsApplication;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthLogin;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthPassword;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRegister;
import main.com.skillbox.ru.developerspublics.api.request.RequestApiAuthRestore;
import main.com.skillbox.ru.developerspublics.api.response.*;
import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.entity.User;
import main.com.skillbox.ru.developerspublics.model.repository.UsersRepository;
import main.com.skillbox.ru.developerspublics.service.CaptchaCodeService;
import main.com.skillbox.ru.developerspublics.service.UserService;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;


@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DevelopersPublicationsApplication.class)
public class UnitTestsUserService {
    @Autowired
    private UserService service;
    @Autowired
    private UsersRepository repository;
    @Autowired
    private CaptchaCodeService captchaCodeService;

    private final String userPassword = "userPassword";
    private final String moderatorPassword = "moderatorPassword";
    private final User testUser = new User("test@test.test", "testUser", userPassword);
    private final User testModerator = new User(
            "testM@testM.test",
            "testModerator",
            moderatorPassword);
    private final String testFilePath = "src/test/java/com/skillbox/ru/developerspublics/test/TestImage.jpg";
    private final String bigFilePath = "src/test/java/com/skillbox/ru/developerspublics/test/BigTestImage.jpg";
    @Value("${moderator.email}")
    private String realEmail;


    private void authUser() {
        saveUser();
        service.authUser(testUser.getEmail(), userPassword);
    }

    private void saveUser() {
        deleteUser();
        service.saveUser(testUser);
    }


    private void saveModerator() {
        service.deleteUser(testModerator);
        testModerator.setIsModerator(1);
        service.saveUser(testModerator);
    }

    private void deleteUser() {
        service.deleteUser(testUser);
    }

    @SneakyThrows
    private MultipartFile getAvatar(String path) {
        return new MockMultipartFile("file", new FileInputStream(new File(path)));
    }

    private CaptchaCode getCaptchaCode() {
        return captchaCodeService
                .getCaptchaCodeBySecretCode(
                        captchaCodeService
                                .createNewCaptcha().get("secretCode").toString());
    }


    @Test
    public void testPostApiAuthLoginWrongPassword() {
        saveUser();
        RequestApiAuthLogin request = new RequestApiAuthLogin();
        request.setEmail(testUser.getEmail());
        request.setPassword(testUser.getPassword());

        ResponseEntity<?> response = service.postApiAuthLogin(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(false));

        deleteUser();
    }


    @Test
    public void testPostApiAuthLoginUserNotFound() {
        saveUser();
        RequestApiAuthLogin request = new RequestApiAuthLogin();
        request.setEmail("123");
        request.setPassword(userPassword);

        ResponseEntity<?> response = service.postApiAuthLogin(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(false));

        deleteUser();
    }


    @Test
    public void testPostApiAuthLogin200() {
        saveUser();
        RequestApiAuthLogin request = new RequestApiAuthLogin();
        request.setEmail(testUser.getEmail());
        request.setPassword(userPassword);

        ResponseEntity<?> response = service.postApiAuthLogin(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultUserResponse(
                        new UserResponse(
                                testUser.getId(),
                                testUser.getName(),
                                testUser.getPhoto(),
                                testUser.getEmail(),
                                testUser.getIsModerator() == 1,
                                service.getModerationCount(testUser),
                                testUser.getIsModerator() == 1
                        )
                ));

        deleteUser();
    }


    @Test
    public void testGetApiAuthCheckUserNotFound() {
        authUser();
        testUser.setEmail("bcjddhvsidos");
        saveUser();
        ResponseEntity<?> response = service.getApiAuthCheck();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(false));
        deleteUser();
    }


    @Test
    public void testGetApiAuthCheckAnonymous() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "anonymous",
                "anonymous",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));
        ResponseEntity<?> response = service.getApiAuthCheck();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(false));
        deleteUser();
    }


    @Test
    public void testGetApiAuthCheckAuthNull() {
        ResponseEntity<?> response = service.getApiAuthCheck();
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(false));
        deleteUser();
    }


    @Test
    public void testGetApiAuthCheckTrue() {
        authUser();
        ResponseEntity<?> response = service.getApiAuthCheck();

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultUserResponse(
                        new UserResponse(
                                testUser.getId(),
                                testUser.getName(),
                                testUser.getPhoto(),
                                testUser.getEmail(),
                                testUser.getIsModerator() == 1,
                                service.getModerationCount(testUser),
                                testUser.getIsModerator() == 1
                        )
                ));

        deleteUser();
    }


    @Test
    public void testPostApiAuthRestoreWrongEmail() {
        testUser.setCode(null);
        saveUser();
        RequestApiAuthRestore requestBody = new RequestApiAuthRestore();
        requestBody.setEmail("1");

        ResponseEntity<?> response = service.postApiAuthRestore(requestBody);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(false));
        User dbUser = service.findUserByLogin(testUser.getEmail());
        Assert.assertNull(dbUser.getCode());
        deleteUser();
    }


    @Test
    public void testPostApiAuthRestore200() {
        testUser.setCode(null);
        testUser.setEmail(realEmail);
        saveUser();
        RequestApiAuthRestore requestBody = new RequestApiAuthRestore();
        requestBody.setEmail(testUser.getEmail());

        ResponseEntity<?> response = service.postApiAuthRestore(requestBody);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));
        User dbUser = service.findUserByLogin(testUser.getEmail());
        Assert.assertNotNull(dbUser.getCode());
        deleteUser();
    }


    @Test
    public void testPostApiAuthPasswordWrongCaptcha() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
                "testCode",
                testUser.getPassword(),
                captchaCode.getCode() + "1",
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(
                        new ErrorsResponse(
                                true,
                                true,
                                false,
                                false,
                                false)));

        deleteUser();
    }


    @Test
    public void testPostApiAuthPasswordWrongPassword() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
                "testCode",
                "1",
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(
                        new ErrorsResponse(
                                true,
                                false,
                                true,
                                false,
                                false)));

        deleteUser();
    }


    @Test
    public void testPostApiAuthPasswordWrongCode() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
                "wrongCode",
                testUser.getPassword(),
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(
                        new ErrorsResponse(
                                false,
                                true,
                                true,
                                false,
                                false)));

        deleteUser();
    }


    @Test
    public void testPostApiAuthPassword200() {
        testUser.setCode("testCode");
        saveUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthPassword request = new RequestApiAuthPassword(
            "testCode",
                testUser.getPassword(),
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthPassword(request);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));

        deleteUser();
    }


    @Test
    public void testPostApiAuthRegister200() {
        CaptchaCode captchaCode = getCaptchaCode();

        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                userPassword,
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));

        deleteUser();
    }


    @Test
    public void testPostApiAuthRegisterEmailExist() {
        authUser();
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                userPassword,
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                true,
                        true,
                        true,
                        true,
                        false)));

        deleteUser();
    }


    @Test
    public void testPostApiAuthRegisterWrongName() {
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                "",
                userPassword,
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                        true,
                        true,
                        true,
                        false,
                        true)));
        deleteUser();
    }


    @Test
    public void testPostApiAuthRegisterWrongPassword() {
        CaptchaCode captchaCode = getCaptchaCode();
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                "",
                captchaCode.getCode(),
                captchaCode.getSecretCode()
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                        true,
                        false,
                        true,
                        false,
                        false)));
        deleteUser();
    }


    @Test
    public void testPostApiAuthRegisterWrongCaptcha() {
        RequestApiAuthRegister requestBody = new RequestApiAuthRegister(
                testUser.getEmail(),
                testUser.getName(),
                testUser.getPassword(),
                "1",
                "2"
        );
        ResponseEntity<?> response = service.postApiAuthRegister(requestBody);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(),
                new ResultFalseErrorsResponse(new ErrorsResponse(
                        true,
                        true,
                        false,
                        false,
                        false)));
        deleteUser();
    }


    @Test
    public void testGetApiAuthLogout() {
        authUser();
        ResponseEntity<?> response = service.getApiAuthLogout();

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Assert.assertEquals(userDetails.getUsername(), "anonymous");

        deleteUser();
    }


    @Test
    public void testPostApiProfileMyNoChanePhotoAndPassword() {
        authUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));

        deleteUser();
    }


    @Test
    public void testPostApiProfileMyWithChangePasswordWithoutChangePhoto() {
        authUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail() + "1");
        requestBody.put("name",testUser.getName() + "1");
        requestBody.put("password",userPassword + "1");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));
        Assert.assertNotEquals(testUser.getPassword(), service.encodePassword(userPassword));
        Assert.assertNotEquals(testUser.getEmail(), repository.findById(testUser.getId()).get().getEmail());
        Assert.assertNotEquals(testUser.getName(), repository.findById(testUser.getId()).get().getName());

        deleteUser();
    }


    @Test
    public void testPostApiProfileMyWithChangePasswordAndPhoto() {
        authUser();
        MultipartFile avatar = getAvatar(testFilePath);
        ResponseEntity<?> response = service.postApiProfileMy(
                null,
                avatar,
                testUser.getEmail(),
                testUser.getName(),
                userPassword + "1",
                "0");

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));
        Assert.assertNotNull(repository.findUserByEmail(testUser.getEmail()).getPhoto());
        Assert.assertNotEquals(testUser.getPassword(), service.encodePassword(userPassword));

        deleteUser();
    }


    @Test
    @SneakyThrows
    public void testPostApiProfileMyDeletePhotoWithoutChangePassword() {
        authUser();
        service.saveAvatar(testUser, new FileInputStream(new File(testFilePath)));
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        requestBody.put("removePhoto","1");
        requestBody.put("photo", "");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), new ResultResponse(true));
        Assert.assertEquals(service.findUserByLogin(testUser.getEmail()).getPhoto(), "");

        deleteUser();
    }


    @Test
    @SneakyThrows
    public void testPostApiProfileMyUserNotFound() {
        authUser();
        deleteUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(
                response.getBody(),
                new ResultFalseErrorsResponse(
                        ErrorsResponse.builder().user("Пользователь не найден!").build()));
        deleteUser();
    }


    @Test
    public void testPostApiProfileMyWrongName() {
        authUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name","");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(
                response.getBody(),
                new ResultFalseErrorsResponse(ErrorsResponse.builder().name("Имя указано неверно").build()));

        deleteUser();
    }


    @Test
    public void testPostApiProfileMyWrongEmail() {
        authUser();
        saveModerator();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testModerator.getEmail());
        requestBody.put("name",testUser.getName());
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(
                response.getBody(),
                new ResultFalseErrorsResponse(ErrorsResponse.builder().email("Этот e-mail уже зарегистрирован").build())
        );

        deleteUser();
        service.deleteUser(testModerator);
    }


    @Test
    public void testPostApiProfileMyWrongPassword() {
        authUser();
        JSONObject requestBody = new JSONObject();
        requestBody.put("email",testUser.getEmail());
        requestBody.put("name",testUser.getName());
        requestBody.put("password","1");
        ResponseEntity<?> response = service.postApiProfileMy(
                requestBody.toString(),
                null,
                null,
                null,
                null,
                null);

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(
                response.getBody(),
                new ResultFalseErrorsResponse(
                        ErrorsResponse.builder().password("Пароль короче 6-ти символов").build())
        );

        deleteUser();
    }


    @Test
    public void testPostApiProfileMyLagePhoto() {
        authUser();
        MultipartFile avatar = getAvatar(bigFilePath);
        ResponseEntity<?> response = service.postApiProfileMy(
                null,
                avatar,
                testUser.getEmail(),
                testUser.getName(),
                userPassword + "1",
                "0");

        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(
                response.getBody(),
                new ResultFalseErrorsResponse(
                        ErrorsResponse.builder().photo("Фото слишком большое, нужно не более 5 Мб").build())
        );

        deleteUser();
    }


    @Test
    public void testPostApiImage200() {
        authUser();
        MultipartFile avatar = getAvatar(testFilePath);

        ResponseEntity<?> response = service.postApiImage(avatar);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNotEquals(response.getBody(), "");
        deleteUser();
    }
}
