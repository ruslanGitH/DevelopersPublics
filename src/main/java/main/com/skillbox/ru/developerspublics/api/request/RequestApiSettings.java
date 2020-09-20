package main.com.skillbox.ru.developerspublics.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestApiSettings {
    @JsonProperty("MULTIUSER_MODE")
    Boolean multiUserMode;
    @JsonProperty("POST_PREMODERATION")
    Boolean postPremoderation;
    @JsonProperty("STATISTICS_IS_PUBLIC")
    Boolean statisticsIsPublic;
}
