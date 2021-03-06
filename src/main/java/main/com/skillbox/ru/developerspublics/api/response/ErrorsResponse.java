package main.com.skillbox.ru.developerspublics.api.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ErrorsResponse {

  private String text;
  private String code;
  private String password;
  private String captcha;
  private String email;
  private String name;
  private String user;
  private String photo;
  private String title;

  public ErrorsResponse(boolean isCodeCorrect,
      boolean isPasswordCorrect,
      boolean isCaptchaCorrect,
      boolean isEmailExist,
      boolean isNameWrong) {
    if (!isCodeCorrect) {
      code = "Ссылка для восстановления пароля устарела. <a href=\"/auth/restore\">" +
          "Запросить ссылку снова</a>";
    }
    if (!isPasswordCorrect) {
      password = "Пароль короче 6-ти символов";
    }
    if (!isCaptchaCorrect) {
      captcha = "Код с картинки введён неверно";
    }
    if (isEmailExist) {
      email = "Этот e-mail уже зарегистрирован";
    }
    if (isNameWrong) {
      name = "Имя указано неверно";
    }
  }

  public ErrorsResponse(String text) {
    this.text = "Текст комментария не задан или слишком короткий";
  }
}
