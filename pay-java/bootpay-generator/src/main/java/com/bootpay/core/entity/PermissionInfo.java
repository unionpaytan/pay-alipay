package com.bootpay.core.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2019-08-14
 */
@TableName("permission_info")
public class PermissionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    private Integer id;

    /**
     * 资源名称
     */
    @TableField("MENU_NAME")
    private String menuName;

    /**
     * 资源路径
     */
    @TableField("MENU_URL")
    private String menuUrl;

    /**
     * 权限标识
     */
    @TableField("IDENTIFIER")
    private String identifier;

    /**
     * 1:一级菜单  2:二级菜单  3:按钮
     */
    @TableField("IS_MENU")
    private String isMenu;

    /**
     * 父级资源ID
     */
    @TableField("P_MENU_ID")
    private Integer pMenuId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }
    public String getMenuUrl() {
        return menuUrl;
    }

    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl;
    }
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getIsMenu() {
        return isMenu;
    }

    public void setIsMenu(String isMenu) {
        this.isMenu = isMenu;
    }
    public Integer getpMenuId() {
        return pMenuId;
    }

    public void setpMenuId(Integer pMenuId) {
        this.pMenuId = pMenuId;
    }

    @Override
    public String toString() {
        return "PermissionInfo{" +
        "id=" + id +
        ", menuName=" + menuName +
        ", menuUrl=" + menuUrl +
        ", identifier=" + identifier +
        ", isMenu=" + isMenu +
        ", pMenuId=" + pMenuId +
        "}";
    }
}
