package babycareai.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class SageMakerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SageMakerConfig.class);


    @Value("${sagemaker.region}")
    private String sagemakerRegion;

    @Bean
    public SageMakerRuntimeClient sageMakerRuntimeClient() {
        return SageMakerRuntimeClient.builder()
                .region(Region.of(sagemakerRegion))
                .build();
    }
}
