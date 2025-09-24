package uz.fido.pfexchange.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataList <T> {
    private Integer page;
    private Integer size;
    private Integer total_pages;
    private Integer total_items;
    private List<T> content;
}
