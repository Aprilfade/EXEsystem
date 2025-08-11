package com.ice.exebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching; // 导入注解

@SpringBootApplication
@EnableCaching // <-- 添加此注解
public class ExeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExeBackendApplication.class, args);
	}

}