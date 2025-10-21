package com.twiiiins;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TwiiiinsApplication {

	public static void main(String[] args) {
		// .env 파일 로드
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory("./")  // 프로젝트 루트에서 .env 찾기
					.ignoreIfMissing()  // .env 파일이 없어도 에러 안남
					.load();
			
			// 환경변수로 등록
			dotenv.entries().forEach(entry -> 
				System.setProperty(entry.getKey(), entry.getValue())
			);
		} catch (Exception e) {
			System.out.println(".env 파일을 찾을 수 없거나 로드할 수 없습니다: " + e.getMessage());
		}
		
		SpringApplication.run(TwiiiinsApplication.class, args);
	}

}

