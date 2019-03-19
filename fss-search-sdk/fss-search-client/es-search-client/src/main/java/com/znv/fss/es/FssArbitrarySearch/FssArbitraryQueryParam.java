package com.znv.fss.es.FssArbitrarySearch;

import java.util.List;

/**
 * Created by User on 2017/8/25.
 */
public class FssArbitraryQueryParam {
    private String enter_time_start;

    private String enter_time_end;

    private List<String> office_id;

    private String office_name;

    private List<String> camera_id;

    private String camera_name;

    private String person_id;

    private int camera_type;

    private int age_start;

    private int age_end;

    private int gender;

    private int glass;

    private int mask;

    private int race;

    private int beard;

    private int emotion;

    private int eye_open;

    private int mouth_open;

    private String gps_xy;

    private String distance;

    private boolean is_calcSim;

    private String feature_name;

    private double sim_threshold;

    private int minimun_should_match;

    private int from;

    private int size;

    public void setEnter_time_start(String enter_time_start) {
        this.enter_time_start = enter_time_start;
    }

    public String getEnter_time_start() {
        return this.enter_time_start;
    }

    public void setEnter_time_end(String enter_time_end) {
        this.enter_time_end = enter_time_end;
    }

    public String getEnter_time_end() {
        return this.enter_time_end;
    }

    public void setOffice_id(List<String> office_id) {
        this.office_id = office_id;
    }

    public List<String> getOffice_id() {
        return this.office_id;
    }

    public void setOffice_name(String office_name) {
        this.office_name = office_name;
    }

    public String getOffice_name() {
        return this.office_name;
    }

    public void setCamera_id(List<String> camera_id) {
        this.camera_id = camera_id;
    }

    public List<String> getCamera_id() {
        return this.camera_id;
    }

    public void setCamera_name(String camera_name) {
        this.camera_name = camera_name;
    }

    public String getCamera_name() {
        return this.camera_name;
    }

    public void setPerson_id(String person_id) {
        this.person_id = person_id;
    }

    public String getPerson_id() {
        return this.person_id;
    }

    public void setCamera_type(int camera_type) {
        this.camera_type = camera_type;
    }

    public int getCamera_type() {
        return this.camera_type;
    }

    public void setAge_start(int age_start) {
        this.age_start = age_start;
    }

    public int getAge_start() {
        return this.age_start;
    }

    public void setAge_end(int age_end) {
        this.age_end = age_end;
    }

    public int getAge_end() {
        return this.age_end;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGender() {
        return this.gender;
    }

    public void setGlass(int glass) {
        this.glass = glass;
    }

    public int getGlass() {
        return this.glass;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return this.mask;
    }

    public void setRace(int race) {
        this.race = race;
    }

    public int getRace() {
        return this.race;
    }

    public void setBeard(int beard) {
        this.beard = beard;
    }

    public int getBeard() {
        return this.beard;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

    public int getEmotion() {
        return this.emotion;
    }

    public void setEye_open(int eye_open) {
        this.eye_open = eye_open;
    }

    public int getEye_open() {
        return this.eye_open;
    }

    public void setMouth_open(int mouth_open) {
        this.mouth_open = mouth_open;
    }

    public int getMouth_open() {
        return this.mouth_open;
    }

    public void setGps_xy(String gps_xy) {
        this.gps_xy = gps_xy;
    }

    public String getGps_xy() {
        return this.gps_xy;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDistance() {
        return this.distance;
    }

    public void setIs_calcSim(boolean is_calcSim) {
        this.is_calcSim = is_calcSim;
    }

    public boolean getIs_calcSim() {
        return this.is_calcSim;
    }

    public void setFeature_name(String feature_name) {
        this.feature_name = feature_name;
    }

    public String getFeature_name() {
        return this.feature_name;
    }

    public void setSim_threshold(double sim_threshold) {
        this.sim_threshold = sim_threshold;
    }

    public double getSim_threshold() {
        return this.sim_threshold;
    }

    public void setMinimun_should_match(int minimun_should_match) {
        this.minimun_should_match = minimun_should_match;
    }

    public int getMinimun_should_match() {
        return this.minimun_should_match;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getFrom() {
        return this.from;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return this.size;
    }
}
