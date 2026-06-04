package com.codegroup.portfolio.service.member;

import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import org.springframework.http.HttpMethod;

class HttpMemberGatewayTest {

    private static final String BASE_URL = "http://localhost:8080/api/v1/external/members";

    private RestTemplate restTemplate;
    private MockRestServiceServer server;
    private HttpMemberGateway gateway;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        gateway = new HttpMemberGateway(restTemplate, BASE_URL);
    }

    @Test
    void shouldReturnMemberWhenApiResponds() {
        server.expect(requestTo(BASE_URL + "/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"id\":1,\"name\":\"Maria\",\"attribution\":\"FUNCIONARIO\"}",
                        MediaType.APPLICATION_JSON));

        MemberResponse member = gateway.getById(1L);

        assertThat(member.id()).isEqualTo(1L);
        assertThat(member.name()).isEqualTo("Maria");
        server.verify();
    }

    @Test
    void shouldThrowNotFoundWhenApiReturns404() {
        server.expect(requestTo(BASE_URL + "/99"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> gateway.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
