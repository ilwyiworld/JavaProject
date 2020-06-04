package com.yiworld.client.service;

import com.yiworld.client.service.impl.command.PrintAllCommand;
import com.yiworld.client.util.SpringBeanFactory;
import com.yiworld.common.enums.SystemCommandEnum;
import com.yiworld.common.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class InnerCommandContext {
    /**
     * 获取执行器实例
     * @param command 执行器实例
     * @return
     */
    public InnerCommand getInstance(String command) {

        Map<String, String> allClazz = SystemCommandEnum.getAllClazz();

        //兼容需要命令后接参数的数据 :q cross
        String[] trim = command.trim().split(" ");
        String clazz = allClazz.get(trim[0]);
        InnerCommand innerCommand = null;
        try {
            if (StringUtil.isEmpty(clazz)){
                clazz = PrintAllCommand.class.getName() ;
            }
            innerCommand = (InnerCommand) SpringBeanFactory.getBean(Class.forName(clazz));
        } catch (Exception e) {
            log.error("Exception", e);
        }

        return innerCommand;
    }

}
