package main.com.skillbox.ru.developerspublics.service;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import main.com.skillbox.ru.developerspublics.api.response.ApiAuthCaptchaResponse;
import main.com.skillbox.ru.developerspublics.model.entity.CaptchaCode;
import main.com.skillbox.ru.developerspublics.model.repository.CaptchaCodesRepository;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class CaptchaCodeService {

  private final CaptchaCodesRepository captchaCodesRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  @Value("${captcha.chars}")
  private int iTotalChars;

  @Value("${captcha.height}")
  private int iHeight;

  @Value("${captcha.width}")
  private int iWidth;

  @Autowired
  public CaptchaCodeService(
      CaptchaCodesRepository captchaCodesRepository) {
    this.captchaCodesRepository = captchaCodesRepository;
    this.bCryptPasswordEncoder = new BCryptPasswordEncoder();
  }


  public CaptchaCode findCaptchaCodeBySecretCode(String secretCode) {
    return captchaCodesRepository.findBySecretCode(secretCode);
  }


  public CaptchaCode findCaptchaCodeByCodeAndSecret(String code, String secretCode) {
    return captchaCodesRepository.findByCodeAndSecretCode(code, secretCode);
  }


  public void saveCaptcha(String code, String secretCode) {
    CaptchaCode captchaCode = new CaptchaCode();
    captchaCode.setCode(code);
    captchaCode.setTime(Instant.now().toEpochMilli());
    captchaCode.setSecretCode(secretCode);
    captchaCodesRepository.save(captchaCode);
  }


  public void deleteOldCaptcha(long captchaLifeTime) {
    captchaCodesRepository.deleteAll(
        new ArrayList<>(
            captchaCodesRepository.findByTimeLessThan(
                new Date(Instant.now().toEpochMilli() - captchaLifeTime)))
    );
  }


  @SneakyThrows
  public JSONObject createNewCaptcha() {
    //шрифт
    int fontSize = (int) (1.67 * iWidth / iTotalChars);
    //фон
    Font fntStyle = new Font("Arial", Font.BOLD, fontSize);

    Random randChars = new Random();
    //генерируем слово
    String code = (Long.toString(Math.abs(randChars.nextLong()), 36)).substring(0, iTotalChars);
    //генерируем картинку
    BufferedImage biImage = new BufferedImage(iWidth, iHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2dImage = (Graphics2D) biImage.getGraphics();
    int iCircle = 15;
    for (int i = 0; i < iCircle; i++) {
      g2dImage.setColor(
          new Color(randChars.nextInt(255), randChars.nextInt(255), randChars.nextInt(255)));
    }
    g2dImage.setFont(fntStyle);
    for (int i = 0; i < iTotalChars; i++) {
      g2dImage.setColor(
          new Color(randChars.nextInt(255), randChars.nextInt(255), randChars.nextInt(255)));
      if (i % 2 == 0) {
        g2dImage.drawString(code.substring(i, i + 1), (int) (fontSize * i * 0.6),
            (int) (fontSize / 1.25));
      } else {
        g2dImage.drawString(code.substring(i, i + 1), (int) (fontSize * i * 0.6),
            iHeight - fontSize / 4);
      }
    }
    //создаем stream в нужном формате
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    ImageIO.write(biImage, "png", stream);
    //кодируем картинку в текст
    String base64 = Base64.getEncoder().encodeToString(stream.toByteArray());
    //убираем мусор
    stream.close();
    g2dImage.dispose();

    //сохраняем капчу в репозиторий
    String secretCode = bCryptPasswordEncoder.encode(code);
    saveCaptcha(code, secretCode);

    JSONObject result = new JSONObject();
    result.put("secretCode", secretCode);
    result.put("base64", base64);

    return result;
  }


  public ResponseEntity<?> getApiAuthCaptcha() {
    //создадим новую капчу
    JSONObject newCaptcha = createNewCaptcha();

    //собираем ответ
    return new ResponseEntity<>(
        new ApiAuthCaptchaResponse(
            newCaptcha.get("secretCode").toString(),
            "data:image/png;base64, " + newCaptcha.get("base64").toString()
        ), HttpStatus.OK);
  }
}