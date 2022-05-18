package capstoneDesign.carbonTracker.apartment.dto;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "energy")
public class AptEnergyResponse {

    @Id
    private String energyId;

    @Field(type = FieldType.Keyword)
    private String kaptCode;

    @Field(type = FieldType.Keyword)
    private String date;

    @Field(type = FieldType.Long)
    private Long helect;

    @Field(type = FieldType.Long)
    private Long hgas;

    @Field(type = FieldType.Long)
    private Long hwaterCool;

    @Field(type = FieldType.Long)
    private Long carbonEnergy;
}
