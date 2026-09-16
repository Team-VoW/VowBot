package me.kmaxi.wynnvp.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineQueryResponseDTO {
    private int total;
    private List<LineReportDTO> results = List.of();
}
