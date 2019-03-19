package C_Config;

import junit.framework.Assert;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.apache.shiro.mgt.SecurityManager;
import org.junit.Test;
/**
 * Created by Administrator on 2017/7/18.
 */
public class IniMainTest {

    @Test
    public void test() {
        Factory<SecurityManager> factory =
                new IniSecurityManagerFactory("classpath:C_Config/config-main.ini");

        SecurityManager securityManager = factory.getInstance();
        //将SecurityManager设置到SecurityUtils 方便全局使用
        SecurityUtils.setSecurityManager(securityManager);

        Subject subject = SecurityUtils.getSubject();

        UsernamePasswordToken token = new UsernamePasswordToken("zhang", "123");
        subject.login(token);
        Assert.assertTrue(subject.isAuthenticated());
    }
}