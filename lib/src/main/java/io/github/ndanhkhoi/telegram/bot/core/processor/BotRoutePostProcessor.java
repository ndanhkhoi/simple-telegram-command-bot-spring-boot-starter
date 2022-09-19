package io.github.ndanhkhoi.telegram.bot.core.processor;

import io.github.ndanhkhoi.telegram.bot.annotation.BotRoute;
import io.github.ndanhkhoi.telegram.bot.annotation.CommandBody;
import io.github.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import io.github.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import io.github.ndanhkhoi.telegram.bot.constant.CommonConstant;
import io.github.ndanhkhoi.telegram.bot.core.registry.CommandRegistry;
import io.github.ndanhkhoi.telegram.bot.exception.BotException;
import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

/**
 * @author khoinda
 * Created at 09:03:41 May 31, 2022
 */
@Slf4j
public class BotRoutePostProcessor implements BeanPostProcessor, SmartInitializingSingleton, Ordered, BeanFactoryAware, EnvironmentAware {

    private static final String BOT_ROUTE_PACKAGES_PROPERTY = "khoinda.bot.bot-route-packages";
    public static final String DEFAULT_ROUTE_PACKAGE = "io.github.ndanhkhoi.telegram.bot.route";
    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private BeanFactory beanFactory;
    private Environment env;
    private List<String> botRoutePackages;

    private boolean isBotRoutePackage(String packageName) {
        return StringUtils.equals(packageName, DEFAULT_ROUTE_PACKAGE) || botRoutePackages.contains(packageName);
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
                .withParseMode(mapping.parseMode())
                .withDisableWebPagePreview(mapping.disableWebPagePreview())
                .withAccessUserIds(mapping.accessUserIds())
                .withAccessMemberIds(mapping.accessMemberIds())
                .withAccessGroupIds(mapping.accessGroupIds())
                .withAllowAllUserAccess(mapping.allowAllUserAccess())
                .withAllowAllGroupAccess(mapping.allowAllGroupAccess())
                .withOnlyForGroup(mapping.onlyForGroup())
                .withOnlyAdmin(mapping.onlyAdmin())
                .withSendFile(mapping.sendFile())
                .withMethod(method)
                .withDescription(commandDescription)
                .withBodyDescription(bodyDescription)
                .withOnlyForOwner(mapping.onlyForOwner())
                .withOnlyForPrivate(mapping.onlyForPrivate())
                .build();
    }

    private void registerBotCommand(BotCommand botCommand) {
        beanFactory.getBean(CommandRegistry.class).register(botCommand);
        log.debug("Registered command: {}", botCommand.getCmd());
    }

    private Flux<BotCommand> extractBotCommands(Method method, CommandMapping mapping) {
        return Flux.fromArray(mapping.value())
                .map(cmd -> {
                    String commandDescription = Optional.ofNullable(AnnotationUtils.findAnnotation(method, CommandDescription.class))
                            .map(CommandDescription::value)
                            .orElse("");
                    String bodyDescription = Arrays.stream(method.getParameters())
                            .flatMap(parameter -> Optional.ofNullable(AnnotationUtils.findAnnotation(parameter, CommandBody.class))
                                    .stream()
                                    .map(commandBody -> StringUtils.defaultIfBlank(commandBody.description(), parameter.getName())))
                            .findFirst()
                            .orElse("");
                    return extractBotCommand(method, cmd, mapping, commandDescription, bodyDescription);
                });
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
                            .flatMap(entry -> this.extractBotCommands(entry.getKey(), entry.getValue()))
                            .doOnComplete(() -> log.debug(annotatedMethods.size() + " @CommandMapping methods processed on bean '"
                                    + beanName + "': " + annotatedMethods))
                            .doOnError(ex -> {
                                throw Exceptions.errorCallbackNotImplemented(ex);
                            })
                            .subscribeOn(Schedulers.fromExecutor(getTaskExecutor()))
                            .subscribe(this::registerBotCommand);
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

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setEnvironment(Environment env) {
        this.env = env;
        setBotRoutePackages();
    }

    private void setBotRoutePackages() {
        this.botRoutePackages = Binder.get(env)
                .bind(BOT_ROUTE_PACKAGES_PROPERTY, String[].class)
                .map(Arrays::asList)
                .orElse(new ArrayList<>());
    }

    private Executor getTaskExecutor() {
        return beanFactory.getBean("botAsyncTaskExecutor", SimpleAsyncTaskExecutor.class);
    }

}
