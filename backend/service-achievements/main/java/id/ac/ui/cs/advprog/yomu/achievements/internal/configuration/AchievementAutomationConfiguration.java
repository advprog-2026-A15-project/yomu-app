package id.ac.ui.cs.advprog.yomu.achievements.internal.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration(proxyBeanMethods = false)
@EnableAsync
@EnableScheduling
public class AchievementAutomationConfiguration {
}
