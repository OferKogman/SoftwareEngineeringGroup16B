package com.group16b.DomainLayer.Venue;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

@Embeddable
public class Stage {
    private final String stageID;
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
}
