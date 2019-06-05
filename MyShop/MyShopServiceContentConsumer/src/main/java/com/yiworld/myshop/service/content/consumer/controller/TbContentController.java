package com.yiworld.myshop.service.content.consumer.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.github.pagehelper.PageInfo;
import com.yiworld.myshop.commons.domain.TbContent;
import com.yiworld.myshop.service.content.api.TbContentService;
import com.yiworld.myshop.statics.backend.dto.DataTableDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/content")
public class TbContentController {

    @Reference(version = "${services.versions.content.v1}")
    private TbContentService tbContentService;

    //跳转到列表页
    @RequestMapping(value = "list", method = RequestMethod.GET)
    public String list() {
        return "content/list";
    }

    /**
     * 跳转到表单页
     * @return
     */
    @RequestMapping(value = "form", method = RequestMethod.GET)
    public String form() {
        return "content/form";
    }

    @ResponseBody
    @RequestMapping(value = "page", method = RequestMethod.GET)
    public DataTableDTO<TbContent> page(HttpServletRequest request) {
        String strDraw = request.getParameter("draw");
        String strStart = request.getParameter("start");
        String strLength = request.getParameter("length");

        int draw = strDraw == null ? 0 : Integer.parseInt(strDraw);
        int start = strStart == null ? 0 : Integer.parseInt(strStart);
        int length = strLength == null ? 10 : Integer.parseInt(strLength);

        PageInfo<TbContent> pageInfo = tbContentService.page(start, length);
        // 封装 DataTables 需要的结果
        DataTableDTO<TbContent> dataTableDTO = new DataTableDTO<>();
        dataTableDTO.setData(pageInfo.getList());
        dataTableDTO.setDraw(draw);
        dataTableDTO.setError("");
        dataTableDTO.setRecordsTotal(pageInfo.getTotal());
        dataTableDTO.setRecordsFiltered(pageInfo.getTotal());
        return dataTableDTO;
    }

}
