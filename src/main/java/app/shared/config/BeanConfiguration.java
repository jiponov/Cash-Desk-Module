package app.shared.config;

import org.springframework.context.annotation.*;
import org.modelmapper.*;


@Configuration
public class BeanConfiguration {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}