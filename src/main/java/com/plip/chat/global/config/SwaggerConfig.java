package com.plip.chat.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	public static final String BEARER_AUTH_SCHEME = "bearerAuth";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Chat Service API")
						.description("plip-chat REST API")
						.version("v1.0.0"))
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description(
										"Gateway JWT 인증. 클라이언트는 Bearer 토큰을 전달하고, "
												+ "Gateway가 검증 후 downstream에 X-User-UUID를 주입합니다."
								)));
	}
}
