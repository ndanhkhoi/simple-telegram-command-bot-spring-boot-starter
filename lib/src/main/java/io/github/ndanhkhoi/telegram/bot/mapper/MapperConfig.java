package io.github.ndanhkhoi.telegram.bot.mapper;

import org.springframework.context.annotation.Bean;

/**
 * @author ndanhkhoi
 * Created at 10:33:27 August 28, 2022
 */
public class MapperConfig {

    @Bean(name = "updateMapper")
    UpdateMapper updateMapper() {
        return new UpdateMapper();
    }

}
