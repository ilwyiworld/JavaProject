package com.yiworld.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Administrator on 2017/10/13.
 * 使用h5的formData提交form表单
 */
@Controller
public class FreemarkController {

    /*@RequestMapping("/")
    public String index(Model model) {
        return "index";
    }*/

    @RequestMapping("/toUpload")
    public String toUpload(Model model) {
        return "upload";
    }

    @RequestMapping("/toFormdata")
    public String formdata(Model model) {
        return "formdata";
    }

    @RequestMapping("/upload")
    @ResponseBody
    public String upload(@RequestParam("file") MultipartFile[] files, String name, HttpServletRequest request) {
        if(files != null){
            for(MultipartFile file : files){
                System.out.println(file.getOriginalFilename());
            }
        }
        System.out.println(request.getParameter("name"));
        return name;
    }

    @RequestMapping("/formdata")
    @ResponseBody
    public String formdata(@RequestParam("file") MultipartFile[] files, String name,HttpServletRequest request) {
        if(files != null){
            for(MultipartFile file : files){
                System.out.println(file.getOriginalFilename());
            }
        }
        System.out.println(request.getParameter("name"));
        return name;
    }
}