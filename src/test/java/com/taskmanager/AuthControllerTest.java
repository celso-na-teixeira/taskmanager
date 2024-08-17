package com.taskmanager;

import com.taskmanager.dto.UserLoginDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {

    @Autowired
    TestRestTemplate restTemplate;



    private static final String BASE_URL = "/api/v1/taskmanager/auth";

    @Test
    void shouldLoginUser() {
        String encodedPassword = new BCryptPasswordEncoder().encode("password123");
        UserLoginDTO userDTO = new UserLoginDTO("leonardo", encodedPassword);

        ResponseEntity<String> response = restTemplate
                .postForEntity(BASE_URL + "/login", userDTO, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        String jwtToken = response.getBody();
        assertThat(jwtToken).isNotNull();
    }


}
