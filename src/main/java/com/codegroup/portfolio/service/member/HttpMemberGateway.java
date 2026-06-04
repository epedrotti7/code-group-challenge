package com.codegroup.portfolio.service.member;

import com.codegroup.portfolio.dto.member.MemberResponse;
import com.codegroup.portfolio.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class HttpMemberGateway implements MemberGateway {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HttpMemberGateway(RestTemplate restTemplate,
                             @Value("${member.api.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public MemberResponse getById(Long id) {
        try {
            MemberResponse member = restTemplate.getForObject(baseUrl + "/{id}", MemberResponse.class, id);
            if (member == null) {
                throw ResourceNotFoundException.of("Membro", id);
            }
            return member;
        } catch (HttpClientErrorException.NotFound ex) {
            throw ResourceNotFoundException.of("Membro", id);
        }
    }
}
