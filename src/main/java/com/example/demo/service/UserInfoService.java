package com.example.demo.service;

import com.example.demo.dto.UploadDTO;
import com.example.demo.dto.UserInfoDTO;
import com.example.demo.dto.UserInfoResDTO;
import com.example.demo.entity.UserInfo;
import com.example.demo.repository.UserInfoRepository;
import com.example.demo.util.Constant;
import com.example.demo.util.CsvHelper;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserInfoService {
    @Autowired
    UserInfoRepository userInfoRepository;
    Logger logger = LoggerFactory.getLogger(UserInfoService.class);
    @Autowired
    private ModelMapper modelMapper;

    public UserInfoResDTO getUserInfo() {
        List<UserInfo> userInfo = userInfoRepository.findAll();
        List<UserInfoDTO> userInfoList = entityToDtoList(userInfo);
        UserInfoResDTO userInfoResDTO = new UserInfoResDTO();
        userInfoResDTO.setResult(userInfoList);

        return userInfoResDTO;
    }

    public UserInfoResDTO searchUser(double min, double max, Pageable pageable) {
        UserInfoResDTO userInfoResDTO = new UserInfoResDTO();
        try {
            Page<UserInfo> userInfo = userInfoRepository.search(min, max, pageable);
            List<UserInfoDTO> userInfoList = entityToDtoList(userInfo.getContent());
            userInfoResDTO.setResult(userInfoList);
        } catch (Exception e) {
            logger.error("searchUser failed: " + e);
            throw e;
        }
        return userInfoResDTO;
    }

    private List<UserInfoDTO> entityToDtoList(List<UserInfo> userInfo) {
        Type listType = new TypeToken<List<UserInfoDTO>>() {}.getType();
        return modelMapper.map(userInfo, listType);
    }

    private List<UserInfo> dtoListToEntity(List<UserInfoDTO> userInfoDTO) {
        Type listType = new TypeToken<List<UserInfo>>() {}.getType();
        return modelMapper.map(userInfoDTO, listType);
    }

    @Transactional
    public UploadDTO uploadCsv(MultipartFile[] files) throws Exception {
        UploadDTO uploadDTO = new UploadDTO();
        List<UserInfoDTO> userInfoList = new ArrayList<>();
        try {
            for(MultipartFile file:files){
                if (CsvHelper.hasCSVFormat(file)) {
                    List<UserInfoDTO> resultList = CsvHelper.getUserInfoCsv(file.getInputStream());
                    userInfoList.addAll(resultList);
                }
            }
            List<UserInfo> userInfoEntityList = dtoListToEntity(userInfoList);
            userInfoRepository.saveAll(userInfoEntityList);
            uploadDTO.setSuccess(Constant.upload_success);
        } catch (Exception e) {
            logger.error("uploadCsv failed: " + e);
            throw e;
        }
        return uploadDTO;
    }



    public boolean allowedSort(String sort) {
        ArrayList<String> headers = new ArrayList<>();
        headers.add("name");
        headers.add("salary");
        return headers.contains(sort);
    }
}
