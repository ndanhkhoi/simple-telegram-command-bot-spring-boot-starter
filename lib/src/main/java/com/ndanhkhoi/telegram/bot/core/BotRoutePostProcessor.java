package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.annotation.BotRoute;
import com.ndanhkhoi.telegram.bot.annotation.CommandBody;
import com.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.exception.BotException;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author khoinda
 * Created at 09:03:41 May 31, 2022
 */
@Slf4j
public class BotRoutePostProcessor implements BeanPostProcessor, SmartInitializingSingleton, Ordered {

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private final BotProperties botProperties;
    private final CommandRegistry commandRegistry;

    public BotRoutePostProcessor(@Autowired(required = false) BotProperties botProperties, @Autowired(required = false) CommandRegistry commandRegistry) {
        this.botProperties = botProperties;
        this.commandRegistry = commandRegistry;
    }

    private boolean isBotRoutePackage(String packageName) {
        return StringUtils.equals(packageName, CommonConstant.DEFAULT_ROUTE_PACKAGE) || botProperties.getBotRoutePackages().contains(packageName);
    }

    private boolean isBotRoute(Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        return AnnotationUtils.findAnnotation(targetClass, BotRoute.class) != null;
    }

    private void validateCommand(String cmd) {
        if (StringUtils.isNotBlank(cmd)) {
            if (StringUtils.startsWith(cmd, CommonConstant.CMD_PREFIX)) {
                if (cmd.length() > CommonConstant.CMD_MAX_LENGTH) {
                    throw new BotException(String.format(CommonConstant.CMD_MAX_LENGTH_ERROR, CommonConstant.CMD_MAX_LENGTH, CommonConstant.CMD_PREFIX));
                }
                String cmdValue = cmd.substring(1);
                if (!CommonConstant.CMD_PATTERN.matcher(cmdValue).matches()) {
                    throw new BotException(CommonConstant.CMD_PATTERN_ERROR);
                }
            }
            else {
                throw new BotException(String.format(CommonConstant.CMD_PREFIX_ERROR, CommonConstant.CMD_PREFIX));
            }
        }
        else {
            throw new BotException(CommonConstant.CMD_BLANK_ERROR);
        }
    }

    private BotCommand extractBotCommand(Method method, String cmd, CommandMapping mapping, String commandDescription, String bodyDescription) {
        this.validateCommand(cmd);
        return BotCommand.builder()
                .withCmd(cmd)
                .withUseHtml(mapping.useHtml())
                .withDisableWebPagePreview(mapping.disableWebPagePreview())
                .withAccessUserIds(mapping.accessUserIds())
                .withAccessMemberIds(mapping.accessMemberIds())
                .withAccessGroupIds(mapping.accessGroupIds())
                .withAllowAllUserAccess(mapping.allowAllUserAccess())
                .withOnlyAdmin(mapping.onlyAdmin())
                .withSendFile(mapping.sendFile())
                .withMethod(method)
                .withDescription(commandDescription)
                .withBodyDescription(bodyDescription)
                .withOnlyForOwner(mapping.onlyForOwner())
                .build();
    }

    private Flux<BotCommand> extractBotCommands(Method method, CommandMapping mapping) {
        String commandDescription = Arrays.stream(method.getDeclaredAnnotationsByType(CommandDescription.class))
                .findFirst()
                .map(CommandDescription::value)
                .orElse("");
        String bodyDescription = Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.getDeclaredAnnotationsByType(CommandBody.class).length > 0)
                .findFirst()
                .map(parameter -> {
                    String description = parameter.getDeclaredAnnotationsByType(CommandBody.class)[0].description();
                    return StringUtils.defaultIfBlank(description, parameter.getName());
                })
                .orElse("");
        return Flux.fromArray(mapping.value())
                .map(cmd -> extractBotCommand(method, cmd, mapping, commandDescription, bodyDescription));
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!this.nonAnnotatedClasses.contains(bean.getClass())) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            String packageName = targetClass.getPackageName();
            if (isBotRoutePackage(packageName) && isBotRoute(bean)) {

                Map<Method, CommandMapping> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                        (MethodIntrospector.MetadataLookup<CommandMapping>) method -> AnnotationUtils.findAnnotation(method, CommandMapping.class));

                if (annotatedMethods.isEmpty()) {
                    this.nonAnnotatedClasses.add(bean.getClass());
                    log.trace("No @CommandMapping annotations found on bean type: " + bean.getClass());
                }
                else {
                    Flux.fromIterable(annotatedMethods.entrySet())
                            .flatMap(e -> this.extractBotCommands(e.getKey(), e.getValue()))
                            .doOnError(ex -> {
                                throw Exceptions.errorCallbackNotImplemented(ex);
                            })
                            .subscribe(commandRegistry::register);
                    log.debug( annotatedMethods.size() + " @CommandMapping methods processed on bean '"
                            + beanName + "': " + annotatedMethods);
                }
            }
            else {
                nonAnnotatedClasses.add(bean.getClass());
            }
        }
        return bean;
    }

    @Override
    public void afterSingletonsInstantiated() {
        nonAnnotatedClasses.clear();
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
