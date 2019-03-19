package B_Authorization;

/**
 * Created by Administrator on 2017/7/17.
 */

import Z_Common.BaseTest;
import junit.framework.Assert;
import org.junit.Test;

public class AuthorizerTest extends BaseTest {

    @Test
    public void testIsPermitted() {
        login("classpath:B_Authorization/authorizer.ini", "zhang", "123");
        //判断拥有权限：user:update
        Assert.assertTrue(subject().isPermitted("user1:update"));
        Assert.assertTrue(subject().isPermitted("user2:update"));
        //通过二进制位的方式表示权限
        Assert.assertTrue(subject().isPermitted("+user1+2"));//新增权限
        Assert.assertTrue(subject().isPermitted("+user1+8"));//查看权限
        Assert.assertTrue(subject().isPermitted("+user2+10"));//新增及查看

        Assert.assertFalse(subject().isPermitted("+user1+4"));//没有删除权限

        Assert.assertTrue(subject().isPermitted("menu:view"));//通过MyRolePermissionResolver解析得到的权限
    }

    @Test
    public void testIsPermitted2() {
        login("classpath:B_Authorization/jdbc-authorizer.ini", "zhang", "123");
        //判断拥有权限：user:update
        Assert.assertTrue(subject().isPermitted("user1:update"));
        Assert.assertTrue(subject().isPermitted("user2:update"));
        //通过二进制位的方式表示权限
        Assert.assertTrue(subject().isPermitted("+user1+2"));//新增权限
        Assert.assertTrue(subject().isPermitted("+user1+8"));//查看权限
        Assert.assertTrue(subject().isPermitted("+user2+10"));//新增及查看

        Assert.assertFalse(subject().isPermitted("+user1+4"));//没有删除权限

        Assert.assertTrue(subject().isPermitted("menu:view"));//通过MyRolePermissionResolver解析得到的权限
    }
}
