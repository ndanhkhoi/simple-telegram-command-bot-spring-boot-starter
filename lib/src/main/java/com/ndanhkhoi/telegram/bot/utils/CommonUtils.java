package com.ndanhkhoi.telegram.bot.utils;

import com.ndanhkhoi.telegram.bot.exception.BotException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;

/**
 * @author ndanhkhoi
 * Created at 00:47:14 October 06, 2021
 */
@Slf4j
@UtilityClass
public final class CommonUtils {

    public static Object getProperty(Object bean, String name) {
        try {
            return PropertyUtils.getProperty(bean, name);
        }
        catch (Exception ex) {
            log.error("Error !", ex);
            throw new BotException(ex);
        }
    }

}
