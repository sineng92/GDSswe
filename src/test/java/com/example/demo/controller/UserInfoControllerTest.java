package com.example.demo.controller;

import com.example.demo.dto.UserInfoDTO;
import com.example.demo.dto.UserInfoResDTO;
import com.example.demo.exceptions.ServiceException;
import com.example.demo.service.UserInfoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserInfoController.class)
class UserInfoControllerTest {

    @MockBean
    UserInfoService userInfoService;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void searchUser() throws Exception {
        UserInfoResDTO userInfoResDTO = new UserInfoResDTO();
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setName("ooi");
        userInfoDTO.setSalary(1000.00);
        List<UserInfoDTO> userInfoListDTO = Arrays.asList(userInfoDTO);
        userInfoResDTO.setResult(userInfoListDTO);

        Mockito.when(userInfoService.searchUser(0, 4000, PageRequest.of(0, 10))).thenReturn(userInfoResDTO);
        Mockito.when(userInfoService.allowedSort("name")).thenReturn(true);

        var content = mockMvc.perform(MockMvcRequestBuilders
                        .get("/users?min=0&max=4000&offset=0&limit=10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Mockito.verify(userInfoService, times(1)).searchUser(0, 4000, PageRequest.of(0, 10));

        var userInfoResult = objectMapper.readValue(content, UserInfoResDTO.class);
        assertEquals(userInfoResult.getResult().get(0).getName(), userInfoDTO.getName());
        assertEquals(userInfoResult.getResult().get(0).getSalary(), userInfoDTO.getSalary());
    }

    @Test
    void searchUserLimit0() throws Exception {
        UserInfoResDTO userInfoResDTO = new UserInfoResDTO();
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setName("ooi");
        userInfoDTO.setSalary(1000.00);
        List<UserInfoDTO> userInfoListDTO = Arrays.asList(userInfoDTO);
        userInfoResDTO.setResult(userInfoListDTO);

        Mockito.when(userInfoService.searchUser(0, 4000, PageRequest.of(0, 2147483647, Sort.by("name").ascending()))).thenReturn(userInfoResDTO);
        Mockito.when(userInfoService.allowedSort("name")).thenReturn(true);

        // cover limit 0
        var content = mockMvc.perform(MockMvcRequestBuilders
                        .get("/users?min=0&max=4000&offset=0&limit=0&sort=Name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Mockito.verify(userInfoService, times(1)).searchUser(0, 4000, PageRequest.of(0, 2147483647, Sort.by("name").ascending()));

        var userInfoResult = objectMapper.readValue(content, UserInfoResDTO.class);
        assertEquals(userInfoResult.getResult().get(0).getName(), userInfoDTO.getName());
        assertEquals(userInfoResult.getResult().get(0).getSalary(), userInfoDTO.getSalary());
    }

    @Test
    void searchUserIllegalSort() throws Exception {
        UserInfoResDTO userInfoResDTO = new UserInfoResDTO();
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setName("ooi");
        userInfoDTO.setSalary(1000.00);
        List<UserInfoDTO> userInfoListDTO = Arrays.asList(userInfoDTO);
        userInfoResDTO.setResult(userInfoListDTO);

        Mockito.when(userInfoService.searchUser(0, 4000, PageRequest.of(0, 2147483647))).thenReturn(userInfoResDTO);
        Mockito.when(userInfoService.allowedSort("name")).thenReturn(true);

        // cover sorting with illegal
        var content = mockMvc.perform(MockMvcRequestBuilders
                        .get("/users?min=0&max=4000&offset=0&limit=0&sort=test")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Mockito.verify(userInfoService, times(1)).searchUser(0, 4000, PageRequest.of(0, 2147483647));

        var userInfoResult = objectMapper.readValue(content, UserInfoResDTO.class);
        assertEquals(userInfoResult.getResult().get(0).getName(), userInfoDTO.getName());
        assertEquals(userInfoResult.getResult().get(0).getSalary(), userInfoDTO.getSalary());
    }

    @Test
    void searchUserThrowServiceException() {
        Mockito.when(userInfoService.searchUser(0, 4000, PageRequest.of(0, 2147483647))).thenThrow(new ServiceException("exception"));
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/users?min=0&max=4000&offset=0&limit=0&sort=test")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        } catch (Exception e) {
            assertEquals("exception", e.getMessage());
        }
    }

    @Test
    void searchUserThrowMethodException() {
        //pass in parameter with wrong data type. eg max = A
        try {
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/users?min=0&max=A&offset=0&limit=0&sort=test")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is4xxClientError());
        } catch (Exception e) {
            assertEquals("exception", e.getMessage());
        }
    }

    @Test
    void uploadCsv() throws Exception {
        String fileName = "sample-file-mock.csv";
        MockMultipartFile sampleFile = new MockMultipartFile(
                "file",
                fileName,
                "text/csv",
                "This is the file content".getBytes());

        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/upload");
        mockMvc.perform(multipartRequest.file(sampleFile))
                .andExpect(status().isOk());
    }

    @Test
    void uploadCsvThrowExecption() throws Exception {
        String fileName = "sample-file-mock.csv";
        MockMultipartFile sampleFile = new MockMultipartFile(
                "file",
                fileName,
                "text/csv",
                "This is the file content".getBytes());

        Mockito.when(userInfoService.uploadCsv(any())).thenThrow(new Exception());
        MockMultipartHttpServletRequestBuilder multipartRequest =
                MockMvcRequestBuilders.multipart("/upload");

        mockMvc.perform(multipartRequest.file(sampleFile))
                .andExpect(status().is4xxClientError());
    }
}