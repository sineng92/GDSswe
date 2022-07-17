package com.example.demo.controller;

import com.example.demo.dto.UploadDTO;
import com.example.demo.dto.UserInfoResDTO;
import com.example.demo.exceptions.ServiceException;
import com.example.demo.service.UserInfoService;
import com.example.demo.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserInfoController {

    @Autowired
    UserInfoService userInfoService;

    @GetMapping(value = "/getUserInfo", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserInfoResDTO getUserInfo() {
        return userInfoService.getUserInfo();
    }

    @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserInfoResDTO> searchUser(
            @RequestParam(value = "min", defaultValue = "0.0", required = false) double min,
            @RequestParam(value = "max", defaultValue = "4000.0", required = false) double max,
            @RequestParam(value = "offset", defaultValue = "0", required = false) int offset,
            @RequestParam(value = "limit", defaultValue = "0", required = false) int limit,
//            @RequestParam(value = "sort", required = false) sorting sort)
            @RequestParam(value = "sort", required = false) String sort) {
        try {
            String newSort = sort;
            Pageable pageable;

            if (limit == 0) {
                limit = Integer.MAX_VALUE;
            }

            if (newSort != null) {
                newSort = sort.toLowerCase();
                if (userInfoService.allowedSort(newSort)) {
                    pageable = PageRequest.of(offset, limit, Sort.by(newSort).ascending());
                } else {
                    pageable = PageRequest.of(offset, limit);
                }
            } else {
                pageable = PageRequest.of(offset, limit);
            }
            return new ResponseEntity(userInfoService.searchUser(min, max, pageable), HttpStatus.OK);
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }

    }

    @PostMapping(path = "/upload", produces = MediaType.APPLICATION_JSON_VALUE, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<UploadDTO> uploadCsv(@RequestParam("file") MultipartFile[] file) {
        UploadDTO uploadDTO = new UploadDTO();
        try {
            for (MultipartFile eachFile : file) {
                userInfoService.uploadCsv(eachFile);
                uploadDTO.setSuccess(Constant.upload_success);
            }
            return new ResponseEntity(uploadDTO, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            uploadDTO.setSuccess(Constant.upload_fail);
            result.put("success", uploadDTO.getSuccess());
            result.put("error", e.getMessage());
            return new ResponseEntity(result, HttpStatus.BAD_REQUEST);
//            throw new ServiceException(e.getMessage());
        }
    }

//    enum sorting {
//        name,
//        salary
//    }
}
