package babycareai.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sagemakerruntime.SageMakerRuntimeClient;

@Configuration
public class SageMakerConfig {

    @Value("${sagemaker.region}")
    private String sagemakerRegion;

    @Value("${s3.credentials.access-key}")
    private String accessKey;

    @Value("${s3.credentials.secret-key}")
    private String secretKey;

    @Bean
    public SageMakerRuntimeClient sageMakerRuntimeClient() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return SageMakerRuntimeClient.builder()
                .region(Region.of(sagemakerRegion))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }
}
