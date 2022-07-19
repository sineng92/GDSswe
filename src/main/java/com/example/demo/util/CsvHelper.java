package com.example.demo.util;

import com.example.demo.dto.UserInfoDTO;
import com.example.demo.entity.UserInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvHelper {

    public static String TYPE = "text/csv";

    public static boolean allHeaderSame(List<String> header) {
        ArrayList<String> headers = new ArrayList<>();
        headers.add("Name");
        headers.add("Salary");
        return header.containsAll(headers);
    }

    public static boolean hasCSVFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    @Async
    public static List<UserInfo> getUserInfoCsv(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(fileReader,
                     CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim())) {
            List<UserInfo> userInfoDTOList = new ArrayList<>();
            Iterable<CSVRecord> csvRecords = csvParser.getRecords();
            if (csvParser.getHeaderMap().size() > 2) {
                throw new RuntimeException("There is an extra column in the csv");
            } else {

                if (allHeaderSame(csvParser.getHeaderNames())) {
                    for (CSVRecord csvRecord : csvRecords) {
                        double salary = Double.parseDouble(csvRecord.get("Salary"));
                        if (salary > 0) {
                            UserInfo userInfo = new UserInfo();
                            userInfo.setName(csvRecord.get("Name"));
                            userInfo.setSalary(salary);
                            userInfoDTOList.add(userInfo);
                        }
                    }
                } else {
                    throw new RuntimeException("Wrong information in csv");
                }

            }
            return userInfoDTOList;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse CSV file: " + e.getMessage());
        }
    }
}
