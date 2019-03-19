package com.znv.fss.hbase.staytime;

/**
 * Created by ZNV on 2017/6/7.
 */
public class StayTimeOut {
    private String camera_id;
    private String camera_name;
    private int camera_type;
    private String duration_time;
    private float gpsx;
    private float gpsy;
    private String office_id;
    private String office_name;
    private String rt_image_data;
    private String person_id;
    private int lib_id;
    /*private String enter_time;
    private String leave_time;*/
    private String img_url;

    public void setCamera_id(String camera_id) {
        this.camera_id = camera_id;
    }

    public String getCamera_id() {
        return this.camera_id;
    }

    public void setCamera_name(String camera_name) {
        this.camera_name = camera_name;
    }

    public String getCamera_name() {
        return this.camera_name;
    }

    public void setCamera_type(int camera_type) {
        this.camera_type = camera_type;
    }

    public int getCamera_type() {
        return this.camera_type;
    }

    public void setDuration_time(String duration_time) {
        this.duration_time = duration_time;
    }

    public String getDuration_time() {
        return this.duration_time;
    }

    public void setGpsx(float gpsx) {
        this.gpsx = gpsx;
    }

    public float getGpsx() {
        return this.gpsx;
    }

    public void setGpsy(float gpsy) {
        this.gpsy = gpsy;
    }

    public float getGpsy() {
        return this.gpsy;
    }

    public void setOffice_id(String office_id) {
        this.office_id = office_id;
    }

    public String getOffice_id() {
        return this.office_id;
    }

    public void setOffice_name(String office_name) {
        this.office_name = office_name;
    }

    public String getOffice_name() {
        return this.office_name;
    }

    public void setRt_image_data(String rt_image_data) {
        this.rt_image_data = rt_image_data;
    }

    public String getRt_image_data() {
        return this.rt_image_data;
    }

    public void setPerson_id(String person_id) {
        this.person_id = person_id;
    }

    public String getPerson_id() {
        return this.person_id;
    }

    public void setLib_id(int lib_id) {
        this.lib_id = lib_id;
    }

    public int getLib_id() {
        return this.lib_id;
    }
/*    public void setEnter_time(String enter_time) {
        this.enter_time = enter_time;
    }

    public String getEnter_time() {
        return this.enter_time;
    }
    public void setLeave_time(String leave_time) {
        this.leave_time = leave_time;
    }

    public String getLeave_time() {
        return this.leave_time;
    }*/

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

}
