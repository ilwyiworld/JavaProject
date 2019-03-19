package B_Authorization.permission;

import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.RolePermissionResolver;
import org.apache.shiro.authz.permission.WildcardPermission;

import java.util.Arrays;
import java.util.Collection;


/**
 * Created by Administrator on 2017/7/17.
 * RolePermissionResolver用于根据角色解析相应的权限集合
 */
public class MyRolePermissionResolver implements RolePermissionResolver {

    public Collection<Permission> resolvePermissionsInRole(String roleString) {
        if("role1".equals(roleString)) {
            //此处的实现很简单，如果用户拥有role1，那么就返回一个“menu:*”的权限。
            return Arrays.asList((Permission)new WildcardPermission("menu:*"));
        }
        return null;
    }
}