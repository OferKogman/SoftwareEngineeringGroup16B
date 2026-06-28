package com.group16b.DomainLayer.Venue;

import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class Stage {
    private String stageID;
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name="startRow", column=@Column(name="stage_startRow")),
        @AttributeOverride(name="startColumn", column=@Column(name="stage_startColumn")),
        @AttributeOverride(name="rowCount", column=@Column(name="stage_rowCount")),
        @AttributeOverride(name="columnCount", column=@Column(name="stage_columnCount"))
    }) 
    private GridRectangle area;

    public Stage(String stageID, GridRectangle area) {
        this.stageID = stageID;
        this.area = area;
    }
    public Stage() {
        // Default constructor for JPA
        this.stageID = null;
    }

    public String getStageID() {
        return stageID;
    }

    public GridRectangle getArea() {
        return area;
    }
    public void setArea(GridRectangle area) {
        this.area = area;
    }    

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stage other)) {
            return false;
        }
        return Objects.equals(stageID, other.stageID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stageID);
    }
}
