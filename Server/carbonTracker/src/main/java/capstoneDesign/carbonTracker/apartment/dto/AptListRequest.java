package capstoneDesign.carbonTracker.apartment.dto;

import lombok.Getter;

@Getter
public class AptListRequest {
    private int code;
    private int pageNum;
    private int count;
}
