package com.example.demo.service;

import com.example.demo.controller.UserInfoController;
import com.example.demo.dto.UserInfoDTO;
import com.example.demo.dto.UserInfoResDTO;
import com.example.demo.entity.UserInfo;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.util.CsvHelper;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class UserInfoServiceTest {

    @InjectMocks
    UserInfoService userInfoService;

    @Mock
    UserInfoRepository userInfoRepository;

    @Mock
    private ModelMapper mockModelMapper;
    private UserInfoResDTO userInfoResDTO = new UserInfoResDTO();
    private UserInfoDTO userInfoDTO = new UserInfoDTO();
    private UserInfo userInfoEntity = new UserInfo();
    private List<UserInfoDTO> userInfoDTOList = new ArrayList<>();
    private List<UserInfo> userInfoEntityList = new ArrayList<>();
    private CsvHelper csvHelper;

    @BeforeEach
    public void setUp() {
        //pre set entity
        userInfoEntity.setName("ooi");
        userInfoEntity.setSalary(1000.00);
        userInfoEntityList.add(userInfoEntity);

        // pre set dto
        userInfoDTO.setName("ooi");
        userInfoDTO.setSalary(1000.00);
        userInfoDTOList.add(userInfoDTO);
        userInfoResDTO.setResult(userInfoDTOList);

    }
    @Test
    void searchUser() {
        // convert entity to dto, return dto
        Type listType = new TypeToken<List<UserInfoDTO>>() {}.getType();
        Mockito.when(mockModelMapper.map(userInfoEntityList, listType)).thenReturn(userInfoDTOList);
        Page<UserInfo> userInfoList = new PageImpl(userInfoEntityList);

        Mockito.when(userInfoRepository.search(0,4000.0, PageRequest.of(0, 10))).thenReturn(userInfoList);

        var result = userInfoService.searchUser(0,4000.0, PageRequest.of(0, 10));

        Mockito.verify(userInfoRepository, times(1)).search(0,4000.0, PageRequest.of(0, 10));
        assertEquals(result.getResult().size(),userInfoList.getContent().size());
        assertEquals(result.getResult(),userInfoResDTO.getResult());
    }

    @Test
    void searchUserThrowException() {
        Mockito.when(userInfoRepository.search(0,4000.0, PageRequest.of(0, 10))).thenThrow(new RuntimeException("Exception"));
        try {
            userInfoService.searchUser(0,4000.0, PageRequest.of(0, 10));
        }catch (Exception e) {
            assertEquals("Exception", e.getMessage());
        }
    }
    @Test
    void uploadCsv() throws Exception {
        InputStream uploadStream = UserInfoServiceTest.class.getClassLoader().getResourceAsStream("Book1.csv");
        MockMultipartFile file = new MockMultipartFile("file.csv", "","text/csv",uploadStream);
        var result = userInfoService.uploadCsv(new MockMultipartFile[]{file});
        assertEquals(result.getSuccess(),1);
    }

    @Test
    void uploadCsvThrowExceptionExtraColumn() throws Exception {
        InputStream uploadStream = UserInfoServiceTest.class.getClassLoader().getResourceAsStream("Book2.csv");
        MockMultipartFile file = new MockMultipartFile("file.csv", "","text/csv",uploadStream);
        try {
            userInfoService.uploadCsv(new MockMultipartFile[]{file});
        }catch (Exception e) {
            assertEquals("There is an extra column in the csv", e.getMessage());
        }
    }

    @Test
    void uploadCsvThrowExceptionWrongInfo() throws Exception {
        InputStream uploadStream = UserInfoServiceTest.class.getClassLoader().getResourceAsStream("Book3.csv");
        MockMultipartFile file = new MockMultipartFile("file.csv", "","text/csv",uploadStream);
        try {
            userInfoService.uploadCsv(new MockMultipartFile[]{file});
        }catch (Exception e) {
            assertEquals("Wrong information in csv", e.getMessage());
        }
    }


    @Test
    void allowedSort() {
        String sort = "name";
        var result = userInfoService.allowedSort(sort);
        assertTrue(result);
    }
}