package com.openshift.bucket.services;

import com.openshift.bucket.models.Request;
import com.openshift.bucket.models.Response;
import io.quarkus.qute.Template;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;


@ApplicationScoped
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    /**
     * Amazon S3 client Inject
     */
     S3Client s3Client;

    /**
     * Text file template
     */
    @Inject
    Template amazonS3;

    /**
     * Amazon S3 Configuration
     */
    @ConfigProperty(name = "quarkus.s3.aws.region")
    String region;
    @ConfigProperty(name = "quarkus.s3.endpoint-override")
    String amazonS3Url;
    @ConfigProperty(name = "bucket.name")
    String bucketName;
    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.access-key-id")
    String amazonS3AccessKeyId;
    @ConfigProperty(name = "quarkus.s3.aws.credentials.static-provider.secret-access-key")
    String amazonS3SecretKey;

    @PostConstruct
    void init() {
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(amazonS3AccessKeyId, amazonS3SecretKey)))
                .endpointOverride(URI.create(amazonS3Url))
                .build();
    }

    /**
     * Service - create txt file and upload the created file to the Amazon S3
     */
    public Response createAndUploadFileService(Request request) {

        Response response = new Response();
        try {
            logger.info("Amazon S3 Request :- {}", request);

            String fileContent = getTxtFileContent(request).subscribeAsCompletionStage().join();
            logger.info("Created text file :- {}", fileContent);

            String fileName = getFileName(request).subscribeAsCompletionStage().join();
            logger.info("Created text file name :- {}", fileName);

            response = uploadTheFile(fileContent, fileName).subscribeAsCompletionStage().join();
            logger.info("File upload response :- {}", response);

        }catch (Exception e){
            logger.error("Something went wrong :- ",e);
        }

        return response;
    }

    /**
     * Create The File
     */
    private Uni<String> getTxtFileContent(Request request){
        return Uni.createFrom().item(()->amazonS3.data("file_date", request.getFileData()).render());
    }

    /**
     * Create the file name
     */
    private Uni<String> getFileName(Request request){
        return Uni.createFrom().item(()-> request.getFileName() + LocalDateTime.now() + ".txt");
    }

    /**
     * Upload the file to the Amazon S3
     */
    private Uni<Response> uploadTheFile(String fileContent, String fileName){
        Response response = new Response();
        return Uni.createFrom().item(()-> {
            try {
                InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes(StandardCharsets.UTF_8));

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build();

                PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, fileContent.length()));

                logger.info("File Store Response: {}", putObjectResponse.eTag());

                response.setErrorCode("0000");
                response.setErrorDescription("File uploaded successfully");
                response.setExceptionMessage("");
            }catch (Exception e){
                response.setErrorCode("500");
                response.setErrorDescription("File upload failed");
                response.setExceptionMessage(e.getMessage());
            }

            return response;
        });
    }
}
