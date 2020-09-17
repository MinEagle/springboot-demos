package com.panda.springbootjpaquerydsl.controller;

import com.panda.springbootjpaquerydsl.model.UserModel;
import com.panda.springbootjpaquerydsl.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class UserController {

    @Resource
    UserService userService;

    @GetMapping("/user")
    public List<UserModel> UserModel() {
        return userService.selectAllUserModelList();
    }

    @Bean
    public RouterFunction<ServerResponse> fooFunction() {
        return RouterFunctions.route()
                .GET("/v1/foo", request -> ServerResponse.ok()
                        .body(userService.selectFirstUser()))
                .build();
    }


}
