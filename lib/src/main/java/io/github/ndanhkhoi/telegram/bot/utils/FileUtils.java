package io.github.ndanhkhoi.telegram.bot.utils;

import io.github.ndanhkhoi.telegram.bot.constant.CommonConstant;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@UtilityClass
public final class FileUtils {

    @SneakyThrows
    public static InputFile getInputFile(File file) {
        InputFile inputFile = new InputFile();
        String fileName = StringUtils.firstNonBlank(file.getName(), CommonConstant.TEMP_PREFIX + getPosfixFileInstantByTime(ZoneId.systemDefault()));
        inputFile.setMedia(new FileInputStream(file), fileName);
        return inputFile;
    }

    public static InputFile getInputFile(byte[] bytes, String fileName) {
        InputFile inputFile = new InputFile();
        String finalFileName = StringUtils.firstNonBlank(fileName, CommonConstant.TEMP_PREFIX + getPosfixFileInstantByTime(ZoneId.systemDefault()));
        inputFile.setMedia(new ByteArrayInputStream(bytes), finalFileName);
        return inputFile;
    }

    @SneakyThrows
    public static InputFile getInputFile(ByteArrayResource resource) {
        InputFile inputFile = new InputFile();
        String fileName = StringUtils.firstNonBlank(resource.getFilename(), CommonConstant.TEMP_PREFIX + getPosfixFileInstantByTime(ZoneId.systemDefault()));
        inputFile.setMedia(resource.getInputStream(), fileName);
        return inputFile;
    }

    @SneakyThrows
    public static InputFile getInputFile(InputStream inputStream) {
        InputFile inputFile = new InputFile();
        inputFile.setMedia(inputStream, CommonConstant.TEMP_PREFIX + getPosfixFileInstantByTime(ZoneId.systemDefault()));
        return inputFile;
    }

    public static String getPosfixFileInstantByTime(ZoneId zoneId) {
        ZonedDateTime now = Instant.now().atZone(zoneId);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return df.format(now);
    }

}
