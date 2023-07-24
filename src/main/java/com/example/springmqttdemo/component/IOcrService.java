package com.example.springmqttdemo.component;

import com.example.springmqttdemo.config.BaiduOcrProperties;
import com.example.springmqttdemo.model.IdcardInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Encoder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
@Slf4j
public class IOcrService {


    private final BaiduOcrProperties baiduOcrProperties;

    //固定字符
    private static List<String> FIXED_STR;


    static {
        FIXED_STR=Arrays.asList("姓名", "性别", "民族", "出生", "住址", "公民身份号码", "签发机关", "有效期限");
    }



    /**
     * 请求ocr接口，返回json数据
     * @param multipartFile 文件
     * @return json 字符串数据
     */
    public String actionOcr(MultipartFile file) {
        IdcardInfo idcardInfo = new IdcardInfo();
        try {
            //接收上传文件
//            //Receiving uploaded files
//            String fileName = System.currentTimeMillis()+file.getOriginalFilename();
//            File destFile = new File("C:\\Users\\zsswa\\Desktop\\ocr\\"+fileName);
//            destFile.getParentFile().mkdirs();
//            System.out.println(destFile);
//            file.transferTo(destFile);

            byte[] bytes = file.getBytes();
            BASE64Encoder encoder = new BASE64Encoder();
            String base64Img = encoder.encode(bytes).trim();

            //开始准备请求API
            //Start preparing the request API
            //创建请求头
            //Create request header
            HttpHeaders headers = new HttpHeaders();
            //设置请求头格式
            //Set the request header format
            headers.setContentType(MediaType.APPLICATION_JSON);
            //构建请求参数
            //Build request parameters
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            //添加请求参数images，并将Base64编码的图片传入
            //Add the request parameter Images and pass in the Base64 encoded image
            map.add("images", base64Img);
            //构建请求
            //Build request
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
            RestTemplate restTemplate = new RestTemplate();
            //发送请求
            //Send the request
            Map json = restTemplate.postForEntity("http://192.168.0.46:9977/predict/ocr_system", request, Map.class).getBody();
            System.out.println(json);
            //解析Json返回值
            //Parse the Json return value
            List<List<Map>> json1 = (List<List<Map>>) json.get("results");

            //获取身份证号
            getIdCardNum(json1,idcardInfo);
            //获取姓名
            getName(json1,idcardInfo);
            //获取民族
            getNation(json1,idcardInfo);
            //获取出生日期
            getBirth(json1,idcardInfo);
            //获取住址
            getAddress(json1,idcardInfo);





            //获取签发机关
            getIssue(json1,idcardInfo);
            //获取有效期限
            getValid(json1,idcardInfo);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "上传失败," + e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
            return "上传失败," + e.getMessage();
        }
        return idcardInfo.toString();
    }

    //获取身份证号
    private void getIdCardNum(List<List<Map>> json,IdcardInfo idcardInfo) {
        Pattern idPattern = Pattern.compile("\\d{16,18}");
        Pattern xPattern = Pattern.compile("\\d*[Xx]");
        for (int i = 0; i < json.get(0).size(); i++) {
            String text = json.get(0).get(i).get("text").toString();
            if (FIXED_STR.contains(text)) {
                continue;
            }
            Matcher idMatcher = idPattern.matcher(text);
            Matcher xMatcher = xPattern.matcher(text);

            if (xMatcher.find()) {
                String res = xMatcher.group();
                if (res.length() == 18) {
                    idcardInfo.setIdNumber(res.replace("号码", ""));
                    idcardInfo.setSex((Integer.parseInt(res.substring(16, 17)) % 2 == 0) ? "女" : "男");
                }
            } else if (idMatcher.find()) {
                String res = idMatcher.group();
                if (res.length() == 18) {
                    idcardInfo.setIdNumber(res.replace("号码", ""));
                    idcardInfo.setSex((Integer.parseInt(res.substring(16, 17)) % 2 == 0) ? "女" : "男");
                }
            }
            if (idcardInfo.getIdNumber()!=null) {
                break;
            }
        }
    }

    //获取姓名
    private void getName(List<List<Map>> json,IdcardInfo idcardInfo) {
        Pattern namePattern = Pattern.compile("[\u4e00-\u9fa5]{2,4}");
        for (int i = 0; i < json.get(0).size(); i++) {
            String text = json.get(0).get(i).get("text").toString();
            if (FIXED_STR.contains(text)) {
                continue;
            }
            if (text.startsWith("姓名")) {
                idcardInfo.setName(text.replace("姓名", ""));
            }else {
                Matcher nameMatcher = namePattern.matcher(text);
                if (nameMatcher.find()) {
                    //获取姓名
                    String res = nameMatcher.group();
                        idcardInfo.setName(res);

                }
            }
            if (idcardInfo.getName()!=null) {
                break;
            }
        }
    }

    //获取民族
    private void getNation(List<List<Map>> json,IdcardInfo idcardInfo) {

        for (int i = 0; i < json.get(0).size(); i++) {
            String text = json.get(0).get(i).get("text").toString();
            if (FIXED_STR.contains(text)) {
                continue;
            }
            if (text.startsWith("民族")) {
                idcardInfo.setNation(text.replace("民族", ""));
            }
            if (text.startsWith("族")) {
                idcardInfo.setNation(text.replace("族", ""));
            }
            if (idcardInfo.getNation()!=null) {
                break;
            }
        }
    }

    //获取出生日期
    private void getBirth(List<List<Map>> json,IdcardInfo idcardInfo) {
        Pattern birthPattern = Pattern.compile("\\d{4}年\\d{1,2}月\\d{1,2}日");
        for (int i = 0; i < json.get(0).size(); i++) {
            String text = json.get(0).get(i).get("text").toString();
            if (FIXED_STR.contains(text)) {
                continue;
            }
            if (text.startsWith("出生")) {
                idcardInfo.setBirth(text.replace("出生", ""));
            }else {
                Matcher birthMatcher = birthPattern.matcher(text);
                if (birthMatcher.find()) {
                    String res = birthMatcher.group();
                    idcardInfo.setBirth(res);
                }
            }
            if (idcardInfo.getBirth()!=null) {
                break;
            }
        }
    }

    //获取住址
    private void getAddress(List<List<Map>> json,IdcardInfo idcardInfo) {
        //大于8个汉字
        Pattern addressPattern = Pattern.compile("[\u4e00-\u9fa5]{8,}");
        for (int i = 0; i < json.get(0).size(); i++) {
            String text = json.get(0).get(i).get("text").toString();
            if (FIXED_STR.contains(text)) {
                continue;
            }
            if (text.startsWith("住址")) {
                idcardInfo.setAddress(text.replace("住址", ""));
            }else {
                Matcher addressMatcher = addressPattern.matcher(text);
                if (addressMatcher.find()) {
                    String res = addressMatcher.group();
                    idcardInfo.setAddress(res);
                }
            }
            if (idcardInfo.getAddress()!=null) {
                if (i+1<json.get(0).size()) {
                    String text1 = json.get(0).get(i+1).get("text").toString();
                    if (!FIXED_STR.contains(text)) {
                        idcardInfo.setAddress(idcardInfo.getAddress()+text1);
                    }
                }
                break;
            }
        }
    }

    //获取签发机关
    private void getIssue(List<List<Map>> json,IdcardInfo idcardInfo) {
        Pattern issuePattern = Pattern.compile("[\u4e00-\u9fa5]{8,}");
        for (int i = 0; i < json.get(0).size(); i++) {
            String text = json.get(0).get(i).get("text").toString();
            if (FIXED_STR.contains(text)) {
                continue;
            }
            if (text.startsWith("签发机关")) {
                idcardInfo.setIssue(text.replace("签发机关", ""));
            }else {
                Matcher issueMatcher = issuePattern.matcher(text);
                if (issueMatcher.find()) {
                    String res = issueMatcher.group();
                    idcardInfo.setIssue(res);
                }
            }
            if (idcardInfo.getIssue()!=null) {
                break;
            }
        }
    }

    //获取有效期限
    private void getValid(List<List<Map>> json,IdcardInfo idcardInfo) {
        Pattern validPattern = Pattern.compile("\\d{4}.\\d{1,2}.\\d{1,2}-\\d{4}.\\d{1,2}.\\d{1,2}.");
        for (int i = 0; i < json.get(0).size(); i++) {
            String text = json.get(0).get(i).get("text").toString();
            if (FIXED_STR.contains(text)) {
                continue;
            }
            if (text.startsWith("有效期限")) {
                idcardInfo.setValidPeriod(text.replace("有效期限", ""));
            } else {
                Matcher validMatcher = validPattern.matcher(text);
                if (validMatcher.find()) {
                    String res = validMatcher.group();
                    idcardInfo.setValidPeriod(res);
                }
            }
            if (idcardInfo.getValidPeriod() != null) {
                break;
            }
        }
    }




}
