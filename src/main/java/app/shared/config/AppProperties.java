package app.shared.config;

import lombok.*;
import org.springframework.boot.context.properties.*;
import org.springframework.stereotype.*;


@Data
@Component
@ConfigurationProperties(prefix = "api")
public class AppProperties {

    private String key;
}