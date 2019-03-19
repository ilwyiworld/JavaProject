package com.znv.fss.hbase.searchbyimage;

/**
 * Created by estine on 2016/12/22.
 */
public class FssOutputData {
    private float similarity = 0f;
    private float sim = 0f; // 相似度

    // private String lastId = ""; // hbase rowkey部分字段，组新的startkey使用
    private String rowKey = ""; // 每条记录的rowkey，排序使用
    private String slatKey = ""; // 各分区盐值
    //private long totalPage = 1; // 总页数
    //private long currentPage = 0; // 当前页
    //private long recordSum = 0;

    private String imageData = ""; // base64编码后图片数据（方便页面呈现）
    private String timeStamp = ""; // 图片抓拍时间
    private String uuid = ""; // rowkey 字段
    private String trackIdx = ""; // 跟踪ID
    private String cameraId = ""; // 摄像头ID
    private String cameraName = ""; // 摄像头名称
    private String officeId = ""; // 局站ID
    private String officeName = ""; // 局站名称
    private String taskIdx = ""; // 任务ID
    //private String resultIdx = ""; // 当任务ID和跟踪ID都一样时，可以用这个字段区分
    private String img_url = "";
    private float pitch = 0.0f;
    private float roll = 0.0f;
    //private String frameTime = ""; // 识别的时间戳
    private float yaw = 0.0f; // 人脸姿势变化角度
    //private String imageName = ""; // 人脸图像名称
    private float gpsx = 0.0f;
    private float gpsy = 0.0f;
    private float qualityScore = 0.0f;

    // private int age = 0;
    private int beard = 0;
    private int emotion = 0;
    private int eyeOpen = 0;
    private int gender = 0;
    private int glass = 0;
    private int imgHeight = 0;
    private int imgWidth = 0;
    private int left = 0;
    private int mask = 0;
    private int mouthOpen = 0;
    private int race = 0;
    private int top = 0;

    private int frameIndex = 0; // 帧号
    private int right = 0; // 人脸框右下角横坐标
    private int bottom = 0; // 人脸框右下角纵坐标

    private int lib_id;
    private String person_id;
    private String is_alarm;
    private String big_picture_uuid;

    public float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(float similarity) {
        this.similarity = similarity;
    }

    public String getBig_picture_uuid() {
        return big_picture_uuid;
    }

    public void setBig_picture_uuid(String big_picture_uuid) {
        this.big_picture_uuid = big_picture_uuid;
    }

    public float getSim() {
        return sim;
    }

    public void setSim(float sim) {
        this.sim = sim;
    }

    // public String getLastId() {
    // return lastId;
    // }
    //
    // public void setLastId(String lastId) {
    // this.lastId = lastId;
    // }

    public String getRowKey() {
        return rowKey;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getSlatKey() {
        return slatKey;
    }

    public void setSlatKey(String slatKey) {
        this.slatKey = slatKey;
    }

//    public long getTotalPage() {
//        return totalPage;
//    }
//
//    public void setTotalPage(long totalPage) {
//        this.totalPage = totalPage;
//    }
//
//    public long getCurrentPage() {
//        return currentPage;
//    }
//
//    public long getRecordSum() {
//        return recordSum;
//    }
//
//    public void setRecordSum(long recordSum) {
//        this.recordSum = recordSum;
//    }
//
//    public void setCurrentPage(long currentPage) {
//        this.currentPage = currentPage;
//    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getImageData() {
        return imageData;
    }

    public void setImageData(String imageData) {
        this.imageData = imageData;
    }

    public String getTrackIdx() {
        return trackIdx;
    }

    public void setTrackIdx(String trackIdx) {
        this.trackIdx = trackIdx;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getOfficeId() {
        return officeId;
    }

    public void setOfficeId(String officeId) {
        this.officeId = officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getTaskIdx() {
        return taskIdx;
    }

    public void setTaskIdx(String taskIdx) {
        this.taskIdx = taskIdx;
    }

//    public String getResultIdx() {
//        return resultIdx;
//    }
//
//    public void setResultIdx(String resultIdx) {
//        this.resultIdx = resultIdx;
//    }

//    public String getFrameTime() {
//        return frameTime;
//    }
//
//    public void setFrameTime(String frameTime) {
//        this.frameTime = frameTime;
//    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

//    public String getImageName() {
//        return imageName;
//    }
//
//    public void setImageName(String imageName) {
//        this.imageName = imageName;
//    }

//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }

    public int getBeard() {
        return beard;
    }

    public void setBeard(int beard) {
        this.beard = beard;
    }

    public int getEmotion() {
        return emotion;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

    public int getEyeOpen() {
        return eyeOpen;
    }

    public void setEyeOpen(int eyeOpen) {
        this.eyeOpen = eyeOpen;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public int getGlass() {
        return glass;
    }

    public void setGlass(int glass) {
        this.glass = glass;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public int getMouthOpen() {
        return mouthOpen;
    }

    public void setMouthOpen(int mouthOpen) {
        this.mouthOpen = mouthOpen;
    }

    public float getPitch() {
        return pitch;
    }

    public float getGpsx() {
        return gpsx;
    }

    public void setGpsx(float gpsx) {
        this.gpsx = gpsx;
    }

    public float getGpsy() {
        return gpsy;
    }

    public void setGpsy(float gpsy) {
        this.gpsy = gpsy;
    }

    public float getQualityScore() {
        return qualityScore;
    }

    public void setQualityScore(float qualityScore) {
        this.qualityScore = qualityScore;
    }

    public int getRace() {
        return race;
    }

    public void setRace(int race) {
        this.race = race;
    }

    public float getRoll() {
        return roll;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getFrameIndex() {
        return frameIndex;
    }

    public void setFrameIndex(int frameIndex) {
        this.frameIndex = frameIndex;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getLib_id() {
        return lib_id;
    }

    public void setLib_id(int lib_id) {
        this.lib_id = lib_id;
    }

    public String getPerson_id() {
        return person_id;
    }

    public void setPerson_id(String person_id) {
        this.person_id = person_id;
    }

    public String getIs_alarm() {
        return is_alarm;
    }

    public void setIs_alarm(String is_alarm) {
        this.is_alarm = is_alarm;
    }
}
