package io.github.ndanhkhoi.telegram.bot.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.SneakyThrows;

/**
 * @author ndanhkhoi
 * Created at 19:33:28 April 29, 2022
 */
public class UpdateMapper extends ObjectMapper {

    public UpdateMapper() {
        super();
        super.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        super.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        super.setDateFormat(new StdDateFormat());
    }

    @SneakyThrows
    public String writeValueAsPrettyString(Object obj) {
        return super.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    @Override
    @SneakyThrows
    public String writeValueAsString(Object obj) {
        return super.writeValueAsString(obj);
    }

}
