package capstoneDesign.carbonTracker.apartment.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Document(indexName = "apt")
public class AptListResponse {

    @Id
    private String kaptCode;

    @Field(type = FieldType.Keyword)
    private String kaptName;

    @Field(type = FieldType.Text)
    private String doroJuso;

    @Field(type = FieldType.Text)
    private String bjdJuso;

    @Field(type = FieldType.Keyword)
    private String bjdCode;
}
