package E_Realm.service;


import E_Realm.dao.PermissionDao;
import E_Realm.dao.PermissionDaoImpl;
import E_Realm.entity.Permission;

public class PermissionServiceImpl implements PermissionService {

    private PermissionDao permissionDao = new PermissionDaoImpl();

    public Permission createPermission(Permission permission) {
        return permissionDao.createPermission(permission);
    }

    public void deletePermission(Long permissionId) {
        permissionDao.deletePermission(permissionId);
    }
}
