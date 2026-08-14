package com.nightchallenge.backend.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 용도: 공통 CORS 설정 검증.
 * 환경변수로 전달된 프론트엔드 출처의 API 접근 허용 여부를 확인한다.
 */
@SpringJUnitWebConfig(classes = {WebConfig.class, WebConfigTest.TestConfig.class})
@TestPropertySource(properties = "app.cors.allowed-origins=http://localhost:5173, https://night-path.example")
class WebConfigTest {

    private final MockMvc mockMvc;

    /**
     * 용도: CORS 테스트 환경 구성.
     * Spring MVC 설정이 적용된 MockMvc를 생성한다.
     */
    WebConfigTest(WebApplicationContext context) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    /**
     * 용도: 로컬 프론트 출처 허용 검증.
     * 기본 로컬 주소에서 전달된 사전 요청에 허용 헤더가 반환되는지 확인한다.
     */
    @Test
    void allowsLocalFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/cors-test")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    /**
     * 용도: 추가 배포 출처 허용 검증.
     * 쉼표로 추가한 배포 주소가 공백 제거 후 허용되는지 확인한다.
     */
    @Test
    void allowsAdditionalDeploymentOrigin() throws Exception {
        mockMvc.perform(options("/api/cors-test")
                        .header("Origin", "https://night-path.example")
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://night-path.example"));
    }

    /**
     * 용도: 미허용 출처 차단 검증.
     * 허용 목록에 없는 출처의 사전 요청이 거부되는지 확인한다.
     */
    @Test
    void rejectsUnknownOrigin() throws Exception {
        mockMvc.perform(options("/api/cors-test")
                        .header("Origin", "https://unknown.example")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Configuration
    @EnableWebMvc
    static class TestConfig {

        /**
         * 용도: CORS 검증용 API 제공.
         * 공통 설정이 적용될 최소 API Controller를 등록한다.
         */
        @Bean
        TestController testController() {
            return new TestController();
        }
    }

    @RestController
    static class TestController {

        /**
         * 용도: CORS 검증 응답 제공.
         * 사전 요청이 참조할 테스트 API 경로를 제공한다.
         */
        @GetMapping("/api/cors-test")
        String corsTest() {
            return "ok";
        }
    }
}
