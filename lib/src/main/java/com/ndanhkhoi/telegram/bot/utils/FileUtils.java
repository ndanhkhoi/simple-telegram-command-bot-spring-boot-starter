package com.ndanhkhoi.telegram.bot.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@UtilityClass
public final class FileUtils {

    public static InputFile getInputFile(File file) throws FileNotFoundException {
        InputFile inputFile = new InputFile();
        inputFile.setMedia(new FileInputStream(file), file.getName());
        return inputFile;
    }

    public static InputFile getInputFile(byte[] bytes, String fileName) {
        InputFile inputFile = new InputFile();
        inputFile.setMedia(new ByteArrayInputStream(bytes), fileName);
        return inputFile;
    }

    public static String getPosfixFileInstantByTime(ZoneId zoneId) {
        ZonedDateTime now = Instant.now().atZone(zoneId);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return df.format(now);
    }

}
