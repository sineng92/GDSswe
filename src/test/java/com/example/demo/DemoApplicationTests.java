package com.example.demo;

import com.example.demo.controller.UserInfoController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class DemoApplicationTests {

    @Autowired
    UserInfoController userInfoController;

    @Test
    void contextLoads() {
        assertThat(userInfoController).isNotNull();
    }

}
