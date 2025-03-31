package cit.edu.mmr;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
//@EnableConfigurationProperties(FileStorageProperties.class)
@ComponentScan(basePackages = "cit.edu.mmr")
@EnableJpaRepositories("cit.edu.mmr.repository")
public class MmrApplication {

	public static void main(String[] args) {
		SpringApplication.run(MmrApplication.class, args);
	}

}
