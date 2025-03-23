package cit.edu.mmr;

import cit.edu.mmr.util.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class MmrApplication {

	public static void main(String[] args) {
		SpringApplication.run(MmrApplication.class, args);
	}

}
