package com.exam.onlineexamsystem.dto;

public class RoomAssignReq {
    private Integer seatsPerRoom = 30;
    private String roomPrefix = "A";
    private Integer startRoom = 101;
    private String examDate; // yyyy-MM-dd
    private String examTime; // e.g. 09:00-11:00
    private String address;

    public Integer getSeatsPerRoom() { return seatsPerRoom; }
    public void setSeatsPerRoom(Integer seatsPerRoom) { this.seatsPerRoom = seatsPerRoom; }

    public String getRoomPrefix() { return roomPrefix; }
    public void setRoomPrefix(String roomPrefix) { this.roomPrefix = roomPrefix; }

    public Integer getStartRoom() { return startRoom; }
    public void setStartRoom(Integer startRoom) { this.startRoom = startRoom; }

    public String getExamDate() { return examDate; }
    public void setExamDate(String examDate) { this.examDate = examDate; }

    public String getExamTime() { return examTime; }
    public void setExamTime(String examTime) { this.examTime = examTime; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
